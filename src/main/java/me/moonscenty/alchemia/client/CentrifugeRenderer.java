package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.entity.CentrifugeBlockEntity;
import me.moonscenty.alchemia.client.legacy.LegacyAssets;
import me.moonscenty.alchemia.client.legacy.LegacyModels;
import me.moonscenty.alchemia.client.legacy.model.LegacyModelBaker;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * Draws the centrifuge as the original did: two plates that stand still, and between them a core with a weight on
 * either arm, spun about the upright while it has something in it.
 * <p>
 * The parts are the original's model ({@code ModelCentrifuge}), read out of its code. The original drew it the
 * right way up without the flip models usually get, so its "top" plate is at the bottom; the two are the same.
 */
public class CentrifugeRenderer implements BlockEntityRenderer<CentrifugeBlockEntity> {
    static final ResourceLocation TEXTURE = Alchemia.id("textures/entity/centrifuge.png");
    private static final String[] STILL = {"Top", "Bottom"};
    private static final String[] SPINNING = {"Crossbar", "Dingus1", "Dingus2", "Core"};

    /** The original's parts, as of the import {@link #bakedFrom} names. */
    private static int bakedFrom = -1;
    private static ModelPart[] still;
    private static ModelPart[] spinning;

    public CentrifugeRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CentrifugeBlockEntity centrifuge, float partial, PoseStack pose, MultiBufferSource buffers,
            int light, int overlay) {
        draw(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)), centrifuge.rotation(partial), light,
                overlay);
    }

    /** The centrifuge with its rotor at a given turn. Shared with the item, which stands still. */
    static void draw(PoseStack pose, VertexConsumer buffer, float rotation, int light, int overlay) {
        if (!refresh()) {
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

    /** Bakes the original's parts again whenever the import has changed what there is. */
    private static boolean refresh() {
        int generation = LegacyModels.generation();
        if (generation != bakedFrom) {
            bakedFrom = generation;
            var model = LegacyModels.get(LegacyAssets.CENTRIFUGE);
            still = model.map(read -> bake(read, STILL)).orElse(null);
            spinning = model.map(read -> bake(read, SPINNING)).orElse(null);
        }
        return still != null && spinning != null;
    }

    private static ModelPart[] bake(me.moonscenty.alchemia.client.legacy.model.LegacyModel read, String[] names) {
        ModelPart[] parts = new ModelPart[names.length];
        for (int i = 0; i < names.length; i++) {
            parts[i] = LegacyModelBaker.field(read, names[i]);
        }
        return parts;
    }
}
