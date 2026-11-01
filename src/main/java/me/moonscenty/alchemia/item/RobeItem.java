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
import net.minecraft.world.item.component.DyedItemColor;

public class RobeItem extends ArmorItem implements VisDiscount {
    // original's undyed colour
    public static final int UNDYED = 0x6A3880;

    private final int discount;
    private final boolean drab;

    public RobeItem(Holder<ArmorMaterial> material, Type type, int discount, boolean drab, Properties properties) {
        super(material, type, properties.rarity(Rarity.UNCOMMON));
        this.discount = discount;
        this.drab = drab;
    }

    @Override
    public int visDiscount(ItemStack stack, Player wearer) {
        return discount;
    }

    public int discount() {
        return discount;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(discountLine(stack));
    }

    public boolean drab() {
        return drab;
    }

    public static int dyed(ItemStack stack) {
        return DyedItemColor.getOrDefault(stack, UNDYED);
    }
}
