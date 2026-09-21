package me.moonscenty.alchemia.registry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.Aspect.Blending;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The thirty-five aspects, defined in code so add-ons can register their own the same way.
 * <p>
 * Ids and colours follow the original. The six primals come first; every compound names the two aspects it is mixed
 * from, so the whole tree is spelled out here.
 */
public class ModAspects {
    public static final ResourceKey<Registry<Aspect>> KEY = ResourceKey.createRegistryKey(Alchemia.id("aspect"));
    public static final DeferredRegister<Aspect> ASPECTS = DeferredRegister.create(KEY, Alchemia.MODID);
    public static final Registry<Aspect> REGISTRY = ASPECTS.makeRegistry(builder -> builder.sync(true));

    // The six primals
    public static final DeferredHolder<Aspect, Aspect> AIR = primal("aer", 0xFFFF7E, Blending.ADDITIVE);
    public static final DeferredHolder<Aspect, Aspect> EARTH = primal("terra", 0x56C000, Blending.ADDITIVE);
    public static final DeferredHolder<Aspect, Aspect> FIRE = primal("ignis", 0xFF5A01, Blending.ADDITIVE);
    public static final DeferredHolder<Aspect, Aspect> WATER = primal("aqua", 0x3CD4FC, Blending.ADDITIVE);
    public static final DeferredHolder<Aspect, Aspect> ORDER = primal("ordo", 0xD5D4EC, Blending.ADDITIVE);
    public static final DeferredHolder<Aspect, Aspect> ENTROPY = primal("perditio", 0x404040, Blending.FLAT);

    // Compounds, each mixed from two aspects above it
    public static final DeferredHolder<Aspect, Aspect> VOID = compound("vacuos", 0x888888, Blending.FLAT, AIR, ENTROPY);
    public static final DeferredHolder<Aspect, Aspect> LIGHT = compound("lux", 0xFFD585, AIR, FIRE);
    public static final DeferredHolder<Aspect, Aspect> MOTION = compound("motus", 0xCDCCF4, AIR, ORDER);
    public static final DeferredHolder<Aspect, Aspect> COLD = compound("gelum", 0xE1FFFF, FIRE, ENTROPY);
    public static final DeferredHolder<Aspect, Aspect> CRYSTAL = compound("vitreus", 0x80FFFF, EARTH, AIR);
    public static final DeferredHolder<Aspect, Aspect> METAL = compound("metallum", 0xB5B5CD, EARTH, ORDER);
    public static final DeferredHolder<Aspect, Aspect> LIFE = compound("victus", 0xDE0005, EARTH, WATER);
    public static final DeferredHolder<Aspect, Aspect> DEATH = compound("mortuus", 0x887788, WATER, ENTROPY);
    public static final DeferredHolder<Aspect, Aspect> ENERGY = compound("potentia", 0xC0FFFF, ORDER, FIRE);
    public static final DeferredHolder<Aspect, Aspect> EXCHANGE = compound("permutatio", 0x578357, ENTROPY, ORDER);
    public static final DeferredHolder<Aspect, Aspect> AURA = compound("auram", 0xFFC0FF, ENERGY, AIR);
    public static final DeferredHolder<Aspect, Aspect> FLUX = compound("vitium", 0x800080, ENTROPY, ENERGY);
    public static final DeferredHolder<Aspect, Aspect> DARKNESS = compound("tenebrae", 0x222222, VOID, LIGHT);
    public static final DeferredHolder<Aspect, Aspect> ELDRITCH = compound("alienis", 0x805080, VOID, DARKNESS);
    public static final DeferredHolder<Aspect, Aspect> FLIGHT = compound("volatus", 0xE7E7D7, AIR, MOTION);
    public static final DeferredHolder<Aspect, Aspect> PLANT = compound("herba", 0x01AC00, LIFE, EARTH);
    public static final DeferredHolder<Aspect, Aspect> TOOL = compound("instrumentum", 0x4040EE, METAL, ENERGY);
    public static final DeferredHolder<Aspect, Aspect> CRAFT = compound("fabrico", 0x809D80, EXCHANGE, TOOL);
    public static final DeferredHolder<Aspect, Aspect> MECHANISM = compound("machina", 0x8080A0, MOTION, TOOL);
    public static final DeferredHolder<Aspect, Aspect> TRAP = compound("vinculum", 0x9A8080, MOTION, ENTROPY);
    public static final DeferredHolder<Aspect, Aspect> SOUL = compound("spiritus", 0xEBEBFB, LIFE, DEATH);
    public static final DeferredHolder<Aspect, Aspect> MIND = compound("cognitio", 0xFFC2B3, FIRE, SOUL);
    public static final DeferredHolder<Aspect, Aspect> SENSES = compound("sensus", 0x0FD9FF, AIR, SOUL);
    public static final DeferredHolder<Aspect, Aspect> AVERSION = compound("aversio", 0xC05050, SOUL, ENTROPY);
    public static final DeferredHolder<Aspect, Aspect> PROTECT = compound("praemunio", 0x00C0C0, SOUL, EARTH);
    public static final DeferredHolder<Aspect, Aspect> DESIRE = compound("desiderium", 0xE6BE44, SOUL, VOID);
    public static final DeferredHolder<Aspect, Aspect> UNDEAD = compound("exanimis", 0x3A4000, MOTION, DEATH);
    public static final DeferredHolder<Aspect, Aspect> BEAST = compound("bestia", 0x9F6409, MOTION, LIFE);
    public static final DeferredHolder<Aspect, Aspect> MAN = compound("humanus", 0xFFD7C0, SOUL, LIFE);

    /** Which aspect two others mix into, keyed by the pair in either order. Built once the registry is frozen. */
    private static Map<Long, Holder<Aspect>> combinations;

    private static DeferredHolder<Aspect, Aspect> primal(String tag, int color, Blending blending) {
        return ASPECTS.register(tag, () -> Aspect.primal(color, blending));
    }

    private static DeferredHolder<Aspect, Aspect> compound(String tag, int color, Holder<Aspect> first, Holder<Aspect> second) {
        return compound(tag, color, Blending.ADDITIVE, first, second);
    }

    private static DeferredHolder<Aspect, Aspect> compound(String tag, int color, Blending blending, Holder<Aspect> first, Holder<Aspect> second) {
        return ASPECTS.register(tag, () -> Aspect.compound(color, blending, first, second));
    }

    /** The aspect the two mix into, if any. Order does not matter. */
    public static Optional<Holder<Aspect>> combine(Holder<Aspect> first, Holder<Aspect> second) {
        if (combinations == null) {
            combinations = buildCombinations();
        }
        return Optional.ofNullable(combinations.get(pairKey(first, second)));
    }

    public static List<Holder<Aspect>> primals() {
        return REGISTRY.holders().filter(holder -> holder.value().isPrimal()).map(holder -> (Holder<Aspect>) holder).toList();
    }

    /** One of the six, at random. */
    public static Holder<Aspect> randomPrimal(net.minecraft.util.RandomSource random) {
        List<Holder<Aspect>> all = primals();
        return all.get(random.nextInt(all.size()));
    }

    public static List<Holder<Aspect>> compounds() {
        return REGISTRY.holders().filter(holder -> !holder.value().isPrimal()).map(holder -> (Holder<Aspect>) holder).toList();
    }

    private static Map<Long, Holder<Aspect>> buildCombinations() {
        Map<Long, Holder<Aspect>> built = new HashMap<>();
        REGISTRY.holders().forEach(holder -> holder.value().components()
                .ifPresent(parts -> built.putIfAbsent(pairKey(parts.get(0), parts.get(1)), holder)));
        return built;
    }

    /** Packs two aspect ids into one key that is the same whichever way round they are given. */
    private static long pairKey(Holder<Aspect> first, Holder<Aspect> second) {
        int a = REGISTRY.getId(first.value());
        int b = REGISTRY.getId(second.value());
        return ((long) Math.min(a, b) << 32) | (Math.max(a, b) & 0xFFFFFFFFL);
    }
}
