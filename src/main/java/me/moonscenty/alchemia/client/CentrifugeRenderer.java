package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.entity.CentrifugeBlockEntity;
import me.moonscenty.alchemia.client.legacy.LegacyAssets;
import me.moonscenty.alchemia.client.legacy.LegacyCached;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class CentrifugeRenderer implements BlockEntityRenderer<CentrifugeBlockEntity> {
    static final ResourceLocation TEXTURE = Alchemia.id("textures/entity/centrifuge.png");
    private static final String[] STILL = {"Top", "Bottom"};
    private static final String[] SPINNING = {"Crossbar", "Dingus1", "Dingus2", "Core"};

    private static final LegacyCached<ModelPart[]> STILL_PARTS = LegacyCached.fields(LegacyAssets.CENTRIFUGE, STILL);
    private static final LegacyCached<ModelPart[]> SPINNING_PARTS =
            LegacyCached.fields(LegacyAssets.CENTRIFUGE, SPINNING);

    public CentrifugeRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CentrifugeBlockEntity centrifuge, float partial, PoseStack pose, MultiBufferSource buffers,
            int light, int overlay) {
        draw(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)), centrifuge.rotation(partial), light,
                overlay);
    }

    static void draw(PoseStack pose, VertexConsumer buffer, float rotation, int light, int overlay) {
        ModelPart[] still = STILL_PARTS.get();
        ModelPart[] spinning = SPINNING_PARTS.get();
        if (still == null || spinning == null) {
            return;
        }
        pose.pushPose();
        pose.translate(0.5F, 0.5F, 0.5F);
        for (ModelPart part : still) {
            part.render(pose, buffer, light, overlay);
        }
        pose.mulPose(Axis.YP.rotationDegrees(rotation));
        for (ModelPart part : spinning) {
            part.render(pose, buffer, light, overlay);
        }
        pose.popPose();
    }
}
