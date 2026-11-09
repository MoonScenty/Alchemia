package me.moonscenty.alchemia.wand.spell;

import me.moonscenty.alchemia.registry.ModEntities;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
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
    private static final double FALLS = 0.05;
    private static final double DRIFTS = 0.015;
    private static final int MELTS_AFTER = 100;
    private static final int CHILLS_FOR = 200;
    // original: share of speed kept off anything, off a body, and how many bounces
    private static final double KEEPS = 0.5;
    private static final double KEEPS_BOULDER = 0.8;
    private static final double KEEPS_OFF_FLESH = 0.15;
    private static final int BOUNCES = 3;
    private static final int BOUNCES_BOULDER = 6;
    private static final double LIFTS = -0.9;

    private float hurts = HURTS;
    private int chills;
    private boolean splinter;
    private int bounces = BOUNCES;
    private double keeps = KEEPS;

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

    public FrostShard asBoulder() {
        this.bounces = BOUNCES_BOULDER;
        this.keeps = KEEPS_BOULDER;
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
        Vec3 going = getDeltaMovement();
        if (where instanceof EntityHitResult struck) {
            going = offBody(struck.getEntity(), going);
        } else if (where instanceof BlockHitResult wall) {
            going = offWall(wall, going);
        }
        going = going.scale(keeps);
        setDeltaMovement(going);
        setPos(position().subtract(going.normalize().scale(0.05)));
        hasImpulse = true;
        if (!(level() instanceof ServerLevel served)) {
            return;
        }

        if (where instanceof EntityHitResult struck) {
            strike(served, struck.getEntity(), where);
        }
        if (!isRemoved() && bounces-- <= 0) {
            land(served, where);
            discard();
        }
    }

    private Vec3 offBody(Entity body, Vec3 going) {
        return new Vec3(
                Mth.floor(getX()) != Mth.floor(body.getX()) ? -going.x : going.x,
                Mth.floor(getY()) != Mth.floor(body.getY()) ? going.y * LIFTS : going.y,
                Mth.floor(getZ()) != Mth.floor(body.getZ()) ? -going.z : going.z).scale(KEEPS_OFF_FLESH);
    }

    private Vec3 offWall(BlockHitResult wall, Vec3 going) {
        Direction face = wall.getDirection();
        playSound(level().getBlockState(wall.getBlockPos()).getSoundType().getBreakSound(), 0.3F,
                1.2F / (random.nextFloat() * 0.2F + 0.9F));
        return new Vec3(
                face.getStepX() != 0 ? -going.x : going.x,
                face.getStepY() != 0 ? going.y * LIFTS : going.y,
                face.getStepZ() != 0 ? -going.z : going.z);
    }

    private void strike(ServerLevel level, Entity body, HitResult where) {
        Vec3 before = body.getDeltaMovement();
        // original: splinters get through the moment of grace, more often the longer they fly
        if (splinter && body.invulnerableTime > 0 && random.nextInt(10) < tickCount + 3) {
            body.invulnerableTime = 0;
        }
        body.hurt(damageSources().thrown(this, getOwner()), hurts);
        if (chills > 0 && body instanceof LivingEntity chilled) {
            chilled.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, CHILLS_FOR, chills - 1));
        }
        if (splinter) {
            body.setDeltaMovement(before.add(body.getDeltaMovement().subtract(before).scale(0.1)));
            land(level, where);
            discard();
        }
    }

    @Override
    protected void land(ServerLevel level, HitResult where) {
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ICE.defaultBlockState()),
                getX(), getY(), getZ(), 12, 0.2, 0.2, 0.2, 0.15);
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.3F,
                1.2F / (random.nextFloat() * 0.2F + 0.9F));
    }
}
