package me.moonscenty.alchemia.item;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** The key to a great hall of the Outer Lands, kept in the maze's key room. It goes into the lock on the hall's door. */
public class RunedTabletItem extends Item {
    public RunedTabletItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.alchemia.runed_tablet.text").withStyle(ChatFormatting.DARK_PURPLE));
    }
}
