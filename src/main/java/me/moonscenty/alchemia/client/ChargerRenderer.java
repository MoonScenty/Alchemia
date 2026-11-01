package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.entity.ArcaneWorkbenchChargerBlockEntity;
import me.moonscenty.alchemia.client.legacy.LegacyAssets;
import me.moonscenty.alchemia.client.legacy.LegacyCached;
import me.moonscenty.alchemia.client.legacy.model.LegacyMesh;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ChargerRenderer implements BlockEntityRenderer<ArcaneWorkbenchChargerBlockEntity> {
    private static final ResourceLocation TEXTURE = Alchemia.id("textures/entity/vis_relay.png");
    private static final int GLOW_DIM = 50;
    private static final int GLOW_RANGE = 150;

    // flipped: the original's old OBJ loader turned the texture V over
    static final LegacyCached<LegacyMesh> RELAY = LegacyCached.flippedMesh(LegacyAssets.RELAY_MESH);

    public ChargerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ArcaneWorkbenchChargerBlockEntity charger, float partialTick, PoseStack pose,
            MultiBufferSource buffers, int light, int overlay) {
        LegacyMesh relay = RELAY.get();
        if (relay == null) {
            return;
        }
        int ring = relay.group("RingFloat");
        int support = relay.group("Support");
        int crystal = relay.group("Crystal");
        if (ring < 0 || support < 0 || crystal < 0) {
            return;
        }
        float age = charger.getLevel() == null ? 0 : charger.getLevel().getGameTime() + partialTick;

        pose.pushPose();
        pose.translate(0.5F, 0.5F, 0.5F);
        // mesh is modelled lying down and upside down
        pose.mulPose(Axis.XP.rotationDegrees(90.0F));
        pose.mulPose(Axis.ZP.rotationDegrees(45.0F));
        var solid = buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        relay.render(ring, pose.last(), solid, -1, light, overlay);

        pose.pushPose();
        pose.mulPose(Axis.XP.rotationDegrees(180.0F));
        pose.translate(0.0F, 0.0F, 0.5F);
        for (int index = 0; index < 4; index++) {
            relay.render(support, pose.last(), solid, -1, light, overlay);
            pose.mulPose(Axis.ZP.rotationDegrees(90.0F));
        }
        pose.popPose();

        float pulse = Mth.sin(age / 2.0F) * 0.05F + 0.95F;
        int glow = Math.min(LightTexture.FULL_BLOCK, (int) (GLOW_DIM + GLOW_RANGE * pulse));
        relay.render(crystal, pose.last(), buffers.getBuffer(RenderType.entityTranslucent(TEXTURE)), -1, glow,
                overlay);
        pose.popPose();
    }
}
