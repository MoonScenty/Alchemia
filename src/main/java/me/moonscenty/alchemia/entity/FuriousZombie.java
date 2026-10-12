package me.moonscenty.alchemia.entity;

import me.moonscenty.alchemia.registry.ModEntities;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.level.Level;

/**
 * An angry zombie grown huge, and growing with its anger. Every blow it takes makes it a tenth angrier, up to twice
 * as angry as it began, and the anger cools by a five-hundredth a tick. As angry as it is, it is that much bigger
 * and bites five harder for each whole of anger past the first.
 * <p>
 * The original grew its picture by its anger and its hitbox by more than that, so that it stood in four blocks of
 * room drawn in two. Here both grow alike: its size is its anger.
 */
public class FuriousZombie extends AngryZombie {
    private static final float CALM = 1.0F;
    private static final float FURIOUS = 2.0F;
    private static final float PER_BLOW = 0.1F;
    private static final float COOLS = 0.002F;
    private static final double BITE = 7.0;
    private static final double BITE_PER_ANGER = 5.0;

    /** Not saved, as the original's was not: a furious zombie comes back calm. */
    private float anger = CALM;

    public FuriousZombie(EntityType<? extends FuriousZombie> type, Level level) {
        super(type, level);
        xpReward = 15;
    }

    public FuriousZombie(Level level) {
        this(ModEntities.FURIOUS_ZOMBIE.get(), level);
    }

    public static AttributeSupplier.Builder attributes() {
        return AngryZombie.attributes()
                .add(Attributes.MAX_HEALTH, 60.0)
                .add(Attributes.ATTACK_DAMAGE, BITE);
    }

    @Override
    protected void addBehaviourGoals() {
        super.addBehaviourGoals();
        goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.4F));
    }

    public float anger() {
        return anger;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!level().isClientSide) {
            anger = Math.min(FURIOUS, anger + PER_BLOW);
        }
        return super.hurt(source, amount);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) {
            return;
        }
        if (anger > CALM) {
            anger = Math.max(CALM, anger - COOLS);
        }
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(BITE + (anger - CALM) * BITE_PER_ANGER);
        if (getAttribute(Attributes.SCALE).getBaseValue() != anger) {
            getAttribute(Attributes.SCALE).setBaseValue(anger);
        }
    }
}
