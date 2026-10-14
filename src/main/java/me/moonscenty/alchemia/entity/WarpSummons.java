package me.moonscenty.alchemia.entity;

import me.moonscenty.alchemia.entity.cult.CultistPortal;
import me.moonscenty.alchemia.network.Mist;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * What warp calls up out of the dark, and where it puts it.
 * <p>
 * Everything warp calls is put the original's way: up to fifty tries, each seven to twenty-four blocks off the player
 * along each of the three ways or not at all, on solid ground with room to stand and no water -- so above or below as
 * readily as beside, in the cave under the floor or on the hill over the house.
 */
public final class WarpSummons {
    /** The most guardians one fog brings, and the most spiders one swarm. */
    private static final int MOST_GUARDIANS = 8;
    private static final int MOST_SPIDERS = 50;

    private WarpSummons() {
    }

    /** A fog comes down on the player, and so many guardians walk in it, already after them. */
    public static void mist(ServerPlayer player, int guardians) {
        PacketDistributor.sendToPlayer(player, Mist.WARP);
        for (int i = 0; i < Math.min(MOST_GUARDIANS, guardians); i++) {
            EldritchGuardian guardian = new EldritchGuardian(player.serverLevel());
            if (place(player.serverLevel(), player, guardian, 0.0, false)) {
                guardian.setTarget(player);
                player.serverLevel().addFreshEntity(guardian);
            }
        }
    }

    /**
     * Spiders all around, as many as the warp is deep up to fifty. Unless they are real, only the player sees them,
     * and they bite for nothing and are gone in a minute.
     */
    public static void spiders(ServerPlayer player, int warp, boolean real) {
        for (int i = 0; i < Math.min(MOST_SPIDERS, warp); i++) {
            MindSpider spider = new MindSpider(player.serverLevel());
            if (place(player.serverLevel(), player, spider, 0.0, false)) {
                spider.setTarget(player);
                if (!real) {
                    spider.setViewer(player.getGameProfile().getName());
                    spider.setHarmless(true);
                }
                player.serverLevel().addFreshEntity(spider);
            }
        }
    }

    /** A lesser crimson portal somewhere near. */
    public static boolean portal(ServerPlayer player) {
        CultistPortal portal = new CultistPortal(player.serverLevel());
        if (!place(player.serverLevel(), player, portal, 1.0, true)) {
            return false;
        }
        portal.finalizeSpawn(player.serverLevel(), player.serverLevel().getCurrentDifficultyAt(portal.blockPosition()),
                MobSpawnType.EVENT, null);
        return player.serverLevel().addFreshEntity(portal);
    }

    /** Finds the thing somewhere to stand near the player; leaves it there and says so, or says it found nowhere. */
    static boolean place(ServerLevel level, Player player, Mob mob, double up, boolean centred) {
        BlockPos at = player.blockPosition();
        for (int tries = 0; tries < 50; tries++) {
            BlockPos spot = at.offset(off(level), off(level), off(level));
            BlockPos under = spot.below();
            if (!level.getBlockState(under).isFaceSturdy(level, under, Direction.UP)) {
                continue;
            }
            double half = centred ? 0.5 : 0.0;
            mob.moveTo(spot.getX() + half, spot.getY() + up, spot.getZ() + half, level.random.nextFloat() * 360.0F,
                    0.0F);
            if (level.noCollision(mob) && level.isUnobstructed(mob) && !level.containsAnyLiquid(mob.getBoundingBox())) {
                return true;
            }
        }
        return false;
    }

    private static int off(ServerLevel level) {
        return (7 + level.random.nextInt(18)) * (level.random.nextInt(3) - 1);
    }
}
