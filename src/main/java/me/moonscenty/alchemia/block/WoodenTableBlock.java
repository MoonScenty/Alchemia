package me.moonscenty.alchemia.block;

import me.moonscenty.alchemia.block.entity.ArcaneWorkbenchBlockEntity;
import me.moonscenty.alchemia.block.entity.ResearchTableBlockEntity;
import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * A wooden table, the original's {@code BlockTable}: where a thaumaturge starts. Set scribing tools down on it and it
 * becomes a research table with the tools in their place; lay a wand on it and it becomes an arcane workbench with
 * the wand in its slot.
 */
public class WoodenTableBlock extends Block {
    public WoodenTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        boolean tools = held.is(ModItems.SCRIBING_TOOLS.get());
        boolean wand = held.getItem() instanceof WandItem && !WandItem.isStaff(held);
        if (!tools && !wand) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        ItemStack laid = held.copyWithCount(1);
        if (tools) {
            level.setBlockAndUpdate(pos, ModBlocks.RESEARCH_TABLE.get().defaultBlockState()
                    .setValue(ResearchTableBlock.FACING, player.getDirection().getOpposite()));
            if (level.getBlockEntity(pos) instanceof ResearchTableBlockEntity table) {
                table.put(ResearchTableBlockEntity.SLOT_TOOLS, laid);
            }
        } else {
            level.setBlockAndUpdate(pos, ModBlocks.ARCANE_WORKBENCH.get().defaultBlockState());
            if (level.getBlockEntity(pos) instanceof ArcaneWorkbenchBlockEntity bench) {
                bench.setItem(ArcaneWorkbenchBlockEntity.SLOT_WAND, laid);
            }
        }
        held.consume(1, player);
        return ItemInteractionResult.CONSUME;
    }
}
