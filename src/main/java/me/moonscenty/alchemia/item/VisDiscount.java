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
    /** The four places armour can give a discount; baubles give theirs from wherever they are worn. */
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

    /**
     * How much less a wand spends of this one primal while this is worn, over and above {@link #visDiscount}: the
     * apprentice's ring of a primal is five parts off that primal and nothing off the rest.
     */
    default int visDiscount(ItemStack stack, Player wearer,
            net.minecraft.core.Holder<me.moonscenty.alchemia.aspect.Aspect> aspect) {
        return 0;
    }

    /** Everything the player has on, armour and baubles, added up. */
    static int worn(Player wearer) {
        return worn(wearer, null);
    }

    /** The same, with what is off this one primal besides, when there is a primal to ask about. */
    static int worn(Player wearer, net.minecraft.core.Holder<me.moonscenty.alchemia.aspect.Aspect> aspect) {
        if (wearer == null) {
            return 0;
        }
        java.util.List<ItemStack> pieces = new java.util.ArrayList<>();
        for (EquipmentSlot slot : WORN) {
            pieces.add(wearer.getItemBySlot(slot));
        }
        top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(wearer).ifPresent(curios -> {
            var baubles = curios.getEquippedCurios();
            for (int i = 0; i < baubles.getSlots(); i++) {
                pieces.add(baubles.getStackInSlot(i));
            }
        });
        int total = 0;
        for (ItemStack piece : pieces) {
            if (piece.getItem() instanceof VisDiscount gear) {
                total += gear.visDiscount(piece, wearer);
                if (aspect != null) {
                    total += gear.visDiscount(piece, wearer, aspect);
                }
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

    /** The same, for one primal: the general discount and that primal's own. */
    static float rate(float cap, Player wearer,
            net.minecraft.core.Holder<me.moonscenty.alchemia.aspect.Aspect> aspect) {
        return Math.max(LEAST, cap - worn(wearer, aspect) / 100.0F);
    }
}
