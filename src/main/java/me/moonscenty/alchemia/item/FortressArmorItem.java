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
 * <p>Three pieces, as the original had: a helm, a cuirass and greaves. There are no boots.
 *
 * <p>What it does beyond stopping blows -- a set that is worth more the more of it is worn, and a helm somebody can
 * have goggles or a mask worked into -- is not here yet.
 */
public class FortressArmorItem extends ArmorItem {
    public FortressArmorItem(Holder<ArmorMaterial> material, Type type, int lasts, Properties properties) {
        super(material, type, properties.durability(type.getDurability(lasts)).rarity(Rarity.RARE));
    }
}
