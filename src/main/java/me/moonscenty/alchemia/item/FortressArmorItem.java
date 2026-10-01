package me.moonscenty.alchemia.item;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

/**
 * Fortress armour: a suit of alchemium taken apart on the altar and built back up into plate.
 *
 * <p>It stops as much as void metal does and lasts as long as alchemium, which no single metal manages, and it
 * takes an enchantment better than anything else in the mod.
 *
 * <p>Three pieces, as the original had: a helm, a cuirass and greaves. There are no boots.
 *
 * <p>What it does beyond stopping blows is in {@link FortressArmourEvents}, because a blow is something that
 * happens to a wearer rather than to an item. A helm somebody can have goggles or a mask worked into is not here
 * yet.
 */
public class FortressArmorItem extends ArmorItem {
    public FortressArmorItem(Holder<ArmorMaterial> material, Type type, int lasts, Properties properties) {
        super(material, type, properties.durability(type.getDurability(lasts)).rarity(Rarity.RARE));
    }

    /**
     * What this piece is worth beyond its armour.
     * <p>
     * Written per piece rather than per suit, because a piece lying in a chest has no suit to belong to, and
     * what somebody wants to know about a piece in a chest is what putting it on would add.
     */
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("item.alchemia.fortress.turns",
                        share(FortressArmourEvents.TURNS_BLOWS),
                        share(FortressArmourEvents.TURNS_BURNING),
                        share(FortressArmourEvents.TURNS_WORKINGS))
                .withStyle(ChatFormatting.AQUA));
    }

    /** A share written the way a tooltip wants it: out of a hundred, with the halves and quarters kept. */
    private static String share(float of) {
        return String.format("%.4g", of * 100.0F);
    }
}
