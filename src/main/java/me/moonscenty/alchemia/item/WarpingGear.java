package me.moonscenty.alchemia.item;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Something that carries warp of its own while it is held or worn.
 *
 * <p>It is not warp the player has earned. It goes away when the thing is put down, and while it is in hand it is
 * counted with everything else: it makes a warp event likelier and worse, and it slows how fast the pent-up
 * pressure behind one bleeds off afterwards. A tool that is stronger than it ought to be has to be paid for
 * somewhere, and this is where the original paid for it.
 */
public interface WarpingGear {
    /** The places gear is counted from: the hand doing the work, and the four places armour goes. */
    EquipmentSlot[] COUNTED = {EquipmentSlot.MAINHAND, EquipmentSlot.HEAD, EquipmentSlot.CHEST,
            EquipmentSlot.LEGS, EquipmentSlot.FEET};

    /** How much warp this carries while it is held or worn. */
    int warp(ItemStack stack, Player holder);

    /** Everything the player is holding or wearing, baubles too, added up. */
    static int carried(Player holder) {
        java.util.List<ItemStack> counted = new java.util.ArrayList<>();
        for (EquipmentSlot slot : COUNTED) {
            counted.add(holder.getItemBySlot(slot));
        }
        top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(holder).ifPresent(curios -> {
            var baubles = curios.getEquippedCurios();
            for (int i = 0; i < baubles.getSlots(); i++) {
                counted.add(baubles.getStackInSlot(i));
            }
        });
        int total = 0;
        for (ItemStack stack : counted) {
            total += of(stack, holder);
        }
        return total;
    }

    /** The warp one thing carries: its own, void metal's, and whatever the altar has worked into it. */
    static int of(ItemStack stack, Player holder) {
        if (stack.isEmpty()) {
            return 0;
        }
        int warp = stack.getOrDefault(me.moonscenty.alchemia.registry.ModDataComponents.WARP.get(), 0)
                + VoidGear.warp(stack);
        if (stack.getItem() instanceof WarpingGear gear) {
            warp += gear.warp(stack, holder);
        }
        return warp;
    }
}
