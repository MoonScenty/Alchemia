package me.moonscenty.alchemia.player.effect;

import java.util.function.BiConsumer;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * A lingering ailment brought on by warp. The work is handed to a small function so each one stays a single line
 * where it is registered.
 */
public class WarpEffect extends MobEffect {
    private final int period;
    private final BiConsumer<LivingEntity, Integer> onTick;

    /**
     * @param period how many ticks between doses; 1 means every tick
     * @param onTick what the ailment does, given the sufferer and the amplifier
     */
    public WarpEffect(MobEffectCategory category, int color, int period, BiConsumer<LivingEntity, Integer> onTick) {
        super(category, color);
        this.period = period;
        this.onTick = onTick;
    }

    /** An ailment that only marks the player, with nothing happening on its own. */
    public static WarpEffect inert(MobEffectCategory category, int color) {
        return new WarpEffect(category, color, Integer.MAX_VALUE, (entity, amplifier) -> {
        });
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return period != Integer.MAX_VALUE && duration % period == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide) {
            onTick.accept(entity, amplifier);
        }
        return true;
    }
}
