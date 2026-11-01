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

public class CultistArmorItem extends ArmorItem implements WarpingGear, VisDiscount {
    public enum Kind {
        ROBE(1),
        PLATE(0),
        PRAETOR(0),
        BOOTS(1);

        // warp carried and vis discount percent; original's value
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
    public net.minecraft.resources.ResourceLocation getArmorTexture(ItemStack stack, net.minecraft.world.entity.Entity entity,
            net.minecraft.world.entity.EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
        if (kind == Kind.PLATE && entity instanceof me.moonscenty.alchemia.entity.InhabitedZombie) {
            return me.moonscenty.alchemia.Alchemia.id("textures/models/armor/crimson_plate_husk.png");
        }
        return null;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        if (kind.mark > 0) {
            lines.add(discountLine(stack));
        }
    }
}
