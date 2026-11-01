package me.moonscenty.alchemia.client.legacy.model;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;

import org.joml.Vector3f;

public final class LegacyQuads {
    private LegacyQuads() {
    }

    public static BakedQuad bake(float[][] corners, TextureAtlasSprite sprite, int tint, Vector3f normal) {
        QuadBakingVertexConsumer quad = new QuadBakingVertexConsumer();
        quad.setSprite(sprite);
        quad.setTintIndex(tint);
        quad.setShade(true);
        quad.setDirection(Direction.getNearest(normal.x, normal.y, normal.z));
        for (int i = 0; i < 4; i++) {
            float[] corner = corners[Math.min(i, corners.length - 1)];
            quad.addVertex(corner[0], corner[1], corner[2])
                    .setColor(-1)
                    .setUv(sprite.getU(corner[3]), sprite.getV(corner[4]))
                    .setNormal(normal.x, normal.y, normal.z);
        }
        return quad.bakeQuad();
    }
}
