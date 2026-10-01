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
public class RobeItem extends ArmorItem implements VisDiscount {
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

    /** The same number without a wearer, for anything that only wants to say what the piece is worth. */
    public int discount() {
        return discount;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("item.alchemia.vis_discount", discount)
                .withStyle(ChatFormatting.AQUA));
    }

    /** Whether this piece wears the dark sheet. The two robes share one set of meshes and differ only in this. */
    public boolean drab() {
        return drab;
    }
}
