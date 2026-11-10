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
import java.util.Map;
import java.util.TreeMap;

import me.moonscenty.alchemia.item.Rechargeable;
import net.minecraft.Util;
import net.neoforged.neoforge.client.event.ClientTickEvent;

// original's charge meters: carried things that hold a charge slide in at the left when it changes
@EventBusSubscriber(modid = Alchemia.MODID, value = Dist.CLIENT)
public final class ChargeMeters {
    private static final ResourceLocation HUD = Alchemia.id("textures/gui/hud.png");
    private static final long SHOWN_FOR = 10000L;
    private static final long SLIDES_FOR = 500L;
    private static final Map<Integer, Charge> CHARGED = new TreeMap<>();

    private static final class Charge {
        long time;
        ItemStack item;
        float level;
        int diff;
        long changedAt;

        Charge(long time, ItemStack item, float level) {
            this.time = time;
            this.item = item;
            this.level = level;
        }
    }

    private ChargeMeters() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Player player = Minecraft.getInstance().player;
        if (player != null && player.tickCount % 20 == 0) {
            look(player);
        }
    }

    // hotbar, then the first four trinkets, then armour, each in its own place in the list
    private static void look(Player player) {
        long time = Util.getMillis();
        int place = 0;
        for (int slot = 0; slot < 9; slot++) {
            note(player.getInventory().items.get(slot), place++, time);
        }
        var trinkets = top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player)
                .map(curios -> curios.getEquippedCurios()).orElse(null);
        for (int slot = 0; slot < 4; slot++) {
            note(trinkets != null && slot < trinkets.getSlots() ? trinkets.getStackInSlot(slot) : ItemStack.EMPTY,
                    place++, time);
        }
        for (ItemStack worn : player.getInventory().armor) {
            note(worn, place++, time);
        }
    }

    private static void note(ItemStack stack, int place, long time) {
        if (!(stack.getItem() instanceof Rechargeable charged)) {
            CHARGED.remove(place);
            return;
        }
        float level = charged.chargeLevel(stack);
        if (level < 0.0F) {
            CHARGED.remove(place);
            return;
        }
        Charge known = CHARGED.get(place);
        if (known == null) {
            CHARGED.put(place, new Charge(time, stack.copy(), level));
        } else if (known.item.getItem() == stack.getItem() && known.level != level) {
            known.diff = level > known.level ? 1 : -1;
            known.changedAt = time;
            known.level = level;
            known.time = known.time < time - SHOWN_FOR + SLIDES_FOR ? time : time - SLIDES_FOR;
        } else {
            known.diff = 0;
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        Player player = client.player;
        if (player == null || CHARGED.isEmpty() || client.options.hideGui || client.screen != null) {
            return;
        }
        GuiGraphics graphics = event.getGuiGraphics();
        long time = Util.getMillis();
        int tall = graphics.guiHeight();
        int row = 0;
        RenderSystem.enableBlend();
        for (Charge charge : CHARGED.values()) {
            float level = charge.level * 16.0F;
            float shift = 1.0F;
            if (level > 0.0F) {
                if (charge.time < time - SHOWN_FOR) {
                    continue;
                }
                if (charge.time > time - SLIDES_FOR) {
                    shift = (float) (time - charge.time) / SLIDES_FOR;
                }
                if (charge.time < time - SHOWN_FOR + SLIDES_FOR) {
                    shift = 1.0F - (float) (time - charge.time - SHOWN_FOR + SLIDES_FOR) / SLIDES_FOR;
                }
            }
            int x = (int) (-20.0F + 22.0F * shift);
            int y = tall / 2 + 17 + row * 20;
            RenderSystem.defaultBlendFunc();
            if (level == 0.0F && player.tickCount / 10 % 2 == 0) {
                graphics.setColor(1.0F, 0.0F, 0.0F, 1.0F);
            }
            graphics.blit(HUD, x, y, 144, 99, 16, 3);
            graphics.blit(HUD, x, y, 144, 96, Math.round(level), 3);
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            float faded = (time - charge.changedAt) / 1000.0F;
            if (charge.diff != 0 && faded < 1.0F) {
                RenderSystem.blendFunc(com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA,
                        com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE);
                int moved = Math.round(faded * level);
                graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F - faded);
                graphics.blit(HUD, charge.diff > 0 ? x + moved - 3 : x - moved - 5 + Math.round(level), y - 2, 160, 94, 5, 7);
                graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.defaultBlendFunc();
            }
            graphics.renderItem(charge.item, x, y - 16);
            if (tall / 2 + 50 + ++row * 21 > tall) {
                break;
            }
        }
        RenderSystem.disableBlend();
    }
}
