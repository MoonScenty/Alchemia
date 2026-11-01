package me.moonscenty.alchemia.client;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.client.legacy.LegacyAssets;
import me.moonscenty.alchemia.client.legacy.LegacyCached;
import me.moonscenty.alchemia.client.legacy.LegacyModels;
import me.moonscenty.alchemia.client.legacy.model.LegacyMesh;
import me.moonscenty.alchemia.client.legacy.model.LegacyQuads;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;

public class LegacyScannerModel extends BakedModelWrapper<BakedModel> {
    // frame is modelled about 2.8 blocks across
    private static final float SCALE = 1.0F / 2.8F;
    private static final float MIDDLE = 0.5F;
    private static final float THICK_MIDDLE = 0.1F;
    private static final float SCREEN_HEIGHT = 0.11F;
    private static final float SCREEN_HALF = 1.25F;

    // frame lies flat as modelled; the slot tilt is the original's 60 degrees
    private static final ItemTransforms HELD = new ItemTransforms(
            transform(70, 0, 0, 0, 1, 0, 0.55F),
            transform(70, 0, 0, 0, 1, 0, 0.55F),
            transform(80, 0, 0, 0, 3, -2, 0.9F),
            transform(80, 0, 0, 0, 3, -2, 0.9F),
            transform(0, 0, 0, 0, 0, 0, 1.0F),
            transform(60, 30, 0, 0, 0, 0, 1.2F),
            transform(0, 0, 0, 0, 2, 0, 0.5F),
            transform(90, 0, 0, 0, 0, 0, 1.0F));

    private static final LegacyCached<List<BakedQuad>> QUADS = new LegacyCached<>(() -> LegacyModels.mesh(
            LegacyAssets.SCANNER_MESH).map(LegacyScannerModel::bake).orElse(List.of()));

    public LegacyScannerModel(BakedModel ours) {
        super(ours);
    }

    private static ItemTransform transform(float rx, float ry, float rz, float tx, float ty, float tz, float scale) {
        return new ItemTransform(new Vector3f(rx, ry, rz), new Vector3f(tx / 16.0F, ty / 16.0F, tz / 16.0F),
                new Vector3f(scale, scale, scale));
    }

    private static boolean legacy() {
        return LegacyModels.mesh(LegacyAssets.SCANNER_MESH).isPresent();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random) {
        if (!legacy()) {
            return super.getQuads(state, side, random);
        }
        return side == null ? QUADS.get() : List.of();
    }

    @Override
    public ItemTransforms getTransforms() {
        return legacy() ? HELD : super.getTransforms();
    }

    @Override
    public boolean isGui3d() {
        return legacy() || super.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return legacy() || super.usesBlockLight();
    }

    // the wrapper would forward these to the wrapped model and draw that instead

    @Override
    public BakedModel applyTransform(ItemDisplayContext context, PoseStack pose, boolean leftHand) {
        getTransforms().getTransform(context).apply(leftHand, pose);
        return this;
    }

    @Override
    public List<BakedModel> getRenderPasses(ItemStack stack, boolean fabulous) {
        return List.of(this);
    }

    private static List<BakedQuad> bake(LegacyMesh mesh) {
        var atlas = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS);
        TextureAtlasSprite frame = atlas.apply(Alchemia.id("item/legacy/scanner"));
        TextureAtlasSprite screen = atlas.apply(Alchemia.id("item/legacy/scanscreen"));
        List<BakedQuad> out = new ArrayList<>();
        LegacyMesh flipped = mesh.flippedV();
        Vector3f normal = new Vector3f();
        for (int group = 0; group < flipped.groups(); group++) {
            for (float[][] face : flipped.faces(group)) {
                LegacyMesh.normal(face, normal);
                float[][] corners = new float[4][];
                for (int i = 0; i < 4; i++) {
                    corners[i] = face[Math.min(i, face.length - 1)];
                }
                out.add(quad(frame, corners, normal));
            }
        }
        float h = SCREEN_HALF;
        float y = SCREEN_HEIGHT;
        float[][] up = {{-h, y, -h, 0, 0}, {-h, y, h, 0, 1}, {h, y, h, 1, 1}, {h, y, -h, 1, 0}};
        float[][] down = {up[3], up[2], up[1], up[0]};
        out.add(quad(screen, up, new Vector3f(0, 1, 0)));
        out.add(quad(screen, down, new Vector3f(0, -1, 0)));
        return List.copyOf(out);
    }

    private static BakedQuad quad(TextureAtlasSprite sprite, float[][] corners, Vector3f normal) {
        float[][] placed = new float[corners.length][];
        for (int i = 0; i < corners.length; i++) {
            float[] corner = corners[i];
            placed[i] = new float[] {MIDDLE + corner[0] * SCALE, MIDDLE + (corner[1] - THICK_MIDDLE) * SCALE,
                    MIDDLE + corner[2] * SCALE, corner[3], corner[4]};
        }
        return LegacyQuads.bake(placed, sprite, -1, normal);
    }
}
