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

public class FocusItem extends Item {
    private final Focus focus;

    public FocusItem(Focus focus, Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.RARE));
        this.focus = focus;
    }

    // use this rather than base(): upgrades change cost and behaviour
    public Focus focus(ItemStack stack) {
        Focus tuned = FocusUpgrades.tuned(stack);
        Focus found = tuned == null ? focus : tuned;
        if (stack.is(me.moonscenty.alchemia.registry.ModItems.FOCI.get("primal").get())) {
            return found.costing(primalCost(System.currentTimeMillis()));
        }
        return found;
    }

    // rerolled every 200 ms from a time-seeded generator so client and server agree; original's draw
    public static AspectList primalCost(long millis) {
        java.util.Random draw = new java.util.Random(millis / 200L);
        return AspectList.of(me.moonscenty.alchemia.registry.ModAspects.WATER, 10 + draw.nextInt(5) * 2)
                .add(me.moonscenty.alchemia.registry.ModAspects.AIR, 10 + draw.nextInt(5) * 2)
                .add(me.moonscenty.alchemia.registry.ModAspects.EARTH, 10 + draw.nextInt(5) * 2)
                .add(me.moonscenty.alchemia.registry.ModAspects.FIRE, 10 + draw.nextInt(5) * 2)
                .add(me.moonscenty.alchemia.registry.ModAspects.ORDER, 10 + draw.nextInt(5) * 2)
                .add(me.moonscenty.alchemia.registry.ModAspects.ENTROPY, 10 + draw.nextInt(5) * 2);
    }

    public Focus base() {
        return focus;
    }

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
