package me.moonscenty.alchemia.item;

import java.util.List;

import me.moonscenty.alchemia.aura.node.AuraNode;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * Hangs a node in the air off whatever face is clicked. Creative only: there is no recipe, and it is never used up.
 * <p>
 * The node is drawn up the way a wild one is, so it is anybody's guess what comes out. Something particular is what
 * the command is for.
 */
public class NodePlacerItem extends Item {
    public NodePlacerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos at = context.getClickedPos().relative(context.getClickedFace());
        if (!level.getBlockState(at).canBeReplaced()) {
            return InteractionResult.FAIL;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        AuraNode node = new AuraNode(level);
        node.moveTo(at.getX() + 0.5, at.getY() + 0.5, at.getZ() + 0.5, 0.0F, 0.0F);
        node.drawUp(level.getRandom());
        level.addFreshEntity(node);
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.alchemia.creative_only").withStyle(ChatFormatting.DARK_PURPLE));
    }
}
