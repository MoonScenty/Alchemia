package me.moonscenty.alchemia.item;

import java.util.List;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

// node sight is granted by an item tag, not here
public class GogglesItem extends ArmorItem implements VisDiscount {
    // original's value
    public static final int LASTS = 350;

    // original's value
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
        lines.add(discountLine(stack));
    }
}
