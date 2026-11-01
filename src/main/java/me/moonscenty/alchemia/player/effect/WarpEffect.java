package me.moonscenty.alchemia.player.effect;

import java.util.function.BiConsumer;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class WarpEffect extends MobEffect {
    private final int period;
    private final BiConsumer<LivingEntity, Integer> onTick;

    public WarpEffect(MobEffectCategory category, int color, int period, BiConsumer<LivingEntity, Integer> onTick) {
        super(category, color);
        this.period = period;
        this.onTick = onTick;
    }

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
