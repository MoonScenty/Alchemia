package me.moonscenty.alchemia.client;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import me.moonscenty.alchemia.client.legacy.LegacyModels;
import me.moonscenty.alchemia.client.legacy.model.LegacyMesh;
import me.moonscenty.alchemia.golem.Golem;
import me.moonscenty.alchemia.golem.GolemDesign;
import me.moonscenty.alchemia.golem.GolemParts;
import me.moonscenty.alchemia.golem.GolemParts.Attach;
import me.moonscenty.alchemia.golem.GolemParts.Look;
import me.moonscenty.alchemia.golem.GolemTrait;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Draws a golem as the original put one together: a body of chest, waist, head and two arms out of one mesh, all in
 * the picture of what it is made of, with each of its parts hung on where it belongs. A head part goes on the head
 * and turns with it; arms go on each arm, legs on each leg, and the rest on the body.
 *
 * <p>Its arms swing as it walks and come up together to carry something, which it holds out in front of it. It leans
 * into its going, more if it rolls or flies. A few parts move of their own: claws snap when it strikes, grinders spin
 * up and run down, dart launchers come up to aim, wheels turn as far as it rolled, and a hauler's basket shows what
 * is in its second hand.
 *
 * <p>Not a {@link LivingEntityRenderer}: that turns the world upside down for a model built the way a player's is,
 * and these meshes are built the right way up.
 */
public class GolemRenderer extends EntityRenderer<Golem> {
    private static final String BASE = "golem_base";
    /** How far a hurt golem rolls over when it dies, as any creature does. */
    private static final float DEATH_ROLL = 90.0F;

    /** Each grinder's speed and how far round it has come, by golem. */
    private final Map<Golem, float[]> grinders = new WeakHashMap<>();

    public GolemRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.3F;
    }

    @Override
    public void render(Golem golem, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers,
            int light) {
        Minecraft minecraft = Minecraft.getInstance();
        boolean seen = !golem.isInvisible();
        boolean ghostly = !seen && !golem.isInvisibleTo(minecraft.player);
        if (seen || ghostly) {
            LegacyMesh base = mesh(BASE);
            if (base != null) {
                pose.pushPose();
                float body = Mth.rotLerp(partialTick, golem.yBodyRotO, golem.yBodyRot);
                float head = Mth.rotLerp(partialTick, golem.yHeadRotO, golem.yHeadRot);
                pose.mulPose(Axis.YP.rotationDegrees(180.0F - body));
                if (golem.deathTime > 0) {
                    float fall = Math.min(1.0F, Mth.sqrt((golem.deathTime + partialTick - 1.0F) / 20.0F * 1.6F));
                    pose.mulPose(Axis.ZP.rotationDegrees(fall * DEATH_ROLL));
                }
                float swing = 0.0F;
                float amount = 0.0F;
                if (golem.isAlive()) {
                    swing = golem.walkAnimation.position(partialTick);
                    amount = Math.min(1.0F, golem.walkAnimation.speed(partialTick));
                }
                new Drawing(golem, base, partialTick, pose, buffers, light,
                        LivingEntityRenderer.getOverlayCoords(golem, 0.0F), ghostly ? 0x26FFFFFF : -1)
                        .draw(swing, amount, golem.tickCount + partialTick, Mth.wrapDegrees(head - body),
                                Mth.lerp(partialTick, golem.xRotO, golem.getXRot()));
                pose.popPose();
            }
        }
        super.render(golem, yaw, partialTick, pose, buffers, light);
    }

    /** One golem being drawn, with everything every part of it needs to know. */
    private final class Drawing {
        private final Golem golem;
        private final GolemDesign design;
        private final LegacyMesh base;
        private final float partialTick;
        private final PoseStack pose;
        private final MultiBufferSource buffers;
        private final int light;
        private final int overlay;
        private final int tint;
        private final ResourceLocation material;

        Drawing(Golem golem, LegacyMesh base, float partialTick, PoseStack pose, MultiBufferSource buffers,
                int light, int overlay, int tint) {
            this.golem = golem;
            this.design = golem.design();
            this.base = base;
            this.partialTick = partialTick;
            this.pose = pose;
            this.buffers = buffers;
            this.light = light;
            this.overlay = overlay;
            this.tint = tint;
            this.material = design.material().texture();
        }

        /** The original's numbers throughout. */
        void draw(float swing, float amount, float age, float headYaw, float headPitch) {
            boolean holding = !golem.getMainHandItem().isEmpty();
            boolean rolls = golem.has(GolemTrait.WHEELED) || golem.has(GolemTrait.FLYER);
            double dx = golem.getX() - golem.xo;
            double dz = golem.getZ() - golem.zo;
            double speed = dx * dx + dz * dz;
            float sway = (float) Math.toDegrees(Mth.sin(age * 0.067F) * 0.03F);
            float spread = (float) Math.toDegrees(Mth.cos(age * 0.09F) * 0.05F + 0.05F);
            float rightX = 0.0F;
            float rightY = 0.0F;
            float rightZ = 0.0F;
            float leftX = 0.0F;
            float leftY = 0.0F;
            float leftZ = 0.0F;
            float twist = 0.0F;
            if (holding) {
                rightX = 90.0F - spread / 2.0F;
                rightZ = -2.0F;
                leftX = 90.0F - spread / 2.0F;
                leftZ = 2.0F;
            } else {
                if (rolls) {
                    rightX = sway * 2.0F;
                    leftX = -sway * 2.0F;
                } else {
                    rightX = (float) (Math.toDegrees(Mth.cos(swing * 0.6662F + Mth.PI) * amount) + sway);
                    leftX = (float) (Math.toDegrees(Mth.cos(swing * 0.6662F) * amount) - sway);
                }
                rightZ += spread + 2.0F;
                leftZ -= spread + 2.0F;
            }
            float attack = golem.getAttackAnim(partialTick);
            if (attack > 0.0F) {
                float wiggle = -Mth.sin(Mth.sqrt(attack) * Mth.PI * 2.0F) * 0.2F;
                twist = (float) Math.toDegrees(wiggle);
                rightZ = -(float) Math.toDegrees(Mth.sin(wiggle) * 3.0F);
                rightX = (float) Math.toDegrees(-Mth.cos(wiggle) * 5.0F);
                rightY += twist;
            }

            pose.mulPose(Axis.YP.rotationDegrees(twist));
            float lean = rolls ? 75.0F : 25.0F;
            pose.mulPose(Axis.XN.rotationDegrees((float) (speed * lean)));
            pose.mulPose(Axis.ZN.rotationDegrees((float) (speed * lean * 0.06 * (golem.getYRot() - golem.yRotO))));

            pose.pushPose();
            pose.translate(0.0F, 0.5F, 0.0F);
            group(base, "chest", material, tint);
            group(base, "waist", material, tint);
            if (golem.colour() > 0) {
                int flag = DyeColor.byId(golem.colour() - 1).getMapColor().col;
                group(base, "flag", material, (tint & 0xFF000000) | flag);
            }
            parts(Attach.BODY, Side.MIDDLE);
            pose.popPose();

            pose.pushPose();
            pose.translate(0.0F, 0.75F, -0.03125F);
            pose.mulPose(Axis.YN.rotationDegrees(headYaw));
            pose.mulPose(Axis.XN.rotationDegrees(headPitch));
            parts(Attach.HEAD, Side.MIDDLE);
            group(base, "head", material, tint);
            pose.popPose();

            boolean aiming = design.arms() == GolemParts.Arms.DARTS && golem.fighting();
            pose.pushPose();
            pose.translate(0.20625F, 0.6875F, 0.0F);
            if (aiming) {
                rightX = 90.0F - golem.xRotO + rightX / 10.0F;
                rightY /= 10.0F;
                rightZ /= 10.0F;
            }
            pose.mulPose(Axis.XP.rotationDegrees(rightX));
            pose.mulPose(Axis.YP.rotationDegrees(rightY));
            pose.mulPose(Axis.ZP.rotationDegrees(rightZ));
            group(base, "arm", material, tint);
            parts(Attach.ARMS, Side.RIGHT);
            pose.popPose();

            pose.pushPose();
            pose.translate(-0.20625F, 0.6875F, 0.0F);
            if (aiming) {
                leftX = 90.0F - golem.xRotO + leftX / 10.0F;
                leftY /= 10.0F;
                leftZ /= 10.0F;
            }
            pose.mulPose(Axis.XP.rotationDegrees(leftX));
            pose.mulPose(Axis.YP.rotationDegrees(leftY + 180.0F));
            pose.mulPose(Axis.ZN.rotationDegrees(leftZ));
            group(base, "arm", material, tint);
            parts(Attach.ARMS, Side.LEFT);
            pose.popPose();

            pose.pushPose();
            pose.translate(0.09375F, 0.375F, 0.0F);
            pose.mulPose(Axis.XP.rotationDegrees((float) Math.toDegrees(Mth.cos(swing * 0.6662F) * amount)));
            parts(Attach.LEGS, Side.RIGHT);
            pose.popPose();

            pose.pushPose();
            pose.translate(-0.09375F, 0.375F, 0.0F);
            pose.mulPose(Axis.XP.rotationDegrees(
                    (float) Math.toDegrees(Mth.cos(swing * 0.6662F + Mth.PI) * amount)));
            parts(Attach.LEGS, Side.LEFT);
            pose.popPose();

            pose.pushPose();
            pose.translate(0.0F, 0.625F, 0.0F);
            pose.mulPose(Axis.XP.rotationDegrees(90.0F - spread * 0.5F));
            held(golem.getMainHandItem(), -1.0F, 0.25F);
            pose.popPose();
        }

        /** Every part of the golem hung on at this point, drawn for this side. */
        private void parts(Attach at, Side side) {
            for (GolemParts.Part part : design.parts()) {
                Look look = part.look();
                if (look != null && look.attach() == at) {
                    part(part, look, side);
                }
            }
        }

        /**
         * One part's mesh, a group at a time: the groups named {@code bm...} in the golem's material and the rest
         * in the part's own picture, each moved first if it is one that moves.
         */
        private void part(GolemParts.Part part, Look look, Side side) {
            LegacyMesh mesh = mesh(look.mesh());
            if (mesh == null) {
                return;
            }
            ResourceLocation own = look.textureLocation();
            for (int group = 0; group < mesh.groups(); group++) {
                String name = mesh.name(group);
                pose.pushPose();
                move(part, name, side);
                ResourceLocation picture = name.startsWith("bm") || own == null ? material : own;
                mesh.render(group, pose.last(), buffers.getBuffer(RenderType.entityTranslucent(picture)), tint,
                        light, overlay);
                pose.popPose();
            }
            if (part == GolemParts.Addon.HAULER) {
                ItemStack[] carried = golem.carrying();
                if (carried.length > 1) {
                    pose.pushPose();
                    held(carried[1], 0.825F, -0.25F);
                    pose.popPose();
                }
            }
        }

        /** What the moving parts do before they are drawn. */
        private void move(GolemParts.Part part, String name, Side side) {
            if (part == GolemParts.Arms.CLAWS && name.startsWith("claw")) {
                float snap = golem.getAttackAnim(partialTick) * 4.1F;
                snap *= snap;
                pose.translate(0.0F, -0.2F, 0.0F);
                pose.mulPose((name.endsWith("1") ? Axis.XP : Axis.XN).rotationDegrees(snap));
            } else if (part == GolemParts.Arms.BREAKERS && name.equals("grinder")) {
                float[] grinder = grinders.computeIfAbsent(golem, key -> new float[2]);
                float spin = Math.max(grinder[0], golem.getAttackAnim(partialTick) * 20.0F);
                grinder[1] += spin;
                grinder[0] = spin * 0.99F;
                pose.translate(0.0F, -0.34F, 0.0F);
                pose.mulPose((side == Side.LEFT ? Axis.XN : Axis.XP).rotationDegrees(
                        (golem.tickCount + partialTick) / 2.0F + grinder[1] + (side == Side.LEFT ? 22 : 0)));
            } else if (part == GolemParts.Legs.ROLLER && name.equals("wheel")) {
                pose.translate(0.0F, -0.375F, 0.0F);
                pose.mulPose(Axis.XN.rotationDegrees(GolemEffects.wheel(golem)));
            }
        }

        private void group(LegacyMesh mesh, String name, ResourceLocation picture, int colour) {
            int group = mesh.group(name);
            if (group >= 0) {
                mesh.render(group, pose.last(), buffers.getBuffer(RenderType.entityTranslucent(picture)), colour,
                        light, overlay);
            }
        }

        /** Something it is carrying, held out in front or sat in its basket. */
        private void held(@Nullable ItemStack stack, float out, float flat) {
            if (stack == null || stack.isEmpty()) {
                return;
            }
            pose.pushPose();
            if (out < 0.0F) {
                pose.mulPose(Axis.XN.rotationDegrees(90.0F));
            }
            pose.scale(0.375F, 0.375F, 0.375F);
            pose.translate(0.0F, 0.33F, out);
            if (!(stack.getItem() instanceof BlockItem)) {
                pose.translate(0.0F, 0.0F, flat);
            }
            Minecraft.getInstance().getItemRenderer().renderStatic(golem, stack, ItemDisplayContext.NONE, false,
                    pose, buffers, golem.level(), light, overlay, golem.getId());
            pose.popPose();
        }
    }

    private enum Side {
        LEFT,
        RIGHT,
        MIDDLE
    }

    // --- the meshes ---------------------------------------------------------------------------------------

    private static final Map<String, LegacyMesh> MESHES = new HashMap<>();
    private static int meshesFrom = -1;

    /** One of the original's golem meshes as of the last import, turned over for the loader it was read with. */
    @Nullable
    private static LegacyMesh mesh(String name) {
        int generation = LegacyModels.generation();
        if (generation != meshesFrom) {
            meshesFrom = generation;
            MESHES.clear();
        }
        return MESHES.computeIfAbsent(name, key -> LegacyModels.mesh("models/legacy/golem/" + key + ".obj")
                .map(LegacyMesh::flippedV).orElse(null));
    }

    @Override
    public ResourceLocation getTextureLocation(Golem golem) {
        return golem.design().material().texture();
    }
}
