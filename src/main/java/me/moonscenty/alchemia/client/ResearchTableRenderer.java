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
 * They are the original's: the inkwell and the scroll with its ribbon are read out of its code
 * ({@code ModelResearchTable}) and drawn where its renderer drew them, and the quill is its flat picture stood in the
 * inkwell at its angle, drawn the way a flat item is so it keeps the thin look a pen has when you hold one.
 */
public class ResearchTableRenderer implements BlockEntityRenderer<ResearchTableBlockEntity> {
    public static final ModelResourceLocation QUILL = ModelResourceLocation.standalone(Alchemia.id("block/research_table_quill"));
    /** The original's sheet for the inkwell and the scroll. */
    private static final ResourceLocation LEGACY_TEXTURE = Alchemia.id("textures/entity/research_table.png");
    /** The original tied the scroll with a ribbon in the note's colour, and in this grey when it had none. */
    private static final int RIBBON = 0xFF999999;
    /** The original's quill: half size, a sixteenth thick, and where its renderer put it. */
    private static final float LEGACY_QUILL_SCALE = 0.5F;
    private static final float LEGACY_QUILL_THICK = 0.0625F;

    private final Minecraft client = Minecraft.getInstance();
    private ItemStack carrier = ItemStack.EMPTY;

    /** The original's three parts, as of the import {@link #bakedFrom} names. */
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
