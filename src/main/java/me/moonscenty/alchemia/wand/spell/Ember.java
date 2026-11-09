package me.moonscenty.alchemia.wand.spell;

import me.moonscenty.alchemia.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class Ember extends Bolt {
    // damage, and seconds of burning
    private static final float HURTS = 2.0F;
    private static final int BURNS_FOR = 3;
    private static final int GOES_OUT = 20;
    // chance per rank of alchemist's fire to set the ground alight
    private static final float CATCHES = 0.025F;
    // original: share of speed kept each tick, a beam's, and off the ground
    private static final double SLOWS = 0.95;
    private static final double SLOWS_BEAM = 0.975;
    private static final double SCRAPES = 0.66;
    private static final EntityDataAccessor<Boolean> BEAM =
            SynchedEntityData.defineId(Ember.class, EntityDataSerializers.BOOLEAN);

    private float hurts = HURTS;
    private int burns = BURNS_FOR;
    private int goesOut = GOES_OUT;
    private int alchemists;

    public Ember(EntityType<? extends Ember> type, Level level) {
        super(type, level);
    }

    public Ember(Level level, LivingEntity thrower) {
        super(ModEntities.EMBER.get(), level, thrower);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    @Override
    protected ParticleOptions trail() {
        return ParticleTypes.FLAME;
    }

    @Override
    protected int lives() {
        return goesOut;
    }

    public Ember burning(float hurts, int alchemists) {
        this.hurts = hurts;
        this.burns = BURNS_FOR + alchemists;
        this.alchemists = alchemists;
        return this;
    }

    public Ember lasting(int ticks) {
        this.goesOut = ticks;
        entityData.set(BEAM, ticks > GOES_OUT);
        return this;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(BEAM, false);
    }

    @Override
    public void tick() {
        Vec3 going = getDeltaMovement().scale(entityData.get(BEAM) ? SLOWS_BEAM : SLOWS);
        setDeltaMovement(onGround() ? going.scale(SCRAPES) : going);
        super.tick();
    }

    @Override
    protected void land(ServerLevel level, HitResult where) {
        level.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY(), getZ(), 3, 0.1, 0.1, 0.1, 0.01);
        if (where instanceof EntityHitResult struck) {
            Entity hit = struck.getEntity();
            if (!hit.fireImmune() && hit.hurt(fireball(), hurts)) {
                hit.igniteForSeconds(burns);
            }
        } else if (where instanceof BlockHitResult wall && random.nextFloat() < CATCHES * alchemists
                && (!(getOwner() instanceof Mob) || level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING))) {
            BlockPos at = wall.getBlockPos().relative(wall.getDirection());
            if (level.isEmptyBlock(at)) {
                level.setBlockAndUpdate(at, BaseFireBlock.getState(level, at));
            }
        }
    }
}
