package me.moonscenty.alchemia.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

// worn discounts are whole percentages, summed across armour and baubles
public interface VisDiscount {
    // price floor, original's value: stacked discounts never make work free
    float LEAST = 0.1F;

    int visDiscount(ItemStack stack, Player wearer);

    default int visDiscount(ItemStack stack, Player wearer,
            net.minecraft.core.Holder<me.moonscenty.alchemia.aspect.Aspect> aspect) {
        return 0;
    }

    default Component discountLine(ItemStack stack) {
        return Component.translatable("item.alchemia.vis_discount", visDiscount(stack, null))
                .withStyle(ChatFormatting.AQUA);
    }

    static int worn(Player wearer) {
        return worn(wearer, null);
    }

    static int worn(Player wearer, net.minecraft.core.Holder<me.moonscenty.alchemia.aspect.Aspect> aspect) {
        if (wearer == null) {
            return 0;
        }
        int total = 0;
        for (ItemStack piece : Worn.stacks(wearer, Worn.ARMOUR)) {
            if (piece.getItem() instanceof VisDiscount gear) {
                total += gear.visDiscount(piece, wearer);
                if (aspect != null) {
                    total += gear.visDiscount(piece, wearer, aspect);
                }
            }
        }
        total -= me.moonscenty.alchemia.player.effect.ModEffects.exhaustion(wearer) * EXHAUSTED;
        return total;
    }

    int EXHAUSTED = 10;

    static float rate(float cap, Player wearer) {
        return Math.max(LEAST, cap - worn(wearer) / 100.0F);
    }

    static float rate(float cap, Player wearer,
            net.minecraft.core.Holder<me.moonscenty.alchemia.aspect.Aspect> aspect) {
        return Math.max(LEAST, cap - worn(wearer, aspect) / 100.0F);
    }
}
