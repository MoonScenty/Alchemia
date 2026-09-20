package me.moonscenty.alchemia.registry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.neoforged.neoforge.registries.DeferredBlock;

/**
 * A building block together with the stairs and slab cut from it.
 */
public record StoneSet(
        DeferredBlock<Block> block,
        DeferredBlock<StairBlock> stairs,
        DeferredBlock<SlabBlock> slab) {
}
