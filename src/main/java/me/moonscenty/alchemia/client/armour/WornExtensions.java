package me.moonscenty.alchemia.client.armour;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

/**
 * Tells the game to draw a piece of armour as a model rather than stretch a sheet over the body.
 * <p>
 * One of these to a piece. The model is built the first time it is wanted and then kept, because the layer that
 * draws armour asks for it on every frame of every wearer, and baking a model thirty times a second would be
 * thirty models a second.
 */
public class WornExtensions implements IClientItemExtensions {
    private final ModelLayerLocation layer;
    private HumanoidModel<LivingEntity> worn;

    public WornExtensions(ModelLayerLocation layer) {
        this.layer = layer;
    }

    @Override
    public HumanoidModel<?> getHumanoidArmorModel(LivingEntity wearer, ItemStack stack, EquipmentSlot slot,
            HumanoidModel<?> flat) {
        if (worn == null) {
            worn = new WornModel(Minecraft.getInstance().getEntityModels().bakeLayer(layer));
        }
        // the flat model has been posed for this frame already; the shapes differ but the joints do not
        copyPose(flat, worn);
        return worn;
    }

    @Override
    public Model getGenericArmorModel(LivingEntity wearer, ItemStack stack, EquipmentSlot slot,
            HumanoidModel<?> flat) {
        return getHumanoidArmorModel(wearer, stack, slot, flat);
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
}
