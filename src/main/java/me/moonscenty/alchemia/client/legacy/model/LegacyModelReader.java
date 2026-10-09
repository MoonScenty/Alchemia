package me.moonscenty.alchemia.client.legacy.model;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Reads the shape of one of the original's models out of its constructor.
 *
 * <p>The original built most of its models in code: a constructor that makes {@code ModelRenderer}s, gives each
 * boxes, a pivot and a turn, and hangs them off one another. There is no model file to copy. Instead of writing
 * those numbers down here -- which would be copying the original -- this walks the constructor's bytecode in the
 * player's own jar, one instruction at a time, and records what it would have built.
 *
 * <p>It is a very small interpreter and knows only what model constructors do: constants and arithmetic, locals,
 * loops, arrays, and the handful of {@code ModelRenderer} calls that shape a part. Anything else (a static map, an
 * entity, a call into the game) is carried along as an opaque value and otherwise ignored. Nothing of the original
 * is ever run; its classes reference a game that is not here.
 *
 * <p>Names are the game's obfuscated ones ({@code func_78789_a} is {@code addBox}). Those names did not change
 * between 1.7.10 and 1.8.9 for anything below except the biped's arms and legs, which are listed for both.
 */
public final class LegacyModelReader {
    /** Where a class of the original comes from, by internal name; empty when the jar does not have it. */
    @FunctionalInterface
    public interface ClassSource {
        Optional<byte[]> read(String internalName) throws IOException;
    }

    private static final String RENDERER = "net/minecraft/client/model/ModelRenderer";
    private static final String BIPED = "net/minecraft/client/model/ModelBiped";
    private static final String BASE = "net/minecraft/client/model/ModelBase";
    /** Enough for any constructor; a loop that runs longer than this is not building a model. */
    private static final int STEP_LIMIT = 2_000_000;
    private static final int CALL_DEPTH_LIMIT = 32;

    /** A biped's parts: the field each release kept it in, and the name this game gives it. */
    private static final Map<String, String> BIPED_FIELDS = Map.ofEntries(
            Map.entry("field_78116_c", "head"),
            Map.entry("field_178720_f", "hat"),
            Map.entry("field_78114_d", "hat"),
            Map.entry("field_78115_e", "body"),
            Map.entry("field_178723_h", "right_arm"),
            Map.entry("field_78112_f", "right_arm"),
            Map.entry("field_178724_i", "left_arm"),
            Map.entry("field_78113_g", "left_arm"),
            Map.entry("field_178721_j", "right_leg"),
            Map.entry("field_78123_h", "right_leg"),
            Map.entry("field_178722_k", "left_leg"),
            Map.entry("field_78124_i", "left_leg"));

    /** Something the interpreter does not model. It flows through and is dropped. */
    private static final Object OPAQUE = new Object() {
        @Override
        public String toString() {
            return "opaque";
        }
    };

    private final ClassSource classes;
    private final Map<String, ClassNode> loaded = new HashMap<>();
    private final List<String> notes = new ArrayList<>();

    // the model being built
    private final Map<String, Object> selfFields = new HashMap<>();
    private final Map<String, LegacyPart> bones = new LinkedHashMap<>();
    private final Map<String, LegacyPart> named = new LinkedHashMap<>();
    private final Map<Object[], String> arrayNames = new IdentityHashMap<>();
    private String selfClass;
    private int textureWidth = 64;
    private int textureHeight = 32;
    private int steps;
    private int unnamed;

    private LegacyModelReader(ClassSource classes) {
        this.classes = classes;
    }

    /**
     * Builds the model a constructor would have built.
     *
     * @param modelClass the model's internal name, e.g. {@code thaumcraft/client/renderers/models/gear/ModelFortressArmor}
     * @param descriptor which constructor, e.g. {@code (F)V}
     * @param arguments  what to call it with: boxed ints and floats, in order
     */
    public static Result read(ClassSource classes, String modelClass, String descriptor, Object... arguments)
            throws IOException {
        LegacyModelReader reader = new LegacyModelReader(classes);
        reader.selfClass = modelClass;
        MethodNode constructor = reader.method(modelClass, "<init>", descriptor)
                .orElseThrow(() -> new IOException("No constructor " + descriptor + " in " + modelClass));
        Object[] locals = new Object[Math.max(constructor.maxLocals, arguments.length + 1)];
        locals[0] = Self.INSTANCE;
        int slot = 1;
        for (Object argument : arguments) {
            locals[slot] = argument;
            slot += argument instanceof Long || argument instanceof Double ? 2 : 1;
        }
        reader.run(modelClass, constructor, locals, 0);
        return new Result(reader.finish(), List.copyOf(reader.notes));
    }

    /** The model, and anything the interpreter had to guess at or skip. An empty list is a clean read. */
    public record Result(LegacyModel model, List<String> notes) {
    }

    private LegacyModel finish() {
        // a part nobody kept in a field still needs a name to be found by; it gets one by where it hangs
        for (Map.Entry<String, LegacyPart> bone : bones.entrySet()) {
            nameChildren(bone.getValue());
        }
        return new LegacyModel(textureWidth, textureHeight, Map.copyOf(bones), Map.copyOf(named));
    }

    private void nameChildren(LegacyPart part) {
        for (LegacyPart child : part.children) {
            if (child.name == null) {
                child.name = "part_" + unnamed++;
            }
            nameChildren(child);
        }
    }

    // --- classes and methods

    private Optional<ClassNode> load(String name) throws IOException {
        if (loaded.containsKey(name)) {
            return Optional.ofNullable(loaded.get(name));
        }
        Optional<byte[]> bytes = name.startsWith("net/minecraft/") || name.startsWith("java/")
                ? Optional.empty()
                : classes.read(name);
        ClassNode node = null;
        if (bytes.isPresent()) {
            node = new ClassNode();
            new ClassReader(bytes.get()).accept(node, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        }
        loaded.put(name, node);
        return Optional.ofNullable(node);
    }

    private Optional<MethodNode> method(String owner, String name, String descriptor) throws IOException {
        Optional<ClassNode> node = load(owner);
        if (node.isEmpty()) {
            return Optional.empty();
        }
        for (MethodNode method : node.get().methods) {
            if (method.name.equals(name) && method.desc.equals(descriptor)) {
                return Optional.of(method);
            }
        }
        return Optional.empty();
    }

    /** A virtual call on the model: the most derived class of the original that has the method. */
    private Optional<Map.Entry<String, MethodNode>> dispatch(String name, String descriptor) throws IOException {
        String at = selfClass;
        while (at != null) {
            Optional<ClassNode> node = load(at);
            if (node.isEmpty()) {
                return Optional.empty();
            }
            for (MethodNode method : node.get().methods) {
                if (method.name.equals(name) && method.desc.equals(descriptor)
                        && (method.access & Opcodes.ACC_ABSTRACT) == 0) {
                    return Optional.of(Map.entry(at, method));
                }
            }
            at = node.get().superName;
        }
        return Optional.empty();
    }

    // --- the interpreter

    private Object run(String owner, MethodNode method, Object[] locals, int depth) throws IOException {
        if (depth > CALL_DEPTH_LIMIT) {
            notes.add("calls nested too deep in " + owner + "." + method.name);
            return OPAQUE;
        }
        InsnList code = method.instructions;
        Map<LabelNode, Integer> labels = new HashMap<>();
        for (int i = 0; i < code.size(); i++) {
            if (code.get(i) instanceof LabelNode label) {
                labels.put(label, i);
            }
        }
        Deque<Object> stack = new ArrayDeque<>();
        int pc = 0;
        while (pc < code.size()) {
            if (++steps > STEP_LIMIT) {
                throw new IOException("Gave up reading " + owner + "." + method.name + ": too many steps");
            }
            AbstractInsnNode insn = code.get(pc);
            int op = insn.getOpcode();
            pc++;
            if (op < 0) {
                continue; // labels, line numbers, frames
            }
            switch (op) {
                case Opcodes.NOP -> {
                }
                case Opcodes.ACONST_NULL -> stack.push(OPAQUE);
                case Opcodes.ICONST_M1, Opcodes.ICONST_0, Opcodes.ICONST_1, Opcodes.ICONST_2, Opcodes.ICONST_3,
                        Opcodes.ICONST_4, Opcodes.ICONST_5 -> stack.push(op - Opcodes.ICONST_0);
                case Opcodes.LCONST_0, Opcodes.LCONST_1 -> stack.push((long) (op - Opcodes.LCONST_0));
                case Opcodes.FCONST_0, Opcodes.FCONST_1, Opcodes.FCONST_2 -> stack.push((float) (op - Opcodes.FCONST_0));
                case Opcodes.DCONST_0, Opcodes.DCONST_1 -> stack.push((double) (op - Opcodes.DCONST_0));
                case Opcodes.BIPUSH, Opcodes.SIPUSH -> stack.push(((IntInsnNode) insn).operand);
                case Opcodes.LDC -> {
                    Object constant = ((LdcInsnNode) insn).cst;
                    stack.push(constant instanceof Integer || constant instanceof Float || constant instanceof Long
                            || constant instanceof Double || constant instanceof String ? constant : OPAQUE);
                }
                case Opcodes.ILOAD, Opcodes.LLOAD, Opcodes.FLOAD, Opcodes.DLOAD, Opcodes.ALOAD -> {
                    Object value = locals[((VarInsnNode) insn).var];
                    stack.push(value == null ? OPAQUE : value);
                }
                case Opcodes.ISTORE, Opcodes.LSTORE, Opcodes.FSTORE, Opcodes.DSTORE, Opcodes.ASTORE ->
                        locals[((VarInsnNode) insn).var] = stack.pop();
                case Opcodes.IINC -> {
                    IincInsnNode inc = (IincInsnNode) insn;
                    locals[inc.var] = asInt(locals[inc.var]) + inc.incr;
                }
                case Opcodes.IALOAD, Opcodes.LALOAD, Opcodes.FALOAD, Opcodes.DALOAD, Opcodes.AALOAD, Opcodes.BALOAD,
                        Opcodes.CALOAD, Opcodes.SALOAD -> {
                    Object index = stack.pop();
                    Object array = stack.pop();
                    stack.push(array instanceof Object[] values && index instanceof Integer i && i >= 0
                            && i < values.length && values[i] != null ? values[i] : OPAQUE);
                }
                case Opcodes.IASTORE, Opcodes.LASTORE, Opcodes.FASTORE, Opcodes.DASTORE, Opcodes.AASTORE,
                        Opcodes.BASTORE, Opcodes.CASTORE, Opcodes.SASTORE -> {
                    Object value = stack.pop();
                    Object index = stack.pop();
                    Object array = stack.pop();
                    if (array instanceof Object[] values && index instanceof Integer i && i >= 0 && i < values.length) {
                        values[i] = value;
                        String field = arrayNames.get(values);
                        if (field != null && value instanceof LegacyPart part) {
                            remember(field + "_" + i, part);
                        }
                    }
                }
                case Opcodes.POP -> stack.pop();
                case Opcodes.POP2 -> {
                    Object top = stack.pop();
                    if (!wide(top)) {
                        stack.pop();
                    }
                }
                case Opcodes.DUP -> stack.push(stack.peek());
                case Opcodes.DUP_X1 -> {
                    Object a = stack.pop();
                    Object b = stack.pop();
                    stack.push(a);
                    stack.push(b);
                    stack.push(a);
                }
                case Opcodes.DUP_X2 -> {
                    Object a = stack.pop();
                    Object b = stack.pop();
                    if (wide(b)) {
                        stack.push(a);
                        stack.push(b);
                    } else {
                        Object c = stack.pop();
                        stack.push(a);
                        stack.push(c);
                        stack.push(b);
                    }
                    stack.push(a);
                }
                case Opcodes.DUP2 -> {
                    Object a = stack.peek();
                    if (wide(a)) {
                        stack.push(a);
                    } else {
                        a = stack.pop();
                        Object b = stack.peek();
                        stack.push(a);
                        stack.push(b);
                        stack.push(a);
                    }
                }
                case Opcodes.SWAP -> {
                    Object a = stack.pop();
                    Object b = stack.pop();
                    stack.push(a);
                    stack.push(b);
                }
                case Opcodes.IADD, Opcodes.ISUB, Opcodes.IMUL, Opcodes.IDIV, Opcodes.IREM, Opcodes.ISHL, Opcodes.ISHR,
                        Opcodes.IUSHR, Opcodes.IAND, Opcodes.IOR, Opcodes.IXOR -> {
                    Object b = stack.pop();
                    Object a = stack.pop();
                    stack.push(a instanceof Integer x && b instanceof Integer y ? intOp(op, x, y) : OPAQUE);
                }
                case Opcodes.FADD, Opcodes.FSUB, Opcodes.FMUL, Opcodes.FDIV, Opcodes.FREM -> {
                    Object b = stack.pop();
                    Object a = stack.pop();
                    stack.push(a instanceof Float x && b instanceof Float y ? floatOp(op, x, y) : OPAQUE);
                }
                case Opcodes.DADD, Opcodes.DSUB, Opcodes.DMUL, Opcodes.DDIV, Opcodes.DREM -> {
                    Object b = stack.pop();
                    Object a = stack.pop();
                    stack.push(a instanceof Double x && b instanceof Double y ? doubleOp(op, x, y) : OPAQUE);
                }
                case Opcodes.LADD, Opcodes.LSUB, Opcodes.LMUL, Opcodes.LDIV, Opcodes.LREM, Opcodes.LAND, Opcodes.LOR,
                        Opcodes.LXOR, Opcodes.LSHL, Opcodes.LSHR, Opcodes.LUSHR -> {
                    stack.pop();
                    stack.pop();
                    stack.push(OPAQUE);
                }
                case Opcodes.INEG -> stack.push(stack.peek() instanceof Integer x ? -(Integer) stack.pop() : pop(stack));
                case Opcodes.FNEG -> stack.push(stack.peek() instanceof Float ? -(Float) stack.pop() : pop(stack));
                case Opcodes.DNEG -> stack.push(stack.peek() instanceof Double ? -(Double) stack.pop() : pop(stack));
                case Opcodes.LNEG -> stack.push(stack.peek() instanceof Long ? -(Long) stack.pop() : pop(stack));
                case Opcodes.I2F, Opcodes.L2F, Opcodes.D2F ->
                        stack.push(stack.peek() instanceof Number ? ((Number) stack.pop()).floatValue() : pop(stack));
                case Opcodes.I2D, Opcodes.L2D, Opcodes.F2D ->
                        stack.push(stack.peek() instanceof Number ? ((Number) stack.pop()).doubleValue() : pop(stack));
                case Opcodes.F2I, Opcodes.D2I, Opcodes.L2I ->
                        stack.push(stack.peek() instanceof Number ? ((Number) stack.pop()).intValue() : pop(stack));
                case Opcodes.I2L, Opcodes.F2L, Opcodes.D2L ->
                        stack.push(stack.peek() instanceof Number ? ((Number) stack.pop()).longValue() : pop(stack));
                case Opcodes.I2B -> stack.push(stack.peek() instanceof Integer ? (int) (byte) (int) (Integer) stack.pop() : pop(stack));
                case Opcodes.I2C -> stack.push(stack.peek() instanceof Integer ? (int) (char) (int) (Integer) stack.pop() : pop(stack));
                case Opcodes.I2S -> stack.push(stack.peek() instanceof Integer ? (int) (short) (int) (Integer) stack.pop() : pop(stack));
                case Opcodes.LCMP, Opcodes.FCMPL, Opcodes.FCMPG, Opcodes.DCMPL, Opcodes.DCMPG -> {
                    Object b = stack.pop();
                    Object a = stack.pop();
                    if (a instanceof Number x && b instanceof Number y) {
                        double dx = x.doubleValue();
                        double dy = y.doubleValue();
                        if (Double.isNaN(dx) || Double.isNaN(dy)) {
                            stack.push(op == Opcodes.FCMPG || op == Opcodes.DCMPG ? 1 : -1);
                        } else {
                            stack.push(Double.compare(dx, dy) < 0 ? -1 : dx == dy ? 0 : 1);
                        }
                    } else {
                        stack.push(OPAQUE);
                    }
                }
                case Opcodes.IFEQ, Opcodes.IFNE, Opcodes.IFLT, Opcodes.IFGE, Opcodes.IFGT, Opcodes.IFLE -> {
                    Object a = stack.pop();
                    if (a instanceof Integer x) {
                        if (compare(op, x, 0)) {
                            pc = labels.get(((JumpInsnNode) insn).label);
                        }
                    } else {
                        notes.add("branch on an unknown value in " + owner + "." + method.name + "; fell through");
                    }
                }
                case Opcodes.IF_ICMPEQ, Opcodes.IF_ICMPNE, Opcodes.IF_ICMPLT, Opcodes.IF_ICMPGE, Opcodes.IF_ICMPGT,
                        Opcodes.IF_ICMPLE -> {
                    Object b = stack.pop();
                    Object a = stack.pop();
                    if (a instanceof Integer x && b instanceof Integer y) {
                        if (compare(op - (Opcodes.IF_ICMPEQ - Opcodes.IFEQ), x, y)) {
                            pc = labels.get(((JumpInsnNode) insn).label);
                        }
                    } else {
                        notes.add("branch on an unknown value in " + owner + "." + method.name + "; fell through");
                    }
                }
                case Opcodes.IF_ACMPEQ, Opcodes.IF_ACMPNE -> {
                    Object b = stack.pop();
                    Object a = stack.pop();
                    if (a == OPAQUE || b == OPAQUE) {
                        notes.add("branch on an unknown value in " + owner + "." + method.name + "; fell through");
                    } else if ((a == b) == (op == Opcodes.IF_ACMPEQ)) {
                        pc = labels.get(((JumpInsnNode) insn).label);
                    }
                }
                case Opcodes.IFNULL, Opcodes.IFNONNULL -> {
                    Object a = stack.pop();
                    if (a == OPAQUE) {
                        notes.add("null check on an unknown value in " + owner + "." + method.name + "; fell through");
                    } else if (op == Opcodes.IFNONNULL) {
                        pc = labels.get(((JumpInsnNode) insn).label);
                    }
                }
                case Opcodes.GOTO -> pc = labels.get(((JumpInsnNode) insn).label);
                case Opcodes.IRETURN, Opcodes.LRETURN, Opcodes.FRETURN, Opcodes.DRETURN, Opcodes.ARETURN -> {
                    return stack.pop();
                }
                case Opcodes.RETURN -> {
                    return null;
                }
                case Opcodes.GETSTATIC -> stack.push(OPAQUE);
                case Opcodes.PUTSTATIC -> stack.pop();
                case Opcodes.GETFIELD -> stack.push(getField((FieldInsnNode) insn, stack.pop()));
                case Opcodes.PUTFIELD -> {
                    Object value = stack.pop();
                    putField((FieldInsnNode) insn, stack.pop(), value);
                }
                case Opcodes.INVOKEVIRTUAL, Opcodes.INVOKESPECIAL, Opcodes.INVOKESTATIC, Opcodes.INVOKEINTERFACE ->
                        invoke((MethodInsnNode) insn, stack, depth);
                case Opcodes.INVOKEDYNAMIC -> {
                    Type type = Type.getMethodType(((org.objectweb.asm.tree.InvokeDynamicInsnNode) insn).desc);
                    for (int i = 0; i < type.getArgumentTypes().length; i++) {
                        stack.pop();
                    }
                    stack.push(OPAQUE);
                }
                case Opcodes.NEW -> stack.push(new Unbuilt(((TypeInsnNode) insn).desc));
                case Opcodes.NEWARRAY, Opcodes.ANEWARRAY -> {
                    Object size = stack.pop();
                    stack.push(size instanceof Integer n && n >= 0 && n < 4096 ? new Object[n] : OPAQUE);
                }
                case Opcodes.MULTIANEWARRAY -> {
                    int dimensions = ((org.objectweb.asm.tree.MultiANewArrayInsnNode) insn).dims;
                    for (int i = 0; i < dimensions; i++) {
                        stack.pop();
                    }
                    stack.push(OPAQUE);
                }
                case Opcodes.ARRAYLENGTH -> stack.push(stack.pop() instanceof Object[] values ? values.length : OPAQUE);
                case Opcodes.CHECKCAST -> {
                }
                case Opcodes.INSTANCEOF -> {
                    stack.pop();
                    stack.push(OPAQUE);
                }
                case Opcodes.MONITORENTER, Opcodes.MONITOREXIT -> stack.pop();
                case Opcodes.ATHROW -> {
                    notes.add("reached a throw in " + owner + "." + method.name);
                    return OPAQUE;
                }
                default -> throw new IOException("Cannot read opcode " + op + " in " + owner + "." + method.name);
            }
        }
        return null;
    }

    private static Object pop(Deque<Object> stack) {
        stack.pop();
        return OPAQUE;
    }

    private static boolean wide(Object value) {
        return value instanceof Long || value instanceof Double;
    }

    private static int asInt(Object value) {
        return value instanceof Integer i ? i : 0;
    }

    private static boolean compare(int op, int a, int b) {
        return switch (op) {
            case Opcodes.IFEQ -> a == b;
            case Opcodes.IFNE -> a != b;
            case Opcodes.IFLT -> a < b;
            case Opcodes.IFGE -> a >= b;
            case Opcodes.IFGT -> a > b;
            case Opcodes.IFLE -> a <= b;
            default -> false;
        };
    }

    private static Object intOp(int op, int a, int b) {
        return switch (op) {
            case Opcodes.IADD -> a + b;
            case Opcodes.ISUB -> a - b;
            case Opcodes.IMUL -> a * b;
            case Opcodes.IDIV -> b == 0 ? OPAQUE : a / b;
            case Opcodes.IREM -> b == 0 ? OPAQUE : a % b;
            case Opcodes.ISHL -> a << b;
            case Opcodes.ISHR -> a >> b;
            case Opcodes.IUSHR -> a >>> b;
            case Opcodes.IAND -> a & b;
            case Opcodes.IOR -> a | b;
            case Opcodes.IXOR -> a ^ b;
            default -> OPAQUE;
        };
    }

    private static Object floatOp(int op, float a, float b) {
        return switch (op) {
            case Opcodes.FADD -> a + b;
            case Opcodes.FSUB -> a - b;
            case Opcodes.FMUL -> a * b;
            case Opcodes.FDIV -> a / b;
            case Opcodes.FREM -> a % b;
            default -> OPAQUE;
        };
    }

    private static Object doubleOp(int op, double a, double b) {
        return switch (op) {
            case Opcodes.DADD -> a + b;
            case Opcodes.DSUB -> a - b;
            case Opcodes.DMUL -> a * b;
            case Opcodes.DDIV -> a / b;
            case Opcodes.DREM -> a % b;
            default -> OPAQUE;
        };
    }

    // --- fields

    private Object getField(FieldInsnNode field, Object target) {
        if (target == Self.INSTANCE) {
            return switch (field.name) {
                case "field_78090_t" -> textureWidth;
                case "field_78089_u" -> textureHeight;
                default -> {
                    String bone = BIPED_FIELDS.get(field.name);
                    if (bone != null && bones.containsKey(bone)) {
                        yield bones.get(bone);
                    }
                    Object value = selfFields.get(field.name);
                    yield value == null ? OPAQUE : value;
                }
            };
        }
        if (target instanceof LegacyPart part) {
            return switch (field.name) {
                case "field_78795_f" -> part.xRot;
                case "field_78796_g" -> part.yRot;
                case "field_78808_h" -> part.zRot;
                case "field_78800_c" -> part.x;
                case "field_78797_d" -> part.y;
                case "field_78798_e" -> part.z;
                case "field_78809_i" -> part.mirror ? 1 : 0;
                case "field_78807_k" -> part.hidden ? 1 : 0;
                case "field_78806_j" -> part.shown ? 1 : 0;
                case "field_78804_l" -> new Cubes(part);
                case "field_78801_a" -> (float) part.textureWidth;
                case "field_78799_b" -> (float) part.textureHeight;
                default -> OPAQUE;
            };
        }
        return OPAQUE;
    }

    private void putField(FieldInsnNode field, Object target, Object value) {
        if (target == Self.INSTANCE) {
            switch (field.name) {
                case "field_78090_t" -> textureWidth = asInt(value);
                case "field_78089_u" -> textureHeight = asInt(value);
                default -> {
                    String bone = BIPED_FIELDS.get(field.name);
                    if (bone != null && value instanceof LegacyPart part) {
                        bones.put(bone, part);
                        part.name = bone;
                        return;
                    }
                    selfFields.put(field.name, value);
                    if (value instanceof LegacyPart part) {
                        remember(field.name, part);
                    } else if (value instanceof Object[] array) {
                        arrayNames.put(array, field.name);
                        for (int i = 0; i < array.length; i++) {
                            if (array[i] instanceof LegacyPart part) {
                                remember(field.name + "_" + i, part);
                            }
                        }
                    }
                }
            }
            return;
        }
        if (target instanceof LegacyPart part) {
            switch (field.name) {
                case "field_78795_f" -> part.xRot = asFloat(value, part.xRot);
                case "field_78796_g" -> part.yRot = asFloat(value, part.yRot);
                case "field_78808_h" -> part.zRot = asFloat(value, part.zRot);
                case "field_78800_c" -> part.x = asFloat(value, part.x);
                case "field_78797_d" -> part.y = asFloat(value, part.y);
                case "field_78798_e" -> part.z = asFloat(value, part.z);
                case "field_78809_i" -> part.mirror = asInt(value) != 0;
                case "field_78807_k" -> part.hidden = asInt(value) != 0;
                case "field_78806_j" -> part.shown = asInt(value) != 0;
                case "field_78801_a" -> part.textureWidth = (int) asFloat(value, part.textureWidth);
                case "field_78799_b" -> part.textureHeight = (int) asFloat(value, part.textureHeight);
                default -> {
                }
            }
        }
    }

    private void remember(String field, LegacyPart part) {
        if (part.name == null) {
            part.name = field;
        }
        named.putIfAbsent(field, part);
    }

    private static float asFloat(Object value, float otherwise) {
        return value instanceof Number number ? number.floatValue() : otherwise;
    }

    // --- calls

    private void invoke(MethodInsnNode call, Deque<Object> stack, int depth) throws IOException {
        Type type = Type.getMethodType(call.desc);
        Type[] parameters = type.getArgumentTypes();
        Object[] arguments = new Object[parameters.length];
        for (int i = parameters.length - 1; i >= 0; i--) {
            arguments[i] = stack.pop();
        }
        Object target = call.getOpcode() == Opcodes.INVOKESTATIC ? null : stack.pop();
        Object result = call(call, target, arguments, parameters, depth);
        if (call.name.equals("<init>") && target instanceof Unbuilt unbuilt) {
            // NEW was DUPed before the constructor ran; the copy left behind is the finished object now
            List<Object> values = new ArrayList<>(stack);
            stack.clear();
            for (int i = values.size() - 1; i >= 0; i--) {
                stack.push(values.get(i) == unbuilt ? unbuilt.built : values.get(i));
            }
        }
        if (type.getReturnType().getSort() != Type.VOID) {
            stack.push(result == null ? OPAQUE : result);
        }
    }

    private Object call(MethodInsnNode call, Object target, Object[] arguments, Type[] parameters, int depth)
            throws IOException {
        // a constructor finishing an object that NEW left half-made
        if (call.name.equals("<init>") && target instanceof Unbuilt unbuilt) {
            unbuilt.built = construct(call, arguments);
            return null;
        }
        if (target instanceof Unbuilt unbuilt && unbuilt.built != null) {
            target = unbuilt.built;
        }
        if (target == Self.INSTANCE) {
            if (call.name.equals("<init>")) {
                return superConstructor(call, arguments, parameters, depth);
            }
            Optional<Map.Entry<String, MethodNode>> found = call.getOpcode() == Opcodes.INVOKESPECIAL
                    ? method(call.owner, call.name, call.desc).map(m -> Map.entry(call.owner, m))
                    : dispatch(call.name, call.desc);
            if (found.isPresent()) {
                return run(found.get().getKey(), found.get().getValue(), frame(found.get().getValue(), Self.INSTANCE,
                        arguments, parameters), depth + 1);
            }
            return OPAQUE;
        }
        if (target instanceof LegacyPart part) {
            return onPart(call, part, arguments);
        }
        if (target instanceof Cubes cubes) {
            if (call.name.equals("clear")) {
                cubes.part.boxes.clear();
            }
            return OPAQUE;
        }
        if (call.getOpcode() == Opcodes.INVOKESTATIC) {
            return staticCall(call, arguments, parameters, depth);
        }
        return OPAQUE;
    }

    private Object staticCall(MethodInsnNode call, Object[] arguments, Type[] parameters, int depth)
            throws IOException {
        if (call.owner.equals("java/lang/Math") && arguments.length == 1 && arguments[0] instanceof Number n) {
            double x = n.doubleValue();
            Double out = switch (call.name) {
                case "sin" -> Math.sin(x);
                case "cos" -> Math.cos(x);
                case "toRadians" -> Math.toRadians(x);
                case "toDegrees" -> Math.toDegrees(x);
                case "sqrt" -> Math.sqrt(x);
                case "abs" -> Math.abs(x);
                default -> null;
            };
            if (out != null) {
                return switch (Type.getMethodType(call.desc).getReturnType().getSort()) {
                    case Type.FLOAT -> out.floatValue();
                    case Type.INT -> out.intValue();
                    default -> out;
                };
            }
            return OPAQUE;
        }
        Optional<MethodNode> method = method(call.owner, call.name, call.desc);
        if (method.isPresent()) {
            return run(call.owner, method.get(), frame(method.get(), null, arguments, parameters), depth + 1);
        }
        return OPAQUE;
    }

    private static Object[] frame(MethodNode method, Object self, Object[] arguments, Type[] parameters) {
        Object[] locals = new Object[Math.max(method.maxLocals, arguments.length * 2 + 1)];
        int slot = 0;
        if (self != null) {
            locals[slot++] = self;
        }
        for (int i = 0; i < arguments.length; i++) {
            locals[slot] = arguments[i];
            slot += parameters[i].getSize();
        }
        return locals;
    }

    /** {@code new} something. Only parts are built; anything else is a value nobody will look inside. */
    private Object construct(MethodInsnNode call, Object[] arguments) {
        if (!call.owner.equals(RENDERER)) {
            return OPAQUE;
        }
        int u = 0;
        int v = 0;
        if (arguments.length == 3 && arguments[1] instanceof Integer x && arguments[2] instanceof Integer y) {
            u = x;
            v = y;
        } else if (arguments.length == 2 && arguments[1] instanceof String name) {
            LegacyPart part = new LegacyPart(null, 0, 0, textureWidth, textureHeight);
            notes.add("part made by name " + name + "; its texture corner is the default");
            return part;
        }
        return new LegacyPart(null, u, v, textureWidth, textureHeight);
    }

    /**
     * The model's own constructor calling up its chain. Classes of the original are read like any other; the game's
     * {@code ModelBiped} is not in the jar, so the seven parts it would have made are made here, the way it made them.
     */
    private Object superConstructor(MethodInsnNode call, Object[] arguments, Type[] parameters, int depth)
            throws IOException {
        if (call.owner.equals(BIPED)) {
            float grow = arguments.length > 0 && arguments[0] instanceof Number n ? n.floatValue() : 0.0F;
            float drop = arguments.length > 1 && arguments[1] instanceof Number n ? n.floatValue() : 0.0F;
            int width = arguments.length > 3 && arguments[2] instanceof Integer w ? w : 64;
            int height = arguments.length > 3 && arguments[3] instanceof Integer h ? h : 32;
            biped(grow, drop, width, height);
            return null;
        }
        if (call.owner.equals(BASE) || call.owner.startsWith("java/")) {
            return null;
        }
        Optional<MethodNode> method = method(call.owner, call.name, call.desc);
        if (method.isPresent()) {
            return run(call.owner, method.get(), frame(method.get(), Self.INSTANCE, arguments, parameters), depth + 1);
        }
        notes.add("could not read the constructor of " + call.owner);
        return null;
    }

    /** What {@code ModelBiped(float, float, int, int)} builds. These are the game's numbers, not the original's. */
    private void biped(float grow, float drop, int width, int height) {
        textureWidth = width;
        textureHeight = height;
        bones.put("head", bone("head", 0, 0, false, -4, -8, -4, 8, 8, 8, grow, 0, drop));
        bones.put("hat", bone("hat", 32, 0, false, -4, -8, -4, 8, 8, 8, grow + 0.5F, 0, drop));
        bones.put("body", bone("body", 16, 16, false, -4, 0, -2, 8, 12, 4, grow, 0, drop));
        bones.put("right_arm", bone("right_arm", 40, 16, false, -3, -2, -2, 4, 12, 4, grow, -5, 2 + drop));
        bones.put("left_arm", bone("left_arm", 40, 16, true, -1, -2, -2, 4, 12, 4, grow, 5, 2 + drop));
        bones.put("right_leg", bone("right_leg", 0, 16, false, -2, 0, -2, 4, 12, 4, grow, -1.9F, 12 + drop));
        bones.put("left_leg", bone("left_leg", 0, 16, true, -2, 0, -2, 4, 12, 4, grow, 1.9F, 12 + drop));
    }

    private LegacyPart bone(String name, int u, int v, boolean mirror, float x, float y, float z, int w, int h, int d,
            float grow, float px, float py) {
        LegacyPart part = new LegacyPart(name, u, v, textureWidth, textureHeight);
        part.mirror = mirror;
        part.boxes.add(new LegacyPart.Box(u, v, x, y, z, w, h, d, grow, mirror));
        part.x = px;
        part.y = py;
        return part;
    }

    private Object onPart(MethodInsnNode call, LegacyPart part, Object[] a) {
        switch (call.name + call.desc) {
            case "func_78789_a(FFFIII)Lnet/minecraft/client/model/ModelRenderer;" -> {
                addBox(part, a, 0.0F);
                return part;
            }
            case "func_78790_a(FFFIIIF)V" -> {
                addBox(part, a, asFloat(a[6], 0.0F));
                return null;
            }
            case "func_78793_a(FFF)V" -> {
                part.x = asFloat(a[0], part.x);
                part.y = asFloat(a[1], part.y);
                part.z = asFloat(a[2], part.z);
                return null;
            }
            case "func_78787_b(II)Lnet/minecraft/client/model/ModelRenderer;" -> {
                part.textureWidth = asInt(a[0]);
                part.textureHeight = asInt(a[1]);
                return part;
            }
            case "func_78784_a(II)Lnet/minecraft/client/model/ModelRenderer;" -> {
                part.u = asInt(a[0]);
                part.v = asInt(a[1]);
                return part;
            }
            case "func_78792_a(Lnet/minecraft/client/model/ModelRenderer;)V" -> {
                if (a[0] instanceof LegacyPart child && child != part && !part.children.contains(child)) {
                    part.children.add(child);
                }
                return null;
            }
            default -> {
                // rendering, compiling a display list, and the like: nothing that changes the shape
                return OPAQUE;
            }
        }
    }

    private void addBox(LegacyPart part, Object[] a, float grow) {
        if (!(a[3] instanceof Integer w && a[4] instanceof Integer h && a[5] instanceof Integer d)) {
            notes.add("a box of " + part.name + " has a size that could not be worked out; left out");
            return;
        }
        part.boxes.add(new LegacyPart.Box(part.u, part.v, asFloat(a[0], 0), asFloat(a[1], 0), asFloat(a[2], 0),
                w, h, d, grow, part.mirror));
    }

    /** The model under construction. */
    private enum Self {
        INSTANCE
    }

    /** What NEW leaves on the stack until its constructor runs. */
    private static final class Unbuilt {
        final String type;
        Object built;

        Unbuilt(String type) {
            this.type = type;
        }
    }

    /** A part's {@code cubeList}, which constructors clear to take a biped's own boxes away. */
    private record Cubes(LegacyPart part) {
    }
}
