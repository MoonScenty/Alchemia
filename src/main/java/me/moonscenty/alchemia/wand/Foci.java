package me.moonscenty.alchemia.wand;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import me.moonscenty.alchemia.item.FocusPouchItem;
import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.registry.ModSounds;
import me.moonscenty.alchemia.registry.ModTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
 *
 * <p>What a focus is called, for that purpose, is its kind <em>and</em> what is worked into it. Two shock foci,
 * one plain and one upgraded, are two different foci to anybody carrying both, and a cycle that could not tell
 * them apart would never reach the second.
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
        List<ItemStack> pouches = worn(bag);
        for (int which = 0; which < pouches.size(); which++) {
            for (int inside = 0; inside < FocusPouchItem.SIZE; inside++) {
                if (FocusPouchItem.item(pouches.get(which), inside).is(ModTags.Items.FOCI)) {
                    spots.add(new Spot(bag.getContainerSize() + which, inside));
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

    /** The pouches worn on the belt, which count as squares past the end of the bag. */
    private static List<ItemStack> worn(Inventory bag) {
        List<ItemStack> pouches = new ArrayList<>();
        top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(bag.player).ifPresent(curios -> {
            var baubles = curios.getEquippedCurios();
            for (int i = 0; i < baubles.getSlots(); i++) {
                if (baubles.getStackInSlot(i).getItem() instanceof FocusPouchItem) {
                    pouches.add(baubles.getStackInSlot(i));
                }
            }
        });
        return pouches;
    }

    /** A square of the bag, or past its end, a worn pouch. */
    private static ItemStack at(Inventory bag, int slot) {
        if (slot < bag.getContainerSize()) {
            return bag.getItem(slot);
        }
        List<ItemStack> pouches = worn(bag);
        int which = slot - bag.getContainerSize();
        return which < pouches.size() ? pouches.get(which) : ItemStack.EMPTY;
    }

    private static ItemStack read(Inventory bag, Spot spot) {
        return spot.inBag() ? bag.getItem(spot.slot()) : FocusPouchItem.item(at(bag, spot.slot()), spot.inPouch());
    }

    private static void write(Inventory bag, Spot spot, ItemStack stack) {
        if (spot.inBag()) {
            bag.setItem(spot.slot(), stack);
            return;
        }
        ItemStack pouch = at(bag, spot.slot());
        List<ItemStack> items = new ArrayList<>(FocusPouchItem.SIZE);
        for (int inside = 0; inside < FocusPouchItem.SIZE; inside++) {
            items.add(inside == spot.inPouch() ? stack : FocusPouchItem.item(pouch, inside));
        }
        FocusPouchItem.setContents(pouch, items);
    }

    /** Puts a focus in the first pouch with room, and says whether one had room. */
    private static boolean intoPouch(Inventory bag, ItemStack focus) {
        int squares = bag.getContainerSize() + worn(bag).size();
        for (int slot = 0; slot < squares; slot++) {
            ItemStack pouch = at(bag, slot);
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

    /**
     * What one focus is called: its kind, and then every rank worked into it in the order they went in.
     * <p>
     * This is both how the cycle is sorted and how the wheel says which one was picked. Naming a focus rather
     * than numbering it means the client and the server need not agree about the order of anybody's bag -- the
     * client says which focus, and the server finds a focus of that name among the ones actually carried.
     */
    public static String name(ItemStack stack) {
        if (stack.isEmpty()) {
            return "";
        }
        StringBuilder written = new StringBuilder(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
        for (FocusUpgrade upgrade : FocusUpgrades.on(stack)) {
            written.append('|').append(upgrade.getSerializedName());
        }
        return written.toString();
    }

    private static String named(ItemStack stack) {
        return name(stack);
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
        player.level().playSound(null, player.blockPosition(), ModSounds.CAMERA_TICKS.get(),
                SoundSource.PLAYERS, 0.3F, fitted ? 1.0F : 0.9F);
    }

    /**
     * Every focus the player is carrying, in the order the cycle runs in.
     * <p>
     * Copies, and only for looking at: the wheel draws these, and whichever is picked is named rather than
     * handed back. Both sides can work this out for themselves, which is why the wheel can be drawn at all.
     */
    public static List<ItemStack> carried(Player player) {
        Inventory bag = player.getInventory();
        List<ItemStack> found = new ArrayList<>();
        for (Spot spot : carried(bag)) {
            found.add(read(bag, spot).copy());
        }
        return found;
    }

    /**
     * Fits the focus of this name, if one of that name is being carried.
     * <p>
     * The swap is the same as the cycle's: what was on the wand goes into the square the new one came out of.
     *
     * @return what is now fitted, or nothing when the player is carrying no such focus
     */
    public static ItemStack fit(Player player, ItemStack wand, String name) {
        Inventory bag = player.getInventory();
        if (name.isEmpty() || name.equals(name(on(wand)))) {
            return ItemStack.EMPTY;
        }
        for (Spot spot : carried(bag)) {
            if (!name(read(bag, spot)).equals(name)) {
                continue;
            }
            ItemStack taken = read(bag, spot).copy();
            write(bag, spot, WandItem.focus(wand).copy());
            WandItem.setFocus(wand, taken);
            say(player, taken, true);
            return taken;
        }
        return ItemStack.EMPTY;
    }

    /** The name of a focus, for anything that wants to say which one without holding the stack. */
    public static Optional<ResourceLocation> kind(ItemStack focus) {
        return focus.isEmpty() ? Optional.empty() : Optional.of(BuiltInRegistries.ITEM.getKey(focus.getItem()));
    }
}
