package me.moonscenty.alchemia.wand.spell;

import me.moonscenty.alchemia.registry.ModEntities;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class Ember extends Bolt {
    // damage, and seconds of burning
    private static final float HURTS = 2.0F;
    private static final int BURNS_FOR = 3;
    private static final int GOES_OUT = 20;
    // chance per rank of alchemist's fire to set the ground alight
    private static final float CATCHES = 0.025F;

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
        return this;
    }

    @Override
    protected void land(ServerLevel level, HitResult where) {
        level.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY(), getZ(), 3, 0.1, 0.1, 0.1, 0.01);
        if (!(where instanceof EntityHitResult struck)) {
            return;
        }
        Entity hit = struck.getEntity();
        if (hit.hurt(damageSources().indirectMagic(this, getOwner()), hurts)) {
            hit.igniteForSeconds(burns);
        }
        if (random.nextFloat() < CATCHES * alchemists) {
            catchGround(level);
        }
    }

    private void catchGround(ServerLevel level) {
        net.minecraft.core.BlockPos at = blockPosition();
        if (level.isEmptyBlock(at)
                && net.minecraft.world.level.block.BaseFireBlock.canBePlacedAt(level, at,
                        getDirection())) {
            level.setBlock(at, net.minecraft.world.level.block.BaseFireBlock.getState(level, at), 3);
        }
    }
}
