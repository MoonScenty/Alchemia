package me.moonscenty.alchemia.client;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(modid = Alchemia.MODID, value = Dist.CLIENT)
public final class WarpFog {
    private static final float THICKEST = 0.1F;
    private static final float THICKENS = 0.001F;
    private static final float NEAREST = 12.0F;

    private static int left = -1;
    private static float thickness;

    private WarpFog() {
    }

    public static void bring(int ticks, boolean topUp) {
        left = topUp ? Math.max(left, ticks) : ticks;
    }

    @SubscribeEvent
    public static void ticked(ClientTickEvent.Pre event) {
        if (left < 0 || Minecraft.getInstance().isPaused()) {
            return;
        }
        thickness = left < 100 ? THICKEST * left / 100.0F : Math.min(THICKEST, thickness + THICKENS);
        if (--left < 0) {
            thickness = 0.0F;
        }
    }

    @SubscribeEvent
    public static void fog(ViewportEvent.RenderFog event) {
        if (thickness <= 0.0F) {
            return;
        }
        float share = thickness / THICKEST;
        float far = event.getFarPlaneDistance();
        float end = far + (NEAREST - far) * share;
        event.setNearPlaneDistance(Math.min(event.getNearPlaneDistance(), end * 0.1F));
        event.setFarPlaneDistance(Math.min(far, end));
        event.setCanceled(true);
    }
}
