package me.moonscenty.alchemia.item;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

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
 * <p>
 * They also take something off what a wand spends, which the original gave them and which is not said with a tag
 * because it is a number rather than a yes or a no.
 */
public class GogglesItem extends ArmorItem implements VisDiscount {
    /** How many knocks they take before giving out. The original's number. */
    public static final int LASTS = 350;

    /** How much less a wand spends while they are on. As much as a whole piece of a void robe, and the original's number. */
    public static final int DISCOUNT = 5;

    public GogglesItem(Holder<ArmorMaterial> material, Properties properties) {
        super(material, Type.HELMET, properties.durability(LASTS).rarity(Rarity.RARE));
    }

    @Override
    public int visDiscount(ItemStack stack, Player wearer) {
        return DISCOUNT;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("item.alchemia.vis_discount", DISCOUNT).withStyle(ChatFormatting.AQUA));
    }
}
