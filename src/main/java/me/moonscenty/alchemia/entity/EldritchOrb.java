package me.moonscenty.alchemia.entity;

import me.moonscenty.alchemia.registry.ModEntities;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

// Gone on its first burst; the original's could burst again the next tick.
public class EldritchOrb extends ThrowableProjectile {
    private static final int LASTS = 100;

    public EldritchOrb(EntityType<? extends EldritchOrb> type, Level level) {
        super(type, level);
    }

    public EldritchOrb(Level level, LivingEntity thrower) {
        super(ModEntities.ELDRITCH_ORB.get(), thrower, level);
        Vec3 look = thrower.getLookAngle();
        moveTo(thrower.getX() + look.x / 2.0, thrower.getEyeY() + look.y / 2.0, thrower.getZ() + look.z / 2.0,
                thrower.getYRot(), thrower.getXRot());
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    @Override
    public void tick() {
        super.tick();
        if (tickCount > LASTS && !level().isClientSide) {
            discard();
        }
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (level().isClientSide || isRemoved()) {
            return;
        }
        if (getOwner() instanceof LivingEntity thrower) {
            float hurts = (float) thrower.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.666F;
            for (LivingEntity caught : level().getEntitiesOfClass(LivingEntity.class,
                    getBoundingBox().inflate(2.0), living -> living != thrower && living.isAlive()
                            && !living.getType().is(EntityTypeTags.UNDEAD))) {
                caught.hurt(damageSources().indirectMagic(this, thrower), hurts);
                caught.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 0), thrower);
            }
        }
        playSound(SoundEvents.FIRE_EXTINGUISH, 0.5F, 2.6F + (random.nextFloat() - random.nextFloat()) * 0.8F);
        level().broadcastEntityEvent(this, (byte) 16);
        discard();
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 16) {
            me.moonscenty.alchemia.client.EldritchEffects.burst(this);
        } else {
            super.handleEntityEvent(id);
        }
    }
}
