package me.moonscenty.alchemia.item;

import java.util.List;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.wand.Focus;
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

    public Focus focus() {
        return focus;
    }

    /**
     * The price, written the way the original wrote it: a heading that says whether it is charged once or every
     * tick, then a line for each aspect in its own colour.
     */
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        AspectList cost = focus.cost();
        if (cost.isEmpty()) {
            return;
        }
        lines.add(Component.translatable(focus.perTick()
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
}
