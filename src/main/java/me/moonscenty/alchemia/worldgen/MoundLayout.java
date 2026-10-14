package me.moonscenty.alchemia.worldgen;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.neoforged.fml.loading.FMLPaths;

/**
 * The shape of the original's mound, read out of the player's own Thaumcraft 5 jar.
 *
 * <p>The original wrote its mound down as some two and a half thousand calls of {@code setBlockState}, one per block,
 * split over two methods because one was too long. There is nothing to read but that code, and writing its numbers
 * down here would be copying the original. Instead this walks those two methods' bytecode in the jar in
 * {@code legacy/}, the same way the original's models are read, and keeps what each call would have put where: a
 * tiny stack machine that knows constants, adding and taking away, a new {@code BlockPos}, a block from
 * {@code Blocks} and its state, and the call that sets it. Anything else empties the stack, so the checks before
 * and the treasure after (which this mod writes out itself, as logic) leave nothing behind.
 *
 * <p>The corner the original was handed is read as nought, so every position comes out as an offset from it. The
 * game's blocks are known by their obfuscated field names, which this turns into this game's states; a block
 * this does not know is left out and said once in the log. The spawners and the chest are kept apart, by where
 * they are, for {@link MoundFeature} to fill in.
 */
public final class MoundLayout {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String FOLDER = "legacy";
    private static final String CLASS = "thaumcraft/common/lib/world/objects/WorldGenMound";
    /** The whole of it is in the first method and the second it hands on to, in that order. */
    private static final List<String> METHODS = List.of("func_180709_b", "generate2");

    private static final String BLOCKS = "net/minecraft/init/Blocks";
    private static final String BLOCK_POS = "net/minecraft/util/BlockPos";
    private static final String DEFAULT_STATE = "func_176223_P";
    private static final String STATE_FROM_META = "func_176203_a";
    private static final String SET_BLOCK_STATE = "func_175656_a";

    private static final String SPAWNER = "field_150474_ac";
    private static final String CHEST = "field_150486_ae";
    /** The trap the chest is sometimes swapped for, and what is under it: the feature does both itself. */
    private static final List<String> TREASURE = List.of("field_150447_bR", "field_150335_W");

    private static final Object OPAQUE = new Object();

    private static volatile Optional<MoundLayout> loaded;

    /** One block of the mound, from the corner the original was handed. */
    public record Placement(BlockPos offset, BlockState state) {
    }

    private final List<Placement> blocks;
    private final List<BlockPos> spawners;
    private final BlockPos chest;

    private MoundLayout(List<Placement> blocks, List<BlockPos> spawners, BlockPos chest) {
        this.blocks = List.copyOf(blocks);
        this.spawners = List.copyOf(spawners);
        this.chest = chest;
    }

    /** Every block, in the order the original set them; a later one can overwrite an earlier. */
    public List<Placement> blocks() {
        return blocks;
    }

    /** Where the spawners stand, the skeletons' first and the zombies' second. */
    public List<BlockPos> spawners() {
        return spawners;
    }

    /** Where the chest goes, at the far end of the burial chamber. */
    public BlockPos chest() {
        return chest;
    }

    /** The mound, or nothing when there is no Thaumcraft 5 jar in {@code legacy/} or it could not be read. */
    public static Optional<MoundLayout> get() {
        Optional<MoundLayout> found = loaded;
        if (found == null) {
            synchronized (MoundLayout.class) {
                found = loaded;
                if (found == null) {
                    found = load(FMLPaths.GAMEDIR.get().resolve(FOLDER));
                    loaded = found;
                }
            }
        }
        return found;
    }

    static Optional<MoundLayout> load(Path folder) {
        if (!Files.isDirectory(folder)) {
            LOGGER.warn("No {} folder; the mound will not generate", folder);
            return Optional.empty();
        }
        try (Stream<Path> files = Files.list(folder)) {
            for (Path jar : files.filter(path -> path.toString().endsWith(".jar")).sorted().toList()) {
                Optional<byte[]> bytes = readClass(jar);
                if (bytes.isPresent()) {
                    MoundLayout layout = read(bytes.get());
                    LOGGER.info("Read the mound from {}: {} blocks", jar.getFileName(), layout.blocks.size());
                    return Optional.of(layout);
                }
            }
        } catch (IOException | RuntimeException e) {
            LOGGER.error("Could not read the mound from {}", folder, e);
            return Optional.empty();
        }
        LOGGER.warn("No Thaumcraft 5 jar in {}; the mound will not generate", folder);
        return Optional.empty();
    }

    private static Optional<byte[]> readClass(Path jar) throws IOException {
        try (ZipFile zip = new ZipFile(jar.toFile())) {
            ZipEntry entry = zip.getEntry(CLASS + ".class");
            if (entry == null) {
                return Optional.empty();
            }
            try (InputStream in = zip.getInputStream(entry)) {
                return Optional.of(in.readAllBytes());
            }
        }
    }

    static MoundLayout read(byte[] bytes) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, ClassReader.SKIP_FRAMES);
        List<Placement> blocks = new ArrayList<>();
        List<BlockPos> spawners = new ArrayList<>();
        BlockPos[] chest = {null};
        List<String> unknown = new ArrayList<>();
        for (String name : METHODS) {
            MethodNode method = node.methods.stream().filter(m -> m.name.equals(name)).findFirst()
                    .orElseThrow(() -> new IllegalStateException("The mound has no method " + name));
            walk(method, (pos, block, meta) -> {
                if (block.equals(SPAWNER)) {
                    spawners.add(pos);
                } else if (block.equals(CHEST)) {
                    chest[0] = pos;
                } else if (!TREASURE.contains(block)) {
                    BlockState state = state(block, meta);
                    if (state != null) {
                        blocks.add(new Placement(pos, state));
                    } else if (!unknown.contains(block + ":" + meta)) {
                        unknown.add(block + ":" + meta);
                    }
                }
            });
        }
        if (!unknown.isEmpty()) {
            LOGGER.warn("The mound has blocks this does not know, left out: {}", unknown);
        }
        if (chest[0] == null || spawners.size() != 2) {
            throw new IllegalStateException("The mound's chest and spawners are not where they were");
        }
        return new MoundLayout(blocks, spawners, chest[0]);
    }

    @FunctionalInterface
    private interface Sink {
        void set(BlockPos pos, String block, int meta);
    }

    /** A block out of {@code Blocks}, and its metadata once asked for (or -1 for its default state). */
    private record Legacy(String block, int meta) {
    }

    /** A block out of {@code Blocks}, before its state is asked for. */
    private record Field(String block) {
    }

    private static void walk(MethodNode method, Sink sink) {
        Deque<Object> stack = new ArrayDeque<>();
        for (AbstractInsnNode insn : method.instructions) {
            int op = insn.getOpcode();
            if (op < 0) {
                continue; // labels, line numbers
            }
            if (op >= Opcodes.ICONST_M1 && op <= Opcodes.ICONST_5) {
                stack.push(op - Opcodes.ICONST_0);
            } else if (op == Opcodes.BIPUSH || op == Opcodes.SIPUSH) {
                stack.push(((IntInsnNode) insn).operand);
            } else if (op == Opcodes.LDC && ((LdcInsnNode) insn).cst instanceof Integer value) {
                stack.push(value);
            } else if (op == Opcodes.ILOAD) {
                stack.push(0); // the corner, which everything is an offset from
            } else if (op == Opcodes.ALOAD || op == Opcodes.NEW) {
                stack.push(OPAQUE);
            } else if (op == Opcodes.DUP && !stack.isEmpty()) {
                stack.push(stack.peek());
            } else if (op == Opcodes.POP && !stack.isEmpty()) {
                stack.pop();
            } else if ((op == Opcodes.IADD || op == Opcodes.ISUB) && stack.size() >= 2
                    && stack.pop() instanceof Integer b && stack.pop() instanceof Integer a) {
                stack.push(op == Opcodes.IADD ? a + b : a - b);
            } else if (op == Opcodes.GETSTATIC && ((FieldInsnNode) insn).owner.equals(BLOCKS)) {
                stack.push(new Field(((FieldInsnNode) insn).name));
            } else if (insn instanceof MethodInsnNode call && !call(call, stack, sink)) {
                stack.clear();
            } else if (!(insn instanceof MethodInsnNode)) {
                stack.clear();
            }
        }
    }

    /** The calls a block's placing is made of; false for anything else, or when the stack is not what they need. */
    private static boolean call(MethodInsnNode call, Deque<Object> stack, Sink sink) {
        if (call.owner.equals(BLOCK_POS) && call.name.equals("<init>") && call.desc.equals("(III)V")) {
            if (stack.size() < 5 || !(stack.pop() instanceof Integer z) || !(stack.pop() instanceof Integer y)
                    || !(stack.pop() instanceof Integer x)) {
                return false;
            }
            stack.pop();
            stack.pop();
            stack.push(new BlockPos(x, y, z));
            return true;
        }
        if (call.name.equals(DEFAULT_STATE)) {
            if (stack.isEmpty() || !(stack.pop() instanceof Field field)) {
                return false;
            }
            stack.push(new Legacy(field.block(), -1));
            return true;
        }
        if (call.name.equals(STATE_FROM_META)) {
            if (stack.size() < 2 || !(stack.pop() instanceof Integer meta) || !(stack.pop() instanceof Field field)) {
                return false;
            }
            stack.push(new Legacy(field.block(), meta));
            return true;
        }
        if (call.name.equals(SET_BLOCK_STATE)) {
            if (stack.size() < 3 || !(stack.pop() instanceof Legacy state) || !(stack.pop() instanceof BlockPos pos)) {
                return false;
            }
            stack.pop();
            sink.set(pos, state.block(), state.meta());
            stack.push(OPAQUE);
            return true;
        }
        return false;
    }

    /** The 1.8.9 block and metadata, as this game's state; null for one the mound does not use. */
    static BlockState state(String block, int meta) {
        return switch (block) {
            case "field_150350_a" -> Blocks.AIR.defaultBlockState();
            case "field_150348_b" -> Blocks.STONE.defaultBlockState();
            case "field_150349_c" -> Blocks.GRASS_BLOCK.defaultBlockState();
            case "field_150346_d" -> Blocks.DIRT.defaultBlockState();
            case "field_150347_e" -> Blocks.COBBLESTONE.defaultBlockState();
            case "field_150341_Y" -> Blocks.MOSSY_COBBLESTONE.defaultBlockState();
            case "field_150411_aY" -> Blocks.IRON_BARS.defaultBlockState();
            case "field_150431_aC" -> Blocks.SNOW.defaultBlockState();
            case "field_150329_H" -> switch (Math.max(meta, 0)) {
                case 0 -> Blocks.DEAD_BUSH.defaultBlockState();
                case 2 -> Blocks.FERN.defaultBlockState();
                default -> Blocks.SHORT_GRASS.defaultBlockState();
            };
            case "field_150417_aV" -> switch (Math.max(meta, 0)) {
                case 1 -> Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
                case 2 -> Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
                case 3 -> Blocks.CHISELED_STONE_BRICKS.defaultBlockState();
                default -> Blocks.STONE_BRICKS.defaultBlockState();
            };
            case "field_150446_ar" -> stairs(Math.max(meta, 0));
            case "field_150468_ap" -> Blocks.LADDER.defaultBlockState()
                    .setValue(LadderBlock.FACING, horizontal(meta, Direction.NORTH));
            default -> null;
        };
    }

    /** 1.8.9's stairs: the low two bits the way they face (east, west, south, north), the third upside down. */
    private static BlockState stairs(int meta) {
        Direction facing = switch (meta & 3) {
            case 0 -> Direction.EAST;
            case 1 -> Direction.WEST;
            case 2 -> Direction.SOUTH;
            default -> Direction.NORTH;
        };
        return Blocks.COBBLESTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, facing)
                .setValue(StairBlock.HALF, (meta & 4) != 0 ? Half.TOP : Half.BOTTOM);
    }

    /** A facing from its old number, which is this game's own; up or down was taken as the fallback. */
    private static Direction horizontal(int meta, Direction fallback) {
        Direction direction = Direction.from3DDataValue(Math.max(meta, 0));
        return direction.getAxis().isHorizontal() ? direction : fallback;
    }
}
