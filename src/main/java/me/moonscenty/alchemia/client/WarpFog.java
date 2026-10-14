package me.moonscenty.alchemia.client;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

/**
 * The fog warp brings down, and a guardian keeps about itself.
 * <p>
 * The original thickened the game's fog to a density of a tenth over five seconds, held it, and thinned it again
 * over the last five. The game's fog is a distance now rather than a density, so the same thickening is drawn as the
 * fog closing in from the edge of sight to about a dozen blocks.
 */
@EventBusSubscriber(modid = Alchemia.MODID, value = Dist.CLIENT)
public final class WarpFog {
    private static final float THICKEST = 0.1F;
    private static final float THICKENS = 0.001F;
    /** How near the fog closes at its thickest, in blocks. */
    private static final float NEAREST = 12.0F;

    private static int left = -1;
    private static float thickness;

    private WarpFog() {
    }

    /** A fog for so long, or at least so long if it only tops up what is there. */
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
