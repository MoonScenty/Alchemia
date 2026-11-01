package me.moonscenty.alchemia.client.armour;

import me.moonscenty.alchemia.item.RobeItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

/**
 * How the robes are drawn.
 *
 * <p>Both take the original's violet until somebody dyes them. The cloth robe is plain armour, as the original drew
 * it. The void robe is drawn on the original's own model when the jar is there -- two of them, in fact: the original
 * built the model twice, and what it hung on the body depended on which. The one made for the robe itself carries a
 * breastplate, a scroll and a book; the one made for the hood and the leggings carries the skirt instead.
 *
 * <p>The skirt swings with the legs. Its front and back panels turn with the stride by the original's sums.
 */
public class RobeExtensions implements IClientItemExtensions {
    /** The cloth robe: plain armour in the original's violet. */
    public static final RobeExtensions CLOTH = new RobeExtensions(false);
    /** The void robe: the original's model when there is one. */
    public static final RobeExtensions VOID = new RobeExtensions(true);

    private final boolean modelled;

    private RobeExtensions(boolean modelled) {
        this.modelled = modelled;
    }

    @Override
    public int getDefaultDyeColor(ItemStack stack) {
        return FastColor.ARGB32.opaque(RobeItem.dyed(stack));
    }

    // the void robe is drawn exactly as the cult's robe is: same two models, same skirt
    @Override
    public HumanoidModel<?> getHumanoidArmorModel(LivingEntity wearer, ItemStack stack, EquipmentSlot slot,
            HumanoidModel<?> flat) {
        if (!modelled) {
            return flat;
        }
        return CultistArmourExtensions.ROBE.getHumanoidArmorModel(wearer, stack, slot, flat);
    }

    @Override
    public Model getGenericArmorModel(LivingEntity wearer, ItemStack stack, EquipmentSlot slot,
            HumanoidModel<?> flat) {
        return getHumanoidArmorModel(wearer, stack, slot, flat);
    }

    @Override
    public void setupModelAnimations(LivingEntity wearer, ItemStack stack, EquipmentSlot slot, Model model,
            float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw,
            float headPitch) {
        CultistArmourExtensions.ROBE.setupModelAnimations(wearer, stack, slot, model, limbSwing, limbSwingAmount,
                partialTick, ageInTicks, netHeadYaw, headPitch);
    }
}
