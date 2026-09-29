package me.moonscenty.alchemia.client.armour;

import java.util.Map;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Building a worn model that is only worn in places.
 * <p>
 * Armour is drawn by a layer that expects a whole body and hides the parts the piece does not cover. So a pair
 * of boots still has to have a head, and a pair of goggles still has to have legs -- they simply have nothing in
 * them. Anything with nothing in it costs nothing to not draw, which is why it is cheaper to hand over an empty
 * head than to argue with the layer about it.
 * <p>
 * Where each part hangs from is the game's own arrangement and not ours to pick.
 */
public final class WornShape {
    /** Where each part of a body hangs from. Straight out of the humanoid model everything else is drawn on. */
    private static final Map<String, PartPose> HANGS = Map.of(
            "head", PartPose.ZERO,
            "hat", PartPose.ZERO,
            "body", PartPose.ZERO,
            "right_arm", PartPose.offset(-5.0F, 2.0F, 0.0F),
            "left_arm", PartPose.offset(5.0F, 2.0F, 0.0F),
            "right_leg", PartPose.offset(-1.9F, 12.0F, 0.0F),
            "left_leg", PartPose.offset(1.9F, 12.0F, 0.0F));

    private WornShape() {
    }

    /** A body with shapes in the named parts and nothing anywhere else. */
    public static LayerDefinition only(Map<String, CubeListBuilder> filled) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        HANGS.forEach((part, hangs) ->
                root.addOrReplaceChild(part, filled.getOrDefault(part, CubeListBuilder.create()), hangs));
        return LayerDefinition.create(mesh, 64, 32);
    }
}
