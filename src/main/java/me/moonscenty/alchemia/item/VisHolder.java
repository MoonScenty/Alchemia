package me.moonscenty.alchemia.item;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Something that carries vis about and can be made to pay it out: a wand, and later a staff or a sceptre.
 * <p>
 * The arcane workbench is written against this rather than against the wand, so the bench could be built and tried
 * before there was any wand to lay on it, and so anything else that stores vis can be used at one later.
 */
public interface VisHolder {
    /** How much of one aspect this is carrying. */
    int held(ItemStack stack, Holder<Aspect> aspect);

    /** Pays out a price. Only called once {@link #holds} has said it can be paid. */
    void take(ItemStack stack, AspectList cost, Player player);

    /** Whether the whole price could be paid out of this. */
    default boolean holds(ItemStack stack, AspectList cost) {
        for (Holder<Aspect> aspect : cost.sortedByName()) {
            if (held(stack, aspect) < cost.get(aspect)) {
                return false;
            }
        }
        return true;
    }

    /** The holder in a stack, if it is one. */
    static VisHolder of(ItemStack stack) {
        return stack.getItem() instanceof VisHolder holder ? holder : null;
    }

    /** Whether a price can be paid out of a stack. A free price needs nothing, not even a wand. */
    static boolean canPay(ItemStack stack, AspectList cost) {
        if (cost.isEmpty()) {
            return true;
        }
        VisHolder holder = of(stack);
        return holder != null && holder.holds(stack, cost);
    }
}
