package me.moonscenty.alchemia.wand.spell;

import java.util.List;

import me.moonscenty.alchemia.entity.Pech;
import me.moonscenty.alchemia.registry.ModEntities;
import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

// one random affliction (all three with nightshade) on everything near the burst
public class PechBlast extends ThrowableProjectile {
    private static final double REACHES = 2.0;
    private static final float HURTS = 2.0F;
    private static final int LASTS = 100;
    private static final float FALLS = 0.025F;
    private static final int GIVES_UP = 500;
    // ticks per rank of extend
    private static final int LONGER = 40;

    private int strength;
    private int longer;
    private boolean nightshade;

    public PechBlast(EntityType<? extends PechBlast> type, Level level) {
        super(type, level);
    }

    public PechBlast(Level level, LivingEntity thrower) {
        super(ModEntities.PECH_BLAST.get(), thrower, level);
    }

    public PechBlast cursing(int strength, int longer, boolean nightshade) {
        this.strength = strength;
        this.longer = longer;
        this.nightshade = nightshade;
        return this;
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
    }

    @Override
    protected double getDefaultGravity() {
        return FALLS;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            level().addParticle(ParticleTypes.WITCH, getX(), getY(), getZ(), 0.0, 0.0, 0.0);
        } else if (tickCount > GIVES_UP) {
            discard();
        }
    }

    @Override
    protected void onHit(HitResult where) {
        super.onHit(where);
        burst(where);
    }

    public void burst(HitResult where) {
        if (!(level() instanceof ServerLevel served)) {
            return;
        }
        served.sendParticles(ParticleTypes.WITCH, getX(), getY(), getZ(), 24, 0.4, 0.4, 0.4, 0.1);
        served.playSound(null, getX(), getY(), getZ(), ModSounds.POOF.get(),
                SoundSource.PLAYERS, 0.4F, 1.0F + (float) served.random.nextGaussian() * 0.05F);

        Entity thrower = getOwner();
        List<LivingEntity> caught = served.getEntitiesOfClass(LivingEntity.class,
                new AABB(position(), position()).inflate(REACHES),
                alive -> alive != thrower && !(alive instanceof Pech));
        int lasts = LASTS + longer * LONGER;
        for (LivingEntity hit : caught) {
            hit.hurt(damageSources().thrown(this, thrower), HURTS + strength);
            // slowness gets one amplifier rank more than the other two, as in the original
            if (nightshade) {
                hit.addEffect(new MobEffectInstance(MobEffects.POISON, lasts, strength));
                hit.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, lasts, strength + 1));
                hit.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, lasts, strength));
            } else {
                switch (random.nextInt(3)) {
                    case 0 -> hit.addEffect(new MobEffectInstance(MobEffects.POISON, lasts, strength));
                    case 1 -> hit.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, lasts, strength + 1));
                    default -> hit.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, lasts, strength));
                }
            }
        }
        discard();
    }

    @Override
    protected void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("strength", strength);
        tag.putInt("longer", longer);
        tag.putBoolean("nightshade", nightshade);
    }

    @Override
    protected void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        strength = tag.getInt("strength");
        longer = tag.getInt("longer");
        nightshade = tag.getBoolean("nightshade");
    }

    @Override
    protected boolean canHitEntity(Entity other) {
        return super.canHitEntity(other) && other != getOwner();
    }

    @Override
    protected void onHitEntity(EntityHitResult struck) {
        super.onHitEntity(struck);
    }
}
