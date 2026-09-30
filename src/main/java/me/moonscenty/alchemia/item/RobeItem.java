package me.moonscenty.alchemia.item;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Rarity;

/**
 * A robe: cloth over a wand-bearer rather than plate over a soldier.
 *
 * <p>It stops almost nothing. What it is worth is that a wand spends less while it is on, which is a thing worn
 * armour has never done in this game and so has to be plumbed in rather than declared.
 *
 * <p>Its worn shape is a bought mesh, not boxes, and is drawn by a layer of our own. The material therefore has
 * no sheet of its own: see {@link me.moonscenty.alchemia.registry.ModArmorMaterials}.
 *
 * @param discount how much less a wand spends while this piece is worn, in whole parts of a hundred
 */
public class RobeItem extends ArmorItem {
    private final int discount;
    private final boolean drab;

    public RobeItem(Holder<ArmorMaterial> material, Type type, int discount, boolean drab, Properties properties) {
        super(material, type, properties.rarity(Rarity.UNCOMMON));
        this.discount = discount;
        this.drab = drab;
    }

    /** How much less a wand spends while this is worn, in whole parts of a hundred. */
    public int discount() {
        return discount;
    }

    /** Whether this piece wears the dark sheet. The two robes share one set of meshes and differ only in this. */
    public boolean drab() {
        return drab;
    }
}
