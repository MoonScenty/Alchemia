package me.moonscenty.alchemia.aura;

import javax.annotation.Nullable;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.block.CrystalType;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;

/**
 * What a shard set into an aura device tunes it to: one of the six primals or flux, or, for a balanced shard, all of
 * the primals at once.
 *
 * <p>Written down as the original wrote it, a small number: nothing is {@link #NONE}, balance is {@link #BALANCED},
 * and the seven single aspects follow, aer to vitium.
 */
public final class ShardTuning {
    public static final int NONE = -1;
    public static final int BALANCED = 0;
    private static final CrystalType[] SINGLE = {CrystalType.AIR, CrystalType.FIRE, CrystalType.WATER,
            CrystalType.EARTH, CrystalType.ORDER, CrystalType.ENTROPY, CrystalType.FLUX};

    private ShardTuning() {
    }

    /** What a shard tunes to, or {@link #NONE} for anything that is not one. */
    public static int of(ItemStack stack) {
        if (stack.is(ModItems.BALANCED_SHARD.get())) {
            return BALANCED;
        }
        for (int index = 0; index < SINGLE.length; index++) {
            if (stack.is(ModItems.SHARDS.get(SINGLE[index]).get())) {
                return index + 1;
            }
        }
        return NONE;
    }

    /** The one aspect a tuning stands for; none for balance or for nothing. */
    @Nullable
    public static Holder<Aspect> aspect(int tuning) {
        return tuning >= 1 && tuning <= SINGLE.length ? SINGLE[tuning - 1].aspect() : null;
    }

    /** The shard that tunes to it, to give back when it is taken out. */
    public static ItemStack shard(int tuning) {
        if (tuning == BALANCED) {
            return new ItemStack(ModItems.BALANCED_SHARD.get());
        }
        return tuning >= 1 && tuning <= SINGLE.length ? new ItemStack(ModItems.SHARDS.get(SINGLE[tuning - 1]).get())
                : ItemStack.EMPTY;
    }

    /** Whether it is a shard at all. */
    public static boolean isShard(ItemStack stack) {
        return of(stack) != NONE;
    }
}
