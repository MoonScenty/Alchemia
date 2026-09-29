package me.moonscenty.alchemia.wand;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import me.moonscenty.alchemia.item.FocusPouchItem;
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
 * <p>Foci in a pouch count as carried, exactly as loose ones do. That is what a pouch is for — a player with one
 * has every focus on a single key without eighteen squares gone out of their bag.
 *
 * <p>The cycle runs in alphabetical order of what the foci are called rather than in the order they happen to be
 * lying in. Tidying a bag should not change which focus comes next.
 */
public final class Foci {
    private Foci() {
    }

    /**
     * Where a focus is: a square of the bag, and if that square holds a pouch, a square inside it.
     *
     * @param slot    the square of the player's bag
     * @param inPouch the square inside the pouch there, or -1 when the focus is the bag's square itself
     */
    private record Spot(int slot, int inPouch) {
        boolean inBag() {
            return inPouch < 0;
        }
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
        Inventory bag = player.getInventory();
        List<Spot> spots = carried(bag);
        if (spots.isEmpty()) {
            return ItemStack.EMPTY;
        }
        Spot spot = after(bag, spots, on(wand));

        ItemStack taken = read(bag, spot).copy();
        // the old focus goes where the new one just left, which is the one square certain to be free
        write(bag, spot, WandItem.focus(wand).copy());
        WandItem.setFocus(wand, taken);
        say(player, taken, true);
        return taken;
    }

    /**
     * Takes the focus off the wand and hands it back, into a pouch if there is room in one.
     *
     * @return what came off, or nothing if there was nothing on it
     */
    public static ItemStack remove(Player player, ItemStack wand) {
        ItemStack fitted = WandItem.focus(wand).copy();
        if (fitted.isEmpty()) {
            return ItemStack.EMPTY;
        }
        WandItem.setFocus(wand, ItemStack.EMPTY);
        if (!intoPouch(player.getInventory(), fitted) && !player.getInventory().add(fitted)) {
            player.drop(fitted, false);
        }
        say(player, fitted, false);
        return fitted;
    }

    /** Every focus the player is carrying, loose or pouched, soonest-named first. */
    private static List<Spot> carried(Inventory bag) {
        List<Spot> spots = new ArrayList<>();
        for (int slot = 0; slot < bag.getContainerSize(); slot++) {
            ItemStack held = bag.getItem(slot);
            if (held.is(ModTags.Items.FOCI)) {
                spots.add(new Spot(slot, -1));
            } else if (held.getItem() instanceof FocusPouchItem) {
                for (int inside = 0; inside < FocusPouchItem.SIZE; inside++) {
                    if (FocusPouchItem.item(held, inside).is(ModTags.Items.FOCI)) {
                        spots.add(new Spot(slot, inside));
                    }
                }
            }
        }
        spots.sort(Comparator.comparing(spot -> named(read(bag, spot))));
        return spots;
    }

    /**
     * The spot holding the focus that comes after the one fitted.
     * <p>
     * With nothing fitted, or with the last of them fitted, that is the first. A player holding one focus and
     * pressing the key therefore swaps it on and off, which is what one focus ought to do.
     */
    private static Spot after(Inventory bag, List<Spot> spots, ItemStack fitted) {
        if (fitted.isEmpty()) {
            return spots.get(0);
        }
        String here = named(fitted);
        for (Spot spot : spots) {
            if (named(read(bag, spot)).compareTo(here) > 0) {
                return spot;
            }
        }
        return spots.get(0);
    }

    private static ItemStack read(Inventory bag, Spot spot) {
        return spot.inBag() ? bag.getItem(spot.slot()) : FocusPouchItem.item(bag.getItem(spot.slot()), spot.inPouch());
    }

    private static void write(Inventory bag, Spot spot, ItemStack stack) {
        if (spot.inBag()) {
            bag.setItem(spot.slot(), stack);
            return;
        }
        ItemStack pouch = bag.getItem(spot.slot());
        List<ItemStack> items = new ArrayList<>(FocusPouchItem.SIZE);
        for (int inside = 0; inside < FocusPouchItem.SIZE; inside++) {
            items.add(inside == spot.inPouch() ? stack : FocusPouchItem.item(pouch, inside));
        }
        FocusPouchItem.setContents(pouch, items);
    }

    /** Puts a focus in the first pouch with room, and says whether one had room. */
    private static boolean intoPouch(Inventory bag, ItemStack focus) {
        for (int slot = 0; slot < bag.getContainerSize(); slot++) {
            ItemStack pouch = bag.getItem(slot);
            if (!(pouch.getItem() instanceof FocusPouchItem)) {
                continue;
            }
            for (int inside = 0; inside < FocusPouchItem.SIZE; inside++) {
                if (FocusPouchItem.item(pouch, inside).isEmpty()) {
                    write(bag, new Spot(slot, inside), focus);
                    return true;
                }
            }
        }
        return false;
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
