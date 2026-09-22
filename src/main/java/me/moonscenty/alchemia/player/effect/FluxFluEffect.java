package me.moonscenty.alchemia.player.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * The flu the taint gives. A point of harm every forty ticks, and the gap halves with each level above the first,
 * so a bad case is a steady drain rather than a shock. It passes over the undead, who have nothing left to spoil.
 */
public class FluxFluEffect extends MobEffect {
    private static final int PERIOD = 40;
    private static final float DOSE = 1.0F;

    public FluxFluEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        int gap = PERIOD >> amplifier;
        return gap <= 0 || duration % gap == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide && !entity.isInvertedHealAndHarm()) {
            entity.hurt(entity.damageSources().magic(), DOSE);
        }
        return true;
    }
}
