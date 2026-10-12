package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.entity.FireBat;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Draws a firebat as the original did: small, burning at full brightness whatever the hour, bobbing as it flies,
 * and a vampire in a skin of its own. A devil or a vampire is drawn nearly twice the size of the others.
 */
public class FireBatRenderer extends MobRenderer<FireBat, FireBatModel> {
    private static final ResourceLocation FIRE = Alchemia.id("textures/entity/firebat.png");
    private static final ResourceLocation VAMPIRE = Alchemia.id("textures/entity/vampirebat.png");
    private static final float SMALL = 0.35F;
    private static final float LARGE = 0.6F;

    public FireBatRenderer(EntityRendererProvider.Context context) {
        super(context, new FireBatModel(), 0.25F);
    }

    @Override
    public ResourceLocation getTextureLocation(FireBat bat) {
        return bat.isVampire() ? VAMPIRE : FIRE;
    }

    @Override
    protected void scale(FireBat bat, PoseStack pose, float partial) {
        float size = bat.isDevil() || bat.isVampire() ? LARGE : SMALL;
        pose.scale(size, size, size);
    }

    @Override
    protected void setupRotations(FireBat bat, PoseStack pose, float bob, float yBodyRot, float partial,
            float scale) {
        pose.translate(0.0F, bat.isHanging() ? -0.1F : Mth.cos(bob * 0.3F) * 0.1F, 0.0F);
        super.setupRotations(bat, pose, bob, yBodyRot, partial, scale);
    }

    /** It is a fire, and lights itself. */
    @Override
    protected int getBlockLightLevel(FireBat bat, BlockPos pos) {
        return 15;
    }

    @Override
    protected int getSkyLightLevel(FireBat bat, BlockPos pos) {
        return 15;
    }
}
