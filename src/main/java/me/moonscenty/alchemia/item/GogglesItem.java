package me.moonscenty.alchemia.item;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Rarity;

/**
 * Goggles that show what is there.
 * <p>
 * They stop about what a hat stops. What they are for is seeing: an aura node is invisible to anyone not wearing
 * a pair, and the whole of the aura -- the thing half this mod is built on -- is a thing a player is told about
 * rather than shown until these are on their face.
 * <p>
 * That they reveal is said with a tag rather than with code here, because the sight that reads it was written
 * before these existed and asks only whether anything worn is in the list. Anything else that ought to reveal,
 * ours or somebody else's, goes in the same list and needs nothing more.
 */
public class GogglesItem extends ArmorItem {
    /** How many knocks they take before giving out. The original's number. */
    public static final int LASTS = 350;

    public GogglesItem(Holder<ArmorMaterial> material, Properties properties) {
        super(material, Type.HELMET, properties.durability(LASTS).rarity(Rarity.RARE));
    }
}
