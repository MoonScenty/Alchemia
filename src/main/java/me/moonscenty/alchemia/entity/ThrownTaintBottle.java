package me.moonscenty.alchemia.entity;

import me.moonscenty.alchemia.aura.BiomePaint;
import me.moonscenty.alchemia.block.taint.FluxGooBlock;
import me.moonscenty.alchemia.player.effect.ModEffects;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModEntities;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.worldgen.ModBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;

/**
 * A thrown bottle of taint, the original's {@code EntityBottleTaint}. Where it breaks, everything living within five
 * blocks that is not tainted or undead is poisoned by the taint for five seconds, and the ground about it goes over
 * to tainted land, with flux goo left lying here and there.
 */
public class ThrownTaintBottle extends ThrowableItemProjectile {
    /** How far the poison reaches, and how many spots about the break it tries to taint. */
    private static final double REACH = 5.0;
    private static final int SPOTS = 10;
    private static final int POISONS_FOR = 100;
    /** Told to the client when it breaks, as a thrown potion is. */
    private static final byte BROKE = 3;

    public ThrownTaintBottle(EntityType<? extends ThrownTaintBottle> type, Level level) {
        super(type, level);
    }

    public ThrownTaintBottle(Level level, LivingEntity thrower) {
        super(ModEntities.TAINT_BOTTLE.get(), thrower, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.BOTTLE_TAINT.get();
    }

    @Override
    protected double getDefaultGravity() {
        return 0.05;
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (!(level() instanceof ServerLevel server)) {
            return;
        }
        for (LivingEntity near : server.getEntitiesOfClass(LivingEntity.class,
                new AABB(position(), position()).inflate(REACH))) {
            if (!(near instanceof TaintedMob) && !near.isInvertedHealAndHarm()) {
                near.addEffect(new MobEffectInstance(ModEffects.TAINT_POISON, POISONS_FOR, 0, false, true), this);
            }
        }
        spread(server);
        server.broadcastEntityEvent(this, BROKE);
        discard();
    }

    /** Ten tries, each a coin toss, at a spot within four: tainted land, and goo where it will lie. */
    private void spread(ServerLevel level) {
        BlockPos here = blockPosition();
        for (int tries = 0; tries < SPOTS; tries++) {
            int x = (int) ((random.nextFloat() - random.nextFloat()) * 4.0F);
            int z = (int) ((random.nextFloat() - random.nextFloat()) * 4.0F);
            BlockPos at = here.offset(x, 0, z);
            if (!level.random.nextBoolean()
                    || BiomePaint.at(level, at).is(ModBiomes.TAINTED_LAND)) {
                continue;
            }
            BiomePaint.named(level, ModBiomes.TAINTED_LAND).ifPresent(tainted -> BiomePaint.paint(level, at, tainted));
            if (!goo(level, at)) {
                goo(level, at.below());
            }
        }
    }

    /** Lays goo at a spot if the ground under it is solid and nothing much is there. */
    private static boolean goo(ServerLevel level, BlockPos at) {
        BlockPos under = at.below();
        if (level.getBlockState(under).isFaceSturdy(level, under, Direction.UP)
                && level.getBlockState(at).canBeReplaced()) {
            level.setBlock(at, ModBlocks.FLUX_GOO.get().defaultBlockState(), Block.UPDATE_ALL);
            return true;
        }
        return false;
    }

    /** The bottle bursting: shards of it, a spray of taint, and the smash. */
    @Override
    public void handleEntityEvent(byte id) {
        if (id != BROKE) {
            super.handleEntityEvent(id);
            return;
        }
        for (int i = 0; i < 8; i++) {
            level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, getItem()), getX(), getY(), getZ(),
                    random.nextGaussian() * 0.15, random.nextDouble() * 0.2, random.nextGaussian() * 0.15);
        }
        ItemStack goo = new ItemStack(ModItems.TAINTED_GOO.get());
        for (int i = 0; i < 40; i++) {
            level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, goo), getX(),
                    getY() + random.nextFloat() * getBbHeight(), getZ(), (random.nextFloat() * 2.0 - 1.0) * 0.3,
                    (random.nextFloat() * 2.0 - 1.0) * 0.3 + 0.1, (random.nextFloat() * 2.0 - 1.0) * 0.3);
        }
        level().playLocalSound(getX(), getY(), getZ(), SoundEvents.SPLASH_POTION_BREAK, getSoundSource(), 1.0F,
                random.nextFloat() * 0.1F + 0.9F, false);
    }
}
