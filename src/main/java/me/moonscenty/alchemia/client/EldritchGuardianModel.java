package me.moonscenty.alchemia.client;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import me.moonscenty.alchemia.client.legacy.LegacyAssets;
import me.moonscenty.alchemia.client.legacy.LegacyModels;
import me.moonscenty.alchemia.client.legacy.model.LegacyModelBaker;
import me.moonscenty.alchemia.entity.EldritchGuardian;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

/**
 * The guardian's body, the original's {@code ModelEldritchGuardian} read out of its code: a hood, plate on the chest
 * and shoulders, two arms held out before it, and a skirt of panels and a cloak that hang from the waist.
 * <p>
 * It does not walk. Its arms bob, lifting when it casts, and its panels and cloak sway, by the original's sums. It is
 * drawn as see-through as its renderer says.
 */
public class EldritchGuardianModel extends EntityModel<EldritchGuardian> {
    private static final List<String> ROOTS = List.of("BeltR", "Mbelt", "MbeltL", "MbeltR", "BeltL", "Chestplate",
            "Hood1", "Backplate", "ShoulderplateTopR", "ShoulderplateR1", "ShoulderplateR2", "ShoulderplateR3",
            "ShoulderR", "ArmL1", "ArmR1", "ShoulderL", "ShoulderplateLtop", "ShoulderplateL1", "ShoulderplateL2",
            "ShoulderplateL3", "LegpanelR4", "LegpanelR5", "LegpanelR6", "BackpanelR1", "BackpanelR2", "BackpanelR3",
            "BackpanelL3", "LegpanelL4", "LegpanelL5", "LegpanelL6", "BackpanelL1", "BackpanelL2", "Cloak1",
            "SidepanelR1", "SidepanelL1", "SidepanelL2", "SidepanelR2", "LegpanelC1");

    private static int bakedFrom = -1;
    private static Map<String, ModelPart> parts = Map.of();
    /** How much of it shows this frame, from the renderer. */
    float alpha = 1.0F;

    @Override
    public void setupAnim(EldritchGuardian guardian, float limbSwing, float limbSwingAmount, float age,
            float headYaw, float headPitch) {
        if (!refresh()) {
            return;
        }
        ModelPart hood = parts.get("Hood1");
        hood.yRot = headYaw * Mth.DEG_TO_RAD;
        hood.xRot = headPitch * Mth.DEG_TO_RAD;
        parts.get("ArmL1").xRot = -1.0F - guardian.armLiftL + Mth.sin((age + 20.0F) / 10.0F) * 0.08F;
        parts.get("ArmR1").xRot = -1.0F - guardian.armLiftR + Mth.sin(age / 10.0F) * 0.08F;
        ModelPart c1 = parts.get("LegpanelC1");
        ModelPart c2 = c1.getChild("LegpanelC2");
        c1.xRot = -0.15F + Mth.sin(age / 8.0F) * 0.12F;
        c2.xRot = Mth.sin((age - 5.0F) / 8.0F) * 0.13F;
        c2.getChild("LegpanelC3").xRot = Mth.sin((age - 10.0F) / 8.0F) * 0.14F;
        ModelPart cloak1 = parts.get("Cloak1");
        ModelPart cloak2 = cloak1.getChild("Cloak2");
        cloak1.xRot = 0.2F + Mth.sin(age / 7.0F) * 0.08F;
        cloak2.xRot = Mth.sin((age - 5.0F) / 7.0F) * 0.10F;
        cloak2.getChild("Cloak3").xRot = Mth.sin((age - 10.0F) / 7.0F) * 0.12F;
        ModelPart l2 = parts.get("SidepanelL2");
        ModelPart l3 = l2.getChild("SidepanelL3");
        l2.zRot = -0.2F + Mth.sin((age + 10.0F) / 8.0F) * 0.12F;
        l3.zRot = Mth.sin((age + 5.0F) / 8.0F) * 0.13F;
        l3.getChild("SidepanelL4").zRot = Mth.sin(age / 8.0F) * 0.14F;
        ModelPart r2 = parts.get("SidepanelR2");
        ModelPart r3 = r2.getChild("SidepanelR3");
        r2.zRot = 0.2F + Mth.sin((age - 5.0F) / 8.0F) * 0.12F;
        r3.zRot = Mth.sin((age - 10.0F) / 8.0F) * 0.13F;
        r3.getChild("SidepanelR4").zRot = Mth.sin((age - 15.0F) / 8.0F) * 0.14F;
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay, int colour) {
        if (!refresh()) {
            return;
        }
        int faded = FastColor.ARGB32.color((int) (FastColor.ARGB32.alpha(colour) * alpha), colour);
        for (ModelPart part : parts.values()) {
            part.render(pose, buffer, light, overlay, faded);
        }
    }

    /** Bakes the original's parts again whenever the import has changed what there is. */
    private static boolean refresh() {
        int generation = LegacyModels.generation();
        if (generation != bakedFrom) {
            bakedFrom = generation;
            Map<String, ModelPart> baked = new LinkedHashMap<>();
            LegacyModels.get(LegacyAssets.ELDRITCH_GUARDIAN).ifPresent(model -> {
                for (String name : ROOTS) {
                    baked.put(name, LegacyModelBaker.field(model, name));
                }
            });
            parts = baked;
        }
        return !parts.isEmpty();
    }
}
