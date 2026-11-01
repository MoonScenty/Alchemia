package me.moonscenty.alchemia.aspect;

import me.moonscenty.alchemia.Alchemia;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RecipesUpdatedEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

public class AspectReloadHandler {
    @EventBusSubscriber(modid = Alchemia.MODID)
    public static class Server {
        @SubscribeEvent
        public static void onServerStarted(ServerStartedEvent event) {
            Aspects.bind(event.getServer().getRecipeManager(), event.getServer().registryAccess());
        }

        @SubscribeEvent
        public static void onDatapackSync(OnDatapackSyncEvent event) {
            Aspects.bind(event.getPlayerList().getServer().getRecipeManager(),
                    event.getPlayerList().getServer().registryAccess());
        }
    }

    @EventBusSubscriber(modid = Alchemia.MODID, value = Dist.CLIENT)
    public static class Client {
        @SubscribeEvent
        public static void onRecipesUpdated(RecipesUpdatedEvent event) {
            net.minecraft.client.multiplayer.ClientLevel level = net.minecraft.client.Minecraft.getInstance().level;
            if (level != null) {
                Aspects.bind(event.getRecipeManager(), level.registryAccess());
            }
        }
    }
}
