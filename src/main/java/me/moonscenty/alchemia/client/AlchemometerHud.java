package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

// original's thaumometer gauge: the chunk's aura as a stacked bar, or one aspect against its base
@EventBusSubscriber(modid = Alchemia.MODID, value = Dist.CLIENT)
public final class AlchemometerHud {
    private static final ResourceLocation HUD = Alchemia.id("textures/gui/hud.png");
    private static me.moonscenty.alchemia.network.AuraReading reading;

    private AlchemometerHud() {
    }

    public static void read(me.moonscenty.alchemia.network.AuraReading payload) {
        reading = payload;
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        Player player = client.player;
        if (player == null || reading == null || client.options.hideGui || client.screen != null
                || !(player.getMainHandItem().getItem() instanceof me.moonscenty.alchemia.item.AlchemometerItem)) {
            return;
        }
        draw(event.getGuiGraphics(), reading);
    }

    private static void draw(GuiGraphics graphics, me.moonscenty.alchemia.network.AuraReading seen) {
        var aspects = me.moonscenty.alchemia.item.AlchemometerItem.auraAspects();
        boolean single = seen.shown() > 0;
        float whole = 0.0F;
        if (single) {
            whole = (int) (seen.base() * 1.1F);
        } else {
            for (int amount : seen.amounts()) {
                whole += amount;
            }
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        float gap = 0.0F;
        int over = 0;
        for (int index = 0; index < aspects.size() && whole > 0; index++) {
            if (single && index != seen.shown() - 1) {
                continue;
            }
            float share = seen.amounts().get(index) / whole;
            if (single) {
                gap = Math.max(0.0F, 1.0F - share) * 64.0F;
            }
            if (share > 0.0F) {
                if (share > 1.0F) {
                    over = (int) ((share - 1.0F) * 64.0F);
                    share = 1.0F;
                }
                int colour = aspects.get(index).value().color();
                graphics.setColor(((colour >> 16) & 0xFF) / 255F, ((colour >> 8) & 0xFF) / 255F, (colour & 0xFF) / 255F,
                        0.9F);
                graphics.pose().pushPose();
                graphics.pose().translate(5.0F, 9.0F + gap, 0.0F);
                graphics.pose().scale(1.0F, share, 1.0F);
                graphics.blit(HUD, 0, 0, 88, 56, 8, 64);
                graphics.pose().popPose();
                graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                gap += share * 64.0F;
            }
        }
        graphics.blit(HUD, 1, 1, 72, 48, 16, 80);
        if (single) {
            graphics.blit(HUD, 2, 13 + over, 109, 61, 14, 5);
        }
        RenderSystem.disableBlend();
    }
}
