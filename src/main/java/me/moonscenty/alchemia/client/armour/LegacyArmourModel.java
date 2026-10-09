package me.moonscenty.alchemia.client.armour;

import java.util.Optional;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.client.legacy.LegacyModels;
import me.moonscenty.alchemia.client.legacy.model.LegacyModelBaker;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;

/**
 * Armour drawn on a model read out of the original's code, baked when it is first wanted and again whenever the jar
 * has been read again.
 *
 * <p>Empty when there is no jar or the model would not read, and also when it would not bake: a model that fails is
 * no reason to stop drawing the wearer, and the caller falls back to plain armour.
 */
final class LegacyArmourModel {
    private final String key;
    private Worn model;
    private int bakedFrom = -1;

    LegacyArmourModel(String key) {
        this.key = key;
    }

    Optional<Worn> get() {
        if (bakedFrom != LegacyModels.generation()) {
            bakedFrom = LegacyModels.generation();
            try {
                model = LegacyModels.get(key).map(read -> new Worn(LegacyModelBaker.humanoid(read))).orElse(null);
            } catch (RuntimeException e) {
                Alchemia.LOGGER.warn("Could not bake the original's {} model", key, e);
                model = null;
            }
        }
        return Optional.ofNullable(model);
    }

    /**
     * Puts a model in the pose and with the limbs the armour layer gave the flat model this frame.
     * <p>
     * The pose is copied by the game's own copier, which also copies scale. Which limbs a slot covers is not: the
     * layer sets that on the flat model only, so it is copied here by hand.
     */
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
        return onto;
    }

    /** A humanoid model on the original's parts, which can be found by the names the original gave them. */
    static final class Worn extends HumanoidModel<LivingEntity> {
        private final LegacyModelBaker.Baked baked;

        Worn(LegacyModelBaker.Baked baked) {
            super(baked.root());
            this.baked = baked;
        }

        /** Empty when this release of the model has no part by that name. */
        Optional<ModelPart> part(String name) {
            return Optional.ofNullable(baked.parts().get(name));
        }

        void show(String name, boolean shown) {
            part(name).ifPresent(part -> part.visible = shown);
        }
    }
}
