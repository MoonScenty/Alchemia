package me.moonscenty.alchemia.block.entity;

import java.util.List;

import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.network.BlockArc;
import me.moonscenty.alchemia.player.WarpData;
import me.moonscenty.alchemia.player.WarpHandler;
import me.moonscenty.alchemia.player.effect.ModEffects;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public final class InfusionMishaps {
    private static final double NEAR = 10.0;
    private static final int TRIES = 50;

    private InfusionMishaps() {
    }

    public static void strike(ServerLevel level, BlockPos pos) {
        switch (level.random.nextInt(21)) {
            case 0, 2, 10, 13 -> eject(level, pos, Eject.DROP);
            case 1, 11 -> eject(level, pos, Eject.DROP_FLUX);
            case 3, 8, 14 -> zap(level, pos, false);
            case 4, 15 -> eject(level, pos, Eject.DROP_BANG);
            case 5, 16 -> harm(level, pos, false);
            case 6, 17 -> eject(level, pos, Eject.DROP_SPLASH);
            case 7 -> eject(level, pos, Eject.GONE_FLUX);
            case 9 -> level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    1.5F + level.random.nextFloat(), Level.ExplosionInteraction.NONE);
            case 12 -> zap(level, pos, true);
            case 18 -> harm(level, pos, true);
            case 19 -> eject(level, pos, Eject.GONE_SPLASH);
            default -> twist(level, pos);
        }
    }

    private enum Eject {
        DROP(false, false, false, false),
        DROP_SPLASH(false, true, false, false),
        DROP_FLUX(false, false, true, false),
        GONE_SPLASH(true, true, false, false),
        GONE_FLUX(true, false, true, false),
        DROP_BANG(false, false, false, true);

        final boolean gone;
        final boolean splash;
        final boolean flux;
        final boolean bang;

        Eject(boolean gone, boolean splash, boolean flux, boolean bang) {
            this.gone = gone;
            this.splash = splash;
            this.flux = flux;
            this.bang = bang;
        }
    }

    private static void eject(ServerLevel level, BlockPos pos, Eject how) {
        List<BlockPos> stands = InfusionMatrixBlockEntity.around(level, pos);
        for (int tries = 0; tries < TRIES && !stands.isEmpty(); tries++) {
            BlockPos at = stands.get(level.random.nextInt(stands.size()));
            if (!(level.getBlockEntity(at) instanceof ArcanePedestalBlockEntity stand) || stand.held().isEmpty()) {
                continue;
            }
            ItemStack thrown = stand.held();
            stand.hold(ItemStack.EMPTY);
            if (!how.gone) {
                Containers.dropItemStack(level, at.getX(), at.getY() + 1, at.getZ(), thrown);
            }
            if (how.splash) {
                level.playSound(null, at, SoundEvents.GENERIC_SWIM, SoundSource.BLOCKS, 0.3F, 1.0F);
            }
            if (how.flux) {
                AuraHandler.add(level, at, ModAspects.FLUX, 5 + level.random.nextInt(5));
            }
            if (how.bang) {
                level.explode(null, at.getX() + 0.5, at.getY() + 0.5, at.getZ() + 0.5, 1.0F,
                        Level.ExplosionInteraction.NONE);
            }
            level.playSound(null, at, ModSounds.POOF.get(), SoundSource.BLOCKS, 0.4F,
                    1.0F + (float) level.random.nextGaussian() * 0.05F);
            return;
        }
    }

    private static void zap(ServerLevel level, BlockPos pos, boolean all) {
        for (LivingEntity target : near(level, pos)) {
            BlockArc.send(level, pos, target, 0.3F - level.random.nextFloat() * 0.1F, 0.0F,
                    0.3F - level.random.nextFloat() * 0.1F);
            target.hurt(level.damageSources().source(DamageTypes.MAGIC), 4 + level.random.nextInt(4));
            if (!all) {
                return;
            }
        }
    }

    private static void harm(ServerLevel level, BlockPos pos, boolean all) {
        for (LivingEntity target : near(level, pos)) {
            if (level.random.nextBoolean()) {
                target.addEffect(new MobEffectInstance(ModEffects.TAINT_POISON, 120, 0, false, true));
            } else {
                target.addEffect(new MobEffectInstance(ModEffects.FLUX_FLU, 2400, 0, true, true));
            }
            if (!all) {
                return;
            }
        }
    }

    // original's odds
    private static void twist(ServerLevel level, BlockPos pos) {
        List<Player> watching = level.getEntitiesOfClass(Player.class, around(pos));
        if (watching.isEmpty()) {
            return;
        }
        Player player = watching.get(level.random.nextInt(watching.size()));
        if (level.random.nextFloat() < 0.25F) {
            WarpHandler.add(player, WarpData.Kind.STICKY, 1);
        } else {
            WarpHandler.add(player, WarpData.Kind.TEMPORARY, 2 + level.random.nextInt(4));
        }
    }

    private static List<LivingEntity> near(ServerLevel level, BlockPos pos) {
        return level.getEntitiesOfClass(LivingEntity.class, around(pos));
    }

    private static AABB around(BlockPos pos) {
        return new AABB(pos).inflate(NEAR);
    }
}
