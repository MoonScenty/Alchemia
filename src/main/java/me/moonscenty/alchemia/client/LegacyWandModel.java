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
import me.moonscenty.alchemia.client.legacy.model.LegacyQuads;
import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.registry.ModWandParts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
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

public class LegacyWandModel extends BakedModelWrapper<BakedModel> {
    private static final float SCALE = 0.8F;
    private static final float MIDDLE = 0.5F;
    public static final ModelResourceLocation STAFF = ModelResourceLocation.standalone(Alchemia.id("item/staff"));

    private static final Map<String, BakedModel> BUILT = new ConcurrentHashMap<>();
    private static volatile int builtFrom = -1;

    private final ItemOverrides overrides;

    public LegacyWandModel(BakedModel ours) {
        super(ours);
        this.overrides = new ItemOverrides() {
            @Override
            public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level,
                    @Nullable LivingEntity holder, int seed) {
                boolean staff = WandItem.isStaff(stack);
                LegacyMesh mesh = LegacyModels.mesh(staff ? LegacyAssets.STAFF_MESH : LegacyAssets.WAND_MESH)
                        .orElse(null);
                BakedModel held = staff ? Minecraft.getInstance().getModelManager().getModel(STAFF) : ours;
                return mesh == null ? ours : built(mesh, stack, held);
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
        ResourceLocation rodLook = WandItem.rod(stack).texture();
        boolean crossbar = WandItem.isSceptre(stack);
        return BUILT.computeIfAbsent(rod.getPath() + "/" + cap.getPath() + "/" + tip + (crossbar ? "/sceptre" : ""),
                key -> new Built(bake(mesh, rodLook, cap.getPath(), tip, crossbar), ours));
    }

    private static List<BakedQuad> bake(LegacyMesh mesh, ResourceLocation rod, String cap, String tip,
            boolean crossbar) {
        var atlas = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS);
        TextureAtlasSprite rodSprite = atlas.apply(rod);
        TextureAtlasSprite capSprite = atlas.apply(Alchemia.id("item/wand/cap_" + cap));
        List<BakedQuad> quads = new ArrayList<>();
        add(quads, mesh, "rod", rodSprite);
        add(quads, mesh, "cap1", capSprite);
        add(quads, mesh, "cap2", capSprite);
        if (crossbar) {
            add(quads, mesh, "crossbar", capSprite);
        }
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
            float[][] shrunk = new float[face.length][];
            for (int i = 0; i < face.length; i++) {
                float[] corner = face[i];
                shrunk[i] = new float[] {shrink(corner[0]), shrink(corner[1]), shrink(corner[2]), corner[3],
                        corner[4]};
            }
            quads.add(LegacyQuads.bake(shrunk, sprite, -1, normal));
        }
    }

    private static float shrink(float at) {
        return MIDDLE + (at - MIDDLE) * SCALE;
    }

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
    }
}
