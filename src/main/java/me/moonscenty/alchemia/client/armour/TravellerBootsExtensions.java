package me.moonscenty.alchemia.client.armour;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

/**
 * Tells the game to draw the boots as a model rather than stretch a sheet over the leg.
 * <p>
 * Built once and kept, because the layer that draws armour asks for this on every frame of every player wearing
 * a pair and baking a model thirty times a second would be thirty models a second.
 */
public final class TravellerBootsExtensions implements IClientItemExtensions {
    public static final TravellerBootsExtensions INSTANCE = new TravellerBootsExtensions();

    private HumanoidModel<LivingEntity> boots;

    private TravellerBootsExtensions() {
    }

    @Override
    public HumanoidModel<?> getHumanoidArmorModel(LivingEntity wearer, ItemStack stack, EquipmentSlot slot,
            HumanoidModel<?> flat) {
        if (boots == null) {
            boots = new TravellerBootsModel(
                    Minecraft.getInstance().getEntityModels().bakeLayer(TravellerBootsModel.LAYER));
        }
        // the flat model has been posed for this frame already; the shapes differ but the joints do not
        copyPose(flat, boots);
        return boots;
    }

    /**
     * The pose the flat model was given this frame, put on ours.
     * <p>
     * Written out rather than handed to the game's own copier, which will only copy between two models of the
     * same kind of wearer and cannot be told that both of these draw anything alive.
     */
    @SuppressWarnings("unchecked")
    private static void copyPose(HumanoidModel<?> from, HumanoidModel<LivingEntity> onto) {
        ((HumanoidModel<LivingEntity>) from).copyPropertiesTo(onto);
    }

    @Override
    public Model getGenericArmorModel(LivingEntity wearer, ItemStack stack, EquipmentSlot slot,
            HumanoidModel<?> flat) {
        return getHumanoidArmorModel(wearer, stack, slot, flat);
    }
}
