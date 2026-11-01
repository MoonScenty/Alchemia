package me.moonscenty.alchemia.wand.spell;

import me.moonscenty.alchemia.registry.ModEntities;
import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;

// the original also electrified the ground with a custom block; not ported yet
public class ShockOrb extends Bolt {
    private static final int REACHES = 4;
    private static final float HURTS = 5.0F;
    private static final int BURNS_OUT = 500;
    private static final int SPARKS = 60;

    private int reaches = REACHES;
    private float hurts = HURTS;

    public ShockOrb(EntityType<? extends ShockOrb> type, Level level) {
        super(type, level);
    }

    public ShockOrb(Level level, LivingEntity thrower) {
        super(ModEntities.SHOCK_ORB.get(), level, thrower);
    }

    public ShockOrb striking(int reaches, float hurts) {
        this.reaches = reaches;
        this.hurts = hurts;
        return this;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.05;
    }

    @Override
    protected ParticleOptions trail() {
        return ParticleTypes.ELECTRIC_SPARK;
    }

    @Override
    protected int lives() {
        return BURNS_OUT;
    }

    @Override
    protected void land(ServerLevel level, HitResult where) {
        Entity thrower = getOwner();
        for (LivingEntity caught : level.getEntitiesOfClass(LivingEntity.class,
                new AABB(position(), position()).inflate(reaches),
                alive -> alive != thrower && alive.hasLineOfSight(this))) {
            caught.hurt(damageSources().indirectMagic(this, thrower), hurts);
        }
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, getX(), getY(), getZ(),
                SPARKS, reaches / 2.0, 0.5, reaches / 2.0, 0.2);
        level.playSound(null, getX(), getY(), getZ(), ModSounds.SHOCK.get(),
                SoundSource.PLAYERS, 1.0F, 1.0F + (random.nextFloat() - random.nextFloat()) * 0.2F);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("reaches", reaches);
        tag.putFloat("hurts", hurts);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        reaches = tag.contains("reaches") ? tag.getInt("reaches") : REACHES;
        hurts = tag.contains("hurts") ? tag.getFloat("hurts") : HURTS;
    }
}
