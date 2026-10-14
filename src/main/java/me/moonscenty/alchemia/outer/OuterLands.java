package me.moonscenty.alchemia.outer;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.player.WarpHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/**
 * The Outer Lands: a dimension of solid ancient bedrock with mazes carved in it, under a fog of dark purple, no sky,
 * no weather, no night. One comes in through the portal over an opened eldritch altar and goes out through the
 * portal at the heart of the maze.
 * <p>
 * Going in, one arrives beside the portal of the nearest maze -- a step to one side of it on each axis, as the
 * original put one. Going out, one arrives beside the portal that maze was opened from, or at the world's spawn
 * when that is not known. Anyone standing in a portal waits five seconds before it takes them again.
 */
@EventBusSubscriber(modid = Alchemia.MODID)
public final class OuterLands {
    public static final ResourceKey<Level> LEVEL = ResourceKey.create(Registries.DIMENSION, Alchemia.id("outer_lands"));
    public static final ResourceKey<LevelStem> STEM = ResourceKey.create(Registries.LEVEL_STEM,
            Alchemia.id("outer_lands"));
    public static final ResourceKey<DimensionType> TYPE = ResourceKey.create(Registries.DIMENSION_TYPE,
            Alchemia.id("outer_lands"));
    /** What a player learns on first coming in. */
    public static final ResourceLocation ENTERED = Alchemia.id("enter_outer");
    /** How long before a portal will take the same player again. */
    public static final int COOLDOWN = 100;

    private OuterLands() {
    }

    public static boolean isOuterLands(Level level) {
        return level.dimension() == LEVEL;
    }

    @SubscribeEvent
    public static void loaded(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level && level.dimension() == Level.OVERWORLD) {
            Labyrinth.of(level.getServer());
        }
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) {
        Labyrinth.forget();
    }

    /** Nobody but the creative builds in a great hall while what it holds is alive within thirty-two blocks. */
    @SubscribeEvent
    public static void placing(net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !isOuterLands(level)
                || !(event.getEntity() instanceof net.minecraft.world.entity.player.Player player)
                || player.isCreative()) {
            return;
        }
        Labyrinth labyrinth = Labyrinth.current();
        BlockPos at = event.getPos();
        Cell cell = labyrinth == null ? null : labyrinth.get(at.getX() >> 4, at.getZ() >> 4);
        if (cell != null && cell.isBossRoom() && !level.getEntitiesOfClass(
                me.moonscenty.alchemia.entity.boss.ThaumcraftBoss.class,
                new net.minecraft.world.phys.AABB(at).inflate(32.0)).isEmpty()) {
            event.setCanceled(true);
        }
    }

    /** Sends a player standing in the portal here through it, whichever way it goes. */
    public static void travel(ServerPlayer player, BlockPos portal) {
        if (player.isPassenger() || player.isVehicle()) {
            return;
        }
        if (player.isOnPortalCooldown()) {
            player.setPortalCooldown(COOLDOWN);
            return;
        }
        player.setPortalCooldown(COOLDOWN);
        Labyrinth labyrinth = Labyrinth.of(player.server);
        ServerLevel here = player.serverLevel();
        if (!isOuterLands(here)) {
            ServerLevel outer = player.server.getLevel(LEVEL);
            if (outer == null) {
                return;
            }
            ChunkPos cell = labyrinth.nearestPortal(portal.getX() >> 4, portal.getZ() >> 4);
            if (cell == null) {
                labyrinth.ensureMaze(portal.below(), here.random);
                cell = labyrinth.nearestPortal(portal.getX() >> 4, portal.getZ() >> 4);
            }
            if (cell == null) {
                return;
            }
            BlockPos beside = new BlockPos(cell.getMinBlockX() + 8, MazeBuilder.Y + 3, cell.getMinBlockZ() + 8);
            send(player, outer, beside);
            WarpHandler.learn(player, ENTERED);
        } else {
            ServerLevel overworld = player.server.overworld();
            BlockPos back = labyrinth.linkOf(new ChunkPos(portal));
            if (back == null) {
                ChunkPos nearest = labyrinth.nearestPortal(portal.getX() >> 4, portal.getZ() >> 4);
                back = nearest == null ? null : labyrinth.linkOf(nearest);
            }
            if (back == null) {
                BlockPos spawn = overworld.getSharedSpawnPos();
                back = overworld.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawn);
                player.changeDimension(new DimensionTransition(overworld, Vec3.atBottomCenterOf(back), Vec3.ZERO,
                        player.getYRot(), player.getXRot(), DimensionTransition.DO_NOTHING));
                return;
            }
            send(player, overworld, back);
        }
    }

    /** To a step beside this portal on each axis, at its own height. */
    private static void send(ServerPlayer player, ServerLevel to, BlockPos portal) {
        double x = portal.getX() + 0.5 + (to.random.nextBoolean() ? 1 : -1);
        double z = portal.getZ() + 0.5 + (to.random.nextBoolean() ? 1 : -1);
        player.changeDimension(new DimensionTransition(to, new Vec3(x, portal.getY(), z), Vec3.ZERO,
                player.getYRot(), player.getXRot(), DimensionTransition.DO_NOTHING));
    }
}
