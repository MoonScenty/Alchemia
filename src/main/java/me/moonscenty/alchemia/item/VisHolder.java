package me.moonscenty.alchemia.item;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface VisHolder {
    // frugal focus upgrades only discount WORKING, never crafting
    enum Spending {
        WORKING,
        CRAFTING
    }

    int held(ItemStack stack, Holder<Aspect> aspect);

    void take(ItemStack stack, AspectList cost, Player player, Spending why);

    boolean holds(ItemStack stack, AspectList cost, Player player, Spending why);

    default float priceRate(ItemStack stack, Player player, Spending why, Holder<Aspect> aspect) {
        return 1.0F;
    }

    static VisHolder of(ItemStack stack) {
        return stack.getItem() instanceof VisHolder holder ? holder : null;
    }

    static boolean canPay(ItemStack stack, AspectList cost, Player player, Spending why) {
        if (cost.isEmpty()) {
            return true;
        }
        VisHolder holder = of(stack);
        return holder != null && holder.holds(stack, cost, player, why);
    }

    static boolean pay(ItemStack stack, AspectList cost, Player player, Spending why) {
        VisHolder holder = of(stack);
        if (holder == null || !holder.holds(stack, cost, player, why)) {
            return false;
        }
        if (!player.level().isClientSide) {
            holder.take(stack, cost, player, why);
        }
        return true;
    }
}
