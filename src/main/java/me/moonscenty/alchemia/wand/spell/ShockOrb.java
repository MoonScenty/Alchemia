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

/**
 * What the shock focus becomes once earth shock is worked into it: a ball lobbed at the ground rather than a
 * bolt sent at a face.
 *
 * <p>It is the answer to a crowd. A bolt picks one thing and hurts it badly; this picks the floor and hurts
 * everything standing on it.
 *
 * <p><b>Different from the original.</b> The original also left the ground itself live for a while afterwards,
 * which it did by setting a block of its own down in a scatter round the blast. There is no such block in this
 * mod yet, so what is left behind is only the flash.
 */
public class ShockOrb extends Bolt {
    /** How far the blast reaches and what it does, before anything is worked in. */
    private static final int REACHES = 4;
    private static final float HURTS = 5.0F;
    private static final int BURNS_OUT = 500;
    /** How many sparks are thrown off, and how fast they scatter. */
    private static final int SPARKS = 60;

    private int reaches = REACHES;
    private float hurts = HURTS;

    public ShockOrb(EntityType<? extends ShockOrb> type, Level level) {
        super(type, level);
    }

    public ShockOrb(Level level, LivingEntity thrower) {
        super(ModEntities.SHOCK_ORB.get(), level, thrower);
    }

    /** How wide and how hard, which is what force and widening are worth to it. */
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

    /** Everything round where it fell that can be seen from it. */
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
