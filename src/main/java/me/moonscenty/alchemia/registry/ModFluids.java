package me.moonscenty.alchemia.registry;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * The liquids of the craft.
 */
public final class ModFluids {
    public static final DeferredRegister<FluidType> TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, Alchemia.MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, Alchemia.MODID);

    /**
     * Water with bath salts dissolved in it: silvery, faintly lit, and a ward against warp for whoever steps into it.
     * It flows as water does but, like the original's, never makes a new source of itself.
     */
    public static final DeferredHolder<FluidType, FluidType> PURIFYING_TYPE = TYPES.register("purifying_fluid",
            () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("block.alchemia.purifying_fluid")
                    .lightLevel(5)
                    .rarity(Rarity.RARE)
                    .canExtinguish(true)
                    .supportsBoating(true)
                    .canHydrate(true)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                    .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH)));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> PURIFYING =
            FLUIDS.register("purifying_fluid", () -> new BaseFlowingFluid.Source(purifying()));
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> FLOWING_PURIFYING =
            FLUIDS.register("flowing_purifying_fluid", () -> new BaseFlowingFluid.Flowing(purifying()));

    /** Liquid death: a slow purple ooze, four steps to a source, that dissolves the living. */
    public static final DeferredHolder<FluidType, FluidType> DEATH_TYPE = TYPES.register("liquid_death",
            () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("block.alchemia.liquid_death")
                    .lightLevel(4)
                    .density(2000)
                    .viscosity(3000)
                    .rarity(Rarity.RARE)
                    .canSwim(false)
                    .canDrown(false)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_LAVA)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA)));
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> DEATH =
            FLUIDS.register("liquid_death", () -> new BaseFlowingFluid.Source(death()));
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> FLOWING_DEATH =
            FLUIDS.register("flowing_liquid_death", () -> new BaseFlowingFluid.Flowing(death()));

    private static BaseFlowingFluid.Properties death() {
        return new BaseFlowingFluid.Properties(DEATH_TYPE, DEATH, FLOWING_DEATH)
                .block(ModBlocks.LIQUID_DEATH)
                .bucket(ModItems.LIQUID_DEATH_BUCKET)
                .slopeFindDistance(2)
                .levelDecreasePerBlock(2)
                .tickRate(20);
    }

    private static BaseFlowingFluid.Properties purifying() {
        return new BaseFlowingFluid.Properties(PURIFYING_TYPE, PURIFYING, FLOWING_PURIFYING)
                .block(ModBlocks.PURIFYING_FLUID)
                .bucket(ModItems.PURIFYING_FLUID_BUCKET);
    }

    private ModFluids() {
    }
}
