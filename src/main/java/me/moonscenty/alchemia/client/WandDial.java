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
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.item.FocusItem;
import me.moonscenty.alchemia.item.VisHolder;
import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.wand.Focus;
import net.minecraft.Util;
import net.minecraft.core.Holder;

// original's wand dial: a gauge for each primal, the focus in the middle, its cooldown, and on sneaking the numbers
@EventBusSubscriber(modid = Alchemia.MODID, value = Dist.CLIENT)
public final class WandDial {
    private static final ResourceLocation HUD = Alchemia.id("textures/gui/hud.png");
    private static final DecimalFormat TENTHS = new DecimalFormat("#######.#");
    private static final Map<Integer, AspectList> BEFORE = new HashMap<>();
    private static long nextLook;
    private static ItemStack counted = ItemStack.EMPTY;
    private static int count;
    private static int countedAt;

    private WandDial() {
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        Player player = client.player;
        if (player == null || client.options.hideGui || client.screen != null
                || !(player.getMainHandItem().getItem() instanceof WandItem)) {
            return;
        }
        draw(event.getGuiGraphics(), client, player, player.getMainHandItem(),
                event.getPartialTick().getGameTimeDeltaPartialTick(false));
    }

    private static AspectList vis(WandItem wand, ItemStack stack) {
        AspectList all = AspectList.EMPTY;
        for (Holder<Aspect> primal : ModAspects.primals()) {
            all = all.withAmount(primal, wand.held(stack, primal));
        }
        return all;
    }

    private static void draw(GuiGraphics graphics, Minecraft client, Player player, ItemStack stack, float partial) {
        WandItem wand = (WandItem) stack.getItem();
        int slot = player.getInventory().selected;
        AspectList now = vis(wand, stack);
        long time = Util.getMillis();
        if (!BEFORE.containsKey(slot) || nextLook <= time) {
            BEFORE.put(slot, now);
            nextLook = time + 1000L;
        }
        AspectList before = BEFORE.get(slot);
        ItemStack fitted = WandItem.focus(stack);
        Focus focus = fitted.getItem() instanceof FocusItem item ? item.focus(fitted) : null;
        int most = Math.max(1, WandItem.capacity(stack));

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        var pose = graphics.pose();
        pose.pushPose();
        pose.pushPose();
        pose.scale(0.5F, 0.5F, 1.0F);
        graphics.blit(HUD, 0, 0, 0, 0, 64, 64);
        pose.popPose();
        pose.translate(16.0F, 16.0F, 0.0F);
        int index = 0;
        for (Holder<Aspect> primal : ModAspects.primals()) {
            int amount = now.get(primal);
            pose.pushPose();
            pose.mulPose(Axis.ZP.rotationDegrees(90.0F - 15.0F + index * 24.0F));
            pose.translate(0.0F, -32.0F, 0.0F);
            pose.scale(0.5F, 0.5F, 1.0F);
            int filled = (int) (30.0F * amount / most);
            int colour = primal.value().color();
            graphics.setColor(((colour >> 16) & 0xFF) / 255F, ((colour >> 8) & 0xFF) / 255F, (colour & 0xFF) / 255F,
                    0.8F);
            if (filled > 0) {
                graphics.blit(HUD, -4, 35 - filled, 104, 0, 8, filled);
            }
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            graphics.blit(HUD, -8, -3, 72, 0, 16, 42);
            int lifted = 0;
            boolean costs = focus != null && focus.cost().get(primal) > 0;
            if (costs) {
                graphics.blit(HUD, -4, -8, 136, 0, 8, 8);
                lifted = 8;
            }
            if (before.get(primal) > amount) {
                graphics.blit(HUD, -4, -8 - lifted, 128, 0, 8, 8);
            } else if (before.get(primal) < amount) {
                graphics.blit(HUD, -4, -8 - lifted, 120, 0, 8, 8);
            }
            if (player.isShiftKeyDown()) {
                pose.pushPose();
                pose.mulPose(Axis.ZP.rotationDegrees(-90.0F));
                graphics.drawString(client.font, String.valueOf(amount), -32, -4, 0xFFFFFF);
                if (costs) {
                    float rate = wand.priceRate(stack, player, VisHolder.Spending.WORKING, primal);
                    graphics.drawString(client.font, TENTHS.format(focus.cost().get(primal) * rate), 8, -4, 0xFFFFFF);
                }
                pose.popPose();
            }
            pose.popPose();
            index++;
        }

        if (focus != null) {
            var picked = me.moonscenty.alchemia.wand.spell.Swapping.remembered(stack);
            if (picked != null) {
                drawTrade(graphics, client, player, new ItemStack(picked));
            } else {
                graphics.renderItem(fitted, -8, -8);
                float left = player.getCooldowns().getCooldownPercent(wand, partial) * focus.cooldown() / 1000.0F;
                if (left > 0.0F) {
                    pose.pushPose();
                    pose.translate(0.0F, 0.0F, 150.0F);
                    pose.scale(0.5F, 0.5F, 1.0F);
                    String seconds = TENTHS.format(left) + "s";
                    graphics.drawString(client.font, seconds, -client.font.width(seconds) / 2, -4, 0xFFFFFF);
                    pose.popPose();
                }
            }
        }
        pose.popPose();
        RenderSystem.disableBlend();
    }

    // original: Equal Trade shows the block it will lay and how many of it are carried
    private static void drawTrade(GuiGraphics graphics, Minecraft client, Player player, ItemStack picked) {
        if (!ItemStack.isSameItem(picked, counted) || player.tickCount != countedAt) {
            count = 0;
            for (ItemStack held : player.getInventory().items) {
                if (ItemStack.isSameItem(held, picked)) {
                    count += held.getCount();
                }
            }
            counted = picked;
            countedAt = player.tickCount;
        }
        graphics.renderItem(picked, -8, -8);
        var pose = graphics.pose();
        pose.pushPose();
        pose.translate(0.0F, -client.font.lineHeight, 500.0F);
        pose.scale(0.5F, 0.5F, 1.0F);
        String shown = String.valueOf(count);
        int wide = client.font.width(shown);
        for (int across = -1; across <= 1; across++) {
            for (int down = -1; down <= 1; down++) {
                if ((across == 0 || down == 0) && (across != 0 || down != 0)) {
                    graphics.drawString(client.font, shown, across + 16 - wide, down + 24, 0x000000, false);
                }
            }
        }
        graphics.drawString(client.font, shown, 16 - wide, 24, 0xFFFFFF, false);
        pose.popPose();
    }
}
