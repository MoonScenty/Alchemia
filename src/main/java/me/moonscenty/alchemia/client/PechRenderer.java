package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.entity.Pech;
import me.moonscenty.alchemia.item.WandItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * A pech in the original's skin for its kind -- forager, mage or stalker -- with whatever it holds in its right hand.
 */
public class PechRenderer extends MobRenderer<Pech, PechModel> {
    private static final ResourceLocation[] SKINS = {
            Alchemia.id("textures/entity/pech_forage.png"),
            Alchemia.id("textures/entity/pech_thaum.png"),
            Alchemia.id("textures/entity/pech_stalker.png")};

    public PechRenderer(EntityRendererProvider.Context context) {
        super(context, new PechModel(), 0.25F);
        addLayer(new HeldItem(this, context));
    }

    @Override
    public ResourceLocation getTextureLocation(Pech pech) {
        return SKINS[Math.floorMod(pech.getKind(), SKINS.length)];
    }

    /**
     * What it holds, in its right hand. Its arms are shorter than a biped's, so the original held things higher up
     * them than a biped does; it lifted a wand and a bow a little further still.
     */
    private static final class HeldItem extends RenderLayer<Pech, PechModel> {
        /** How much higher up the arm than a biped's hand the original put a pech's: 7/16 against 0.32. */
        private static final float SHORTER = 0.4375F - 0.32F;
        private final net.minecraft.client.renderer.ItemInHandRenderer items;

        HeldItem(RenderLayerParent<Pech, PechModel> parent, EntityRendererProvider.Context context) {
            super(parent);
            this.items = context.getItemInHandRenderer();
        }

        @Override
        public void render(PoseStack pose, MultiBufferSource buffers, int light, Pech pech, float limbSwing,
                float limbSwingAmount, float partial, float age, float netHeadYaw, float headPitch) {
            ItemStack held = pech.getMainHandItem();
            if (held.isEmpty()) {
                return;
            }
            pose.pushPose();
            getParentModel().translateToHand(HumanoidArm.RIGHT, pose);
            pose.translate(0.0F, -SHORTER, 0.0F);
            if (held.getItem() instanceof WandItem) {
                pose.translate(0.0F, -0.1F, 0.0F);
            } else if (held.is(Items.BOW)) {
                pose.translate(-0.075F, -0.1F, 0.0F);
            }
            pose.mulPose(Axis.XP.rotationDegrees(-90.0F));
            pose.mulPose(Axis.YP.rotationDegrees(180.0F));
            pose.translate(1.0F / 16.0F, 0.125F, -0.625F);
            items.renderItem(pech, held, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, false, pose, buffers, light);
            pose.popPose();
        }
    }
}
