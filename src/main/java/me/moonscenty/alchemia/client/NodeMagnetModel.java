package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import me.moonscenty.alchemia.client.legacy.LegacyAssets;
import me.moonscenty.alchemia.client.legacy.LegacyModels;
import me.moonscenty.alchemia.client.legacy.model.LegacyModelBaker;
import me.moonscenty.alchemia.entity.NodeMagnet;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * The node magnet, which is the original's {@code ModelNodeMagnet} read out of its code: a box with a glass dome and a
 * crystal at the back and a magnet at the front, on a tripod. The box turns to face the node it is pulling, and the
 * magnet's arms spin while it pulls.
 */
public class NodeMagnetModel extends EntityModel<NodeMagnet> {
    private static int bakedFrom = -1;
    private static ModelPart base;
    private static ModelPart tripod;
    private static ModelPart[] legs;
    private static ModelPart[] magnet;

    @Override
    public void setupAnim(NodeMagnet magnetEntity, float limbSwing, float limbSwingAmount, float ageInTicks,
            float netHeadYaw, float headPitch) {
        if (!refresh()) {
            return;
        }
        base.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        base.xRot = headPitch * Mth.DEG_TO_RAD;
        for (ModelPart arm : magnet) {
            arm.zRot = magnetEntity.turned * Mth.DEG_TO_RAD;
        }
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay, int colour) {
        if (!refresh()) {
            return;
        }
        tripod.render(pose, buffer, light, overlay, colour);
        for (ModelPart leg : legs) {
            leg.render(pose, buffer, light, overlay, colour);
        }
        base.render(pose, buffer, light, overlay, colour);
    }

    private static boolean refresh() {
        int generation = LegacyModels.generation();
        if (generation != bakedFrom) {
            bakedFrom = generation;
            base = null;
            LegacyModels.get(LegacyAssets.NODE_MAGNET).ifPresent(model -> {
                base = LegacyModelBaker.field(model, "base");
                tripod = LegacyModelBaker.field(model, "tripod");
                legs = new ModelPart[] {LegacyModelBaker.field(model, "leg1"), LegacyModelBaker.field(model, "leg2"),
                        LegacyModelBaker.field(model, "leg3"), LegacyModelBaker.field(model, "leg4")};
                magnet = new ModelPart[] {base.getChild("magbase"), base.getChild("magcross"),
                        base.getChild("magl"), base.getChild("magr")};
            });
        }
        return base != null;
    }
}
