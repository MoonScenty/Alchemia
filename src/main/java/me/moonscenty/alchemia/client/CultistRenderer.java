package me.moonscenty.alchemia.client;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.entity.cult.Cultist;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * A cultist as the original drew one: a plain body in the cult's one skin, which is the old sixty-four by thirty-two
 * sheet, and everything that tells the knight from the cleric is what they have on -- the robe and the plate are
 * drawn on the original's own models.
 */
public class CultistRenderer extends HumanoidMobRenderer<Cultist, HumanoidModel<Cultist>> {
    private static final ResourceLocation SKIN = Alchemia.id("textures/entity/cultist.png");

    public CultistRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(
                LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F), 64, 32).bakeRoot()),
                0.5F);
        addLayer(new HumanoidArmorLayer<>(this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()));
    }

    @Override
    public ResourceLocation getTextureLocation(Cultist cultist) {
        return SKIN;
    }
}
