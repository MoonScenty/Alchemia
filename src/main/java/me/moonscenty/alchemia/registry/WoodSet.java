package me.moonscenty.alchemia.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.neoforged.neoforge.registries.DeferredBlock;

/**
 * All blocks made from one kind of tree.
 */
public record WoodSet(
        String name,
        DeferredBlock<RotatedPillarBlock> log,
        DeferredBlock<Block> planks,
        DeferredBlock<LeavesBlock> leaves,
        DeferredBlock<SaplingBlock> sapling,
        DeferredBlock<StairBlock> stairs,
        DeferredBlock<SlabBlock> slab) {

    public TagKey<Block> logsBlockTag() {
        return TagKey.create(Registries.BLOCK, log.getId().withPath(name + "_logs"));
    }

    public TagKey<Item> logsItemTag() {
        return TagKey.create(Registries.ITEM, log.getId().withPath(name + "_logs"));
    }
}
