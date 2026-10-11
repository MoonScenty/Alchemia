package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aura.ShardTuning;
import me.moonscenty.alchemia.block.DioptraBlock;
import me.moonscenty.alchemia.block.entity.DioptraBlockEntity;
import me.moonscenty.alchemia.client.legacy.LegacyAssets;
import me.moonscenty.alchemia.client.legacy.LegacyModels;
import me.moonscenty.alchemia.client.legacy.LegacyRenderTypes;
import me.moonscenty.alchemia.client.legacy.model.LegacyMesh;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

/**
 * The dioptra as the original drew it: the crystal in its top, coloured by its shard, and over it, while it works, the
 * map of the aura -- a sheet of light lifted at each chunk as high as the aura there is strong, coloured by what
 * aspect it is, inside a glowing box.
 */
public class DioptraRenderer implements BlockEntityRenderer<DioptraBlockEntity> {
    private static final ResourceLocation CRYSTAL = Alchemia.id("textures/entity/vis_relay.png");
    private static final ResourceLocation GRID = Alchemia.id("textures/entity/dioptra_grid.png");
    private static final ResourceLocation SIDE = Alchemia.id("textures/entity/dioptra_side.png");
    /** How see-through the map and its box are. The original's. */
    private static final float MAP_ALPHA = 0.9F;
    private static final float BOX_ALPHA = 0.8F;
    /** The map sits a whisker inside the block's edges and a whisker over its top. */
    private static final float INSET = 0.005F;
    private static final float WIDTH = 0.99F;
    private static final int CELLS = DioptraBlockEntity.ACROSS - 1;

    private static int meshFrom = -1;
    private static LegacyMesh mesh;

    public DioptraRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(DioptraBlockEntity dioptra, float partial, PoseStack pose, MultiBufferSource buffers, int light,
            int overlay) {
        if (dioptra.tuning() == ShardTuning.NONE || dioptra.getLevel() == null) {
            return;
        }
        float age = dioptra.getLevel().getGameTime() + partial;
        Holder<Aspect> shown = ShardTuning.aspect(dioptra.tuning());
        float red;
        float green;
        float blue;
        if (shown != null) {
            int colour = shown.value().color();
            red = (colour >> 16 & 0xFF) / 255.0F;
            green = (colour >> 8 & 0xFF) / 255.0F;
            blue = (colour & 0xFF) / 255.0F;
        } else {
            red = Mth.sin(age / 12.0F) * 0.3F + 0.7F;
            green = Mth.sin(age / 11.0F) * 0.3F + 0.7F;
            blue = Mth.sin(age / 10.0F) * 0.3F + 0.7F;
        }
        crystal(pose, buffers, overlay, red, green, blue);
        if (dioptra.getBlockState().getValue(DioptraBlock.ENABLED)) {
            map(dioptra, pose, buffers.getBuffer(LegacyRenderTypes.glowBothSides(GRID)), overlay);
            box(pose, buffers.getBuffer(LegacyRenderTypes.glowBothSides(SIDE)), overlay, red, green, blue);
        }
    }

    /** The original's vis relay crystal, turned on its point and a little stretched, in the shard's colour. */
    private static void crystal(PoseStack pose, MultiBufferSource buffers, int overlay, float red, float green,
            float blue) {
        LegacyMesh relay = legacyMesh();
        int crystal = relay == null ? -1 : relay.group("Crystal");
        if (crystal < 0) {
            return;
        }
        pose.pushPose();
        pose.translate(0.5F, 0.5F, 0.5F);
        pose.mulPose(Axis.YP.rotationDegrees(45.0F));
        pose.mulPose(Axis.XP.rotationDegrees(90.0F));
        pose.scale(1.3F, 1.3F, 1.2F);
        int colour = 0xFF000000 | (int) (red * 255) << 16 | (int) (green * 255) << 8 | (int) (blue * 255);
        relay.render(crystal, pose.last(), buffers.getBuffer(RenderType.entityTranslucent(CRYSTAL)), colour,
                LightTexture.FULL_BRIGHT, overlay);
        pose.popPose();
    }

    /** The sheet over the top: one square between every four chunks, in the colour of the four of them together. */
    private static void map(DioptraBlockEntity dioptra, PoseStack pose, VertexConsumer into, int overlay) {
        PoseStack.Pose at = pose.last();
        for (int a = 0; a < CELLS; a++) {
            for (int b = 0; b < CELLS; b++) {
                int colour = blend(dioptra.aspect(a, b), dioptra.aspect(a + 1, b), dioptra.aspect(a + 1, b + 1),
                        dioptra.aspect(a, b + 1));
                if (colour < 0) {
                    continue;
                }
                float x0 = across(a);
                float x1 = across(a + 1);
                float z0 = across(b);
                float z1 = across(b + 1);
                float h00 = high(dioptra, a, b);
                float h10 = high(dioptra, a + 1, b);
                float h11 = high(dioptra, a + 1, b + 1);
                float h01 = high(dioptra, a, b + 1);
                quad(into, at, overlay, colour, MAP_ALPHA, x0, h00, z0, x1, h10, z0, x1, h11, z1, x0, h01, z1);
                // skirts down to the block along the edges of the map
                if (a == 0) {
                    quad(into, at, overlay, colour, MAP_ALPHA, x0, 1, z0, x0, h00, z0, x0, h01, z1, x0, 1, z1);
                }
                if (a == CELLS - 1) {
                    quad(into, at, overlay, colour, MAP_ALPHA, x1, 1, z0, x1, h10, z0, x1, h11, z1, x1, 1, z1);
                }
                if (b == 0) {
                    quad(into, at, overlay, colour, MAP_ALPHA, x0, 1, z0, x1, 1, z0, x1, h10, z0, x0, h00, z0);
                }
                if (b == CELLS - 1) {
                    quad(into, at, overlay, colour, MAP_ALPHA, x0, 1, z1, x1, 1, z1, x1, h11, z1, x0, h01, z1);
                }
            }
        }
    }

    private static float across(int cell) {
        return INSET + WIDTH * cell / CELLS;
    }

    private static float high(DioptraBlockEntity dioptra, int x, int z) {
        return 1.001F + dioptra.amount(x, z) / DioptraBlockEntity.FULL;
    }

    /** The four colours mixed evenly; nothing where any of the four was not read. */
    private static int blend(Holder<Aspect> first, Holder<Aspect> second, Holder<Aspect> third,
            Holder<Aspect> fourth) {
        if (first == null || second == null || third == null || fourth == null) {
            return -1;
        }
        int red = 0;
        int green = 0;
        int blue = 0;
        for (Holder<Aspect> aspect : new Holder[] {first, second, third, fourth}) {
            int colour = aspect.value().color();
            red += colour >> 16 & 0xFF;
            green += colour >> 8 & 0xFF;
            blue += colour & 0xFF;
        }
        return (red / 4) << 16 | (green / 4) << 8 | blue / 4;
    }

    /** The box of light the map stands in, a block high over the dioptra, in the shard's colour. */
    private static void box(PoseStack pose, VertexConsumer into, int overlay, float red, float green, float blue) {
        PoseStack.Pose at = pose.last();
        int colour = (int) (red * 255) << 16 | (int) (green * 255) << 8 | (int) (blue * 255);
        float x0 = 0;
        float x1 = 1;
        float y0 = 1;
        float y1 = 2;
        float z0 = 0;
        float z1 = 1;
        quad(into, at, overlay, colour, BOX_ALPHA, x0, y0, z0, x1, y0, z0, x1, y1, z0, x0, y1, z0);
        quad(into, at, overlay, colour, BOX_ALPHA, x1, y0, z1, x0, y0, z1, x0, y1, z1, x1, y1, z1);
        quad(into, at, overlay, colour, BOX_ALPHA, x0, y0, z1, x0, y0, z0, x0, y1, z0, x0, y1, z1);
        quad(into, at, overlay, colour, BOX_ALPHA, x1, y0, z0, x1, y0, z1, x1, y1, z1, x1, y1, z0);
    }

    /** Four corners in order, the whole picture across them. */
    private static void quad(VertexConsumer into, PoseStack.Pose at, int overlay, int rgb, float alpha,
            float ax, float ay, float az, float bx, float by, float bz, float cx, float cy, float cz,
            float dx, float dy, float dz) {
        int colour = (int) (alpha * 255) << 24 | rgb;
        vertex(into, at, overlay, colour, ax, ay, az, 0, 1);
        vertex(into, at, overlay, colour, bx, by, bz, 1, 1);
        vertex(into, at, overlay, colour, cx, cy, cz, 1, 0);
        vertex(into, at, overlay, colour, dx, dy, dz, 0, 0);
    }

    private static void vertex(VertexConsumer into, PoseStack.Pose at, int overlay, int colour, float x, float y,
            float z, float u, float v) {
        into.addVertex(at, x, y, z).setColor(colour).setUv(u, v).setOverlay(overlay)
                .setLight(LightTexture.FULL_BRIGHT).setNormal(at, 0.0F, 1.0F, 0.0F);
    }

    @Override
    public AABB getRenderBoundingBox(DioptraBlockEntity dioptra) {
        return new AABB(dioptra.getBlockPos()).inflate(0.3).expandTowards(0, 1.0, 0);
    }

    /** The original's vis relay mesh, once per import, turned over as the older loader read it. */
    private static LegacyMesh legacyMesh() {
        int generation = LegacyModels.generation();
        if (generation != meshFrom) {
            meshFrom = generation;
            mesh = LegacyModels.mesh(LegacyAssets.RELAY_MESH).map(LegacyMesh::flippedV).orElse(null);
        }
        return mesh;
    }
}
