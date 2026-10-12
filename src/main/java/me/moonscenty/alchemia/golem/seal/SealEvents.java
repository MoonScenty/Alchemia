package me.moonscenty.alchemia.golem.seal;

import java.util.Map;
import java.util.WeakHashMap;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.golem.task.Tasks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * Keeps seals and tasks going: every tick each world's seals work, once a second its tasks age, and a player coming
 * into a world is told of its seals.
 */
@EventBusSubscriber(modid = Alchemia.MODID)
public final class SealEvents {
    private static final Map<ServerLevel, Integer> TICKS = new WeakHashMap<>();

    private SealEvents() {
    }

    @SubscribeEvent
    public static void tick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        int ticks = TICKS.merge(level, 1, Integer::sum);
        if (ticks % 20 == 0) {
            Tasks.clearSuspendedOrExpired(level);
        }
        Seals.tick(level);
    }

    @SubscribeEvent
    public static void loggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Seals.tell(player);
        }
    }

    @SubscribeEvent
    public static void changedWorld(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Seals.tell(player);
        }
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) {
        Tasks.clear();
        Provisioning.clear();
        TICKS.clear();
    }
}
