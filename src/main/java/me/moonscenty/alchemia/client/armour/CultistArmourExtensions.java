package me.moonscenty.alchemia.client.armour;

import java.util.List;
import java.util.Optional;

import me.moonscenty.alchemia.client.legacy.LegacyAssets;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

/**
 * How the Crimson Cult's robe and plate are drawn: each on the original's own model, which the original built twice,
 * once for the piece on the chest and once for the hood or helm and the legs. The robe hangs a skirt and the plate a
 * cloak and a tabard, and both swing with the stride by the original's sums. The boots are plain armour and are not
 * drawn here.
 */
public class CultistArmourExtensions implements IClientItemExtensions {
    public static final CultistArmourExtensions ROBE = new CultistArmourExtensions(LegacyAssets.ROBE,
            LegacyAssets.ROBE_SKIRT, false);
    public static final CultistArmourExtensions PLATE = new CultistArmourExtensions(LegacyAssets.KNIGHT_ARMOUR,
            LegacyAssets.KNIGHT_ARMOUR_LOWER, true);
    public static final CultistArmourExtensions PRAETOR = new CultistArmourExtensions(LegacyAssets.LEADER_ARMOUR,
            LegacyAssets.LEADER_ARMOUR_LOWER, true);

    private static final float STRIDE = 0.6662F;
    private static final float SWING = 1.4F;

    private final LegacyArmourModel chest;
    private final LegacyArmourModel rest;
    private final boolean plate;

    private CultistArmourExtensions(String chest, String rest, boolean plate) {
        this.chest = new LegacyArmourModel(chest);
        this.rest = new LegacyArmourModel(rest);
        this.plate = plate;
    }

    @Override
    public HumanoidModel<?> getHumanoidArmorModel(LivingEntity wearer, ItemStack stack, EquipmentSlot slot,
            HumanoidModel<?> flat) {
        Optional<LegacyArmourModel.Worn> found = (slot == EquipmentSlot.CHEST ? chest : rest).get();
        if (found.isEmpty()) {
            return flat;
        }
        LegacyArmourModel.Worn drawn = LegacyArmourModel.dress(flat, found.get());
        drawn.head.xScale *= FortressExtensions.HELM_SCALE;
        drawn.head.yScale *= FortressExtensions.HELM_SCALE;
        drawn.head.zScale *= FortressExtensions.HELM_SCALE;
        return drawn;
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
        if (!(model instanceof LegacyArmourModel.Worn drawn)) {
            return;
        }
        // whichever leg is further back holds the cloth out behind it
        float swing = Math.min(Mth.cos(limbSwing * STRIDE) * SWING * limbSwingAmount,
                Mth.cos(limbSwing * STRIDE + Mth.PI) * SWING * limbSwingAmount);
        if (plate) {
            turn(drawn, List.of("Frontcloth1", "LegClothR", "LegClothL"), swing - 0.1047198F);
            turn(drawn, List.of("Frontcloth2"), swing - 0.3316126F);
            turn(drawn, List.of("Cloak1"), -swing / 2.0F + 0.1396263F);
            turn(drawn, List.of("Cloak2"), -swing / 2.0F + 0.3069452F);
            turn(drawn, List.of("Cloak3"), -swing / 2.0F + 0.4465716F);
        } else {
            turn(drawn, List.of("FrontclothR1", "FrontclothL1"), swing - 0.1047198F);
            turn(drawn, List.of("FrontclothR2", "FrontclothL2"), swing - 0.3316126F);
            turn(drawn, List.of("ClothBackR1", "ClothBackL1"), -swing + 0.1047198F);
            turn(drawn, List.of("ClothBackR2", "ClothBackL2", "ClothBackR3", "ClothBackL3"), -swing + 0.2268928F);
        }
    }

    private static void turn(LegacyArmourModel.Worn drawn, List<String> names, float xRot) {
        names.forEach(name -> drawn.part(name).ifPresent(part -> part.xRot = xRot));
    }
}
