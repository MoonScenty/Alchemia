package me.moonscenty.alchemia.item;

import java.util.List;

import me.moonscenty.alchemia.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

/** A brain in a jar in the hand, saying how much it has eaten, as the original's did. */
public class BrainJarItem extends BlockItem {
    public BrainJarItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        Integer xp = stack.get(ModDataComponents.STORED_XP.get());
        if (xp != null && xp > 0) {
            lines.add(Component.translatable("block.alchemia.brain_jar.xp", xp)
                    .withStyle(ChatFormatting.DARK_PURPLE));
        }
    }
}
