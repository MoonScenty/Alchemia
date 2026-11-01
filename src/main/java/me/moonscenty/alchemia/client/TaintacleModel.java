package me.moonscenty.alchemia.client;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import me.moonscenty.alchemia.client.legacy.LegacyModels;
import me.moonscenty.alchemia.client.legacy.model.LegacyModelBaker;
import me.moonscenty.alchemia.entity.Taintacle;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;

public class TaintacleModel<T extends Taintacle> extends EntityModel<T> {
    // original's value
    private static final float TAPER = 0.88F;

    private final String key;
    private final int length;
    private int bakedFrom = -1;
    private ModelPart root;
    private List<ModelPart> segments = List.of();
    private List<ModelPart> ends = List.of();
    private float rising;
    private float height;

    public TaintacleModel(String key, int length) {
        this.key = key;
        this.length = length;
    }

    @Override
    public void prepareMobModel(T tentacle, float limbSwing, float limbSwingAmount, float partial) {
        height = tentacle.getBbHeight();
        float growsFor = height * 10.0F;
        float age = tentacle.tickCount + partial;
        rising = age < growsFor ? (growsFor - age) / growsFor * height : 0.0F;
    }

    @Override
    public void setupAnim(T tentacle, float limbSwing, float limbSwingAmount, float ageInTicks,
            float netHeadYaw, float headPitch) {
        if (!refresh()) {
            return;
        }
        float mod = 0.0625F * 0.2F;
        float flail = tentacle.flail;
        float speed = flail > 1.0F ? 3.0F : 1.0F - mod;
        float swing = flail + (tentacle.hurtTime <= 0 ? -mod : mod);
        root.xRot = 0.0F;
        for (int index = 0; index < segments.size(); index++) {
            ModelPart segment = segments.get(index);
            segment.xRot = 0.15F * swing * Mth.sin(ageInTicks * 0.1F * speed - index / 2.0F);
            segment.zRot = 0.1F / swing * Mth.sin(ageInTicks * 0.15F - index / 2.0F);
        }
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay, int colour) {
        if (!refresh()) {
            return;
        }
        pose.pushPose();
        pose.translate(0.0F, (height == 3.0F ? 0.6F : 1.2F) + rising, 0.0F);
        pose.scale(height / 3.0F, height / 3.0F, height / 3.0F);
        ends.forEach(end -> end.visible = false);
        root.render(pose, buffer, light, overlay, colour);
        ends.forEach(end -> end.visible = true);
        root.getAllParts().forEach(part -> part.skipDraw = !ends.contains(part));
        root.render(pose, buffer, LightTexture.FULL_BRIGHT, overlay, colour);
        root.getAllParts().forEach(part -> part.skipDraw = false);
        pose.popPose();
    }

    private boolean refresh() {
        int generation = LegacyModels.generation();
        if (generation != bakedFrom) {
            bakedFrom = generation;
            root = LegacyModels.get(key).map(read -> LegacyModelBaker.field(read, "tentacle")).orElse(null);
            if (root != null) {
                List<ModelPart> all = root.getAllParts().toList();
                for (ModelPart part : all) {
                    if (part != root) {
                        part.xScale = TAPER;
                        part.yScale = TAPER;
                        part.zScale = TAPER;
                        part.x *= TAPER;
                        part.y *= TAPER;
                        part.z *= TAPER;
                    }
                }
                segments = all.subList(1, Math.min(all.size(), length));
                ends = all.subList(Math.min(all.size(), length), all.size());
            }
        }
        return root != null;
    }
}
