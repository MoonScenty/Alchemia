package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.ResearchTableBlock;
import me.moonscenty.alchemia.block.entity.ResearchTableBlockEntity;
import me.moonscenty.alchemia.client.legacy.LegacyAssets;
import me.moonscenty.alchemia.client.legacy.LegacyModels;
import me.moonscenty.alchemia.client.legacy.model.LegacyModelBaker;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * What stands on the research table: an inkwell with its quill once there are tools on it, and a rolled scroll once
 * there is a note.
 * <p>
 * With the original's jar these are the original's: the inkwell and the scroll with its ribbon are read out of its
 * code ({@code ModelResearchTable}) and drawn where its renderer drew them, and the quill is its flat picture stood
 * in the inkwell at its angle. Without it they are our own models, laid out for our own desk.
 * <p>
 * The quill is drawn the way a flat item is either way, so it keeps the thin extruded look a pen has when you hold
 * one.
 */
public class ResearchTableRenderer implements BlockEntityRenderer<ResearchTableBlockEntity> {
    public static final ModelResourceLocation QUILL = ModelResourceLocation.standalone(Alchemia.id("block/research_table_quill"));
    public static final ModelResourceLocation INKWELL =
            ModelResourceLocation.standalone(Alchemia.id("block/research_table_inkwell"));
    public static final ModelResourceLocation SCROLL =
            ModelResourceLocation.standalone(Alchemia.id("block/research_table_scroll"));

    // Where the quill's foot sits, in block space: the mouth of the inkwell, which the model puts at 12..14 across
    // and 16..18 up. Everything below turns about that point, so the pen leans without lifting out of the ink.
    private static final float FOOT_X = 0.8125F;
    private static final float FOOT_Y = 1.08F;
    private static final float FOOT_Z = 0.8125F;
    /** Turned off-square so it does not read as part of the blocky furniture. */
    private static final float TURN = 60F;
    /** Leant over the way a pen rests in its pot. */
    private static final float LEAN = -14F;
    private static final float SCALE = 0.55F;

    // Where the nib sits within the quill's own square, measured off the texture. It is well off-centre, so anchoring
    // the square would leave the pen hovering beside the inkwell instead of standing in it.
    private static final float NIB_U = 0.281F;
    private static final float NIB_V = 0.281F;

    /** The original's sheet for the inkwell and the scroll, there only when its jar is. */
    private static final ResourceLocation LEGACY_TEXTURE = Alchemia.id("textures/entity/research_table.png");
    /** The original tied the scroll with a ribbon in the note's colour, and in this grey when it had none. */
    private static final int RIBBON = 0xFF999999;
    /** The original's quill: half size, a sixteenth thick, and where its renderer put it. */
    private static final float LEGACY_QUILL_SCALE = 0.5F;
    private static final float LEGACY_QUILL_THICK = 0.0625F;

    private final Minecraft client = Minecraft.getInstance();
    private ItemStack carrier = ItemStack.EMPTY;

    /** The original's three parts, as of the import {@link #bakedFrom} names; null without a jar. */
    private static int bakedFrom = -1;
    private static ModelPart inkwell;
    private static ModelPart scrollTube;
    private static ModelPart scrollRibbon;

    /**
     * The item renderer draws nothing for an empty stack, so it is handed the scribing tools to carry the quill
     * model. Only the model is drawn; the stack just has to be something.
     */
    private ItemStack carrier() {
        if (carrier.isEmpty()) {
            carrier = new ItemStack(ModItems.SCRIBING_TOOLS.get());
        }
        return carrier;
    }

    public ResearchTableRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ResearchTableBlockEntity table, float partialTick, PoseStack pose, MultiBufferSource buffers,
            int light, int overlay) {
        BlockState state = table.getBlockState();
        boolean tools = state.getValue(ResearchTableBlock.HAS_TOOLS);
        boolean notes = state.getValue(ResearchTableBlock.HAS_NOTES);
        if (!tools && !notes) {
            return;
        }
        Direction facing = state.getValue(ResearchTableBlock.FACING);
        refresh();
        if (inkwell != null) {
            renderLegacy(facing, tools, notes, pose, buffers, light);
        } else {
            renderOurs(facing, tools, notes, pose, buffers, light);
        }
    }

    /** As the original's renderer drew them: from the middle of the desk's top, turned upside down and round. */
    private void renderLegacy(Direction facing, boolean tools, boolean notes, PoseStack pose,
            MultiBufferSource buffers, int light) {
        pose.pushPose();
        pose.translate(0.5F, 1.0F, 0.5F);
        pose.mulPose(Axis.XP.rotationDegrees(180F));
        pose.mulPose(Axis.YP.rotationDegrees(switch (facing) {
            case EAST -> 90F;
            case WEST -> 270F;
            case SOUTH -> 180F;
            default -> 0F;
        }));
        var buffer = buffers.getBuffer(RenderType.entityCutoutNoCull(LEGACY_TEXTURE));
        if (notes) {
            scrollTube.render(pose, buffer, light, OverlayTexture.NO_OVERLAY);
            scrollRibbon.render(pose, buffer, light, OverlayTexture.NO_OVERLAY, RIBBON);
        }
        if (tools) {
            inkwell.render(pose, buffer, light, OverlayTexture.NO_OVERLAY);

            // the quill: turned back upright, set in the inkwell and turned a little, then drawn as a flat picture
            // from its corner, mirrored, as the original's own flat drawing laid its picture out
            pose.pushPose();
            pose.mulPose(Axis.XP.rotationDegrees(180F));
            pose.translate(-0.5F, 0.1F, 0.125F);
            pose.mulPose(Axis.YP.rotationDegrees(60F));
            pose.scale(LEGACY_QUILL_SCALE, LEGACY_QUILL_SCALE, LEGACY_QUILL_SCALE);
            pose.translate(0.5F, 0.5F, -LEGACY_QUILL_THICK / 2F);
            pose.mulPose(Axis.YP.rotationDegrees(180F));
            client.getItemRenderer().render(carrier(), ItemDisplayContext.NONE, false, pose, buffers, light,
                    OverlayTexture.NO_OVERLAY, client.getModelManager().getModel(QUILL));
            pose.popPose();
        }
        pose.popPose();
    }

    /** Our own inkwell and scroll, which are laid out for our own desk, and the quill stood in our inkwell. */
    private void renderOurs(Direction facing, boolean tools, boolean notes, PoseStack pose,
            MultiBufferSource buffers, int light) {
        pose.pushPose();
        // turn about the middle of the block so everything travels with the desk
        pose.translate(0.5F, 0.5F, 0.5F);
        pose.mulPose(Axis.YP.rotationDegrees(180 - facing.toYRot()));
        pose.translate(-0.5F, -0.5F, -0.5F);

        if (notes) {
            drawModel(SCROLL, pose, buffers, light);
        }
        if (tools) {
            drawModel(INKWELL, pose, buffers, light);

            // stand it in the inkwell at the back right of the desk, and tilt about that point
            pose.translate(FOOT_X, FOOT_Y, FOOT_Z);
            pose.mulPose(Axis.YP.rotationDegrees(TURN));
            pose.mulPose(Axis.ZP.rotationDegrees(LEAN));
            pose.scale(SCALE, SCALE, SCALE);
            // the renderer centres the square on the origin, so shift it until the nib is what sits there instead
            pose.translate(0.5F - NIB_U, 0.5F - NIB_V, 0F);

            client.getItemRenderer().render(carrier(), ItemDisplayContext.NONE, false,
                    pose, buffers, light, OverlayTexture.NO_OVERLAY, client.getModelManager().getModel(QUILL));
        }
        pose.popPose();
    }

    private void drawModel(ModelResourceLocation location, PoseStack pose, MultiBufferSource buffers, int light) {
        BakedModel model = client.getModelManager().getModel(location);
        client.getBlockRenderer().getModelRenderer().renderModel(pose.last(),
                buffers.getBuffer(RenderType.cutout()), null, model, 1F, 1F, 1F, light, OverlayTexture.NO_OVERLAY);
    }

    /** Bakes the original's parts again whenever the import has changed what there is. */
    private static void refresh() {
        int generation = LegacyModels.generation();
        if (generation == bakedFrom) {
            return;
        }
        bakedFrom = generation;
        var model = LegacyModels.get(LegacyAssets.RESEARCH_TABLE);
        inkwell = model.map(read -> LegacyModelBaker.field(read, "Inkwell")).orElse(null);
        scrollTube = model.map(read -> LegacyModelBaker.field(read, "ScrollTube")).orElse(null);
        scrollRibbon = model.map(read -> LegacyModelBaker.field(read, "ScrollRibbon")).orElse(null);
    }
}
