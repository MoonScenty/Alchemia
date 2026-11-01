package me.moonscenty.alchemia.player.effect;

import me.moonscenty.alchemia.entity.TaintedMob;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

// Skips the undead and heals the taint's own creatures instead (original's PotionFluxTaint).
public class TaintPoisonEffect extends MobEffect {
    private static final int PERIOD = 40;
    private static final float DOSE = 1.0F;

    public TaintPoisonEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        int gap = PERIOD >> amplifier;
        return gap <= 0 || duration % gap == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) {
            return true;
        }
        if (entity instanceof TaintedMob) {
            entity.heal(DOSE);
        } else if (!entity.isInvertedHealAndHarm()) {
            entity.hurt(entity.damageSources().magic(), DOSE);
        }
        return true;
    }
}
