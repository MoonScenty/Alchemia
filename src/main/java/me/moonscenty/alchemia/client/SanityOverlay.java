package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.systems.RenderSystem;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.player.WarpData;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/**
 * While a sanity checker is in the main hand, the original's gauge of warp in the top left corner: a glass tube
 * filled from the bottom with lasting warp in deep purple, then the warp that sticks in a lighter one, then the
 * warp that passes in pink. Past a hundred the three are shrunk to share the tube and a warning sits over the top.
 */
@EventBusSubscriber(modid = Alchemia.MODID, value = Dist.CLIENT)
public final class SanityOverlay {
    private static final ResourceLocation HUD = Alchemia.id("textures/gui/hud.png");
    /** How tall the tube is, and how much warp fills it. */
    private static final int TUBE = 48;
    private static final float FULL = 100.0F;

    private SanityOverlay() {
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        Player player = client.player;
        if (player == null || client.options.hideGui || client.screen != null
                || !player.getMainHandItem().is(ModItems.SANITY_CHECKER.get())) {
            return;
        }
        draw(event.getGuiGraphics(), WarpData.of(player));
    }

    private static void draw(GuiGraphics graphics, WarpData warp) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(HUD, 1, 1, 152, 0, 20, 76);

        float total = warp.total();
        float shrink = 1.0F;
        if (total > FULL) {
            shrink = FULL / total;
            total = FULL;
        }
        // the empty part of the tube, at the top, then the three kinds in from there
        int gap = (int) ((FULL - total) / FULL * TUBE);
        int passing = (int) (warp.temporary() / FULL * TUBE * shrink);
        int sticking = (int) (warp.sticky() / FULL * TUBE * shrink);
        if (warp.temporary() > 0) {
            bar(graphics, gap, passing, 1.0F, 0.5F, 1.0F);
        }
        if (warp.sticky() > 0) {
            bar(graphics, gap + passing, sticking, 0.75F, 0.0F, 0.75F);
        }
        if (warp.permanent() > 0) {
            bar(graphics, gap + passing + sticking, TUBE - gap - passing - sticking, 0.5F, 0.0F, 0.5F);
        }

        graphics.blit(HUD, 1, 1, 176, 0, 20, 76);
        if (total >= FULL) {
            graphics.blit(HUD, 1, 1, 216, 0, 20, 16);
        }
        RenderSystem.disableBlend();
    }

    /** One stretch of the tube, from so far down it, in a colour laid over the sheet's white column. */
    private static void bar(GuiGraphics graphics, int from, int height, float red, float green, float blue) {
        if (height <= 0) {
            return;
        }
        graphics.setColor(red, green, blue, 1.0F);
        graphics.blit(HUD, 7, 21 + from, 200, from, 8, height);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
