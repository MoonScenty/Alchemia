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

public class ShockOrb extends Bolt {
    private static final int REACHES = 4;
    private static final float HURTS = 5.0F;
    private static final int BURNS_OUT = 500;
    private static final int SPARKS = 60;
    // original: tries at electrifying the ground around the blast
    private static final int GROUNDS = 20;

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
                alive -> alive.hasLineOfSight(this))) {
            caught.hurt(damageSources().indirectMagic(this, thrower), hurts);
        }
        electrify(level);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, getX(), getY(), getZ(),
                SPARKS, reaches / 2.0, 0.5, reaches / 2.0, 0.2);
        level.playSound(null, getX(), getY(), getZ(), ModSounds.SHOCK.get(),
                SoundSource.PLAYERS, 1.0F, 1.0F + (random.nextFloat() - random.nextFloat()) * 0.2F);
    }

    // original: drops straight down from above the blast to open ground the orb can see
    private void electrify(ServerLevel level) {
        int spread = Math.max(1, reaches);
        int x0 = net.minecraft.util.Mth.floor(getX());
        int y0 = net.minecraft.util.Mth.floor(getY());
        int z0 = net.minecraft.util.Mth.floor(getZ());
        var shock = me.moonscenty.alchemia.registry.ModBlocks.SHOCK.get();
        for (int tries = 0; tries < GROUNDS; tries++) {
            int x = x0 + random.nextInt(spread) - random.nextInt(spread);
            int z = z0 + random.nextInt(spread) - random.nextInt(spread);
            int y = y0 + spread;
            net.minecraft.core.BlockPos ground = new net.minecraft.core.BlockPos(x, y, z);
            while (level.isEmptyBlock(ground) && y > y0 - spread) {
                ground = new net.minecraft.core.BlockPos(x, --y, z);
            }
            net.minecraft.core.BlockPos above = ground.above();
            if (level.isEmptyBlock(above) && !level.isEmptyBlock(ground) && !level.getBlockState(above).is(shock)
                    && level.clip(new net.minecraft.world.level.ClipContext(position(),
                            net.minecraft.world.phys.Vec3.atCenterOf(above),
                            net.minecraft.world.level.ClipContext.Block.COLLIDER,
                            net.minecraft.world.level.ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS) {
                level.setBlockAndUpdate(above, shock.defaultBlockState());
            }
        }
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
