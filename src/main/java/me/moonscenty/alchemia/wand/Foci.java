package me.moonscenty.alchemia.wand;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.registry.ModTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Fitting a focus to a wand and taking it off again.
 *
 * <p>Both are one action from the player's side and a swap from the bag's side: whatever was on the wand goes back
 * where the new one came from, so nothing is ever created, destroyed, or left without a home.
 *
 * <p>The cycle runs in alphabetical order of what the foci are called rather than in the order they happen to be
 * sitting in the bag. Tidying a bag should not change which focus comes next.
 */
public final class Foci {
    private Foci() {
    }

    /** What is on the wand, or nothing. */
    public static ItemStack on(ItemStack wand) {
        return WandItem.focus(wand);
    }

    /**
     * Puts the next focus on the wand, and whatever was on it back where that one came from.
     *
     * @return what is now fitted, or nothing if the player had no focus to fit
     */
    public static ItemStack next(Player player, ItemStack wand) {
        List<Integer> slots = carried(player);
        if (slots.isEmpty()) {
            return ItemStack.EMPTY;
        }
        Inventory bag = player.getInventory();
        int slot = after(bag, slots, on(wand));

        ItemStack taken = bag.getItem(slot).copy();
        // the old focus goes into the slot the new one just left, which is the one place certain to be free
        bag.setItem(slot, WandItem.focus(wand).copy());
        WandItem.setFocus(wand, taken);
        say(player, taken, true);
        return taken;
    }

    /**
     * Takes the focus off the wand and hands it back.
     *
     * @return what came off, or nothing if there was nothing on it
     */
    public static ItemStack remove(Player player, ItemStack wand) {
        ItemStack fitted = WandItem.focus(wand).copy();
        if (fitted.isEmpty()) {
            return ItemStack.EMPTY;
        }
        WandItem.setFocus(wand, ItemStack.EMPTY);
        if (!player.getInventory().add(fitted)) {
            player.drop(fitted, false);
        }
        say(player, fitted, false);
        return fitted;
    }

    /** Which slots of the player's bag hold a focus, soonest-named first. */
    private static List<Integer> carried(Player player) {
        Inventory bag = player.getInventory();
        List<Integer> slots = new ArrayList<>();
        for (int slot = 0; slot < bag.getContainerSize(); slot++) {
            if (bag.getItem(slot).is(ModTags.Items.FOCI)) {
                slots.add(slot);
            }
        }
        slots.sort(Comparator.comparing(slot -> named(bag.getItem(slot))));
        return slots;
    }

    /**
     * The slot holding the focus that comes after the one fitted.
     * <p>
     * With nothing fitted, or with the last of them fitted, that is the first. A player holding one focus and
     * pressing the key therefore swaps it on and off, which is what one focus ought to do.
     */
    private static int after(Inventory bag, List<Integer> slots, ItemStack fitted) {
        if (fitted.isEmpty()) {
            return slots.get(0);
        }
        String here = named(fitted);
        for (int slot : slots) {
            if (named(bag.getItem(slot)).compareTo(here) > 0) {
                return slot;
            }
        }
        return slots.get(0);
    }

    private static String named(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }

    /**
     * Says what happened, above the hotbar.
     * <p>
     * The original drew the fitted focus beside the crosshair and left it there. That is a heads-up display and
     * wants writing as one; until it exists, being told once is better than having to read the tooltip.
     */
    private static void say(Player player, ItemStack focus, boolean fitted) {
        player.displayClientMessage(Component.translatable(
                fitted ? "item.alchemia.wand.focus_fitted" : "item.alchemia.wand.focus_removed",
                focus.getHoverName()), true);
        player.level().playSound(null, player.blockPosition(),
                fitted ? SoundEvents.ITEM_FRAME_ADD_ITEM : SoundEvents.ITEM_FRAME_REMOVE_ITEM,
                SoundSource.PLAYERS, 0.4F, fitted ? 1.4F : 1.1F);
    }

    /** The name of a focus, for anything that wants to say which one without holding the stack. */
    public static Optional<ResourceLocation> kind(ItemStack focus) {
        return focus.isEmpty() ? Optional.empty() : Optional.of(BuiltInRegistries.ITEM.getKey(focus.getItem()));
    }
}
