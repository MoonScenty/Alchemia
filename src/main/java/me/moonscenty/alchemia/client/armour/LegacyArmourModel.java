package me.moonscenty.alchemia.client.armour;

import java.util.List;
import java.util.Optional;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.client.legacy.LegacyCached;
import me.moonscenty.alchemia.client.legacy.LegacyModels;
import me.moonscenty.alchemia.client.legacy.model.LegacyModelBaker;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

final class LegacyArmourModel {
    static final float HELM_SCALE = 1.01F;
    private static final float STRIDE = 0.6662F;
    private static final float SWING = 1.4F;

    private final LegacyCached<Worn> model;

    LegacyArmourModel(String key) {
        this.model = new LegacyCached<>(() -> {
            try {
                return LegacyModels.get(key).map(read -> new Worn(LegacyModelBaker.humanoid(read))).orElse(null);
            } catch (RuntimeException e) {
                Alchemia.LOGGER.warn("Could not bake the original's {} model", key, e);
                return null;
            }
        });
    }

    Optional<Worn> get() {
        return Optional.ofNullable(model.get());
    }

    // copyPropertiesTo skips limb visibility, which the armour layer sets on the flat model only
    @SuppressWarnings("unchecked")
    static <M extends HumanoidModel<LivingEntity>> M dress(HumanoidModel<?> flat, M onto) {
        ((HumanoidModel<LivingEntity>) flat).copyPropertiesTo(onto);
        onto.head.visible = flat.head.visible;
        onto.hat.visible = flat.hat.visible;
        onto.body.visible = flat.body.visible;
        onto.rightArm.visible = flat.rightArm.visible;
        onto.leftArm.visible = flat.leftArm.visible;
        onto.rightLeg.visible = flat.rightLeg.visible;
        onto.leftLeg.visible = flat.leftLeg.visible;
        onto.head.xScale *= HELM_SCALE;
        onto.head.yScale *= HELM_SCALE;
        onto.head.zScale *= HELM_SCALE;
        return onto;
    }

    static float swing(float limbSwing, float limbSwingAmount) {
        return Math.min(Mth.cos(limbSwing * STRIDE) * SWING * limbSwingAmount,
                Mth.cos(limbSwing * STRIDE + Mth.PI) * SWING * limbSwingAmount);
    }

    static void turn(Worn drawn, List<String> names, float xRot) {
        names.forEach(name -> drawn.part(name).ifPresent(part -> part.xRot = xRot));
    }

    static final class Worn extends HumanoidModel<LivingEntity> {
        private final LegacyModelBaker.Baked baked;

        Worn(LegacyModelBaker.Baked baked) {
            super(baked.root());
            this.baked = baked;
        }

        Optional<ModelPart> part(String name) {
            return Optional.ofNullable(baked.parts().get(name));
        }

        void show(String name, boolean shown) {
            part(name).ifPresent(part -> part.visible = shown);
        }
    }
}
