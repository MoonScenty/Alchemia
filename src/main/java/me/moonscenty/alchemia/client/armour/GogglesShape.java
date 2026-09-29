package me.moonscenty.alchemia.client.armour;

import net.minecraft.client.model.geom.builders.CubeListBuilder;

/**
 * The shape of the goggles of revealing, as they were drawn.
 * <p>
 * Written out by {@code tools/gen_goggles.py} from {@code thaumref/models/goggles/model.json}, along with the
 * sheet they are textured from. Every number here answers to a box in that drawing, so this file is not the
 * place to change any of them -- change the drawing and run the tool.
 */
public final class GogglesShape {
    private GogglesShape() {
    }

    /** The boxes, in head space: the head hangs from the neck, so its top is at minus eight. */
    public static CubeListBuilder goggles() {
        return CubeListBuilder.create()
                // band
                .texOffs(0, 0)
                .addBox(-5.00F, -6.00F, -4.20F, 10.00F, 4.00F, 2.00F)
                // left_cup
                .texOffs(24, 0)
                .addBox(-4.50F, -6.20F, -5.40F, 4.00F, 4.40F, 1.20F)
                // right_cup
                .texOffs(35, 0)
                .addBox(0.50F, -6.20F, -5.40F, 4.00F, 4.40F, 1.20F)
                // left_lens
                .texOffs(46, 0)
                .addBox(-4.00F, -5.70F, -5.80F, 3.00F, 3.40F, 0.50F)
                // right_lens
                .texOffs(53, 0)
                .addBox(1.00F, -5.70F, -5.80F, 3.00F, 3.40F, 0.50F)
                // left_strap
                .texOffs(0, 6)
                .addBox(-5.40F, -5.40F, -2.20F, 1.00F, 2.40F, 7.40F)
                // right_strap
                .texOffs(17, 6)
                .addBox(4.40F, -5.40F, -2.20F, 1.00F, 2.40F, 7.40F)
                // back_strap
                .texOffs(34, 6)
                .addBox(-5.40F, -5.40F, 4.40F, 10.80F, 2.40F, 0.80F)
                // cube8
                .texOffs(0, 16)
                .addBox(-2.00F, -6.00F, 4.80F, 4.00F, 1.00F, 1.00F)
                // cube9
                .texOffs(10, 16)
                .addBox(-2.00F, -5.00F, 4.80F, 1.00F, 1.50F, 1.00F)
                // cube10
                .texOffs(14, 16)
                .addBox(-2.00F, -3.50F, 4.80F, 4.00F, 1.00F, 1.00F)
                // cube11
                .texOffs(24, 16)
                .addBox(1.00F, -5.00F, 4.80F, 1.00F, 1.50F, 1.00F);
    }
}
