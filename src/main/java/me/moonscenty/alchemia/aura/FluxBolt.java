package me.moonscenty.alchemia.aura;

import me.moonscenty.alchemia.block.taint.FluxGooBlock;
import me.moonscenty.alchemia.player.effect.ModEffects;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

// The original's taint lightning: strikes one to three times, hurting and tainting everything under it
// each time, and leaving puddles of goo. Drawn as vanilla lightning; none of vanilla's fire or rod effects.
public class FluxBolt extends LightningBolt {
    private static final double REACH = 3.0;
    private static final float HARM = 3.0F;
    private static final int FLU = 1200;
    private static final int PUDDLES = 4;

    private int state = 2;
    private int strikesLeft;

    public FluxBolt(EntityType<? extends FluxBolt> type, Level level) {
        super(type, level);
        setVisualOnly(true);
        strikesLeft = random.nextInt(3) + 1;
    }

    public FluxBolt(Level level, Vec3 at) {
        this(ModEntities.FLUX_BOLT.get(), level);
        moveTo(at);
    }

    // the first strike; on normal or hard it splashes goo about
    public static boolean strike(ServerLevel level, BlockPos at, RandomSource random) {
        FluxBolt bolt = new FluxBolt(level, Vec3.atBottomCenterOf(at));
        Difficulty difficulty = level.getDifficulty();
        if ((difficulty == Difficulty.NORMAL || difficulty == Difficulty.HARD) && loaded(level, at)) {
            puddle(level, at);
            for (int i = 0; i < PUDDLES; i++) {
                puddle(level, at.offset(random.nextInt(5) - 2, random.nextInt(5) - 2, random.nextInt(5) - 2));
            }
        }
        return level.addFreshEntity(bolt);
    }

    private static boolean loaded(Level level, BlockPos at) {
        return level.hasChunksAt(at.offset(-10, -10, -10), at.offset(10, 10, 10));
    }

    private static void puddle(Level level, BlockPos at) {
        if (level.isEmptyBlock(at)) {
            level.setBlock(at, FluxGooBlock.of(ModBlocks.FLUX_GOO.get(), FluxGooBlock.FULL), Block.UPDATE_ALL);
        }
    }

    public int strikesLeft() {
        return strikesLeft;
    }

    @Override
    public void tick() {
        if (state == 2 && level().isClientSide) {
            level().playLocalSound(getX(), getY(), getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER,
                    10000.0F, 0.8F + random.nextFloat() * 0.2F, false);
            level().playLocalSound(getX(), getY(), getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.WEATHER,
                    2.0F, 0.5F + random.nextFloat() * 0.2F, false);
        }

        state--;
        if (state < 0) {
            if (strikesLeft == 0) {
                discard();
                return;
            }
            if (state < -random.nextInt(10)) {
                strikesLeft--;
                state = 1;
                seed = random.nextLong();
                if (!level().isClientSide && loaded(level(), blockPosition())) {
                    puddle(level(), blockPosition());
                }
            }
        }

        if (state >= 0 && level() instanceof ServerLevel server) {
            AABB under = new AABB(getX() - REACH, getY() - REACH, getZ() - REACH,
                    getX() + REACH, getY() + 6.0 + REACH, getZ() + REACH);
            for (Entity hit : server.getEntities(this, under, Entity::isAlive)) {
                hit.hurt(server.damageSources().lightningBolt(), HARM);
                if (hit instanceof LivingEntity living) {
                    living.addEffect(new MobEffectInstance(ModEffects.TAINT_POISON, FLU, 0, false, true));
                }
            }
        }
    }
}
