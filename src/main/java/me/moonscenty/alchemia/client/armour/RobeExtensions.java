package me.moonscenty.alchemia.client.armour;

import me.moonscenty.alchemia.item.RobeItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class RobeExtensions implements IClientItemExtensions {
    public static final RobeExtensions CLOTH = new RobeExtensions(false);
    public static final RobeExtensions VOID = new RobeExtensions(true);

    private final boolean modelled;

    private RobeExtensions(boolean modelled) {
        this.modelled = modelled;
    }

    @Override
    public int getDefaultDyeColor(ItemStack stack) {
        return FastColor.ARGB32.opaque(RobeItem.dyed(stack));
    }

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
