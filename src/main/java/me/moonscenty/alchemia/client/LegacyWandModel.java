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
import net.minecraft.core.registries.BuiltInRegistries;
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
 * The wand on the original's own mesh: a rod with a cap at either end, each part wearing the picture of what it is
 * made of, as the original put one together.
 * <p>
 * Our wand's model file says only how a wand is held and shown; this builds what it looks like from the mesh, for
 * whatever the wand in hand is made of. The mesh is drawn at four fifths about its middle, which is the length a
 * wand is held at.
 * <p>
 * The original's mesh also has a crossbar for a sceptre, which is not drawn since our wands are never sceptres; a
 * focus fitted to the wand sits on its tip, in the focus's own picture, as the original drew it.
 */
public class LegacyWandModel extends BakedModelWrapper<BakedModel> {
    /** How much smaller the mesh is drawn, and about where. */
    private static final float SCALE = 0.8F;
    private static final float MIDDLE = 0.5F;

    /** Built wands by rod and cap, for as long as the import that built them lasts. */
    private static final Map<String, BakedModel> BUILT = new ConcurrentHashMap<>();
    private static volatile int builtFrom = -1;

    private final ItemOverrides overrides;

    public LegacyWandModel(BakedModel ours) {
        super(ours);
        this.overrides = new ItemOverrides() {
            @Override
            public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level,
                    @Nullable LivingEntity holder, int seed) {
                LegacyMesh mesh = LegacyModels.mesh(LegacyAssets.WAND_MESH).orElse(null);
                return mesh == null ? ours : built(mesh, stack, ours);
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
        ItemStack focus = WandItem.focus(stack);
        String tip = focus.isEmpty() ? "" : BuiltInRegistries.ITEM.getKey(focus.getItem()).getPath();
        return BUILT.computeIfAbsent(rod.getPath() + "/" + cap.getPath() + "/" + tip,
                key -> new Built(bake(mesh, rod.getPath(), cap.getPath(), tip), ours));
    }

    private static List<BakedQuad> bake(LegacyMesh mesh, String rod, String cap, String tip) {
        var atlas = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS);
        TextureAtlasSprite rodSprite = atlas.apply(Alchemia.id("item/wand/rod_" + rod));
        TextureAtlasSprite capSprite = atlas.apply(Alchemia.id("item/wand/cap_" + cap));
        List<BakedQuad> quads = new ArrayList<>();
        add(quads, mesh, "rod", rodSprite);
        add(quads, mesh, "cap1", capSprite);
        add(quads, mesh, "cap2", capSprite);
        if (!tip.isEmpty()) {
            add(quads, mesh, "focus", atlas.apply(Alchemia.id("item/wand/" + tip)));
        }
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
