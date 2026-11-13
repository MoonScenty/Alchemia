package me.moonscenty.alchemia.item;

import me.moonscenty.alchemia.entity.Autocaster;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class AutocasterItem extends Item {
    public AutocasterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos against = context.getClickedPos();
        Direction face = context.getClickedFace();
        Player player = context.getPlayer();
        if (player == null || !level.getBlockState(against).isSolid()) {
            return InteractionResult.PASS;
        }

        BlockPos at = against.relative(face);
        BlockState there = level.getBlockState(at);
        if (!there.canBeReplaced() || !player.mayUseItemAt(at, face, context.getItemInHand())) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        level.removeBlock(at, false);
        Autocaster turret = new Autocaster(level, at, face);
        turret.setOwner(player.getUUID());
        level.addFreshEntity(turret);
        if (!player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
            java.util.List<net.minecraft.network.chat.Component> lines,
            net.minecraft.world.item.TooltipFlag flag) {
        lines.add(net.minecraft.network.chat.Component.translatable("item.alchemia.autocaster.note")
                .withStyle(net.minecraft.ChatFormatting.DARK_PURPLE));
    }
}
