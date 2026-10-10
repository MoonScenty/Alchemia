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
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;

/**
 * A crystal as the original drew it, when its jar is there: a few shards out of the eight in its mesh, standing out
 * of the face that holds the crystal up, in the crystal's colour.
 * <p>
 * The original shuffled the eight shards by a seed made of the block's place and which face holds it, and showed one
 * more of them for every stage of growth. The place goes into the same sum here and comes out the same, since a
 * block position still hashes as it did then, so a crystal grown where one stood in the original looks as that one
 * did.
 * <p>
 * The original could grow shards from every face that had something solid behind it. Ours grow from one, so only
 * that face's shards are drawn.
 * <p>
 * Until its mesh has been read there is nothing to draw, and the model it wraps holds only the crystal's picture.
 */
public class LegacyCrystalModel extends BakedModelWrapper<BakedModel> {
    /** The seed the original took from the block's place. */
    private static final ModelProperty<Integer> SEED = new ModelProperty<>();
    /** The original's grey crystal, coloured by the tint of index {@link #TINT}. */
    private static final ResourceLocation TEXTURE = Alchemia.id("block/legacy/crystal");
    public static final int TINT = 0;
    /** How many shards the mesh has to choose from. */
    private static final int SHARDS = 8;

    /** Baked shards by the face holding them and which of the eight they are; emptied when the import changes. */
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

    /**
     * Which shards show, as the original chose them: the eight shuffled by a seed made of the place and of which face
     * holds the crystal, and the first few taken, one more for each stage of growth.
     */
    private static int shards(Direction holder, int seed, int count) {
        // the original counted the faces up, down, east, west, north, south, from one
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
                QuadBakingVertexConsumer quad = new QuadBakingVertexConsumer();
                quad.setSprite(sprite);
                quad.setTintIndex(TINT);
                quad.setShade(true);
                quad.setDirection(Direction.getNearest(normal.x, normal.y, normal.z));
                for (int i = 0; i < 4; i++) {
                    float[] corner = corners[Math.min(i, corners.length - 1)];
                    quad.addVertex(corner[0], corner[1], corner[2])
                            .setColor(-1)
                            .setUv(sprite.getU(corner[3]), sprite.getV(corner[4]))
                            .setNormal(normal.x, normal.y, normal.z);
                }
                quads.add(quad.bakeQuad());
            }
        }
        return List.copyOf(quads);
    }

    /**
     * The mesh stands on the floor. The original turned it onto each other face with a turn and a shift; these are
     * those, worked out, so that the shards stand out of the face that holds them.
     */
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
