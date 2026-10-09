package me.moonscenty.alchemia.client.armour;

import java.util.List;
import java.util.Optional;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.client.legacy.LegacyAssets;
import me.moonscenty.alchemia.client.legacy.LegacyModels;
import me.moonscenty.alchemia.client.legacy.model.LegacyModelBaker;
import me.moonscenty.alchemia.item.FortressArmorItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

/**
 * Draws fortress armour on the original's own model, read out of the player's jar.
 *
 * <p>Without the jar there is no model to read, and the pieces fall back to plain armour stretched over the body
 * off our own sheets.
 *
 * <h3>What grows with the set</h3>
 * The more of the suit is worn, the more of it there is: shoulder plates, side flaps and a book on the back from two
 * pieces, the scroll, the crest, the gem and the lower plates from three. The parts and the counts are the
 * original's. The goggles and the three masks are worked into a helm later; until then none of them show.
 */
public class FortressExtensions implements IClientItemExtensions {
    /** Shown from two pieces worn. */
    private static final List<String> FROM_TWO = List.of("Book", "flapL", "flapR", "ShoulderplateLtop",
            "ShoulderplateL1", "ShoulderplateRtop", "ShoulderplateR1", "SidepanelR2", "SidepanelL2");
    /** Shown from three. */
    private static final List<String> FROM_THREE = List.of("Scroll", "OrnamentL", "OrnamentL2", "OrnamentR",
            "OrnamentR2", "Gemornament", "Gem", "ShoulderplateL2", "ShoulderplateL3", "ShoulderplateR2",
            "ShoulderplateR3", "SidepanelR3", "SidepanelL3");
    /** Worked into a helm, not worn by default. */
    private static final List<String> HELM_WORK = List.of("Goggles", "Mask_0", "Mask_1", "Mask_2");
    /** The original drew the head a hair larger, so the helm sits over a hat rather than in it. */
    private static final float HELM_SCALE = 1.01F;

    public static final FortressExtensions INSTANCE = new FortressExtensions();

    private Fortress model;
    private int bakedFrom = -1;

    private FortressExtensions() {
    }

    @Override
    public HumanoidModel<?> getHumanoidArmorModel(LivingEntity wearer, ItemStack stack, EquipmentSlot slot,
            HumanoidModel<?> flat) {
        Optional<Fortress> fortress = fortress();
        if (fortress.isEmpty()) {
            return flat;
        }
        Fortress drawn = fortress.get();
        copyPose(flat, drawn);
        // the layer has already said which limbs this slot covers, on the flat model
        drawn.head.visible = flat.head.visible;
        drawn.hat.visible = flat.hat.visible;
        drawn.body.visible = flat.body.visible;
        drawn.rightArm.visible = flat.rightArm.visible;
        drawn.leftArm.visible = flat.leftArm.visible;
        drawn.rightLeg.visible = flat.rightLeg.visible;
        drawn.leftLeg.visible = flat.leftLeg.visible;
        drawn.head.xScale *= HELM_SCALE;
        drawn.head.yScale *= HELM_SCALE;
        drawn.head.zScale *= HELM_SCALE;
        drawn.dress(worn(wearer));
        return drawn;
    }

    @Override
    public Model getGenericArmorModel(LivingEntity wearer, ItemStack stack, EquipmentSlot slot,
            HumanoidModel<?> flat) {
        return getHumanoidArmorModel(wearer, stack, slot, flat);
    }

    /** The model as last read, baked again whenever the jar was read again. */
    private Optional<Fortress> fortress() {
        if (bakedFrom != LegacyModels.generation()) {
            bakedFrom = LegacyModels.generation();
            try {
                model = LegacyModels.get(LegacyAssets.FORTRESS_ARMOUR)
                        .map(read -> new Fortress(LegacyModelBaker.humanoid(read)))
                        .orElse(null);
            } catch (RuntimeException e) {
                // a model that will not bake is no reason to stop drawing the wearer; plain armour it is
                Alchemia.LOGGER.warn("Could not bake the original's fortress armour", e);
                model = null;
            }
        }
        return Optional.ofNullable(model);
    }

    private static int worn(LivingEntity wearer) {
        int pieces = 0;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.isArmor() && wearer.getItemBySlot(slot).getItem() instanceof FortressArmorItem) {
                pieces++;
            }
        }
        return pieces;
    }

    @SuppressWarnings("unchecked")
    private static void copyPose(HumanoidModel<?> from, HumanoidModel<LivingEntity> onto) {
        ((HumanoidModel<LivingEntity>) from).copyPropertiesTo(onto);
    }

    /** The original's model, and its parts by the names the original gave them. */
    private static final class Fortress extends HumanoidModel<LivingEntity> {
        private final LegacyModelBaker.Baked baked;

        Fortress(LegacyModelBaker.Baked baked) {
            super(baked.root());
            this.baked = baked;
        }

        void dress(int pieces) {
            FROM_TWO.forEach(name -> show(name, pieces >= 2));
            FROM_THREE.forEach(name -> show(name, pieces >= 3));
            HELM_WORK.forEach(name -> show(name, false));
        }

        private void show(String name, boolean shown) {
            ModelPart part = baked.parts().get(name);
            if (part != null) {
                part.visible = shown;
            }
        }
    }
}
