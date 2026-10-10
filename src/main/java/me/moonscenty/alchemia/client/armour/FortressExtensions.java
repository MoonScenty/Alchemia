package me.moonscenty.alchemia.client.armour;

import java.util.List;
import java.util.Optional;

import me.moonscenty.alchemia.client.legacy.LegacyAssets;
import me.moonscenty.alchemia.item.FortressArmorItem;
import me.moonscenty.alchemia.item.HelmFitting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
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
    /** Worked into a helm on the altar. None of them is worn until one has been. */
    private static final List<String> HELM_WORK = List.of("Goggles", "Mask_0", "Mask_1", "Mask_2");
    /** The original drew the head a hair larger, so the helm sits over a hat rather than in it. */
    static final float HELM_SCALE = 1.01F;

    public static final FortressExtensions INSTANCE = new FortressExtensions();

    private final LegacyArmourModel model = new LegacyArmourModel(LegacyAssets.FORTRESS_ARMOUR);

    private FortressExtensions() {
    }

    @Override
    public HumanoidModel<?> getHumanoidArmorModel(LivingEntity wearer, ItemStack stack, EquipmentSlot slot,
            HumanoidModel<?> flat) {
        Optional<LegacyArmourModel.Worn> fortress = model.get();
        if (fortress.isEmpty()) {
            return flat;
        }
        LegacyArmourModel.Worn drawn = LegacyArmourModel.dress(flat, fortress.get());
        drawn.head.xScale *= HELM_SCALE;
        drawn.head.yScale *= HELM_SCALE;
        drawn.head.zScale *= HELM_SCALE;
        int pieces = worn(wearer);
        FROM_TWO.forEach(name -> drawn.show(name, pieces >= 2));
        FROM_THREE.forEach(name -> drawn.show(name, pieces >= 3));
        // only what was worked into this very helm, and only on the helm. A cuirass has no face to put it on
        String fitted = slot == EquipmentSlot.HEAD
                ? FortressArmorItem.fitting(stack).map(HelmFitting::part).orElse("")
                : "";
        HELM_WORK.forEach(name -> drawn.show(name, name.equals(fitted)));
        return drawn;
    }

    @Override
    public Model getGenericArmorModel(LivingEntity wearer, ItemStack stack, EquipmentSlot slot,
            HumanoidModel<?> flat) {
        return getHumanoidArmorModel(wearer, stack, slot, flat);
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
}
