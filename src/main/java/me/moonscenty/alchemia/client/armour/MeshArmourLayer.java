package me.moonscenty.alchemia.client.armour;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import me.moonscenty.alchemia.item.MeshArmour;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

/**
 * Draws worn armour that is a carved mesh rather than two flat pictures.
 *
 * <p>Armour is normally two sheets pulled over a copy of the body, and the layer that does it will not draw a
 * mesh. So such a piece carries no sheet at all on its material and nothing is drawn there; this runs instead,
 * hangs each mesh off the limb it belongs to, and lets the limb carry it about.
 *
 * <h3>Two spaces</h3>
 * A mesh is drawn the way a block is: a block to the unit, up is up, and the feet stand on nothing. A limb lives
 * in the space the game draws bodies in, where up is down, the whole body hangs from a point {@value #HANGS_AT}
 * blocks above the feet, and left and right are swapped. Getting from one to the other is a flip and a shift, and
 * both have to happen <em>inside</em> the limb's own turn so that a raised arm carries its sleeve up with it.
 */
public class MeshArmourLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {
    /**
     * How far above the feet the body hangs from.
     * <p>
     * Not a round number, and not ours: the renderer that sets a body up shifts it by exactly this, and a piece
     * that used one and a half would float a thousandth of a block off everything else.
     */
    public static final float HANGS_AT = 1.501F;

    /** Sixteen of a limb's own units to the block. */
    private static final float TO_BLOCKS = 16.0F;

    private static final EquipmentSlot[] WORN = {EquipmentSlot.HEAD, EquipmentSlot.CHEST,
            EquipmentSlot.LEGS, EquipmentSlot.FEET};

    public MeshArmourLayer(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffers, int light, T worn,
            float limbSwing, float limbSwingAmount, float partial, float age, float yaw, float pitch) {
        for (EquipmentSlot slot : WORN) {
            ItemStack piece = worn.getItemBySlot(slot);
            if (piece.getItem() instanceof MeshArmour mesh && piece.getItem() instanceof ArmorItem armour) {
                wear(poseStack, buffers, light, piece, mesh, armour.getType());
            }
        }
    }

    private void wear(PoseStack poseStack, MultiBufferSource buffers, int light, ItemStack piece,
            MeshArmour mesh, ArmorItem.Type type) {
        // the meshes live on the block sheet, so the brush is dipped in that rather than in a sheet of the
        // piece's own
        VertexConsumer into = buffers.getBuffer(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS));
        String set = mesh.meshSet();
        String variant = mesh.meshVariant(piece);
        int colour = mesh.meshTint(piece);
        for (String part : ArmourMeshes.covering(type)) {
            hang(poseStack, into, light, ArmourMeshes.baked(set, variant, part), limb(part), colour);
        }
    }

    /**
     * Which limb a mesh rides on.
     * <p>
     * Straight across: the drawn left arm goes on the left arm. It is tempting to swap them, because the game
     * puts the right arm at a negative x while the drawn right arm is at a positive one -- but the renderer
     * turns a body about before any of this runs, and that turn is already undone below. Measured in the world
     * both arms are where they say they are.
     */
    private ModelPart limb(String part) {
        M body = getParentModel();
        return switch (part) {
            case "head" -> body.head;
            case "body" -> body.body;
            case "left_arm" -> body.leftArm;
            case "right_arm" -> body.rightArm;
            case "left_leg", "left_feet" -> body.leftLeg;
            case "right_leg", "right_feet" -> body.rightLeg;
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
     * <p>
     * The shift is measured from where the limb <em>rests</em>, not from where it is now. A limb does not only
     * turn: crouching drops the head and the body outright, and taking away where the limb is now would take
     * that away with it and leave the hood hanging in the air where the head used to be.
     */
    private void hang(PoseStack poseStack, VertexConsumer into, int light, BakedModel mesh, ModelPart limb,
            int colour) {
        PartPose rest = limb.getInitialPose();
        poseStack.pushPose();
        limb.translateAndRotate(poseStack);
        poseStack.translate(-rest.x / TO_BLOCKS, HANGS_AT - rest.y / TO_BLOCKS, -rest.z / TO_BLOCKS);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        pour(poseStack, into, light, OverlayTexture.NO_OVERLAY, mesh, colour);
        poseStack.popPose();
    }

    /**
     * Every face of a baked mesh, poured into whatever brush is being drawn with.
     * <p>
     * Shared with the renderer that draws a piece in a slot, because pouring a mesh into a brush is the same job
     * whether the mesh is on a shoulder or in a bag.
     */
    public static void pour(PoseStack poseStack, VertexConsumer into, int light, int overlay, BakedModel mesh,
            int colour) {
        PoseStack.Pose pose = poseStack.last();
        float red = FastColor.ARGB32.red(colour) / 255.0F;
        float green = FastColor.ARGB32.green(colour) / 255.0F;
        float blue = FastColor.ARGB32.blue(colour) / 255.0F;
        for (var quad : ArmourMeshes.faces(mesh)) {
            into.putBulkData(pose, quad, red, green, blue, 1.0F, light, overlay);
        }
    }
}
