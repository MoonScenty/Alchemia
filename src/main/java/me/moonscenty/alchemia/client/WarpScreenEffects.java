package me.moonscenty.alchemia.client;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.player.effect.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = Alchemia.MODID, value = Dist.CLIENT)
public final class WarpScreenEffects {
    private static final ResourceLocation VIGNETTE = Alchemia.id("textures/misc/vignette.png");

    private record Filter(Holder<MobEffect> effect, ResourceLocation shader) {
    }

    private static final List<Filter> FILTERS = List.of(
            new Filter(ModEffects.DEADLY_GAZE, Alchemia.id("shaders/post/deadly_gaze.json")),
            new Filter(ModEffects.BLURRED_VISION, Alchemia.id("shaders/post/blurred_vision.json")),
            new Filter(ModEffects.UNNATURAL_HUNGER, Alchemia.id("shaders/post/unnatural_hunger.json")),
            new Filter(ModEffects.SUN_SCORNED, Alchemia.id("shaders/post/sun_scorned.json")));
    private static final Map<Filter, PostChain> ACTIVE = new LinkedHashMap<>();

    private static int vignette;
    private static float darkness;
    private static int width;
    private static int height;

    private WarpScreenEffects() {
    }

    public static void darken(int ticks) {
        vignette = Math.max(vignette, ticks);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        Player player = client.player;
        if (player == null) {
            clear();
            return;
        }
        if (player.hasEffect(ModEffects.DEADLY_GAZE)) {
            vignette = Math.max(vignette, 10);
        } else if (vignette > 0) {
            vignette--;
        }
        for (Filter filter : FILTERS) {
            boolean wanted = player.hasEffect(filter.effect());
            if (wanted && !ACTIVE.containsKey(filter)) {
                load(client, filter);
            } else if (!wanted && ACTIVE.containsKey(filter)) {
                ACTIVE.remove(filter).close();
            }
        }
    }

    private static void load(Minecraft client, Filter filter) {
        try {
            PostChain chain = new PostChain(client.getTextureManager(), client.getResourceManager(),
                    client.getMainRenderTarget(), filter.shader());
            chain.resize(client.getWindow().getWidth(), client.getWindow().getHeight());
            ACTIVE.put(filter, chain);
        } catch (Exception failed) {
            Alchemia.LOGGER.warn("Could not load the {} screen effect", filter.shader(), failed);
        }
    }

    private static void clear() {
        ACTIVE.values().forEach(PostChain::close);
        ACTIVE.clear();
        vignette = 0;
        darkness = 0.0F;
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Pre event) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }
        if (!ACTIVE.isEmpty()) {
            int wide = client.getWindow().getWidth();
            int tall = client.getWindow().getHeight();
            boolean resized = wide != width || tall != height;
            width = wide;
            height = tall;
            float partial = event.getPartialTick().getGameTimeDeltaPartialTick(false);
            for (PostChain chain : ACTIVE.values()) {
                if (resized) {
                    chain.resize(wide, tall);
                }
                chain.process(partial);
            }
            client.getMainRenderTarget().bindWrite(true);
        }
        drawVignette(event.getGuiGraphics(), client.player);
    }

    private static void drawVignette(GuiGraphics graphics, Player player) {
        float wanted = vignette > 0 ? 1.0F : 0.0F;
        darkness += (wanted - darkness) * 0.01F;
        if (darkness <= 0.001F) {
            return;
        }
        float shade = darkness * (1.0F + Mth.sin(player.tickCount / 2.0F) * 0.1F);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ZERO,
                GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        graphics.setColor(shade, shade, shade, 1.0F);
        int wide = graphics.guiWidth();
        int tall = graphics.guiHeight();
        graphics.blit(VIGNETTE, 0, 0, -90, 0.0F, 0.0F, wide, tall, wide, tall);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }
}
