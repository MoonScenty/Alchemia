package me.moonscenty.alchemia.wand.spell;

import me.moonscenty.alchemia.registry.ModEntities;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class FrostShard extends Bolt {
    private static final float HURTS = 3.0F;
    // gravity for spikes, then for splinters
    private static final double FALLS = 0.03;
    private static final double DRIFTS = 0.015;
    private static final int MELTS_AFTER = 100;
    // chill duration in ticks, and the share of speed a boulder keeps off a wall
    private static final int CHILLS_FOR = 200;
    private static final double KEEPS = 0.8;

    private float hurts = HURTS;
    private int chills;
    private boolean splinter;
    private int bounces;

    public FrostShard(EntityType<? extends FrostShard> type, Level level) {
        super(type, level);
    }

    public FrostShard(Level level, LivingEntity thrower) {
        super(ModEntities.FROST_SHARD.get(), level, thrower);
    }

    @Override
    protected double getDefaultGravity() {
        return splinter ? DRIFTS : FALLS;
    }

    public FrostShard biting(float hurts, int chills) {
        this.hurts = hurts;
        this.chills = chills;
        return this;
    }

    public FrostShard asSplinter() {
        this.splinter = true;
        return this;
    }

    public FrostShard bouncing(int walls) {
        this.bounces = walls;
        return this;
    }

    @Override
    protected ParticleOptions trail() {
        return ParticleTypes.SNOWFLAKE;
    }

    @Override
    protected int lives() {
        return MELTS_AFTER;
    }

    @Override
    public void lands(HitResult where) {
        if (!(level() instanceof ServerLevel served)) {
            return;
        }
        if (where instanceof EntityHitResult struck) {
            struck.getEntity().hurt(damageSources().indirectMagic(this, getOwner()), hurts);
            if (chills > 0 && struck.getEntity() instanceof LivingEntity chilled) {
                chilled.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, CHILLS_FOR, chills - 1));
            }
        }
        if (splinter || bounces-- <= 0) {
            land(served, where);
            discard();
            return;
        }
        if (where instanceof BlockHitResult off) {
            comeOffTheWall(off);
        }
    }

    private void comeOffTheWall(BlockHitResult off) {
        Direction face = off.getDirection();
        Vec3 going = getDeltaMovement();
        setDeltaMovement(new Vec3(
                face.getStepX() != 0 ? -going.x : going.x,
                face.getStepY() != 0 ? -going.y : going.y,
                face.getStepZ() != 0 ? -going.z : going.z).scale(KEEPS));
        setPos(position().add(getDeltaMovement().normalize().scale(0.05)));
        playSound(level().getBlockState(off.getBlockPos()).getSoundType().getBreakSound(), 0.3F,
                1.2F / (random.nextFloat() * 0.2F + 0.9F));
    }

    @Override
    protected void land(ServerLevel level, HitResult where) {
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ICE.defaultBlockState()),
                getX(), getY(), getZ(), 12, 0.2, 0.2, 0.2, 0.15);
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.3F,
                1.2F / (random.nextFloat() * 0.2F + 0.9F));
    }
}
