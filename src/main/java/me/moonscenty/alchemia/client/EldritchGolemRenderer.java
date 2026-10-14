package me.moonscenty.alchemia.client;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.client.legacy.LegacyAssets;
import me.moonscenty.alchemia.client.legacy.LegacyModels;
import me.moonscenty.alchemia.client.legacy.model.LegacyModelBaker;
import me.moonscenty.alchemia.entity.boss.EldritchGolem;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * The Eldritch Construct on the original's own model, twice and a bit the size it was built at: its arms smash down
 * when it strikes and swing as it walks, its cloak and front cloth swing with its stride, its head turns a little
 * with where it looks -- and once it has lost it, the stump turns all the way.
 */
public class EldritchGolemRenderer extends MobRenderer<EldritchGolem, EldritchGolemRenderer.Model> {
    private static final ResourceLocation SKIN = Alchemia.id("textures/entity/eldritch_golem.png");
    private static final float SCALE = 2.15F;

    public EldritchGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new Model(), 0.5F);
    }

    @Override
    protected void scale(EldritchGolem golem, PoseStack pose, float partial) {
        pose.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(EldritchGolem golem) {
        return SKIN;
    }

    public static class Model extends EntityModel<EldritchGolem> {
        private static final List<String> ROOTS = List.of("Frontcloth1", "CollarL", "CollarBlack", "Cloak1",
                "CloakCL", "CloakCR", "Cloak3", "Cloak2", "Head", "Head2", "Frontcloth0", "CollarB", "Torso",
                "CollarR", "CollarF", "ArmL", "ArmR", "BackpanelR1", "WaistR1", "WaistR2", "WaistR3", "LegR",
                "WaistL1", "WaistL2", "WaistL3", "Frontcloth2", "BackpanelL1", "LegL");
        private static int bakedFrom = -1;
        private static Map<String, ModelPart> parts = Map.of();
        private boolean headless;

        @Override
        public void prepareMobModel(EldritchGolem golem, float limbSwing, float limbSwingAmount, float partial) {
            if (!refresh()) {
                return;
            }
            int timer = golem.attackTimer();
            if (timer > 0) {
                float smash = -2.0F + 1.5F * swing(timer - partial, 10.0F);
                parts.get("ArmR").xRot = smash;
                parts.get("ArmL").xRot = smash;
            } else {
                parts.get("ArmR").xRot = Mth.cos(limbSwing * 0.4F) * 2.0F * limbSwingAmount * 0.5F;
                parts.get("ArmL").xRot = Mth.cos(limbSwing * 0.4F + Mth.PI) * 2.0F * limbSwingAmount * 0.5F;
            }
        }

        /** The original's triangle wave. */
        private static float swing(float a, float b) {
            return (Math.abs(a % b - b * 0.5F) - b * 0.25F) / (b * 0.25F);
        }

        @Override
        public void setupAnim(EldritchGolem golem, float limbSwing, float limbSwingAmount, float age, float headYaw,
                float headPitch) {
            if (!refresh()) {
                return;
            }
            headless = golem.isHeadless();
            ModelPart head = parts.get("Head");
            if (golem.spawnTimer() > 0) {
                head.yRot = 0.0F;
                head.xRot = golem.spawnTimer() / 2.0F / 57.3F;
            } else {
                head.yRot = headYaw / 4.0F * Mth.DEG_TO_RAD;
                head.xRot = headPitch / 2.0F * Mth.DEG_TO_RAD;
            }
            ModelPart stump = parts.get("Head2");
            stump.yRot = headYaw * Mth.DEG_TO_RAD;
            stump.xRot = headPitch * Mth.DEG_TO_RAD;
            parts.get("LegR").xRot = Mth.cos(limbSwing * 0.4662F) * 1.4F * limbSwingAmount;
            parts.get("LegL").xRot = Mth.cos(limbSwing * 0.4662F + Mth.PI) * 1.4F * limbSwingAmount;
            float stride = Math.min(Mth.cos(limbSwing * 0.44F) * 1.4F * limbSwingAmount,
                    Mth.cos(limbSwing * 0.44F + Mth.PI) * 1.4F * limbSwingAmount);
            parts.get("Frontcloth1").xRot = stride - 0.1047198F;
            parts.get("Frontcloth2").xRot = stride - 0.3316126F;
            parts.get("Cloak1").xRot = -stride / 3.0F + 0.1396263F;
            parts.get("Cloak2").xRot = -stride / 3.0F + 0.3069452F;
            parts.get("Cloak3").xRot = -stride / 3.0F + 0.4465716F;
        }

        @Override
        public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay, int colour) {
            if (!refresh()) {
                return;
            }
            parts.forEach((name, part) -> {
                if (name.equals("Head") && headless || name.equals("Head2") && !headless) {
                    return;
                }
                part.render(pose, buffer, light, overlay, colour);
            });
        }

        private static boolean refresh() {
            int generation = LegacyModels.generation();
            if (generation != bakedFrom) {
                bakedFrom = generation;
                Map<String, ModelPart> baked = new LinkedHashMap<>();
                LegacyModels.get(LegacyAssets.ELDRITCH_GOLEM).ifPresent(model -> {
                    for (String name : ROOTS) {
                        baked.put(name, LegacyModelBaker.field(model, name));
                    }
                });
                parts = baked;
            }
            return !parts.isEmpty();
        }
    }
}
