package me.moonscenty.alchemia.registry;

import me.moonscenty.alchemia.Alchemia;
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

    private ModBlockEntities() {
    }
}
