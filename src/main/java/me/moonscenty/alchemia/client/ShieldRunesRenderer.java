package me.moonscenty.alchemia.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.client.legacy.LegacyAssets;
import me.moonscenty.alchemia.client.legacy.LegacyModels;
import me.moonscenty.alchemia.client.legacy.model.LegacyMesh;
import me.moonscenty.alchemia.entity.cult.Cultist;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/**
 * The runes of a shield flaring where a blow lands, as the original drew them: a half-shell of runes round the one
 * shielded, turned toward the blow, that burns up through fifteen frames in a third of a second or so. A monster's
 * shield ripples instead of burning.
 */
@EventBusSubscriber(modid = Alchemia.MODID, value = Dist.CLIENT)
public final class ShieldRunesRenderer {
    private static final List<Flare> FLARES = new ArrayList<>();
    private static int bakedFrom = -1;
    private static LegacyMesh mesh;

    private record Flare(Entity shielded, float yaw, float pitch, boolean ripple, long born, int lasts) {
    }

    private ShieldRunesRenderer() {
    }

    /** A blow on someone's shield, from the server. */
    public static void flare(int shieldedId, int from) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        Entity shielded = level.getEntity(shieldedId);
        if (shielded == null) {
            return;
        }
        boolean ripple = shielded instanceof Monster && !(shielded instanceof Cultist);
        if (from >= 0) {
            Entity source = level.getEntity(from);
            float yaw = 0.0F;
            float pitch = 90.0F;
            if (source != null) {
                Vec3 d = shielded.getBoundingBox().getCenter().subtract(source.getBoundingBox().getCenter());
                double across = Math.sqrt(d.x * d.x + d.z * d.z);
                yaw = (float) (Mth.atan2(d.z, d.x) * 180.0 / Math.PI) - 90.0F;
                pitch = (float) -(Mth.atan2(d.y, across) * 180.0 / Math.PI);
            }
            add(level, shielded, yaw, pitch, ripple);
        } else if (from == -1) {
            add(level, shielded, 0.0F, 90.0F, ripple);
            add(level, shielded, 0.0F, 270.0F, ripple);
        } else if (from == -2) {
            add(level, shielded, 0.0F, 270.0F, ripple);
        } else {
            add(level, shielded, 0.0F, 90.0F, ripple);
        }
    }

    private static void add(ClientLevel level, Entity shielded, float yaw, float pitch, boolean ripple) {
        FLARES.add(new Flare(shielded, yaw, pitch, ripple, level.getGameTime(), 8 + level.random.nextInt(4)));
    }

    private static boolean refresh() {
        if (bakedFrom != LegacyModels.generation()) {
            bakedFrom = LegacyModels.generation();
            mesh = LegacyModels.mesh(LegacyAssets.HEMIS_MESH).map(LegacyMesh::flippedV).orElse(null);
        }
        return mesh != null;
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || FLARES.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || !refresh()) {
            FLARES.clear();
            return;
        }
        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        Vec3 camera = event.getCamera().getPosition();
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        for (Iterator<Flare> it = FLARES.iterator(); it.hasNext();) {
            Flare flare = it.next();
            float age = level.getGameTime() - flare.born() + partial;
            if (age >= flare.lasts() || flare.shielded().isRemoved()) {
                it.remove();
                continue;
            }
            float fade = age / flare.lasts();
            int frame = Math.min(15, (int) (14 * fade) + 1);
            float alpha = Math.min(1.0F, (1.0F - fade) * 3.0F);
            ResourceLocation texture = Alchemia.id("textures/entity/" + (flare.ripple() ? "ripple" : "hemis")
                    + frame + ".png");
            Vec3 at = flare.shielded().getPosition(partial).add(0.0, flare.shielded().getBbHeight() / 2.0, 0.0);
            pose.pushPose();
            pose.translate(at.x - camera.x, at.y - camera.y, at.z - camera.z);
            pose.mulPose(Axis.YP.rotationDegrees(180.0F - flare.yaw()));
            pose.mulPose(Axis.XP.rotationDegrees(-flare.pitch()));
            float scale = 0.4F * flare.shielded().getBbHeight();
            pose.scale(scale, scale, scale);
            int shade = flare.ripple() ? 128 : 255;
            int colour = (int) (alpha * 255) << 24 | shade << 16 | shade << 8 | shade;
            mesh.render(pose.last(), buffers.getBuffer(RenderType.eyes(texture)), colour,
                    LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
            pose.popPose();
        }
        buffers.endBatch();
    }
}
