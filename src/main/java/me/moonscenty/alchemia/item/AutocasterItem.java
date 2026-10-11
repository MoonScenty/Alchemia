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

/**
 * An autocaster folded up small enough to carry.
 *
 * <p>It goes against any solid face, which is the whole of why it is an entity and not a block: the original let
 * one hang off a ceiling or a wall as readily as stand on the floor, and nothing is gained by making a player
 * build a shelf for it first.
 */
public class AutocasterItem extends Item {
    public AutocasterItem(Properties properties) {
        super(properties);
    }

    /**
     * Puts one against the face that was clicked.
     * <p>
     * Only against something solid, and only where there is room in front of it. Whoever put it there owns it,
     * and nobody else can open it or take it down again.
     */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos against = context.getClickedPos();
        Direction face = context.getClickedFace();
        Player player = context.getPlayer();
        if (player == null || !level.getBlockState(against).isSolidRender(level, against)) {
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

    /** What it is, in one line: the thing it does that no other item in the mod does. */
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
            java.util.List<net.minecraft.network.chat.Component> lines,
            net.minecraft.world.item.TooltipFlag flag) {
        lines.add(net.minecraft.network.chat.Component.translatable("item.alchemia.autocaster.note")
                .withStyle(net.minecraft.ChatFormatting.DARK_PURPLE));
    }
}
