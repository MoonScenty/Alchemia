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

public class FortressExtensions implements IClientItemExtensions {
    private static final List<String> FROM_TWO = List.of("Book", "flapL", "flapR", "ShoulderplateLtop",
            "ShoulderplateL1", "ShoulderplateRtop", "ShoulderplateR1", "SidepanelR2", "SidepanelL2");
    private static final List<String> FROM_THREE = List.of("Scroll", "OrnamentL", "OrnamentL2", "OrnamentR",
            "OrnamentR2", "Gemornament", "Gem", "ShoulderplateL2", "ShoulderplateL3", "ShoulderplateR2",
            "ShoulderplateR3", "SidepanelR3", "SidepanelL3");
    private static final List<String> HELM_WORK = List.of("Goggles", "Mask_0", "Mask_1", "Mask_2");

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
        int pieces = worn(wearer);
        FROM_TWO.forEach(name -> drawn.show(name, pieces >= 2));
        FROM_THREE.forEach(name -> drawn.show(name, pieces >= 3));
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
