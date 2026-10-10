package me.moonscenty.alchemia.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.client.legacy.LegacyAssets;
import me.moonscenty.alchemia.client.legacy.LegacyModels;
import me.moonscenty.alchemia.client.legacy.model.LegacyMesh;
import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.registry.ModWandParts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;

/**
 * The wand on the original's own mesh, when its jar is there: a rod with a cap at either end, each part wearing the
 * picture of what it is made of, as the original put one together.
 * <p>
 * Our wand picks one of forty-five models of its own by what it is made of. This stands in front of that choice:
 * with the original's mesh it builds the wand from the mesh instead, and keeps only how our model is held and shown,
 * so the wand sits in the hand where ours did. The mesh is a little longer than ours, caps and all, so it is drawn
 * at four fifths about its middle to come out the same length.
 * <p>
 * The original's mesh has a crossbar for a sceptre and a place for a focus; neither is drawn, since our wands are
 * never sceptres and do not show their focus yet.
 */
public class LegacyWandModel extends BakedModelWrapper<BakedModel> {
    /** How much smaller the mesh is drawn, and about where, to stand where our own wand did. */
    private static final float SCALE = 0.8F;
    private static final float MIDDLE = 0.5F;

    /** Built wands by rod and cap, for as long as the import that built them lasts. */
    private static final Map<String, BakedModel> BUILT = new ConcurrentHashMap<>();
    private static volatile int builtFrom = -1;

    private final ItemOverrides overrides;

    public LegacyWandModel(BakedModel ours) {
        super(ours);
        ItemOverrides theirs = ours.getOverrides();
        this.overrides = new ItemOverrides() {
            @Nullable
            @Override
            public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level,
                    @Nullable LivingEntity holder, int seed) {
                BakedModel picked = theirs.resolve(model, stack, level, holder, seed);
                LegacyMesh mesh = LegacyModels.mesh(LegacyAssets.WAND_MESH).orElse(null);
                if (mesh == null || picked == null) {
                    return picked;
                }
                return built(mesh, stack, picked);
            }
        };
    }

    @Override
    public ItemOverrides getOverrides() {
        return overrides;
    }

    private static BakedModel built(LegacyMesh mesh, ItemStack stack, BakedModel ours) {
        int generation = LegacyModels.generation();
        if (generation != builtFrom) {
            BUILT.clear();
            builtFrom = generation;
        }
        ResourceLocation rod = ModWandParts.RODS.getKey(WandItem.rod(stack));
        ResourceLocation cap = ModWandParts.CAPS.getKey(WandItem.cap(stack));
        if (rod == null || cap == null) {
            return ours;
        }
        return BUILT.computeIfAbsent(rod.getPath() + "/" + cap.getPath(),
                key -> new Built(bake(mesh, rod.getPath(), cap.getPath()), ours));
    }

    private static List<BakedQuad> bake(LegacyMesh mesh, String rod, String cap) {
        var atlas = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS);
        TextureAtlasSprite rodSprite = atlas.apply(Alchemia.id("item/wand/rod_" + rod));
        TextureAtlasSprite capSprite = atlas.apply(Alchemia.id("item/wand/cap_" + cap));
        List<BakedQuad> quads = new ArrayList<>();
        add(quads, mesh, "rod", rodSprite);
        add(quads, mesh, "cap1", capSprite);
        add(quads, mesh, "cap2", capSprite);
        return List.copyOf(quads);
    }

    private static void add(List<BakedQuad> quads, LegacyMesh mesh, String part, TextureAtlasSprite sprite) {
        int group = mesh.group(part);
        if (group < 0) {
            return;
        }
        Vector3f normal = new Vector3f();
        for (float[][] face : mesh.faces(group)) {
            LegacyMesh.normal(face, normal);
            QuadBakingVertexConsumer quad = new QuadBakingVertexConsumer();
            quad.setSprite(sprite);
            quad.setShade(true);
            quad.setDirection(Direction.getNearest(normal.x, normal.y, normal.z));
            for (int i = 0; i < 4; i++) {
                float[] corner = face[Math.min(i, face.length - 1)];
                quad.addVertex(shrink(corner[0]), shrink(corner[1]), shrink(corner[2]))
                        .setColor(-1)
                        .setUv(sprite.getU(corner[3]), sprite.getV(corner[4]))
                        .setNormal(normal.x, normal.y, normal.z);
            }
            quads.add(quad.bakeQuad());
        }
    }

    private static float shrink(float at) {
        return MIDDLE + (at - MIDDLE) * SCALE;
    }

    /** One built wand: the original's parts, held and shown as our model of the same wand is. */
    private static final class Built extends BakedModelWrapper<BakedModel> {
        private final List<BakedQuad> quads;

        Built(List<BakedQuad> quads, BakedModel ours) {
            super(ours);
            this.quads = quads;
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random) {
            return side == null ? quads : List.of();
        }

        @Override
        public ItemTransforms getTransforms() {
            return originalModel.getTransforms();
        }

        @Override
        public ItemOverrides getOverrides() {
            return ItemOverrides.EMPTY;
        }

        // the wrapper hands both of these to the model it wraps, which would draw our wand instead of this one

        @Override
        public BakedModel applyTransform(ItemDisplayContext context, PoseStack pose, boolean leftHand) {
            getTransforms().getTransform(context).apply(leftHand, pose);
            return this;
        }

        @Override
        public List<BakedModel> getRenderPasses(ItemStack stack, boolean fabulous) {
            return List.of(this);
        }
    }
}
