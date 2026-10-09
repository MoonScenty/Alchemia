package me.moonscenty.alchemia.client.armour;

import java.util.List;
import java.util.Optional;

import me.moonscenty.alchemia.client.legacy.LegacyAssets;
import me.moonscenty.alchemia.item.RobeItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
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

    /** How fast a stride swings, and how far. The game's own walking sums, which the original reused. */
    private static final float STRIDE = 0.6662F;
    private static final float SWING = 1.4F;
    /** How far each panel of the skirt hangs out from straight down, at rest. */
    private static final float FRONT_UPPER = 0.1047198F;
    private static final float FRONT_LOWER = 0.3316126F;
    private static final float BACK_UPPER = 0.1047198F;
    private static final float BACK_LOWER = 0.2268928F;

    private static final List<String> FRONT_UPPERS = List.of("FrontclothR1", "FrontclothL1");
    private static final List<String> FRONT_LOWERS = List.of("FrontclothR2", "FrontclothL2");
    private static final List<String> BACK_UPPERS = List.of("ClothBackR1", "ClothBackL1");
    private static final List<String> BACK_LOWERS = List.of("ClothBackR2", "ClothBackL2", "ClothBackR3",
            "ClothBackL3");

    private final boolean modelled;
    private final LegacyArmourModel robe = new LegacyArmourModel(LegacyAssets.ROBE);
    private final LegacyArmourModel skirt = new LegacyArmourModel(LegacyAssets.ROBE_SKIRT);

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
        // the original made the robe's own piece on one model and the hood and leggings on the other
        Optional<LegacyArmourModel.Worn> found = (slot == EquipmentSlot.CHEST ? robe : skirt).get();
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
        turn(drawn, FRONT_UPPERS, swing - FRONT_UPPER);
        turn(drawn, FRONT_LOWERS, swing - FRONT_LOWER);
        turn(drawn, BACK_UPPERS, -swing + BACK_UPPER);
        turn(drawn, BACK_LOWERS, -swing + BACK_LOWER);
    }

    private static void turn(LegacyArmourModel.Worn drawn, List<String> names, float xRot) {
        names.forEach(name -> drawn.part(name).ifPresent(part -> part.xRot = xRot));
    }
}
