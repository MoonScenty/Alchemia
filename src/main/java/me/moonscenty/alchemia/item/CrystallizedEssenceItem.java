package me.moonscenty.alchemia.item;

import java.util.List;
import java.util.Optional;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class CrystallizedEssenceItem extends Item implements me.moonscenty.alchemia.aspect.ContainedAspects {
    public CrystallizedEssenceItem(Properties properties) {
        super(properties);
    }

    public static Optional<Holder<Aspect>> inside(ItemStack stack) {
        return Optional.ofNullable(stack.get(ModDataComponents.ESSENTIA.get()));
    }

    public static ItemStack of(Holder<Aspect> aspect) {
        ItemStack crystal = new ItemStack(me.moonscenty.alchemia.registry.ModItems.CRYSTALLIZED_ESSENCE.get());
        crystal.set(ModDataComponents.ESSENTIA.get(), aspect);
        return crystal;
    }

    @Override
    public me.moonscenty.alchemia.aspect.AspectList contained(ItemStack stack) {
        return inside(stack).map(aspect -> me.moonscenty.alchemia.aspect.AspectList.of(aspect, 1))
                .orElse(me.moonscenty.alchemia.aspect.AspectList.EMPTY);
    }

    @Override
    public Component getName(ItemStack stack) {
        return inside(stack)
                .map(aspect -> (Component) Component.translatable(getDescriptionId() + ".of",
                        aspect.value().displayName()))
                .orElseGet(() -> super.getName(stack));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        inside(stack).ifPresent(aspect -> lines.add(Component
                .translatable("item.alchemia.crystallized_essence.holding", aspect.value().displayName())
                .withStyle(ChatFormatting.GRAY)));
    }
}
