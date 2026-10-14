package me.moonscenty.alchemia.registry;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.entity.ArcanePedestalBlockEntity;
import me.moonscenty.alchemia.block.entity.ArcaneWorkbenchBlockEntity;
import me.moonscenty.alchemia.block.entity.ArcaneWorkbenchChargerBlockEntity;
import me.moonscenty.alchemia.block.entity.AlembicBlockEntity;
import me.moonscenty.alchemia.block.entity.BufferTubeBlockEntity;
import me.moonscenty.alchemia.block.entity.FilterTubeBlockEntity;
import me.moonscenty.alchemia.block.entity.CrucibleBlockEntity;
import me.moonscenty.alchemia.block.entity.JarBlockEntity;
import me.moonscenty.alchemia.block.entity.TubeBlockEntity;
import me.moonscenty.alchemia.block.entity.ValveTubeBlockEntity;
import me.moonscenty.alchemia.block.entity.EssentiaSmelterBlockEntity;
import me.moonscenty.alchemia.block.entity.InfusionMatrixBlockEntity;
import me.moonscenty.alchemia.block.entity.HoleBlockEntity;
import me.moonscenty.alchemia.block.entity.NitorBlockEntity;
import me.moonscenty.alchemia.block.entity.NodeStabilizerBlockEntity;
import me.moonscenty.alchemia.block.entity.ResearchTableBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Alchemia.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ResearchTableBlockEntity>> RESEARCH_TABLE =
            BLOCK_ENTITIES.register("research_table", () -> BlockEntityType.Builder
                    .of(ResearchTableBlockEntity::new, ModBlocks.RESEARCH_TABLE.get())
                    .build(null));

    /** A focus on the bench, and the one thing being worked into it. */
    public static final DeferredHolder<BlockEntityType<?>,
            BlockEntityType<me.moonscenty.alchemia.block.entity.FocalManipulatorBlockEntity>> FOCAL_MANIPULATOR =
            BLOCK_ENTITIES.register("focal_manipulator", () -> BlockEntityType.Builder
                    .of(me.moonscenty.alchemia.block.entity.FocalManipulatorBlockEntity::new,
                            ModBlocks.FOCAL_MANIPULATOR.get())
                    .build(null));

    /** The centrifuge spinning, and the crystallizer at work. */
    public static final DeferredHolder<BlockEntityType<?>,
            BlockEntityType<me.moonscenty.alchemia.block.entity.CentrifugeBlockEntity>> CENTRIFUGE =
            BLOCK_ENTITIES.register("centrifuge", () -> BlockEntityType.Builder
                    .of(me.moonscenty.alchemia.block.entity.CentrifugeBlockEntity::new, ModBlocks.CENTRIFUGE.get())
                    .build(null));
    public static final DeferredHolder<BlockEntityType<?>,
            BlockEntityType<me.moonscenty.alchemia.block.entity.CrystallizerBlockEntity>> CRYSTALLIZER =
            BLOCK_ENTITIES.register("crystallizer", () -> BlockEntityType.Builder
                    .of(me.moonscenty.alchemia.block.entity.CrystallizerBlockEntity::new,
                            ModBlocks.CRYSTALLIZER.get())
                    .build(null));

    /** Both halves of a thaumatorium; the upper hands everything to the lower. */
    public static final DeferredHolder<BlockEntityType<?>,
            BlockEntityType<me.moonscenty.alchemia.block.entity.ThaumatoriumBlockEntity>> THAUMATORIUM =
            BLOCK_ENTITIES.register("thaumatorium", () -> BlockEntityType.Builder
                    .of(me.moonscenty.alchemia.block.entity.ThaumatoriumBlockEntity::new,
                            ModBlocks.THAUMATORIUM.get())
                    .build(null));

    /** A brain in a jar, eating. */
    public static final DeferredHolder<BlockEntityType<?>,
            BlockEntityType<me.moonscenty.alchemia.block.entity.BrainJarBlockEntity>> BRAIN_JAR =
            BLOCK_ENTITIES.register("brain_jar", () -> BlockEntityType.Builder
                    .of(me.moonscenty.alchemia.block.entity.BrainJarBlockEntity::new, ModBlocks.BRAIN_JAR.get())
                    .build(null));

    /** A mirror of either kind. */
    public static final DeferredHolder<BlockEntityType<?>,
            BlockEntityType<me.moonscenty.alchemia.block.entity.MirrorBlockEntity>> MIRROR =
            BLOCK_ENTITIES.register("mirror", () -> BlockEntityType.Builder
                    .of(me.moonscenty.alchemia.block.entity.MirrorBlockEntity::new, ModBlocks.MIRROR.get(),
                            ModBlocks.MIRROR_ESSENTIA.get())
                    .build(null));

    /** The molten heart of an infernal furnace. */
    public static final DeferredHolder<BlockEntityType<?>,
            BlockEntityType<me.moonscenty.alchemia.block.entity.InfernalFurnaceBlockEntity>> INFERNAL_FURNACE =
            BLOCK_ENTITIES.register("infernal_furnace", () -> BlockEntityType.Builder
                    .of(me.moonscenty.alchemia.block.entity.InfernalFurnaceBlockEntity::new,
                            ModBlocks.INFERNAL_FURNACE.get())
                    .build(null));

    /** The bore at work, and its base, which takes essentia in for it. */
    public static final DeferredHolder<BlockEntityType<?>,
            BlockEntityType<me.moonscenty.alchemia.block.entity.ArcaneBoreBlockEntity>> ARCANE_BORE =
            BLOCK_ENTITIES.register("arcane_bore", () -> BlockEntityType.Builder
                    .of(me.moonscenty.alchemia.block.entity.ArcaneBoreBlockEntity::new, ModBlocks.ARCANE_BORE.get())
                    .build(null));
    public static final DeferredHolder<BlockEntityType<?>,
            BlockEntityType<me.moonscenty.alchemia.block.entity.ArcaneBoreBaseBlockEntity>> ARCANE_BORE_BASE =
            BLOCK_ENTITIES.register("arcane_bore_base", () -> BlockEntityType.Builder
                    .of(me.moonscenty.alchemia.block.entity.ArcaneBoreBaseBlockEntity::new,
                            ModBlocks.ARCANE_BORE_BASE.get())
                    .build(null));

    /** Bellows pumping, which on the server is a furnace being pushed along. */
    public static final DeferredHolder<BlockEntityType<?>,
            BlockEntityType<me.moonscenty.alchemia.block.entity.BellowsBlockEntity>> BELLOWS =
            BLOCK_ENTITIES.register("bellows", () -> BlockEntityType.Builder
                    .of(me.moonscenty.alchemia.block.entity.BellowsBlockEntity::new, ModBlocks.BELLOWS.get())
                    .build(null));

    /** The three lamps at work. */
    public static final DeferredHolder<BlockEntityType<?>,
            BlockEntityType<me.moonscenty.alchemia.block.entity.ArcaneLampBlockEntity>> ARCANE_LAMP =
            BLOCK_ENTITIES.register("arcane_lamp", () -> BlockEntityType.Builder
                    .of(me.moonscenty.alchemia.block.entity.ArcaneLampBlockEntity::new, ModBlocks.ARCANE_LAMP.get())
                    .build(null));
    public static final DeferredHolder<BlockEntityType<?>,
            BlockEntityType<me.moonscenty.alchemia.block.entity.GrowthLampBlockEntity>> GROWTH_LAMP =
            BLOCK_ENTITIES.register("growth_lamp", () -> BlockEntityType.Builder
                    .of(me.moonscenty.alchemia.block.entity.GrowthLampBlockEntity::new, ModBlocks.GROWTH_LAMP.get())
                    .build(null));
    public static final DeferredHolder<BlockEntityType<?>,
            BlockEntityType<me.moonscenty.alchemia.block.entity.FertilityLampBlockEntity>> FERTILITY_LAMP =
            BLOCK_ENTITIES.register("fertility_lamp", () -> BlockEntityType.Builder
                    .of(me.moonscenty.alchemia.block.entity.FertilityLampBlockEntity::new,
                            ModBlocks.FERTILITY_LAMP.get())
                    .build(null));

    /** The inside of a hungry chest, which is a chest's. */
    public static final DeferredHolder<BlockEntityType<?>,
            BlockEntityType<me.moonscenty.alchemia.block.entity.HungryChestBlockEntity>> HUNGRY_CHEST =
            BLOCK_ENTITIES.register("hungry_chest", () -> BlockEntityType.Builder
                    .of(me.moonscenty.alchemia.block.entity.HungryChestBlockEntity::new, ModBlocks.HUNGRY_CHEST.get())
                    .build(null));

    /** A water jug filling itself from the air and pouring into what is round it. */
    public static final DeferredHolder<BlockEntityType<?>,
            BlockEntityType<me.moonscenty.alchemia.block.entity.WaterJugBlockEntity>> WATER_JUG =
            BLOCK_ENTITIES.register("water_jug", () -> BlockEntityType.Builder
                    .of(me.moonscenty.alchemia.block.entity.WaterJugBlockEntity::new, ModBlocks.WATER_JUG.get())
                    .build(null));

    /** A levitator lifting what is in front of it. */
    public static final DeferredHolder<BlockEntityType<?>,
            BlockEntityType<me.moonscenty.alchemia.block.entity.LevitatorBlockEntity>> LEVITATOR =
            BLOCK_ENTITIES.register("levitator", () -> BlockEntityType.Builder
                    .of(me.moonscenty.alchemia.block.entity.LevitatorBlockEntity::new, ModBlocks.LEVITATOR.get())
                    .build(null));

    /** An arcane ear listening for its note. */
    public static final DeferredHolder<BlockEntityType<?>,
            BlockEntityType<me.moonscenty.alchemia.block.entity.ArcaneEarBlockEntity>> ARCANE_EAR =
            BLOCK_ENTITIES.register("arcane_ear", () -> BlockEntityType.Builder
                    .of(me.moonscenty.alchemia.block.entity.ArcaneEarBlockEntity::new, ModBlocks.ARCANE_EAR.get())
                    .build(null));

    /** A spa mixing and pouring. */
    public static final DeferredHolder<BlockEntityType<?>,
            BlockEntityType<me.moonscenty.alchemia.block.entity.SpaBlockEntity>> SPA =
            BLOCK_ENTITIES.register("spa", () -> BlockEntityType.Builder
                    .of(me.moonscenty.alchemia.block.entity.SpaBlockEntity::new, ModBlocks.SPA.get())
                    .build(null));

    /** A recharge pedestal filling the wand on it. */
    public static final DeferredHolder<BlockEntityType<?>,
            BlockEntityType<me.moonscenty.alchemia.block.entity.RechargePedestalBlockEntity>> RECHARGE_PEDESTAL =
            BLOCK_ENTITIES.register("recharge_pedestal", () -> BlockEntityType.Builder
                    .of(me.moonscenty.alchemia.block.entity.RechargePedestalBlockEntity::new,
                            ModBlocks.RECHARGE_PEDESTAL.get())
                    .build(null));

    /** A dioptra reading the aura round it. */
    public static final DeferredHolder<BlockEntityType<?>,
            BlockEntityType<me.moonscenty.alchemia.block.entity.DioptraBlockEntity>> DIOPTRA =
            BLOCK_ENTITIES.register("dioptra", () -> BlockEntityType.Builder
                    .of(me.moonscenty.alchemia.block.entity.DioptraBlockEntity::new, ModBlocks.DIOPTRA.get())
                    .build(null));

    /** An aura totem moving vis; the poles under it have nothing of their own. */
    public static final DeferredHolder<BlockEntityType<?>,
            BlockEntityType<me.moonscenty.alchemia.block.entity.AuraTotemBlockEntity>> AURA_TOTEM =
            BLOCK_ENTITIES.register("aura_totem", () -> BlockEntityType.Builder
                    .of(me.moonscenty.alchemia.block.entity.AuraTotemBlockEntity::new,
                            ModBlocks.AURA_TOTEM_PUSH.get(), ModBlocks.AURA_TOTEM_PULL.get())
                    .build(null));

    /** One square of a portable hole, which remembers what it is standing in for and counts down to giving it back. */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HoleBlockEntity>> HOLE =
            BLOCK_ENTITIES.register("hole", () -> BlockEntityType.Builder
                    .of(HoleBlockEntity::new, ModBlocks.HOLE.get())
                    .build(null));

    /** All sixteen flames share one, since none of them holds anything: it exists only to tick. */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NitorBlockEntity>> NITOR =
            BLOCK_ENTITIES.register("nitor", () -> BlockEntityType.Builder
                    .of(NitorBlockEntity::new,
                            ModBlocks.NITOR.values().stream()
                                    .map(net.neoforged.neoforge.registries.DeferredBlock::get)
                                    .toArray(net.minecraft.world.level.block.Block[]::new))
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NodeStabilizerBlockEntity>> NODE_STABILIZER =
            BLOCK_ENTITIES.register("node_stabilizer", () -> BlockEntityType.Builder
                    .of(NodeStabilizerBlockEntity::new, ModBlocks.NODE_STABILIZER.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ArcaneWorkbenchBlockEntity>> ARCANE_WORKBENCH =
            BLOCK_ENTITIES.register("arcane_workbench", () -> BlockEntityType.Builder
                    .of(ArcaneWorkbenchBlockEntity::new, ModBlocks.ARCANE_WORKBENCH.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ArcaneWorkbenchChargerBlockEntity>>
            ARCANE_WORKBENCH_CHARGER = BLOCK_ENTITIES.register("arcane_workbench_charger",
                    () -> BlockEntityType.Builder
                            .of(ArcaneWorkbenchChargerBlockEntity::new, ModBlocks.ARCANE_WORKBENCH_CHARGER.get())
                            .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CrucibleBlockEntity>> CRUCIBLE =
            BLOCK_ENTITIES.register("crucible", () -> BlockEntityType.Builder
                    .of(CrucibleBlockEntity::new, ModBlocks.CRUCIBLE.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EssentiaSmelterBlockEntity>>
            ESSENTIA_SMELTER = BLOCK_ENTITIES.register("essentia_smelter", () -> BlockEntityType.Builder
                    .of(EssentiaSmelterBlockEntity::new, ModBlocks.ESSENTIA_SMELTER.get(),
                            ModBlocks.ALCHEMIUM_SMELTER.get(), ModBlocks.VOID_SMELTER.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AlembicBlockEntity>> ALEMBIC =
            BLOCK_ENTITIES.register("alembic", () -> BlockEntityType.Builder
                    .of(AlembicBlockEntity::new, ModBlocks.ALEMBIC.get())
                    .build(null));

    /**
     * The plain pipe and the three kinds that only differ in what they let by.
     * <p>
     * A one-way and a restrict keep their answer in the blockstate, so there is nothing for them to remember from
     * one tick to the next that a plain tube does not remember too, and one type serves all three.
     */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TubeBlockEntity>> TUBE =
            BLOCK_ENTITIES.register("tube", () -> BlockEntityType.Builder
                    .of(TubeBlockEntity::new, ModBlocks.TUBE.get(),
                            ModBlocks.TUBE_ONEWAY.get(), ModBlocks.TUBE_RESTRICT.get())
                    .build(null));

    /** The valve remembers when it was last worked, so its handle can be seen swinging rather than snapping. */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ValveTubeBlockEntity>> TUBE_VALVE =
            BLOCK_ENTITIES.register("tube_valve", () -> BlockEntityType.Builder
                    .of(ValveTubeBlockEntity::new, ModBlocks.TUBE_VALVE.get())
                    .build(null));

    /** The filter remembers the aspect it was told to let by. */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FilterTubeBlockEntity>> TUBE_FILTER =
            BLOCK_ENTITIES.register("tube_filter", () -> BlockEntityType.Builder
                    .of(FilterTubeBlockEntity::new, ModBlocks.TUBE_FILTER.get())
                    .build(null));

    /** The buffer remembers what it is sitting on. */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BufferTubeBlockEntity>> TUBE_BUFFER =
            BLOCK_ENTITIES.register("tube_buffer", () -> BlockEntityType.Builder
                    .of(BufferTubeBlockEntity::new, ModBlocks.TUBE_BUFFER.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<JarBlockEntity>> JAR =
            BLOCK_ENTITIES.register("jar", () -> BlockEntityType.Builder
                    .of(JarBlockEntity::new, ModBlocks.JAR.get())
                    .build(null));

    /** The eldritch ring's altar, capstones, obelisk and the way above the altar; and banners. */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<me.moonscenty.alchemia.block.eldritch.EldritchAltarBlockEntity>>
            ELDRITCH_ALTAR = BLOCK_ENTITIES.register("eldritch_altar", () -> BlockEntityType.Builder
                    .of(me.moonscenty.alchemia.block.eldritch.EldritchAltarBlockEntity::new, ModBlocks.ELDRITCH_ALTAR.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<me.moonscenty.alchemia.block.eldritch.EldritchCapBlockEntity>>
            ELDRITCH_CAP = BLOCK_ENTITIES.register("eldritch_cap", () -> BlockEntityType.Builder
                    .of(me.moonscenty.alchemia.block.eldritch.EldritchCapBlockEntity::new, ModBlocks.ELDRITCH_CAP.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<me.moonscenty.alchemia.block.eldritch.EldritchObeliskBlockEntity>>
            ELDRITCH_OBELISK = BLOCK_ENTITIES.register("eldritch_obelisk", () -> BlockEntityType.Builder
                    .of(me.moonscenty.alchemia.block.eldritch.EldritchObeliskBlockEntity::new, ModBlocks.ELDRITCH_OBELISK.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<me.moonscenty.alchemia.block.eldritch.EldritchPortalBlockEntity>>
            ELDRITCH_PORTAL = BLOCK_ENTITIES.register("eldritch_portal", () -> BlockEntityType.Builder
                    .of(me.moonscenty.alchemia.block.eldritch.EldritchPortalBlockEntity::new, ModBlocks.ELDRITCH_PORTAL.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<me.moonscenty.alchemia.block.eldritch.EldritchLockBlockEntity>>
            ELDRITCH_LOCK = BLOCK_ENTITIES.register("eldritch_lock", () -> BlockEntityType.Builder
                    .of(me.moonscenty.alchemia.block.eldritch.EldritchLockBlockEntity::new, ModBlocks.ELDRITCH_LOCK.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<me.moonscenty.alchemia.block.eldritch.CrabSpawnerBlockEntity>>
            CRAB_SPAWNER = BLOCK_ENTITIES.register("crab_spawner", () -> BlockEntityType.Builder
                    .of(me.moonscenty.alchemia.block.eldritch.CrabSpawnerBlockEntity::new, ModBlocks.CRAB_SPAWNER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<me.moonscenty.alchemia.block.BannerBlockEntity>>
            BANNER = BLOCK_ENTITIES.register("banner", () -> BlockEntityType.Builder
                    .of(me.moonscenty.alchemia.block.BannerBlockEntity::new, ModBlocks.BANNER.get()).build(null));

    /** What one pedestal is holding up. */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ArcanePedestalBlockEntity>>
            ARCANE_PEDESTAL = BLOCK_ENTITIES.register("arcane_pedestal", () -> BlockEntityType.Builder
                    .of(ArcanePedestalBlockEntity::new, ModBlocks.ARCANE_PEDESTAL.get(), ModBlocks.ANCIENT_PEDESTAL.get(),
                            ModBlocks.ELDRITCH_PEDESTAL.get())
                    .build(null));

    /** What an infusion is: what is being made, what is owed for it, and how badly it is going. */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<InfusionMatrixBlockEntity>>
            INFUSION_MATRIX = BLOCK_ENTITIES.register("infusion_matrix", () -> BlockEntityType.Builder
                    .of(InfusionMatrixBlockEntity::new, ModBlocks.INFUSION_MATRIX.get())
                    .build(null));

    /** A golem press at work: what it is pressing, and how much more machina that wants. */
    public static final DeferredHolder<BlockEntityType<?>,
            BlockEntityType<me.moonscenty.alchemia.block.entity.GolemPressBlockEntity>> GOLEM_PRESS =
            BLOCK_ENTITIES.register("golem_press", () -> BlockEntityType.Builder
                    .of(me.moonscenty.alchemia.block.entity.GolemPressBlockEntity::new, ModBlocks.GOLEM_PRESS.get())
                    .build(null));

    private ModBlockEntities() {
    }
}
