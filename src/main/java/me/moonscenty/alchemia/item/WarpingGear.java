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

    /** Everything the player is holding or wearing, added up. Trinkets will want adding when there are any. */
    static int carried(Player holder) {
        int total = 0;
        for (EquipmentSlot slot : COUNTED) {
            ItemStack stack = holder.getItemBySlot(slot);
            if (stack.getItem() instanceof WarpingGear gear) {
                total += gear.warp(stack, holder);
            }
        }
        return total;
    }
}
