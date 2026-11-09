package me.moonscenty.alchemia.block;

import net.minecraft.util.Mth;

public final class EssentiaSignal {
    private EssentiaSignal() {
    }

    // original's scale: anything in it reads at least 1
    public static int of(int amount, int capacity) {
        return Mth.floor((float) amount / capacity * 14.0F) + (amount > 0 ? 1 : 0);
    }
}
