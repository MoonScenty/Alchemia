package me.moonscenty.alchemia.wand;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import me.moonscenty.alchemia.item.FocusPouchItem;
import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.item.Worn;
import me.moonscenty.alchemia.registry.ModSounds;
import me.moonscenty.alchemia.registry.ModTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

// the cycle is sorted by name (kind plus upgrades) so inventory order doesn't change it
public final class Foci {
    private Foci() {
    }

    private record Spot(int slot, int inPouch) {
        boolean inBag() {
            return inPouch < 0;
        }
    }

    public static ItemStack on(ItemStack wand) {
        return WandItem.focus(wand);
    }

    public static ItemStack next(Player player, ItemStack wand) {
        Inventory bag = player.getInventory();
        List<Spot> spots = carried(bag);
        if (spots.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return swap(player, bag, after(bag, spots, on(wand)), wand);
    }

    private static ItemStack swap(Player player, Inventory bag, Spot spot, ItemStack wand) {
        ItemStack taken = read(bag, spot).copy();
        // the old focus goes into the slot just vacated
        write(bag, spot, WandItem.focus(wand).copy());
        WandItem.setFocus(wand, taken);
        say(player, taken, true);
        return taken;
    }

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
        spots.sort(Comparator.comparing(spot -> name(read(bag, spot))));
        return spots;
    }

    private static Spot after(Inventory bag, List<Spot> spots, ItemStack fitted) {
        if (fitted.isEmpty()) {
            return spots.get(0);
        }
        String here = name(fitted);
        for (Spot spot : spots) {
            if (name(read(bag, spot)).compareTo(here) > 0) {
                return spot;
            }
        }
        return spots.get(0);
    }

    // worn pouches are addressed as slots past the end of the inventory
    private static List<ItemStack> worn(Inventory bag) {
        return Worn.baubles(bag.player).stream().filter(bauble -> bauble.getItem() instanceof FocusPouchItem)
                .toList();
    }

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

    // foci are picked by name so client and server needn't agree on inventory order
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

    private static void say(Player player, ItemStack focus, boolean fitted) {
        player.displayClientMessage(Component.translatable(
                fitted ? "item.alchemia.wand.focus_fitted" : "item.alchemia.wand.focus_removed",
                focus.getHoverName()), true);
        player.level().playSound(null, player.blockPosition(), ModSounds.CAMERA_TICKS.get(),
                SoundSource.PLAYERS, 0.3F, fitted ? 1.0F : 0.9F);
    }

    public static List<ItemStack> carried(Player player) {
        Inventory bag = player.getInventory();
        List<ItemStack> found = new ArrayList<>();
        for (Spot spot : carried(bag)) {
            found.add(read(bag, spot).copy());
        }
        return found;
    }

    public static ItemStack fit(Player player, ItemStack wand, String name) {
        Inventory bag = player.getInventory();
        if (name.isEmpty() || name.equals(name(on(wand)))) {
            return ItemStack.EMPTY;
        }
        for (Spot spot : carried(bag)) {
            if (name(read(bag, spot)).equals(name)) {
                return swap(player, bag, spot, wand);
            }
        }
        return ItemStack.EMPTY;
    }
}
