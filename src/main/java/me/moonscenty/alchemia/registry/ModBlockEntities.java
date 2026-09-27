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
import me.moonscenty.alchemia.block.entity.EssentiaSmelterBlockEntity;
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
                    .of(EssentiaSmelterBlockEntity::new, ModBlocks.ESSENTIA_SMELTER.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AlembicBlockEntity>> ALEMBIC =
            BLOCK_ENTITIES.register("alembic", () -> BlockEntityType.Builder
                    .of(AlembicBlockEntity::new, ModBlocks.ALEMBIC.get())
                    .build(null));

    /**
     * The plain pipe and the three kinds that only differ in what they let by.
     * <p>
     * A valve, a one-way and a restrict all keep their answer in the blockstate, so there is nothing for them to
     * remember from one tick to the next that a plain tube does not remember too, and one type serves all four.
     */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TubeBlockEntity>> TUBE =
            BLOCK_ENTITIES.register("tube", () -> BlockEntityType.Builder
                    .of(TubeBlockEntity::new, ModBlocks.TUBE.get(), ModBlocks.TUBE_VALVE.get(),
                            ModBlocks.TUBE_ONEWAY.get(), ModBlocks.TUBE_RESTRICT.get())
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

    /** What one pedestal is holding up. */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ArcanePedestalBlockEntity>>
            ARCANE_PEDESTAL = BLOCK_ENTITIES.register("arcane_pedestal", () -> BlockEntityType.Builder
                    .of(ArcanePedestalBlockEntity::new, ModBlocks.ARCANE_PEDESTAL.get())
                    .build(null));

    private ModBlockEntities() {
    }
}
