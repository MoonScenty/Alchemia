package me.moonscenty.alchemia.client.armour;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.LivingEntity;

/**
 * A traveller's boot as it is worn, rather than as it sits in a slot.
 * <p>
 * Armour is normally two flat sheets stretched over the body, which is why a vanilla boot is the leg again half
 * a pixel wider. That is fine for a plate and no good at all for a boot, because a boot has a foot: something
 * that sticks out in front of the leg and is the whole of what makes it read as footwear rather than as trousers.
 * So this is a model, and the shapes in it are the ones drawn in Blockbench.
 * <p>
 * The shape and its sheet are both written out by {@code tools/gen_boots.py}; this is only the wiring. Every
 * other part is left empty -- the layer that draws armour shows the legs alone for a boot, and a part with
 * nothing in it costs nothing to not draw.
 */
public class TravellerBootsModel extends HumanoidModel<LivingEntity> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(Alchemia.id("traveller_boots"), "main");

    /** Where each leg hangs from, which is the game's own numbers and not ours to choose. */
    private static final float LEG_ASIDE = 1.9F;
    private static final float LEG_DOWN = 12.0F;

    public TravellerBootsModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // the parts a humanoid model insists on having, whether or not a boot has anything to put in them
        for (String bare : new String[] {"head", "hat", "body", "right_arm", "left_arm"}) {
            root.addOrReplaceChild(bare, CubeListBuilder.create(), PartPose.ZERO);
        }
        root.addOrReplaceChild("right_leg", TravellerBootsShape.boot(),
                PartPose.offset(-LEG_ASIDE, LEG_DOWN, 0.0F));
        root.addOrReplaceChild("left_leg", TravellerBootsShape.boot(),
                PartPose.offset(LEG_ASIDE, LEG_DOWN, 0.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }
}
