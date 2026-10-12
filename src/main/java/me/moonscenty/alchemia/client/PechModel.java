package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import me.moonscenty.alchemia.client.legacy.LegacyAssets;
import me.moonscenty.alchemia.client.legacy.LegacyModels;
import me.moonscenty.alchemia.client.legacy.model.LegacyModelBaker;
import me.moonscenty.alchemia.entity.Pech;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

/**
 * The pech's body, which is the original's {@code ModelPech} read out of its code: a biped's head, body, arms and
 * legs, a jaw that works as it mutters, and a pack in two pieces on its back that sways as it walks.
 * <p>
 * How it moves is the original's, which is mostly a biped's with shorter arms that hang a little out and drift.
 */
public class PechModel extends EntityModel<Pech> implements ArmedModel {
    /** The original's parts, as of the import {@link #bakedFrom} names. */
    private static int bakedFrom = -1;
    private static ModelPart head;
    private static ModelPart body;
    private static ModelPart rightArm;
    private static ModelPart leftArm;
    private static ModelPart rightLeg;
    private static ModelPart leftLeg;
    private static ModelPart jowls;
    private static ModelPart lowerPack;
    private static ModelPart upperPack;

    @Override
    public void setupAnim(Pech pech, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch) {
        if (!refresh()) {
            return;
        }
        float walk = limbSwing * 0.6662F;
        head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        head.xRot = headPitch * Mth.DEG_TO_RAD;
        jowls.yRot = head.yRot;
        jowls.xRot = head.xRot + Mth.PI / 12.0F + Mth.cos(walk) * limbSwingAmount * 0.25F
                + 0.34906587F * Math.abs(Mth.sin(pech.mumble));
        rightArm.xRot = Mth.cos(walk + Mth.PI) * limbSwingAmount;
        leftArm.xRot = Mth.cos(walk) * limbSwingAmount;
        rightArm.zRot = 0.0F;
        leftArm.zRot = 0.0F;
        rightLeg.xRot = Mth.cos(walk) * 1.4F * limbSwingAmount;
        leftLeg.xRot = Mth.cos(walk + Mth.PI) * 1.4F * limbSwingAmount;
        rightLeg.yRot = 0.0F;
        leftLeg.yRot = 0.0F;
        lowerPack.yRot = Mth.cos(walk) * limbSwingAmount * 0.25F;
        lowerPack.zRot = lowerPack.yRot;
        if (riding) {
            rightArm.xRot -= Mth.PI / 5.0F;
            leftArm.xRot -= Mth.PI / 5.0F;
            rightLeg.xRot = -Mth.PI * 2.0F / 5.0F;
            leftLeg.xRot = -Mth.PI * 2.0F / 5.0F;
            rightLeg.yRot = Mth.PI / 10.0F;
            leftLeg.yRot = -Mth.PI / 10.0F;
        }
        rightArm.yRot = 0.0F;
        leftArm.yRot = 0.0F;
        // a swing of the right arm, as a biped's
        float swing = 1.0F - attackTime;
        swing *= swing;
        swing *= swing;
        swing = 1.0F - swing;
        float lift = Mth.sin(swing * Mth.PI);
        float reach = Mth.sin(attackTime * Mth.PI) * -(head.xRot - 0.7F) * 0.75F;
        rightArm.xRot -= lift * 1.2F + reach;
        rightArm.zRot = Mth.sin(attackTime * Mth.PI) * -0.4F;
        if (pech.isCrouching()) {
            rightArm.xRot += 0.4F;
            leftArm.xRot += 0.4F;
        }
        // the arms hang a little out and drift
        float drift = Mth.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
        rightArm.zRot += drift;
        leftArm.zRot -= drift;
        float sway = Mth.sin(ageInTicks * 0.067F) * 0.05F;
        rightArm.xRot += sway;
        leftArm.xRot -= sway;
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay, int colour) {
        if (!refresh()) {
            return;
        }
        for (ModelPart part : new ModelPart[] {body, rightLeg, leftLeg, head, jowls, lowerPack, upperPack, rightArm,
                leftArm}) {
            part.render(pose, buffer, light, overlay, colour);
        }
    }

    @Override
    public void translateToHand(HumanoidArm side, PoseStack pose) {
        if (refresh()) {
            (side == HumanoidArm.RIGHT ? rightArm : leftArm).translateAndRotate(pose);
        }
    }

    /** Bakes the original's parts again whenever the import has changed what there is. */
    private static boolean refresh() {
        int generation = LegacyModels.generation();
        if (generation != bakedFrom) {
            bakedFrom = generation;
            var model = LegacyModels.get(LegacyAssets.PECH);
            var biped = model.map(LegacyModelBaker::humanoid).orElse(null);
            head = biped == null ? null : biped.part("head");
            body = biped == null ? null : biped.part("body");
            rightArm = biped == null ? null : biped.part("right_arm");
            leftArm = biped == null ? null : biped.part("left_arm");
            rightLeg = biped == null ? null : biped.part("right_leg");
            leftLeg = biped == null ? null : biped.part("left_leg");
            jowls = model.map(read -> LegacyModelBaker.field(read, "Jowls")).orElse(null);
            lowerPack = model.map(read -> LegacyModelBaker.field(read, "LowerPack")).orElse(null);
            upperPack = model.map(read -> LegacyModelBaker.field(read, "UpperPack")).orElse(null);
        }
        return head != null && jowls != null && lowerPack != null && upperPack != null;
    }
}
