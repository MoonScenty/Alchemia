package me.moonscenty.alchemia.client;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.mojang.blaze3d.vertex.PoseStack;

import me.moonscenty.alchemia.client.legacy.LegacyCached;
import me.moonscenty.alchemia.client.legacy.LegacyModels;
import me.moonscenty.alchemia.client.legacy.model.LegacyMesh;
import me.moonscenty.alchemia.client.legacy.model.LegacyQuads;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;

/**
 * An item drawn on parts of one of the original's meshes, each placed where the original's renderer placed it, when
 * the jar is there; otherwise our own model of it.
 * <p>
 * For the things the original drew in its renderer even in the hand, which the game no longer lets an item do: the
 * same parts at the same places are baked into the model once, and held and shown as our own model of it is.
 */
public class LegacyMeshItemModel extends BakedModelWrapper<BakedModel> {
    /** One part of the mesh, and where it goes. */
    public record Placed(String part, Matrix4f at) {
    }

    private final String mesh;
    private final boolean flipped;
    private final ResourceLocation texture;
    private final List<Placed> parts;

    private final LegacyCached<List<BakedQuad>> quads;

    /**
     * @param mesh    the mesh's key in {@link LegacyModels}
     * @param flipped whether the original's loader turned its texture corners over
     * @param texture the picture, on the block atlas
     */
    public LegacyMeshItemModel(BakedModel ours, String mesh, boolean flipped, ResourceLocation texture,
            List<Placed> parts) {
        super(ours);
        this.mesh = mesh;
        this.flipped = flipped;
        this.texture = texture;
        this.parts = List.copyOf(parts);
        this.quads = new LegacyCached<>(() -> LegacyModels.mesh(mesh)
                .map(found -> bake(flipped ? found.flippedV() : found)).orElse(List.of()));
    }

    private boolean legacy() {
        return LegacyModels.mesh(mesh).isPresent();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random) {
        if (!legacy()) {
            return super.getQuads(state, side, random);
        }
        return side == null ? quads.get() : List.of();
    }

    @Override
    public boolean isGui3d() {
        return legacy() || super.isGui3d();
    }

    // the wrapper hands both of these to the model it wraps, which would draw ours instead of this

    @Override
    public BakedModel applyTransform(ItemDisplayContext context, PoseStack pose, boolean leftHand) {
        getTransforms().getTransform(context).apply(leftHand, pose);
        return this;
    }

    @Override
    public List<BakedModel> getRenderPasses(ItemStack stack, boolean fabulous) {
        return List.of(this);
    }

    private List<BakedQuad> bake(LegacyMesh found) {
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(texture);
        List<BakedQuad> out = new ArrayList<>();
        Vector3f normal = new Vector3f();
        Vector4f corner = new Vector4f();
        for (Placed placed : parts) {
            int group = found.group(placed.part());
            if (group < 0) {
                continue;
            }
            for (float[][] face : found.faces(group)) {
                float[][] moved = new float[face.length][];
                for (int i = 0; i < face.length; i++) {
                    placed.at().transform(corner.set(face[i][0], face[i][1], face[i][2], 1.0F));
                    moved[i] = new float[] {corner.x, corner.y, corner.z, face[i][3], face[i][4]};
                }
                LegacyMesh.normal(moved, normal);
                out.add(LegacyQuads.bake(moved, sprite, -1, normal));
            }
        }
        return List.copyOf(out);
    }
}
