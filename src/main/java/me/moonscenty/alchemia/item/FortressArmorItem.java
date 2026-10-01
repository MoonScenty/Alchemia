package me.moonscenty.alchemia.item;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Rarity;

/**
 * Fortress armour: a suit of alchemium taken apart on the altar and built back up into plate.
 *
 * <p>It stops as much as void metal does and lasts as long as alchemium, which no single metal manages, and it
 * takes an enchantment better than anything else in the mod.
 *
 * <p>Four pieces, where the original had three. It had no boots and counted three slots in its set bonus because
 * there was never a fourth to count; the meshes were drawn with feet, and plate worn with somebody else's shoes
 * is not a suit of plate.
 *
 * <p>Worn, it is a carved mesh rather than two sheets, which is the only reason this class is not a plain
 * {@link ArmorItem}. What it does beyond stopping blows -- a set that is worth more the more of it is worn, and a
 * helm somebody can have goggles or a mask worked into -- is not here yet.
 */
public class FortressArmorItem extends ArmorItem implements MeshArmour {
    public FortressArmorItem(Holder<ArmorMaterial> material, Type type, int lasts, Properties properties) {
        super(material, type, properties.durability(type.getDurability(lasts)).rarity(Rarity.RARE));
    }

    @Override
    public String meshSet() {
        return "fortress";
    }
}
