package me.moonscenty.alchemia.aura;

import javax.annotation.Nullable;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.block.CrystalType;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;

// Original's encoding: NONE, BALANCED, then the seven single aspects aer..vitium.
public final class ShardTuning {
    public static final int NONE = -1;
    public static final int BALANCED = 0;
    private static final CrystalType[] SINGLE = {CrystalType.AIR, CrystalType.FIRE, CrystalType.WATER,
            CrystalType.EARTH, CrystalType.ORDER, CrystalType.ENTROPY, CrystalType.FLUX};

    private ShardTuning() {
    }

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

    @Nullable
    public static Holder<Aspect> aspect(int tuning) {
        return tuning >= 1 && tuning <= SINGLE.length ? SINGLE[tuning - 1].aspect() : null;
    }

    public static ItemStack shard(int tuning) {
        if (tuning == BALANCED) {
            return new ItemStack(ModItems.BALANCED_SHARD.get());
        }
        return tuning >= 1 && tuning <= SINGLE.length ? new ItemStack(ModItems.SHARDS.get(SINGLE[tuning - 1]).get())
                : ItemStack.EMPTY;
    }

    public static boolean isShard(ItemStack stack) {
        return of(stack) != NONE;
    }
}
