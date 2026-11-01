package me.moonscenty.alchemia.item;

import java.util.List;

import me.moonscenty.alchemia.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class JarLabelItem extends Item {
    public JarLabelItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        return PhialItem.inside(stack)
                .map(aspect -> (Component) Component.translatable(getDescriptionId() + ".written",
                        aspect.value().displayName()))
                .orElseGet(() -> super.getName(stack));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        if (stack.has(ModDataComponents.ESSENTIA.get())) {
            return;
        }
        lines.add(Component.translatable("item.alchemia.jar_label.blank").withStyle(ChatFormatting.GRAY));
    }
}
