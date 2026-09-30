package me.moonscenty.alchemia.client.armour;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import me.moonscenty.alchemia.item.RobeItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Draws a robe in a slot, in a hand, or on the ground: the same meshes it is worn in, stood upright.
 *
 * <p>A flat picture would have meant drawing each piece twice -- once as a mesh for the body and once as a sprite
 * for the bag -- and the two would drift the first time the robe was redrawn. So there is one robe, and this is
 * it seen from the front instead of from the wearer.
 *
 * <p>Each piece is measured and stood in the middle of the item's box rather than placed by hand. A hood and a
 * pair of boots are nothing like the same size, and a number picked for one would strand the other in a corner
 * of the slot.
 */
public class RobeItemRenderer extends BlockEntityWithoutLevelRenderer {
    /** How much of the box a piece fills, leaving a little air at the edges. */
    private static final float FILLS = 0.94F;

    private static RobeItemRenderer only;

    private RobeItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
    }

    /**
     * The one of these there is.
     * <p>
     * Built when it is first wanted rather than when the mod loads: it asks the game for things the game does not
     * have yet while the mod is still being put together.
     */
    public static RobeItemRenderer get() {
        if (only == null) {
            only = new RobeItemRenderer();
        }
        return only;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack,
            MultiBufferSource buffers, int light, int overlay) {
        if (!(stack.getItem() instanceof RobeItem robe)) {
            return;
        }
        AABB extent = RobeMeshes.extent(robe.getType(), robe.drab());
        float fits = FILLS / (float) Math.max(extent.getXsize(), Math.max(extent.getYsize(), extent.getZsize()));
        Vec3 middle = extent.getCenter();

        poseStack.pushPose();
        // the item's box runs from nothing to one, so its middle is a half along every side
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.scale(fits, fits, fits);
        poseStack.translate(-middle.x, -middle.y, -middle.z);

        VertexConsumer into = buffers.getBuffer(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS));
        for (String part : RobeMeshes.covering(robe.getType())) {
            RobeLayer.pour(poseStack, into, light, overlay, RobeMeshes.baked(part, robe.drab()));
        }
        poseStack.popPose();
    }
}
