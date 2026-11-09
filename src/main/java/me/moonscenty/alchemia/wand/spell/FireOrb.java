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

public class FireOrb extends Bolt {
    // blast power, and what each rank of potency adds
    private static final float BLAST = 1.0F;
    public static final float HARDER = 0.4F;
    // a direct hit first takes this share of the blast as damage; original's rule
    private static final float DIRECT = 1.5F;
    private static final int BURNS_OUT = 500;

    private float blast = BLAST;
    private boolean alight;

    public FireOrb(EntityType<? extends FireOrb> type, Level level) {
        super(type, level);
    }

    public FireOrb(Level level, LivingEntity thrower) {
        super(ModEntities.FIRE_ORB.get(), level, thrower);
    }

    public FireOrb bursting(float blast, boolean alight) {
        this.blast = blast;
        this.alight = alight;
        return this;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.01;
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
            struck.getEntity().hurt(fireball(), blast * DIRECT);
        }
        // original: spares the terrain, and nobody is safe from the blast
        level.explode(null, getX(), getY(), getZ(), blast, alight, Level.ExplosionInteraction.NONE);
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
