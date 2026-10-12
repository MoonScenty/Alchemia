package me.moonscenty.alchemia.entity;

import me.moonscenty.alchemia.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * A chicken the taint has made over. It leaps at what it goes for, flutters down rather than falls, and is never
 * hurt by a fall.
 */
public class TaintedChicken extends TaintedBeast {
    /** How its wings are going, for the renderer, as a chicken's are. */
    public float flap;
    public float flapSpeed;
    public float oFlapSpeed;
    public float oFlap;
    public float flapping = 1.0F;

    public TaintedChicken(EntityType<? extends TaintedChicken> type, Level level) {
        super(type, level);
    }

    public TaintedChicken(Level level) {
        this(ModEntities.TAINTED_CHICKEN.get(), level);
    }

    /** The original's numbers. */
    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 8.0)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.MOVEMENT_SPEED, 0.4)
                .add(Attributes.ARMOR, 2.0);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.3F));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        oFlap = flap;
        oFlapSpeed = flapSpeed;
        flapSpeed += (onGround() ? -1.0F : 4.0F) * 0.3F;
        flapSpeed = Math.max(0.0F, Math.min(1.0F, flapSpeed));
        if (!onGround() && flapping < 1.0F) {
            flapping = 1.0F;
        }
        flapping *= 0.9F;
        Vec3 moving = getDeltaMovement();
        if (!onGround() && moving.y < 0.0) {
            setDeltaMovement(moving.multiply(1.0, 0.9, 1.0));
        }
        flap += flapping * 2.0F;
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.CHICKEN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.CHICKEN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.CHICKEN_HURT;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(SoundEvents.CHICKEN_STEP, 0.15F, 1.0F);
    }

    /** A scrap of goo one time in four, or else a tendril. */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        spawnAtLocation(TaintedMob.gooOrTendril(random, 0.25F), getBbHeight() / 2.0F);
    }
}
