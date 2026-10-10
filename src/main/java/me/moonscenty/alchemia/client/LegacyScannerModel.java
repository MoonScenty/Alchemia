package me.moonscenty.alchemia.client;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.client.legacy.LegacyAssets;
import me.moonscenty.alchemia.client.legacy.LegacyModels;
import me.moonscenty.alchemia.client.legacy.model.LegacyMesh;
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
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;

/**
 * The alchemometer as Thaumcraft 4's thaumometer, when its jar is there: a six-sided brass frame with a glass screen
 * set in it.
 * <p>
 * Thaumcraft 4 drew it by hand, and in the first person drew both the player's arms holding it up to look through.
 * The game no longer lets an item draw the arms that hold it, so this is the frame and its screen as a model, held
 * flat-on in front of the player so that the screen is what they look at, and laid back in a slot as the original
 * laid it. The read-out the original wrote over the screen while scanning is not drawn.
 * <p>
 * Without the jar this is our own flat picture and nothing else.
 */
public class LegacyScannerModel extends BakedModelWrapper<BakedModel> {
    /** The frame is modelled about two and a half blocks across; drawn at this, it fills a slot as an item does. */
    private static final float SCALE = 1.0F / 2.8F;
    private static final float MIDDLE = 0.5F;
    /** The frame is a fifth of a block thick; its middle is set on the middle of the item. */
    private static final float THICK_MIDDLE = 0.1F;
    /** Where the original laid the screen in the frame, and how big. */
    private static final float SCREEN_HEIGHT = 0.11F;
    private static final float SCREEN_HALF = 1.25F;

    /**
     * How it is held and shown. The frame lies flat as modelled, so it is stood up to face the player in the hand and
     * tipped back in a slot, after the original's own slot angle of sixty degrees.
     */
    private static final ItemTransforms HELD = new ItemTransforms(
            transform(70, 0, 0, 0, 1, 0, 0.55F),
            transform(70, 0, 0, 0, 1, 0, 0.55F),
            transform(80, 0, 0, 0, 3, -2, 0.9F),
            transform(80, 0, 0, 0, 3, -2, 0.9F),
            transform(0, 0, 0, 0, 0, 0, 1.0F),
            transform(60, 30, 0, 0, 0, 0, 1.2F),
            transform(0, 0, 0, 0, 2, 0, 0.5F),
            transform(90, 0, 0, 0, 0, 0, 1.0F));

    private static List<BakedQuad> quads = List.of();
    private static int bakedFrom = -1;

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
        return side == null ? baked() : List.of();
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

    // the wrapper hands both of these to the model it wraps, which would draw our picture instead of this

    @Override
    public BakedModel applyTransform(ItemDisplayContext context, PoseStack pose, boolean leftHand) {
        getTransforms().getTransform(context).apply(leftHand, pose);
        return this;
    }

    @Override
    public List<BakedModel> getRenderPasses(ItemStack stack, boolean fabulous) {
        return List.of(this);
    }

    private static List<BakedQuad> baked() {
        int generation = LegacyModels.generation();
        if (generation != bakedFrom) {
            bakedFrom = generation;
            quads = LegacyModels.mesh(LegacyAssets.SCANNER_MESH).map(LegacyScannerModel::bake).orElse(List.of());
        }
        return quads;
    }

    private static List<BakedQuad> bake(LegacyMesh mesh) {
        var atlas = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS);
        TextureAtlasSprite frame = atlas.apply(Alchemia.id("item/legacy/scanner"));
        TextureAtlasSprite screen = atlas.apply(Alchemia.id("item/legacy/scanscreen"));
        List<BakedQuad> out = new ArrayList<>();
        // Thaumcraft 4 read the mesh with the game's loader of the time, which turned the texture corners over
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
        // the screen, both ways up so it shows from either side, as the original drew it unculled
        float h = SCREEN_HALF;
        float y = SCREEN_HEIGHT;
        float[][] up = {{-h, y, -h, 0, 0}, {-h, y, h, 0, 1}, {h, y, h, 1, 1}, {h, y, -h, 1, 0}};
        float[][] down = {up[3], up[2], up[1], up[0]};
        out.add(quad(screen, up, new Vector3f(0, 1, 0)));
        out.add(quad(screen, down, new Vector3f(0, -1, 0)));
        return List.copyOf(out);
    }

    private static BakedQuad quad(TextureAtlasSprite sprite, float[][] corners, Vector3f normal) {
        QuadBakingVertexConsumer quad = new QuadBakingVertexConsumer();
        quad.setSprite(sprite);
        quad.setShade(true);
        quad.setDirection(Direction.getNearest(normal.x, normal.y, normal.z));
        for (float[] corner : corners) {
            quad.addVertex(MIDDLE + corner[0] * SCALE, MIDDLE + (corner[1] - THICK_MIDDLE) * SCALE,
                            MIDDLE + corner[2] * SCALE)
                    .setColor(-1)
                    .setUv(sprite.getU(corner[3]), sprite.getV(corner[4]))
                    .setNormal(normal.x, normal.y, normal.z);
        }
        return quad.bakeQuad();
    }
}
