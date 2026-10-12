package me.moonscenty.alchemia.item;

import java.util.List;
import java.util.Optional;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.aspect.ContainedAspects;
import me.moonscenty.alchemia.registry.ModDataComponents;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * What is left of a wisp: a wisp of essence, two points of whatever the wisp was, and a breath of auram besides.
 * Pechs carry them too.
 */
public class WispyEssenceItem extends Item implements ContainedAspects {
    /** How much of its aspect a wisp leaves. The original's. */
    public static final int HOLDS = 2;

    public WispyEssenceItem(Properties properties) {
        super(properties);
    }

    /** What it is a wisp of, if anything. */
    public static Optional<Holder<Aspect>> inside(ItemStack stack) {
        return Optional.ofNullable(stack.get(ModDataComponents.ESSENTIA.get()));
    }

    public static ItemStack of(Holder<Aspect> aspect) {
        ItemStack essence = new ItemStack(ModItems.WISPY_ESSENCE.get());
        essence.set(ModDataComponents.ESSENTIA.get(), aspect);
        return essence;
    }

    @Override
    public AspectList contained(ItemStack stack) {
        return inside(stack).map(aspect -> AspectList.of(aspect, HOLDS)).orElse(AspectList.EMPTY);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        inside(stack).ifPresent(aspect -> lines.add(Component
                .translatable("item.alchemia.wispy_essence.holding", aspect.value().displayName())
                .withStyle(ChatFormatting.GRAY)));
    }
}
