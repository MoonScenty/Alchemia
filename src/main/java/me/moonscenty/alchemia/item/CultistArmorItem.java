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
 * What the Crimson Cult wears. Iron to the last number, and nobody makes it: it comes off a cultist or not at all.
 * <p>
 * The robe and the boots are a cultist's own and carry a little of the cult with them: a point of warp each while
 * worn, and a wand spends a hundredth less. The plate is a knight's and is only plate.
 */
public class CultistArmorItem extends ArmorItem implements WarpingGear, VisDiscount {
    /** Which of the three the piece belongs to. */
    public enum Kind {
        ROBE(1),
        PLATE(0),
        BOOTS(1);

        /** Warp carried, and parts of a hundred off a wand's bill. The original's: the same for both. */
        private final int mark;

        Kind(int mark) {
            this.mark = mark;
        }
    }

    private final Kind kind;

    public CultistArmorItem(Holder<ArmorMaterial> material, Type type, Kind kind, Properties properties) {
        super(material, type, properties.rarity(Rarity.UNCOMMON));
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }

    @Override
    public int warp(ItemStack stack, Player holder) {
        return kind.mark;
    }

    @Override
    public int visDiscount(ItemStack stack, Player wearer) {
        return kind.mark;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        if (kind.mark > 0) {
            lines.add(Component.translatable("item.alchemia.vis_discount", kind.mark).withStyle(ChatFormatting.AQUA));
        }
    }
}
