package me.moonscenty.alchemia.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import me.moonscenty.alchemia.client.legacy.LegacyAssets;
import me.moonscenty.alchemia.client.legacy.LegacyModels;
import me.moonscenty.alchemia.client.legacy.model.LegacyModelBaker;
import me.moonscenty.alchemia.entity.EldritchCrab;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * The crab's body, the original's {@code ModelEldritchCrab} read out of its code: a shelled tail, a torso, two heads,
 * two claws of three joints and eight legs of two. The tail wears the crimson shell while the crab has one and goes
 * bare once it has broken.
 * <p>
 * How it moves is the original's: the legs scuttle in two sets, the tail sways with the stride, and the claws snap
 * idly whatever it is doing.
 */
public class EldritchCrabModel extends EntityModel<EldritchCrab> {
    private static final List<String> REST = List.of("RFLeg1", "RClaw1", "Head1", "RClaw0", "RClaw2", "LClaw2",
            "LClaw1", "RArm", "Torso", "RRLeg1", "Head0", "LRLeg1", "LFLeg1", "RRLeg0", "RFLeg0", "LFLeg0", "LRLeg0",
            "LClaw0", "LArm");
    private static final float LEG_TURN = 0.2094395F;
    private static final float LEG_LIFT = 0.4363323F;
    private static final float CLAW = 0.3141593F;

    private static int bakedFrom = -1;
    private static Map<String, ModelPart> parts = Map.of();
    private boolean helmed;

    @Override
    public void setupAnim(EldritchCrab crab, float limbSwing, float limbSwingAmount, float ageInTicks,
            float netHeadYaw, float headPitch) {
        if (!refresh()) {
            return;
        }
        helmed = crab.hasHelm();
        for (String leg : List.of("RRLeg1", "RRLeg0")) {
            pose(leg, LEG_TURN, LEG_LIFT);
        }
        for (String leg : List.of("RFLeg1", "RFLeg0")) {
            pose(leg, -LEG_TURN, LEG_LIFT);
        }
        for (String leg : List.of("LRLeg1", "LRLeg0")) {
            pose(leg, -LEG_TURN, -LEG_LIFT);
        }
        for (String leg : List.of("LFLeg1", "LFLeg0")) {
            pose(leg, LEG_TURN, -LEG_LIFT);
        }
        float rear = -(Mth.cos(limbSwing * 0.6662F * 2.0F) * 0.4F) * limbSwingAmount;
        float front = -(Mth.cos(limbSwing * 0.6662F * 2.0F + Mth.PI) * 0.4F) * limbSwingAmount;
        nudge(List.of("RRLeg1", "RRLeg0"), rear);
        nudge(List.of("LRLeg1", "LRLeg0"), -rear);
        nudge(List.of("RFLeg1", "RFLeg0"), front);
        nudge(List.of("LFLeg1", "LFLeg0"), -front);
        float sway = Mth.cos(limbSwing * 0.6662F);
        for (String tail : List.of("TailHelm", "TailBare")) {
            parts.get(tail).yRot = sway * 2.0F * limbSwingAmount * 0.125F;
            parts.get(tail).zRot = sway * limbSwingAmount * 0.125F;
        }
        parts.get("RClaw2").xRot = CLAW - Mth.sin(ageInTicks / 4.0F) * 0.25F;
        parts.get("LClaw2").xRot = CLAW + Mth.sin(ageInTicks / 4.1F) * 0.25F;
        parts.get("RClaw1").xRot = Mth.sin(ageInTicks / 4.0F) * 0.125F;
        parts.get("LClaw1").xRot = -Mth.sin(ageInTicks / 4.1F) * 0.125F;
    }

    private static void pose(String name, float yRot, float zRot) {
        ModelPart part = parts.get(name);
        part.xRot = 0.0F;
        part.yRot = yRot;
        part.zRot = zRot;
    }

    private static void nudge(List<String> names, float by) {
        for (String name : names) {
            parts.get(name).yRot += by;
            parts.get(name).zRot += by;
        }
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay, int colour) {
        if (!refresh()) {
            return;
        }
        parts.get(helmed ? "TailHelm" : "TailBare").render(pose, buffer, light, overlay, colour);
        for (String name : REST) {
            parts.get(name).render(pose, buffer, light, overlay, colour);
        }
    }

    /** Bakes the original's parts again whenever the import has changed what there is. */
    private static boolean refresh() {
        int generation = LegacyModels.generation();
        if (generation != bakedFrom) {
            bakedFrom = generation;
            Map<String, ModelPart> baked = new HashMap<>();
            LegacyModels.get(LegacyAssets.ELDRITCH_CRAB).ifPresent(model -> {
                for (String name : REST) {
                    baked.put(name, LegacyModelBaker.field(model, name));
                }
                baked.put("TailHelm", LegacyModelBaker.field(model, "TailHelm"));
                baked.put("TailBare", LegacyModelBaker.field(model, "TailBare"));
            });
            parts = baked;
        }
        return !parts.isEmpty();
    }
}
