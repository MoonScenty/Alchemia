package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.platform.InputConstants;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.network.ChangeFocus;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = Alchemia.MODID, value = Dist.CLIENT)
public final class WandKeys {
    private static final String CATEGORY = "key.categories.alchemia";

    // V, not the original's F: F is vanilla's swap-hands and both bindings would fire
    public static final KeyMapping CHANGE_FOCUS = new KeyMapping("key.alchemia.change_focus",
            KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, InputConstants.KEY_V, CATEGORY);

    public static final KeyMapping TOOL_SETTING = new KeyMapping("key.alchemia.tool_setting",
            KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, InputConstants.KEY_G, CATEGORY);

    public static final KeyMapping HOVER = new KeyMapping("key.alchemia.hover",
            KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, InputConstants.KEY_H, CATEGORY);

    private static final int HELD_FOR = 5;

    private static int held;

    private WandKeys() {
    }

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(CHANGE_FOCUS);
        event.register(TOOL_SETTING);
        event.register(HOVER);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null) {
            return;
        }
        drain(CHANGE_FOCUS);
        // drain before anything else reads it, or this never fires
        boolean setting = drain(TOOL_SETTING);
        if (drain(HOVER) && client.screen == null && !me.moonscenty.alchemia.item.Hover.worn(player).isEmpty()) {
            PacketDistributor.sendToServer(new me.moonscenty.alchemia.network.ToggleHover());
        }
        var inHand = player.getMainHandItem().getItem();
        if (setting && client.screen == null && (inHand instanceof WandItem
                || inHand instanceof me.moonscenty.alchemia.item.tool.ElementalShovelItem)) {
            PacketDistributor.sendToServer(new me.moonscenty.alchemia.network.ToolSetting());
        }
        if (!(inHand instanceof WandItem) || client.screen != null) {
            held = 0;
            return;
        }

        if (CHANGE_FOCUS.isDown()) {
            held++;
            if (held == HELD_FOR && !player.isShiftKeyDown()) {
                openWheel(client, player);
            }
            return;
        }
        if (held > 0) {
            if (held < HELD_FOR || player.isShiftKeyDown()) {
                PacketDistributor.sendToServer(new ChangeFocus(player.isShiftKeyDown()));
            }
            held = 0;
        }
    }

    private static void openWheel(Minecraft client, LocalPlayer player) {
        var carried = me.moonscenty.alchemia.wand.Foci.carried(player);
        if (carried.isEmpty()) {
            return;
        }
        client.setScreen(new FocusWheelScreen(carried,
                WandItem.focus(player.getMainHandItem()), CHANGE_FOCUS.getKey().getValue()));
    }

    private static boolean drain(KeyMapping key) {
        boolean any = false;
        while (key.consumeClick()) {
            any = true;
        }
        return any;
    }
}
