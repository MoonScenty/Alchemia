package me.moonscenty.alchemia.client.armour;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import me.moonscenty.alchemia.item.RobeItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

/**
 * Draws a robe on whoever is wearing one.
 *
 * <p>Armour is normally two flat sheets pulled over a copy of the body, and the layer that does it will not draw
 * a carved mesh. So the robe's material carries no sheet at all and nothing is drawn there; this runs instead,
 * hangs each mesh off the limb it belongs to, and lets the limb carry it about.
 *
 * <h3>Two spaces</h3>
 * A mesh is drawn the way a block is: a block to the unit, up is up, and the feet stand on nothing. A limb lives
 * in the space the game draws bodies in, where up is down, the whole body hangs from a point {@value #HANGS_AT}
 * blocks above the feet, and left and right are swapped. Getting from one to the other is a flip and a shift, and
 * both have to happen <em>inside</em> the limb's own turn so that a raised arm carries its sleeve up with it.
 */
public class RobeLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {
    /**
     * How far above the feet the body hangs from.
     * <p>
     * Not a round number, and not ours: the renderer that sets a body up shifts it by exactly this, and a robe
     * that used one and a half would float a thousandth of a block off everything else.
     */
    public static final float HANGS_AT = 1.501F;

    /** Sixteen of a limb's own units to the block. */
    private static final float TO_BLOCKS = 16.0F;

    private static final RandomSource STEADY = RandomSource.create();

    public RobeLayer(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffers, int light, T worn,
            float limbSwing, float limbSwingAmount, float partial, float age, float yaw, float pitch) {
        for (EquipmentSlot slot : new EquipmentSlot[] {EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack piece = worn.getItemBySlot(slot);
            if (piece.getItem() instanceof RobeItem robe) {
                wear(poseStack, buffers, light, robe, robe.getType());
            }
        }
    }

    private void wear(PoseStack poseStack, MultiBufferSource buffers, int light, RobeItem robe,
            ArmorItem.Type type) {
        // the meshes live on the block sheet, so the brush has to be dipped in that rather than in a robe's own
        VertexConsumer into = buffers.getBuffer(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS));
        for (String part : RobeMeshes.covering(type)) {
            hang(poseStack, into, light, RobeMeshes.baked(part, robe.drab()), limb(part));
        }
    }

    /**
     * Which limb a mesh rides on.
     * <p>
     * The drawn left is this game's right. A robe drawn with its left sleeve on the left of the page belongs on
     * the arm that is on the left of the page, and that arm is the wearer's right.
     */
    private ModelPart limb(String part) {
        M body = getParentModel();
        return switch (part) {
            case "head" -> body.head;
            case "body" -> body.body;
            case "left_arm" -> body.rightArm;
            case "right_arm" -> body.leftArm;
            case "left_leg", "left_feet" -> body.rightLeg;
            case "right_leg", "right_feet" -> body.leftLeg;
            default -> body.body;
        };
    }

    /**
     * Puts one mesh on one limb.
     * <p>
     * The limb's own turn goes on first, so whatever follows is carried by it. Then the mesh's space is undone:
     * shifted back by where the limb hangs and by how high the body hangs, and flipped in two of three axes.
     * The shift is applied after the turn on purpose -- it is measured from the limb's pivot, which is the point
     * the limb turns about.
     */
    private void hang(PoseStack poseStack, VertexConsumer into, int light, BakedModel mesh, ModelPart limb) {
        poseStack.pushPose();
        limb.translateAndRotate(poseStack);
        poseStack.translate(-limb.x / TO_BLOCKS, HANGS_AT - limb.y / TO_BLOCKS, -limb.z / TO_BLOCKS);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        draw(poseStack, into, light, mesh);
        poseStack.popPose();
    }

    /** Every face of a baked mesh, poured into the brush the body is being drawn with. */
    private void draw(PoseStack poseStack, VertexConsumer into, int light, BakedModel mesh) {
        PoseStack.Pose pose = poseStack.last();
        for (Direction side : Direction.values()) {
            put(pose, into, light, mesh.getQuads(null, side, STEADY));
        }
        put(pose, into, light, mesh.getQuads(null, null, STEADY));
    }

    private void put(PoseStack.Pose pose, VertexConsumer into, int light,
            List<net.minecraft.client.renderer.block.model.BakedQuad> quads) {
        for (var quad : quads) {
            into.putBulkData(pose, quad, 1.0F, 1.0F, 1.0F, 1.0F, light, OverlayTexture.NO_OVERLAY);
        }
    }

    /** Whether the game has anything to draw with yet, so that a reload mid-frame cannot be caught half-done. */
    public static boolean ready() {
        return Minecraft.getInstance().getModelManager() != null;
    }
}
