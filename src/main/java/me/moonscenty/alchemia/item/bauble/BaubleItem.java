package me.moonscenty.alchemia.item.bauble;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/**
 * A bauble: an amulet, a ring or a belt, worn in the slot of that name. What slot is the item's tag. It may hold up
 * a runic shield, and whatever goes on or comes off has the shield counted again.
 */
public class BaubleItem extends Item implements ICurioItem, RunicGear {
    private final int charge;
    private final RunicShield.Upgrade upgrade;

    public BaubleItem(Properties properties) {
        this(properties, 0, RunicShield.Upgrade.NONE);
    }

    public BaubleItem(Properties properties, int charge, RunicShield.Upgrade upgrade) {
        super(properties.stacksTo(1));
        this.charge = charge;
        this.upgrade = upgrade;
    }

    @Override
    public int runicCharge(ItemStack stack) {
        return charge;
    }

    @Override
    public RunicShield.Upgrade upgrade(ItemStack stack) {
        return upgrade;
    }

    @Override
    public void onEquip(SlotContext slot, ItemStack previous, ItemStack stack) {
        RunicShield.markDirty(slot.entity());
    }

    @Override
    public void onUnequip(SlotContext slot, ItemStack next, ItemStack stack) {
        RunicShield.markDirty(slot.entity());
    }
}
