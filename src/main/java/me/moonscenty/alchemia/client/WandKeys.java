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
 * The two keys a wand answers to: the one that changes what it is pointed with, and the one that sets how far
 * the focus on it works.
 * <p>
 * Sneak with it to take the focus off. A tap moves to the next focus; holding it opens the wheel the original
 * had -- every focus being carried laid out in a ring, and whichever the cursor is over when the key is let go
 * is the one fitted. The tap is the quicker of the two for anybody carrying three foci, and the ring is the
 * only workable one for anybody carrying a pouch full.
 * <p>
 * The original used F, which was free in its day. It is vanilla's swap-hands key in this one, and two bindings on
 * one key both fire -- a press would change the focus and swap the hands at once. V is free and falls under the
 * same hand, so V it is; anyone who wants F back can say so in the controls screen.
 * <p>
 * The reach key is the original's G, which is still free.
 */
@EventBusSubscriber(modid = Alchemia.MODID, value = Dist.CLIENT)
public final class WandKeys {
    private static final String CATEGORY = "key.categories.alchemia";

    public static final KeyMapping CHANGE_FOCUS = new KeyMapping("key.alchemia.change_focus",
            KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, InputConstants.KEY_V, CATEGORY);

    /** Only a focus with an architect's eye in it answers to this one. */
    public static final KeyMapping SET_REACH = new KeyMapping("key.alchemia.set_reach",
            KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, InputConstants.KEY_G, CATEGORY);

    /** How long the key has to be held before the ring opens rather than the focus simply moving on. */
    private static final int HELD_FOR = 5;

    private static int held;

    private WandKeys() {
    }

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(CHANGE_FOCUS);
        event.register(SET_REACH);
    }

    /**
     * A tap against a hold.
     * <p>
     * The key is watched rather than its presses counted: how long it has been down is the whole of what tells
     * a tap from a hold. Letting go early moves the focus on; holding past {@value #HELD_FOR} ticks opens the
     * ring, and from then on the ring itself has the keyboard and decides what the letting go means.
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null) {
            return;
        }
        drain(CHANGE_FOCUS);
        drain(SET_REACH);
        if (!(player.getMainHandItem().getItem() instanceof WandItem) || client.screen != null) {
            held = 0;
            return;
        }

        if (SET_REACH.isDown() && SET_REACH.consumeClick()) {
            PacketDistributor.sendToServer(new me.moonscenty.alchemia.network.SetReach());
        }

        if (CHANGE_FOCUS.isDown()) {
            held++;
            // sneaking is taking the focus off, which is a thing done at once rather than chosen from a ring
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

    /** The ring, with every focus being carried on it. Nothing to put on it is nothing to open. */
    private static void openWheel(Minecraft client, LocalPlayer player) {
        var carried = me.moonscenty.alchemia.wand.Foci.carried(player);
        if (carried.isEmpty()) {
            return;
        }
        client.setScreen(new FocusWheelScreen(carried,
                WandItem.focus(player.getMainHandItem()), CHANGE_FOCUS.getKey().getValue()));
    }

    /** Empties a key's queue of presses, which are of no use to anything that watches the key itself. */
    private static void drain(KeyMapping key) {
        while (key.consumeClick()) {
            // nothing: the press queue is read by holding the key down, not by counting clicks
        }
    }
}
