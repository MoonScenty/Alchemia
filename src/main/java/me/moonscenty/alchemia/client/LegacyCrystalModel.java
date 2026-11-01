package me.moonscenty.alchemia.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import org.joml.Vector3f;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.CrystalBlock;
import me.moonscenty.alchemia.client.legacy.LegacyAssets;
import me.moonscenty.alchemia.client.legacy.LegacyModels;
import me.moonscenty.alchemia.client.legacy.model.LegacyMesh;
import me.moonscenty.alchemia.client.legacy.model.LegacyQuads;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;

public class LegacyCrystalModel extends BakedModelWrapper<BakedModel> {
    // original's seed from the BlockPos hash, so shards land where the original's did
    private static final ModelProperty<Integer> SEED = new ModelProperty<>();
    private static final ResourceLocation TEXTURE = Alchemia.id("block/legacy/crystal");
    public static final int TINT = 0;
    private static final int SHARDS = 8;

    private static final Map<Integer, List<BakedQuad>> BAKED = new ConcurrentHashMap<>();
    private static volatile int bakedFrom = -1;

    private final BlockState state;

    public LegacyCrystalModel(BakedModel ours, BlockState state) {
        super(ours);
        this.state = state;
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData data) {
        return data.derive().with(SEED, pos.hashCode()).build();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random,
            ModelData data, @Nullable RenderType renderType) {
        LegacyMesh mesh = LegacyModels.mesh(LegacyAssets.CRYSTAL_MESH).orElse(null);
        if (mesh == null || mesh.groups() < SHARDS) {
            return super.getQuads(state, side, random, data, renderType);
        }
        if (side != null) {
            return List.of();
        }
        BlockState drawn = state != null ? state : this.state;
        Integer seed = data.get(SEED);
        Direction holder = drawn.getValue(CrystalBlock.FACING).getOpposite();
        int mask = shards(holder, seed == null ? 0 : seed, drawn.getValue(CrystalBlock.AGE) + 1);
        int generation = LegacyModels.generation();
        if (generation != bakedFrom) {
            BAKED.clear();
            bakedFrom = generation;
        }
        return BAKED.computeIfAbsent(holder.ordinal() << SHARDS | mask, key -> bake(mesh, holder, mask));
    }

    private static int shards(Direction holder, int seed, int count) {
        // the original numbered faces up, down, east, west, north, south from one
        int face = switch (holder) {
            case UP -> 1;
            case DOWN -> 2;
            case EAST -> 3;
            case WEST -> 4;
            case NORTH -> 5;
            case SOUTH -> 6;
        };
        List<Integer> order = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7);
        Collections.shuffle(order, new Random(face + seed * (1000 * face)));
        int mask = 0;
        for (int i = 0; i < Math.min(count, SHARDS); i++) {
            mask |= 1 << order.get(i);
        }
        return mask;
    }

    private static List<BakedQuad> bake(LegacyMesh mesh, Direction holder, int mask) {
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(TEXTURE);
        List<BakedQuad> quads = new ArrayList<>();
        Vector3f normal = new Vector3f();
        float[][] turned = new float[4][];
        for (int shard = 0; shard < SHARDS; shard++) {
            if ((mask & 1 << shard) == 0) {
                continue;
            }
            for (float[][] face : mesh.faces(shard)) {
                for (int i = 0; i < face.length; i++) {
                    turned[i] = turn(face[i], holder);
                }
                float[][] corners = Arrays.copyOf(turned, face.length);
                LegacyMesh.normal(corners, normal);
                quads.add(LegacyQuads.bake(corners, sprite, TINT, normal));
            }
        }
        return List.copyOf(quads);
    }

    private static float[] turn(float[] corner, Direction holder) {
        float x = corner[0];
        float y = corner[1];
        float z = corner[2];
        return switch (holder) {
            case DOWN -> new float[] {x, y, z, corner[3], corner[4]};
            case UP -> new float[] {x, 1 - y, 1 - z, corner[3], corner[4]};
            case EAST -> new float[] {1 - y, 1 - z, x, corner[3], corner[4]};
            case WEST -> new float[] {y, 1 - z, 1 - x, corner[3], corner[4]};
            case NORTH -> new float[] {x, 1 - z, y, corner[3], corner[4]};
            case SOUTH -> new float[] {1 - x, 1 - z, 1 - y, corner[3], corner[4]};
        };
    }
}
