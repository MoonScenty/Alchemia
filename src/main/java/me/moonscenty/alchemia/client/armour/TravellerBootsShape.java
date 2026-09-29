package me.moonscenty.alchemia.client.armour;

import net.minecraft.client.model.geom.builders.CubeListBuilder;

/**
 * The shape of a traveller's boot, as it was drawn.
 * <p>
 * Written out by {@code tools/gen_boots.py} from {@code thaumref/models/boots/model.json}, along with the sheet
 * it is textured from. Every number here answers to a box in that drawing, so this file is not the place to
 * change any of them -- change the drawing and run the tool.
 * <p>
 * One boot. It is hung off each leg in turn, and the pair in the drawing are mirrors of one another, so carrying
 * both across would have been carrying the same boot twice.
 */
public final class TravellerBootsShape {
    private TravellerBootsShape() {
    }

    /** The boxes of one boot, in leg space: the leg's foot is at twelve and the ground is below it. */
    public static CubeListBuilder boot() {
        return CubeListBuilder.create()
                // left_sole
                .texOffs(0, 0)
                .addBox(-2.50F, 10.00F, -6.50F, 5.00F, 2.00F, 9.00F)
                // left_foot
                .texOffs(28, 0)
                .addBox(-2.50F, 7.00F, -5.50F, 5.00F, 3.00F, 8.00F)
                // left_ankle
                .texOffs(0, 11)
                .addBox(-2.50F, 3.00F, -2.50F, 5.00F, 4.00F, 5.00F)
                // left_band
                .texOffs(20, 11)
                .addBox(-2.70F, 5.00F, -2.70F, 5.40F, 1.50F, 5.40F);
    }
}
