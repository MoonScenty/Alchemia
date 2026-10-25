package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import me.moonscenty.alchemia.client.legacy.LegacyAssets;
import me.moonscenty.alchemia.client.legacy.LegacyModels;
import me.moonscenty.alchemia.client.legacy.model.LegacyModelBaker;
import me.moonscenty.alchemia.entity.AutomatedCrossbow;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * The automated crossbow, which is the original's {@code ModelCrossbow} read out of its code: a crossbow with its
 * ammunition box on top, on a tripod of four legs. The bow turns and tilts to aim, its arms snap forward as it shoots,
 * and the loading bars over the box swing back as it reloads.
 */
public class AutomatedCrossbowModel extends EntityModel<AutomatedCrossbow> {
    private static int bakedFrom = -1;
    private static ModelPart crossbow;
    private static ModelPart[] legs;
    private static ModelPart tripod;
    private static ModelPart[] leftArm;
    private static ModelPart[] rightArm;
    private static ModelPart[] loader;

    /** How far through the reload's winding it is, caught where the partial tick is known. */
    private float winding;

    @Override
    public void prepareMobModel(AutomatedCrossbow turret, float limbSwing, float limbSwingAmount, float partial) {
        winding = turret.winding(partial);
    }

    @Override
    public void setupAnim(AutomatedCrossbow turret, float limbSwing, float limbSwingAmount, float ageInTicks,
            float netHeadYaw, float headPitch) {
        if (!refresh()) {
            return;
        }
        crossbow.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        crossbow.xRot = headPitch * Mth.DEG_TO_RAD;
        float snap = Mth.sin(Mth.sqrt(attackTime) * Mth.TWO_PI) * 0.2F;
        for (ModelPart arm : leftArm) {
            arm.yRot = -0.2F + snap;
        }
        for (ModelPart arm : rightArm) {
            arm.yRot = 0.2F - snap;
        }
        float swing = -0.5F + Mth.sin(Mth.sqrt(winding) * Mth.TWO_PI) * 0.5F;
        for (ModelPart bar : loader) {
            bar.xRot = swing;
        }
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay, int colour) {
        if (!refresh()) {
            return;
        }
        crossbow.render(pose, buffer, light, overlay, colour);
        tripod.render(pose, buffer, light, overlay, colour);
        for (ModelPart leg : legs) {
            leg.render(pose, buffer, light, overlay, colour);
        }
    }

    /** Bakes the original's parts again whenever the import has changed what there is. */
    private static boolean refresh() {
        int generation = LegacyModels.generation();
        if (generation != bakedFrom) {
            bakedFrom = generation;
            crossbow = null;
            LegacyModels.get(LegacyAssets.CROSSBOW).ifPresent(model -> {
                crossbow = LegacyModelBaker.field(model, "crossbow");
                tripod = LegacyModelBaker.field(model, "tripod");
                legs = new ModelPart[] {LegacyModelBaker.field(model, "leg1"), LegacyModelBaker.field(model, "leg2"),
                        LegacyModelBaker.field(model, "leg3"), LegacyModelBaker.field(model, "leg4")};
                leftArm = new ModelPart[] {crossbow.getChild("crossl1"), crossbow.getChild("crossl2"),
                        crossbow.getChild("crossl3")};
                rightArm = new ModelPart[] {crossbow.getChild("crossr1"), crossbow.getChild("crossr2"),
                        crossbow.getChild("crossr3")};
                loader = new ModelPart[] {crossbow.getChild("loadbarcross"), crossbow.getChild("loadbarl"),
                        crossbow.getChild("loadbarr")};
            });
        }
        return crossbow != null;
    }
}
