package me.moonscenty.alchemia.registry;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.wand.WandCap;
import me.moonscenty.alchemia.wand.WandRod;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The pieces a wand is put together from, kept in registries of their own so an add-on can add a rod or a cap the
 * same way the mod does — the same arrangement the aspects use.
 * <p>
 * The numbers are the original's. A plain stick holds a hundred of each primal and a silverwood rod five times
 * that; iron caps spend a tenth more than the price asked, and the better metals pull the aura in faster instead.
 */
public class ModWandParts {
    public static final ResourceKey<Registry<WandRod>> RODS_KEY = ResourceKey.createRegistryKey(Alchemia.id("wand_rod"));
    public static final ResourceKey<Registry<WandCap>> CAPS_KEY = ResourceKey.createRegistryKey(Alchemia.id("wand_cap"));

    public static final DeferredRegister<WandRod> ROD_ENTRIES = DeferredRegister.create(RODS_KEY, Alchemia.MODID);
    public static final DeferredRegister<WandCap> CAP_ENTRIES = DeferredRegister.create(CAPS_KEY, Alchemia.MODID);

    public static final Registry<WandRod> RODS = ROD_ENTRIES.makeRegistry(builder -> builder.sync(true));
    public static final Registry<WandCap> CAPS = CAP_ENTRIES.makeRegistry(builder -> builder.sync(true));

    // --- rods ---------------------------------------------------------------------------------------------

    /** A plain stick. Everyone starts here. */
    public static final DeferredHolder<WandRod, WandRod> WOOD =
            ROD_ENTRIES.register("wood", () -> WandRod.plain(100, 1));
    public static final DeferredHolder<WandRod, WandRod> GREATWOOD =
            ROD_ENTRIES.register("greatwood", () -> WandRod.plain(250, 3));
    public static final DeferredHolder<WandRod, WandRod> SILVERWOOD =
            ROD_ENTRIES.register("silverwood", () -> WandRod.plain(500, 9));

    // the six that make one primal of their own, one rod to each
    public static final DeferredHolder<WandRod, WandRod> REED =
            ROD_ENTRIES.register("reed", () -> WandRod.trickling(375, 6, ModAspects.AIR));
    public static final DeferredHolder<WandRod, WandRod> OBSIDIAN =
            ROD_ENTRIES.register("obsidian", () -> WandRod.trickling(375, 6, ModAspects.EARTH));
    public static final DeferredHolder<WandRod, WandRod> BLAZE =
            ROD_ENTRIES.register("blaze", () -> WandRod.trickling(375, 6, ModAspects.FIRE));
    public static final DeferredHolder<WandRod, WandRod> ICE =
            ROD_ENTRIES.register("ice", () -> WandRod.trickling(375, 6, ModAspects.WATER));
    public static final DeferredHolder<WandRod, WandRod> QUARTZ =
            ROD_ENTRIES.register("quartz", () -> WandRod.trickling(375, 6, ModAspects.ORDER));
    public static final DeferredHolder<WandRod, WandRod> BONE =
            ROD_ENTRIES.register("bone", () -> WandRod.trickling(375, 6, ModAspects.ENTROPY));

    // --- caps ---------------------------------------------------------------------------------------------

    /** Cheap, and it shows: everything costs a tenth more. */
    public static final DeferredHolder<WandCap, WandCap> IRON =
            CAP_ENTRIES.register("iron", () -> new WandCap(1.1F, 0, 1));
    public static final DeferredHolder<WandCap, WandCap> GOLD =
            CAP_ENTRIES.register("gold", () -> new WandCap(1.0F, 0, 3));
    public static final DeferredHolder<WandCap, WandCap> BRASS =
            CAP_ENTRIES.register("brass", () -> new WandCap(1.0F, 1, 3));
    public static final DeferredHolder<WandCap, WandCap> ALCHEMIUM =
            CAP_ENTRIES.register("alchemium", () -> new WandCap(0.9F, 0, 6));
    public static final DeferredHolder<WandCap, WandCap> VOID =
            CAP_ENTRIES.register("void", () -> new WandCap(1.0F, 3, 9));

    private ModWandParts() {
    }
}
