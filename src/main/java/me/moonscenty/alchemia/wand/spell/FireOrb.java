package me.moonscenty.alchemia.wand.spell;

import me.moonscenty.alchemia.registry.ModEntities;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * What the fire focus becomes once a fireball is worked into it: one ball, thrown, that goes off.
 *
 * <p>Not called Fireball: the game already has a class of that name for the ones a ghast throws, and this is
 * nothing to do with it.
 *
 * <p>It is the other half of the same focus rather than a focus of its own. A spray held on a doorway and a ball
 * thrown across a room are different tools, and the one thing this mod can say about which a player has is what
 * they chose to screw into the end of the wand.
 *
 * <p>Whatever it lands on is hit for half again what the blast is worth before the blast itself; the original
 * counted it that way, so a ball caught full in the chest is worse than one that went off nearby.
 */
public class FireOrb extends Bolt {
    /** How big the blast is before anything is worked in, and how much a rank of force adds. */
    private static final float BLAST = 1.0F;
    public static final float HARDER = 0.4F;
    /** What a direct hit is worth, as a share of the blast. */
    private static final float DIRECT = 1.5F;
    private static final int BURNS_OUT = 100;

    private float blast = BLAST;
    /** Whether the ground is left burning round where it went off. */
    private boolean alight;

    public FireOrb(EntityType<? extends FireOrb> type, Level level) {
        super(type, level);
    }

    public FireOrb(Level level, LivingEntity thrower) {
        super(ModEntities.FIRE_ORB.get(), level, thrower);
    }

    /** How hard it goes off, and whether what is left is burning. */
    public FireOrb bursting(float blast, boolean alight) {
        this.blast = blast;
        this.alight = alight;
        return this;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.03;
    }

    @Override
    protected ParticleOptions trail() {
        return ParticleTypes.FLAME;
    }

    @Override
    protected int lives() {
        return BURNS_OUT;
    }

    @Override
    protected void land(ServerLevel level, HitResult where) {
        if (where instanceof EntityHitResult struck) {
            struck.getEntity().hurt(damageSources().indirectMagic(this, getOwner()), blast * DIRECT);
        }
        level.explode(getOwner(), getX(), getY(), getZ(), blast, alight,
                Level.ExplosionInteraction.TNT);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("blast", blast);
        tag.putBoolean("alight", alight);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        blast = tag.contains("blast") ? tag.getFloat("blast") : BLAST;
        alight = tag.getBoolean("alight");
    }
}
