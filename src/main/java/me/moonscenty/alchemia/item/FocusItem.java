package me.moonscenty.alchemia.item;

import java.util.List;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.wand.Focus;
import me.moonscenty.alchemia.wand.FocusUpgrade;
import me.moonscenty.alchemia.wand.FocusUpgrades;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

/**
 * One of the twelve things a wand can be made to do.
 *
 * <p>Every focus is this same item with a different {@link Focus} behind it. The original had a class apiece
 * because each one also carried what it did when it went off, and that is still to come; what is here is the part
 * that is the same for all twelve — one to a stack, rare, and a price written on the front.
 *
 * <p>A focus alone does nothing. It has to be fitted to a wand, and the wand has to be the one pointed at
 * something.
 */
public class FocusItem extends Item {
    private final Focus focus;

    public FocusItem(Focus focus, Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.RARE));
        this.focus = focus;
    }

    /**
     * What this focus does and costs, once what is worked into it is taken into account.
     * <p>
     * This is the one to ask nearly everywhere. A fire focus with a fireball in it is thrown rather than held
     * down and costs eight times as much, and anything reading the plain values would charge the wrong price
     * for the wrong working.
     */
    public Focus focus(ItemStack stack) {
        Focus tuned = FocusUpgrades.tuned(stack);
        return tuned == null ? focus : tuned;
    }

    /** What it is before anybody has worked on it, which is what the upgrades are applied to. */
    public Focus base() {
        return focus;
    }

    /**
     * The price, written the way the original wrote it: a heading that says whether it is charged once or every
     * tick, then a line for each aspect in its own colour.
     */
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        Focus fitted = focus(stack);
        AspectList cost = fitted.cost();
        if (!cost.isEmpty()) {
            lines.add(Component.translatable(fitted.perTick()
                            ? "item.alchemia.focus.cost_per_tick"
                            : "item.alchemia.focus.cost")
                    .withStyle(ChatFormatting.GRAY));
            for (Holder<Aspect> aspect : cost.sortedByName()) {
                Aspect named = aspect.value();
                lines.add(Component.translatable("item.alchemia.focus.cost_line",
                                named.displayName(), cost.get(aspect))
                        .withStyle(style -> style.withColor(named.color())));
            }
        }
        worked(stack, lines);
    }

    /**
     * What has been worked into it, one line apiece.
     * <p>
     * The same thing twice is written once with a number after it, the way an enchantment is: a focus with two
     * ranks of frugal in it is frugal II and not frugal, frugal.
     */
    private static void worked(ItemStack stack, List<Component> lines) {
        java.util.Map<FocusUpgrade, Integer> counted = new java.util.LinkedHashMap<>();
        for (FocusUpgrade upgrade : FocusUpgrades.on(stack)) {
            counted.merge(upgrade, 1, Integer::sum);
        }
        counted.forEach((upgrade, level) -> {
            Component name = Component.translatable(upgrade.translationKey());
            lines.add((level > 1
                    ? Component.translatable("item.alchemia.focus.upgrade_level", name,
                            Component.translatable("enchantment.level." + level))
                    : name).copy().withStyle(ChatFormatting.DARK_PURPLE));
        });
    }
}
