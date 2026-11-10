package me.moonscenty.alchemia.client;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.item.Rechargeable;
import me.moonscenty.alchemia.item.VisHolder;
import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.item.WarpingGear;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

// original's extra tooltip lines
@EventBusSubscriber(modid = Alchemia.MODID, value = Dist.CLIENT)
public final class ItemTooltips {
    private static final java.text.DecimalFormat TENTHS = new java.text.DecimalFormat("#######.#");
    private static final int CHARM_LINES = 5;

    private ItemTooltips() {
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        Player player = event.getEntity();
        var lines = event.getToolTip();
        if (player != null) {
            int warp = WarpingGear.of(stack, player);
            if (warp > 0) {
                lines.add(Component.translatable("item.alchemia.warping", warp).withStyle(ChatFormatting.DARK_PURPLE));
            }
        }
        // the verdant heart writes its own charge line
        if (stack.getItem() instanceof Rechargeable charged && charged.chargeLevel(stack) >= 0.0F
                && !(stack.getItem() instanceof me.moonscenty.alchemia.item.bauble.VerdantHeartItem)) {
            lines.add(Component.translatable("item.alchemia.vis_charge", Math.round(100.0F * charged.chargeLevel(stack)))
                    .withStyle(ChatFormatting.YELLOW));
        }
        if (stack.getItem() instanceof WandItem wand && player != null) {
            wandCosts(lines, wand, stack, player);
        }
        if (stack.is(ModItems.METAL_TOOLS.get("void_sword").get())) {
            lines.add(Component.translatable("item.alchemia.sapless").withStyle(ChatFormatting.GOLD));
        }
        if (stack.getItem() instanceof me.moonscenty.alchemia.item.LootBagItem) {
            lines.add(Component.translatable("item.alchemia.loot_bag.use").withStyle(ChatFormatting.GRAY));
        }
        if (stack.is(ModItems.PRIMAL_CHARM.get()) && player != null) {
            // original: the line changes every few seconds
            int pick = net.minecraft.util.RandomSource.create(player.getId() + player.tickCount / 120).nextInt(100);
            if (pick < 25) {
                lines.add(Component.translatable("item.alchemia.primal_charm." + pick % CHARM_LINES)
                        .withStyle(ChatFormatting.GOLD));
            }
        }
        if (stack.getItem() instanceof me.moonscenty.alchemia.item.AlchemometerItem) {
            lines.add(Component.translatable("item.alchemia.alchemometer.key",
                    WandKeys.CHANGE_FOCUS.getTranslatedKeyMessage()).withStyle(ChatFormatting.DARK_PURPLE));
        }
    }

    // original: what the wand charges for each primal, as a share of the price, and what the fitted focus costs
    private static void wandCosts(java.util.List<Component> lines, WandItem wand, ItemStack stack, Player player) {
        ItemStack fitted = WandItem.focus(stack);
        var focus = fitted.getItem() instanceof me.moonscenty.alchemia.item.FocusItem item ? item.focus(fitted) : null;
        int total = 0;
        int counted = 0;
        for (Holder<Aspect> primal : ModAspects.primals()) {
            float rate = wand.priceRate(stack, player, VisHolder.Spending.WORKING, primal);
            total += Math.round(rate * 100.0F);
            counted++;
            if (Screen.hasShiftDown()) {
                Component line = Component.translatable("item.alchemia.wand.vis_cost", primal.value().displayName(),
                        TENTHS.format(rate * 100.0F));
                if (focus != null && focus.cost().get(primal) > 0) {
                    line = Component.empty().append(line).append(Component.translatable(focus.perTick()
                                    ? "item.alchemia.wand.focus_cost_tick" : "item.alchemia.wand.focus_cost",
                            TENTHS.format(focus.cost().get(primal) * rate)));
                }
                lines.add(Component.empty().append(line).withStyle(ChatFormatting.GRAY));
            }
        }
        if (!Screen.hasShiftDown() && counted > 0) {
            lines.add(Component.translatable("item.alchemia.wand.vis_cost_average", total / counted)
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
    }
}
