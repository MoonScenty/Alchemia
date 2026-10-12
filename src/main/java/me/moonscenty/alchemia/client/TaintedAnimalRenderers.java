package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.entity.TaintedChicken;
import me.moonscenty.alchemia.entity.TaintedCow;
import me.moonscenty.alchemia.entity.TaintedCreeper;
import me.moonscenty.alchemia.entity.TaintedPig;
import me.moonscenty.alchemia.entity.TaintedSheep;
import me.moonscenty.alchemia.entity.TaintedVillager;
import net.minecraft.client.model.ChickenModel;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.model.CreeperModel;
import net.minecraft.client.model.PigModel;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.RabbitModel;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RabbitRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.SlimeRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.item.DyeColor;

/**
 * The animals the taint has made over, drawn as the original drew them: the game's own models, which have not changed
 * shape since, in the original's tainted skins.
 */
public final class TaintedAnimalRenderers {
    private TaintedAnimalRenderers() {
    }

    private static ResourceLocation skin(String name) {
        return Alchemia.id("textures/entity/tainted_" + name + ".png");
    }

    public static class Cow extends MobRenderer<TaintedCow, CowModel<TaintedCow>> {
        private static final ResourceLocation SKIN = skin("cow");

        public Cow(EntityRendererProvider.Context context) {
            super(context, new CowModel<>(context.bakeLayer(ModelLayers.COW)), 0.7F);
        }

        @Override
        public ResourceLocation getTextureLocation(TaintedCow cow) {
            return SKIN;
        }
    }

    public static class Pig extends MobRenderer<TaintedPig, PigModel<TaintedPig>> {
        private static final ResourceLocation SKIN = skin("pig");

        public Pig(EntityRendererProvider.Context context) {
            super(context, new PigModel<>(context.bakeLayer(ModelLayers.PIG)), 0.7F);
        }

        @Override
        public ResourceLocation getTextureLocation(TaintedPig pig) {
            return SKIN;
        }
    }

    /** A chicken, its wings beating as a chicken's do. */
    public static class Chicken extends MobRenderer<TaintedChicken, ChickenModel<TaintedChicken>> {
        private static final ResourceLocation SKIN = skin("chicken");

        public Chicken(EntityRendererProvider.Context context) {
            super(context, new ChickenModel<>(context.bakeLayer(ModelLayers.CHICKEN)), 0.3F);
        }

        @Override
        protected float getBob(TaintedChicken chicken, float partial) {
            float flap = Mth.lerp(partial, chicken.oFlap, chicken.flap);
            float speed = Mth.lerp(partial, chicken.oFlapSpeed, chicken.flapSpeed);
            return (Mth.sin(flap) + 1.0F) * speed;
        }

        @Override
        public ResourceLocation getTextureLocation(TaintedChicken chicken) {
            return SKIN;
        }
    }

    /** A villager, a sixteenth smaller, as the original drew it. */
    public static class Villager extends MobRenderer<TaintedVillager, VillagerModel<TaintedVillager>> {
        private static final ResourceLocation SKIN = skin("villager");

        public Villager(EntityRendererProvider.Context context) {
            super(context, new VillagerModel<>(context.bakeLayer(ModelLayers.VILLAGER)), 0.5F);
            // the original's skin was drawn before villagers wore hats, and has nothing where the hat would be
            getModel().hatVisible(false);
        }

        @Override
        protected void scale(TaintedVillager villager, PoseStack pose, float partial) {
            pose.scale(0.9375F, 0.9375F, 0.9375F);
        }

        @Override
        public ResourceLocation getTextureLocation(TaintedVillager villager) {
            return SKIN;
        }
    }

    /** A creeper, swelling and flashing white as it comes to going off, as a creeper does. */
    public static class Creeper extends MobRenderer<TaintedCreeper, CreeperModel<TaintedCreeper>> {
        private static final ResourceLocation SKIN = skin("creeper");

        public Creeper(EntityRendererProvider.Context context) {
            super(context, new CreeperModel<>(context.bakeLayer(ModelLayers.CREEPER)), 0.5F);
        }

        @Override
        protected void scale(TaintedCreeper creeper, PoseStack pose, float partial) {
            float swell = creeper.swelling(partial);
            float wobble = 1.0F + Mth.sin(swell * 100.0F) * swell * 0.01F;
            swell = Mth.clamp(swell, 0.0F, 1.0F);
            swell *= swell;
            swell *= swell;
            float across = (1.0F + swell * 0.4F) * wobble;
            float up = (1.0F + swell * 0.1F) / wobble;
            pose.scale(across, up, across);
        }

        @Override
        protected float getWhiteOverlayProgress(TaintedCreeper creeper, float partial) {
            float swell = creeper.swelling(partial);
            return (int) (swell * 10.0F) % 2 == 0 ? 0.0F : Mth.clamp(swell, 0.5F, 1.0F);
        }

        @Override
        public ResourceLocation getTextureLocation(TaintedCreeper creeper) {
            return SKIN;
        }
    }

    /** A sheep, its head going down to graze, its fleece dyed purple unless it has been shorn. */
    public static class Sheep extends MobRenderer<TaintedSheep, SheepBody> {
        private static final ResourceLocation SKIN = skin("sheep");

        public Sheep(EntityRendererProvider.Context context) {
            super(context, new SheepBody(context.bakeLayer(ModelLayers.SHEEP)), 0.7F);
            addLayer(new Fleece(this, new SheepBody(context.bakeLayer(ModelLayers.SHEEP_FUR))));
        }

        @Override
        public ResourceLocation getTextureLocation(TaintedSheep sheep) {
            return SKIN;
        }
    }

    /** A sheep's body or its fleece, laid on a made-over sheep: its head lowered to graze as the sheep's is. */
    public static class SheepBody extends QuadrupedModel<TaintedSheep> {
        private float headXRot;

        public SheepBody(ModelPart root) {
            super(root, false, 8.0F, 4.0F, 2.0F, 2.0F, 24);
        }

        @Override
        public void prepareMobModel(TaintedSheep sheep, float limbSwing, float limbSwingAmount, float partial) {
            super.prepareMobModel(sheep, limbSwing, limbSwingAmount, partial);
            head.y = 6.0F + sheep.headEatPositionScale(partial) * 9.0F;
            headXRot = sheep.headEatAngleScale(partial);
        }

        @Override
        public void setupAnim(TaintedSheep sheep, float limbSwing, float limbSwingAmount, float ageInTicks,
                float netHeadYaw, float headPitch) {
            super.setupAnim(sheep, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            head.xRot = headXRot;
        }
    }

    /** The purple fleece, the original's picture of it dyed as the original dyed it. */
    private static final class Fleece extends RenderLayer<TaintedSheep, SheepBody> {
        private static final ResourceLocation FUR = Alchemia.id("textures/entity/tainted_sheep_fur.png");
        private final SheepBody fur;

        Fleece(RenderLayerParent<TaintedSheep, SheepBody> parent, SheepBody fur) {
            super(parent);
            this.fur = fur;
        }

        @Override
        public void render(PoseStack pose, MultiBufferSource buffers, int light, TaintedSheep sheep, float limbSwing,
                float limbSwingAmount, float partial, float age, float netHeadYaw, float headPitch) {
            if (sheep.isSheared() || sheep.isInvisible()) {
                return;
            }
            coloredCutoutModelCopyLayerRender(getParentModel(), fur, FUR, pose, buffers, light, sheep, limbSwing,
                    limbSwingAmount, age, netHeadYaw, headPitch, partial,
                    DyeColor.PURPLE.getTextureDiffuseColor());
        }
    }

    /** A rabbit of whatever colour it was, with the taint laid over it. */
    public static class TaintedRabbit extends RabbitRenderer {
        private static final ResourceLocation OVERLAY = Alchemia.id("textures/entity/tainted_rabbit.png");

        public TaintedRabbit(EntityRendererProvider.Context context) {
            super(context);
            addLayer(new RenderLayer<>(this) {
                @Override
                public void render(PoseStack pose, MultiBufferSource buffers, int light, Rabbit rabbit,
                        float limbSwing, float limbSwingAmount, float partial, float age, float netHeadYaw,
                        float headPitch) {
                    if (rabbit.isInvisible()) {
                        return;
                    }
                    pose.pushPose();
                    pose.translate(0.0, -0.01, 0.0);
                    pose.scale(1.01F, 1.01F, 1.01F);
                    RabbitModel<Rabbit> model = getParentModel();
                    model.renderToBuffer(pose, buffers.getBuffer(RenderType.entityTranslucent(OVERLAY)), light,
                            LivingEntityRenderer.getOverlayCoords(rabbit, 0.0F));
                    pose.popPose();
                }
            });
        }
    }

    /** A slime in the original's purple. */
    public static class AlchemicSlime extends SlimeRenderer {
        private static final ResourceLocation SKIN = Alchemia.id("textures/entity/alchemic_slime.png");

        public AlchemicSlime(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public ResourceLocation getTextureLocation(Slime slime) {
            return SKIN;
        }
    }
}
