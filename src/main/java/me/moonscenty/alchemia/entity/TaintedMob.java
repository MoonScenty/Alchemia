package me.moonscenty.alchemia.entity;

import me.moonscenty.alchemia.player.effect.ModEffects;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;

/**
 * A creature of the taint. They never turn on one another, and count one another as their own side.
 */
public interface TaintedMob {
    /** Whether a tainted creature would ever go for this kind of thing: anything but another of its own. */
    static boolean mayAttack(EntityType<?> type, Entity sample) {
        return !(sample instanceof TaintedMob);
    }

    /** Whether this is on the taint's side. */
    static boolean allied(Entity other) {
        return other instanceof TaintedMob;
    }

    /**
     * What a tainted creature's bite carries: on normal and hard, now and then, taint poison for three seconds or six.
     * Every creature of the taint that bites at all bites with this, as the original's did.
     */
    static void poison(Mob biter, Entity victim) {
        if (!(victim instanceof LivingEntity living)) {
            return;
        }
        Difficulty difficulty = biter.level().getDifficulty();
        int strength = difficulty == Difficulty.NORMAL ? 3 : difficulty == Difficulty.HARD ? 6 : 0;
        if (strength > 0 && biter.getRandom().nextInt(strength + 1) > 2) {
            living.addEffect(new MobEffectInstance(ModEffects.FLUX_FLU, strength * 20, 0), biter);
        }
    }

    /** A scrap of what it was made of: tainted goo this share of the time, or else a tendril. */
    static ItemStack gooOrTendril(RandomSource random, float goo) {
        return new ItemStack(random.nextFloat() < goo ? ModItems.TAINTED_GOO.get() : ModItems.TAINT_TENDRIL.get());
    }
}
