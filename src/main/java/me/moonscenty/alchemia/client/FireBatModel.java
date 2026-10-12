package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import me.moonscenty.alchemia.client.legacy.LegacyAssets;
import me.moonscenty.alchemia.client.legacy.LegacyModels;
import me.moonscenty.alchemia.client.legacy.model.LegacyModelBaker;
import me.moonscenty.alchemia.entity.FireBat;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * The firebat's body, which is the original's {@code ModelFireBat} read out of its code: a head with two ears, a
 * body, and two wings in two pieces each. The old bat's shape, before the game drew bats differently.
 * <p>
 * How it moves is the original's too: hanging, it is turned over with its wings folded about it; flying, its body
 * rocks slowly and its wings beat.
 */
public class FireBatModel extends EntityModel<FireBat> {
    /** The original's parts, as of the import {@link #bakedFrom} names. */
    private static int bakedFrom = -1;
    private static ModelPart head;
    private static ModelPart body;
    private static ModelPart rightWing;
    private static ModelPart leftWing;
    private static ModelPart rightTip;
    private static ModelPart leftTip;

    @Override
    public void setupAnim(FireBat bat, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch) {
        if (!refresh()) {
            return;
        }
        head.xRot = headPitch * Mth.DEG_TO_RAD;
        if (bat.isHanging()) {
            head.yRot = Mth.PI - netHeadYaw * Mth.DEG_TO_RAD;
            head.zRot = Mth.PI;
            head.setPos(0.0F, -2.0F, 0.0F);
            rightWing.setPos(-3.0F, 0.0F, 3.0F);
            leftWing.setPos(3.0F, 0.0F, 3.0F);
            body.xRot = Mth.PI;
            rightWing.xRot = -Mth.PI / 20.0F;
            rightWing.yRot = -Mth.PI * 0.4F;
            rightTip.yRot = -Mth.PI * 0.55F;
            leftWing.xRot = rightWing.xRot;
            leftWing.yRot = -rightWing.yRot;
            leftTip.yRot = -rightTip.yRot;
        } else {
            head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
            head.zRot = 0.0F;
            head.setPos(0.0F, 0.0F, 0.0F);
            rightWing.setPos(0.0F, 0.0F, 0.0F);
            leftWing.setPos(0.0F, 0.0F, 0.0F);
            body.xRot = Mth.PI / 4.0F + Mth.cos(ageInTicks * 0.1F) * 0.15F;
            body.yRot = 0.0F;
            rightWing.yRot = Mth.cos(ageInTicks * 1.3F) * Mth.PI * 0.25F;
            leftWing.yRot = -rightWing.yRot;
            rightTip.yRot = rightWing.yRot * 0.5F;
            leftTip.yRot = -rightWing.yRot * 0.5F;
        }
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay, int colour) {
        if (!refresh()) {
            return;
        }
        head.render(pose, buffer, light, overlay, colour);
        body.render(pose, buffer, light, overlay, colour);
    }

    /** Bakes the original's parts again whenever the import has changed what there is. */
    private static boolean refresh() {
        int generation = LegacyModels.generation();
        if (generation != bakedFrom) {
            bakedFrom = generation;
            var model = LegacyModels.get(LegacyAssets.FIREBAT);
            head = model.map(read -> LegacyModelBaker.field(read, "batHead")).orElse(null);
            body = model.map(read -> LegacyModelBaker.field(read, "batBody")).orElse(null);
            if (body != null) {
                rightWing = body.getChild("batRightWing");
                leftWing = body.getChild("batLeftWing");
                rightTip = rightWing.getChild("batOuterRightWing");
                leftTip = leftWing.getChild("batOuterLeftWing");
            }
        }
        return head != null && body != null;
    }
}
