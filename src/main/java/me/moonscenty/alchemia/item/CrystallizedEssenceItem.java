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

/**
 * A single point of one essentia, set hard enough to carry.
 * <p>
 * Not the crystal that grows on a node -- that one is called after the aspect it is made of, and there is only
 * one of those to an element. This is the aspect itself gone solid, and there is one for every aspect there is.
 * <p>
 * It is drawn in grey and painted by whatever is in it, so one picture serves for every aspect there is and no
 * new one is needed when another is added. What it holds is a component rather than thirty-five items, for the
 * same reason a phial holds one: an item per aspect is an item per aspect forever.
 */
public class CrystallizedEssenceItem extends Item {
    public CrystallizedEssenceItem(Properties properties) {
        super(properties);
    }

    /** What is in it, if anything. A crystal of nothing is a curiosity, not a thing anyone should have. */
    public static Optional<Holder<Aspect>> inside(ItemStack stack) {
        return Optional.ofNullable(stack.get(ModDataComponents.ESSENTIA.get()));
    }

    public static ItemStack of(Holder<Aspect> aspect) {
        ItemStack crystal = new ItemStack(me.moonscenty.alchemia.registry.ModItems.CRYSTALLIZED_ESSENCE.get());
        crystal.set(ModDataComponents.ESSENTIA.get(), aspect);
        return crystal;
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
