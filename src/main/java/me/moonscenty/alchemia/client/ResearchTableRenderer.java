package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.ResearchTableBlock;
import me.moonscenty.alchemia.block.entity.ResearchTableBlockEntity;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Stands the quill up in its inkwell.
 * <p>
 * It is drawn the way a flat item is, rather than as part of the block's model, so it keeps the thin extruded look a
 * pen has when you hold one.
 */
public class ResearchTableRenderer implements BlockEntityRenderer<ResearchTableBlockEntity> {
    public static final ModelResourceLocation QUILL = ModelResourceLocation.standalone(Alchemia.id("block/research_table_quill"));

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

    private final Minecraft client = Minecraft.getInstance();
    private ItemStack carrier = ItemStack.EMPTY;

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
        if (!state.getValue(ResearchTableBlock.HAS_TOOLS)) {
            return;
        }

        BakedModel model = client.getModelManager().getModel(QUILL);
        Direction facing = state.getValue(ResearchTableBlock.FACING);

        pose.pushPose();
        // turn about the middle of the block so the quill travels with the desk
        pose.translate(0.5F, 0.5F, 0.5F);
        pose.mulPose(Axis.YP.rotationDegrees(180 - facing.toYRot()));
        pose.translate(-0.5F, -0.5F, -0.5F);

        // stand it in the inkwell at the back right of the desk, and tilt about that point
        pose.translate(FOOT_X, FOOT_Y, FOOT_Z);
        pose.mulPose(Axis.YP.rotationDegrees(TURN));
        pose.mulPose(Axis.ZP.rotationDegrees(LEAN));
        pose.scale(SCALE, SCALE, SCALE);
        // the renderer centres the square on the origin, so shift it until the nib is what sits there instead
        pose.translate(0.5F - NIB_U, 0.5F - NIB_V, 0F);

        // the item renderer draws nothing for an empty stack, hence the carrier below
        client.getItemRenderer().render(carrier(), ItemDisplayContext.NONE, false,
                pose, buffers, light, OverlayTexture.NO_OVERLAY, model);
        pose.popPose();
    }
}
