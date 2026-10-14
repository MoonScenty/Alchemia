package me.moonscenty.alchemia.item.bauble;

import java.util.function.Supplier;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.item.VisDiscount;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * An apprentice's ring of one primal: five parts in a hundred off what a wand spends of that primal, and nothing
 * off the rest. Found, never made. Its band takes its primal's colour.
 */
public class ApprenticeRingItem extends BaubleItem implements VisDiscount {
    public static final int OFF = 5;
    private final Supplier<Holder<Aspect>> primal;

    public ApprenticeRingItem(Properties properties, Supplier<Holder<Aspect>> primal) {
        super(properties);
        this.primal = primal;
    }

    public Holder<Aspect> primal() {
        return primal.get();
    }

    @Override
    public int visDiscount(ItemStack stack, Player wearer) {
        return 0;
    }

    @Override
    public int visDiscount(ItemStack stack, Player wearer, Holder<Aspect> aspect) {
        return aspect.value() == primal.get().value() ? OFF : 0;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.alchemia.apprentice_ring", primal.get().value().displayName());
    }
}
