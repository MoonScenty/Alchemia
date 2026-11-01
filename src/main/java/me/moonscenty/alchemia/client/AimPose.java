package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

final class AimPose {
    private static final float RATTLES = 500.0F;

    private AimPose() {
    }

    static void shake(PoseStack pose, LivingEntity entity) {
        if (entity.hurtTime > 0) {
            float shake = entity.hurtTime / RATTLES;
            pose.translate((float) (entity.getRandom().nextGaussian() * shake),
                    (float) (entity.getRandom().nextGaussian() * shake),
                    (float) (entity.getRandom().nextGaussian() * shake));
        }
    }

    static void aim(PoseStack pose, LivingEntity entity, float partial) {
        pose.mulPose(Axis.YN.rotationDegrees(Mth.rotLerp(partial, entity.yHeadRotO, entity.yHeadRot)));
        pose.mulPose(Axis.XP.rotationDegrees(Mth.lerp(partial, entity.xRotO, entity.getXRot())));
    }
}
