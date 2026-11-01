package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.entity.BrainJarBlockEntity;
import me.moonscenty.alchemia.client.legacy.LegacyAssets;
import me.moonscenty.alchemia.client.legacy.LegacyCached;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Draws what is inside a brain in a jar, as the original did: the brain, bobbing a little and turning to look at
 * the orb it is drawing in, and the brine it floats in. The jar itself is the block's model, the warded jar's.
 * <p>
 * The brain and the brine are the original's models ({@code ModelBrain} and the brine of {@code ModelJar}), read
 * out of its code. The original drew them upside down and flipped them back, which is kept.
 */
public class BrainJarRenderer implements BlockEntityRenderer<BrainJarBlockEntity> {
    private static final ResourceLocation BRAIN = Alchemia.id("textures/entity/brain.png");
    private static final ResourceLocation BRINE = Alchemia.id("textures/entity/jar_brine.png");

    /** The original's parts, baked again whenever the import changes. */
    private static final LegacyCached<ModelPart[]> BRAIN_PARTS =
            LegacyCached.fields(LegacyAssets.BRAIN, "Shape1", "Shape2", "Shape3");
    private static final LegacyCached<ModelPart> BRINE_PART = LegacyCached.field(LegacyAssets.JAR, "Brine");

    public BrainJarRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(BrainJarBlockEntity jar, float partial, PoseStack pose, MultiBufferSource buffers, int light,
            int overlay) {
        ModelPart[] brain = BRAIN_PARTS.get();
        ModelPart brine = BRINE_PART.get();
        if (brain == null || brine == null) {
            return;
        }
        var player = Minecraft.getInstance().player;
        float age = player == null ? 0.0F : player.tickCount;
        pose.pushPose();
        pose.translate(0.5F, 0.01F, 0.5F);
        pose.mulPose(Axis.XP.rotationDegrees(180.0F));

        pose.pushPose();
        float bob = Mth.sin(age / 14.0F) * 0.03F + 0.03F;
        pose.translate(0.0F, -0.8F + bob, 0.0F);
        pose.mulPose(Axis.YP.rotation(jar.turned(partial)));
        pose.mulPose(Axis.YP.rotationDegrees(-90.0F));
        pose.scale(0.4F, 0.4F, 0.4F);
        var flesh = buffers.getBuffer(RenderType.entityCutoutNoCull(BRAIN));
        for (ModelPart part : brain) {
            part.render(pose, flesh, light, overlay);
        }
        pose.popPose();

        brine.render(pose, buffers.getBuffer(RenderType.entityTranslucent(BRINE)), light, overlay);
        pose.popPose();
    }
}
