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

/**
 * The key that changes what a wand is pointed with.
 * <p>
 * Sneak with it to take the focus off. The original held the key to open a wheel of every focus the player was
 * carrying and let go over one; here a press moves to the next. The wheel is a way of choosing and this is the
 * choosing itself, so it can be built on top later without any of this changing.
 * <p>
 * The original used F, which was free in its day. It is vanilla's swap-hands key in this one, and two bindings on
 * one key both fire -- a press would change the focus and swap the hands at once. V is free and falls under the
 * same hand, so V it is; anyone who wants F back can say so in the controls screen.
 */
@EventBusSubscriber(modid = Alchemia.MODID, value = Dist.CLIENT)
public final class WandKeys {
    private static final String CATEGORY = "key.categories.alchemia";

    public static final KeyMapping CHANGE_FOCUS = new KeyMapping("key.alchemia.change_focus",
            KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, InputConstants.KEY_V, CATEGORY);

    private WandKeys() {
    }

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(CHANGE_FOCUS);
    }

    /**
     * Sent once a press, not once a tick.
     * <p>
     * {@code consumeClick} hands back one press at a time and empties the queue as it goes, so holding the key
     * down does nothing after the first tick of it.
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        boolean pressed = false;
        while (CHANGE_FOCUS.consumeClick()) {
            pressed = true;
        }
        if (pressed && player.getMainHandItem().getItem() instanceof WandItem) {
            PacketDistributor.sendToServer(new ChangeFocus(player.isShiftKeyDown()));
        }
    }
}
