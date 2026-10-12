package me.moonscenty.alchemia.item;

import java.util.List;

import me.moonscenty.alchemia.block.MirrorBlock;
import me.moonscenty.alchemia.block.entity.MirrorBlockEntity;
import me.moonscenty.alchemia.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A mirror in the hand. Held against a mirror of the same kind that has no partner, one of these becomes a mirror
 * that remembers it; put that one down, and the two are a pair. A mirror taken down from a pair remembers its
 * partner the same way.
 */
public class MirrorBlockItem extends BlockItem {
    public MirrorBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!(level.getBlockState(pos).getBlock() instanceof MirrorBlock)) {
            return super.useOn(context);
        }
        Player player = context.getPlayer();
        if (level.isClientSide || player == null) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!(level.getBlockEntity(pos) instanceof MirrorBlockEntity mirror)
                || level.getBlockState(pos).getBlock() != getBlock()) {
            return InteractionResult.PASS;
        }
        if (mirror.paired()) {
            player.displayClientMessage(Component.translatable("block.alchemia.mirror.already_linked")
                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), true);
            return InteractionResult.CONSUME;
        }
        ItemStack linked = context.getItemInHand().copyWithCount(1);
        linked.set(ModDataComponents.MIRROR_LINK.get(), GlobalPos.of(level.dimension(), pos));
        level.playSound(null, pos, SoundEvents.GLASS_PLACE, SoundSource.BLOCKS, 1.0F, 2.0F);
        if (!player.getInventory().add(linked)) {
            player.drop(linked, false);
        }
        if (!player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.CONSUME;
    }

    /** Put down, a mirror that remembers a partner pairs with it, if the partner is free. */
    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, Player player, ItemStack stack,
            BlockState state) {
        boolean changed = super.updateCustomBlockEntityTag(pos, level, player, stack, state);
        GlobalPos link = stack.get(ModDataComponents.MIRROR_LINK.get());
        if (!level.isClientSide && link != null && level.getBlockEntity(pos) instanceof MirrorBlockEntity mirror) {
            mirror.remember(link);
        }
        return changed;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        GlobalPos link = stack.get(ModDataComponents.MIRROR_LINK.get());
        if (link != null) {
            lines.add(Component.translatable("block.alchemia.mirror.linked_to", link.pos().getX(), link.pos().getY(),
                    link.pos().getZ(), link.dimension().location().toString()).withStyle(ChatFormatting.GRAY));
        }
    }
}
