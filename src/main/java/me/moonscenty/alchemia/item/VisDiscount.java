package me.moonscenty.alchemia.item;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Something that makes a wand spend less while it is worn.
 *
 * <p>This is the whole worth of a robe, and half the worth of the goggles. It is also the one thing worn armour
 * has never done in this game, so there is nothing to hook into: a wand asks what its bearer has on at the moment
 * it is made to pay, and that is what this answers.
 *
 * <p>The discount is given in whole parts of a hundred and simply added up across everything worn. Three pieces
 * of a void robe are fifteen parts off every bill, which is a great deal and meant to be.
 */
public interface VisDiscount {
    /** The four places a discount can be worn. Trinkets will want adding here when there are any. */
    EquipmentSlot[] WORN = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    /**
     * The least of an asking price a wand can be brought down to.
     * <p>
     * A tenth, as the original had it. Without a floor a deep enough pile of discounts would make working free,
     * and a craft whose prices can reach nothing has no prices.
     */
    float LEAST = 0.1F;

    /** How much less a wand spends while this piece is worn, in whole parts of a hundred. */
    int visDiscount(ItemStack stack, Player wearer);

    /** Everything the player has on, added up. */
    static int worn(Player wearer) {
        if (wearer == null) {
            return 0;
        }
        int total = 0;
        for (EquipmentSlot slot : WORN) {
            ItemStack piece = wearer.getItemBySlot(slot);
            if (piece.getItem() instanceof VisDiscount gear) {
                total += gear.visDiscount(piece, wearer);
            }
        }
        return total;
    }

    /**
     * What share of an asking price actually gets paid: the cap's own rate, less what the bearer is wearing.
     * <p>
     * The original also took ten parts off per level of vis exhaustion, which is a thing that happens to somebody
     * who has drawn too hard on the aura. There is no such affliction in this mod yet; when there is, it belongs
     * here, where every price already passes through.
     */
    static float rate(float cap, Player wearer) {
        return Math.max(LEAST, cap - worn(wearer) / 100.0F);
    }
}
