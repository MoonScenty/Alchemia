package me.moonscenty.alchemia.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import org.joml.Vector3f;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.BufferTubeBlock;
import me.moonscenty.alchemia.block.FilterTubeBlock;
import me.moonscenty.alchemia.block.RestrictTubeBlock;
import me.moonscenty.alchemia.block.TubeBlock;
import me.moonscenty.alchemia.client.legacy.LegacyAssets;
import me.moonscenty.alchemia.client.legacy.LegacyModels;
import me.moonscenty.alchemia.client.legacy.model.LegacyMesh;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;

/**
 * A tube on the original's mesh, when its jar is there: a small box in the middle, or a large one for a buffer, and
 * a length of pipe out to every side it is joined on. The kinds of tube differ only in which part of the original's
 * one sheet they wear, and the original moved the mesh's texture corners to choose it; this moves them the same way.
 * <p>
 * The valve's handle and the one-way tube's arrow the original drew in its renderers. Ours are kept as they are,
 * picked out of our own model by the pictures they wear, so that the tubes still show which way they go and whether
 * they are shut.
 */
public class LegacyTubeModel extends BakedModelWrapper<BakedModel> {
    /** The original's sheet. */
    private static final ResourceLocation TEXTURE = Alchemia.id("block/legacy/tube");
    /** The pictures our own tube body is drawn with; what ours draws with anything else is kept. */
    private static final Set<ResourceLocation> OUR_BODY = Set.of(Alchemia.id("block/tube"),
            Alchemia.id("block/tube_buffer"), Alchemia.id("block/silverwood_planks"));
    /** The parts of the mesh: the middle box, the pipe to each side in the game's order of sides, the buffer box. */
    private static final int MIDDLE = 0;
    private static final int BUFFER_MIDDLE = 7;
    /** The texture corners the original moved for each kind: the first four across, the next twenty-four down. */
    private static final int ACROSS_BELOW = 4;
    private static final int DOWN_BELOW = 28;
    private static final float FILTER_ACROSS = 0.1875F;
    private static final float RESTRICT_DOWN = 0.125F;
    private static final float BUFFER_DOWN = 0.25F;
    private static final float FILTER_DOWN = 0.375F;
    /** The tint our filter tube is coloured by, taken by the original's middle box for a filter. */
    private static final int FILTER_TINT = 0;

    private static final Map<Integer, List<BakedQuad>> BAKED = new ConcurrentHashMap<>();
    private static volatile int bakedFrom = -1;

    private final BlockState state;

    public LegacyTubeModel(BakedModel ours, BlockState state) {
        super(ours);
        this.state = state;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random,
            ModelData data, @Nullable RenderType renderType) {
        LegacyMesh mesh = LegacyModels.mesh(LegacyAssets.TUBE_MESH).orElse(null);
        List<BakedQuad> ours = super.getQuads(state, side, random, data, renderType);
        if (mesh == null || mesh.groups() <= BUFFER_MIDDLE) {
            return ours;
        }
        List<BakedQuad> kept = new ArrayList<>();
        for (BakedQuad quad : ours) {
            if (!OUR_BODY.contains(quad.getSprite().contents().name())) {
                kept.add(quad);
            }
        }
        if (side != null) {
            return kept;
        }
        BlockState drawn = state != null ? state : this.state;
        int generation = LegacyModels.generation();
        if (generation != bakedFrom) {
            BAKED.clear();
            bakedFrom = generation;
        }
        Kind kind = Kind.of(drawn);
        int joined = 0;
        for (Direction way : Direction.values()) {
            if (drawn.getValue(TubeBlock.SIDES.get(way)) != TubeBlock.Link.NONE) {
                joined |= 1 << way.ordinal();
            }
        }
        int sides = joined;
        kept.addAll(BAKED.computeIfAbsent(kind.ordinal() << 6 | sides, key -> bake(mesh, kind, sides)));
        return kept;
    }

    private static List<BakedQuad> bake(LegacyMesh mesh, Kind kind, int sides) {
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(TEXTURE);
        List<BakedQuad> quads = new ArrayList<>();
        add(quads, mesh, kind == Kind.BUFFER ? BUFFER_MIDDLE : MIDDLE, kind, sprite,
                kind == Kind.FILTER ? FILTER_TINT : -1);
        for (Direction side : Direction.values()) {
            if ((sides & 1 << side.ordinal()) != 0) {
                add(quads, mesh, side.ordinal() + 1, kind, sprite, -1);
            }
        }
        return List.copyOf(quads);
    }

    private static void add(List<BakedQuad> quads, LegacyMesh mesh, int group, Kind kind,
            TextureAtlasSprite sprite, int tint) {
        Vector3f normal = new Vector3f();
        for (float[][] face : mesh.faces(group)) {
            LegacyMesh.normal(face, normal);
            QuadBakingVertexConsumer quad = new QuadBakingVertexConsumer();
            quad.setSprite(sprite);
            quad.setTintIndex(tint);
            quad.setShade(true);
            quad.setDirection(Direction.getNearest(normal.x, normal.y, normal.z));
            for (int i = 0; i < 4; i++) {
                float[] corner = face[Math.min(i, face.length - 1)];
                int index = (int) corner[5];
                float u = corner[3] + (index >= 0 && index < ACROSS_BELOW ? kind.across : 0.0F);
                float v = corner[4] + (index >= ACROSS_BELOW && index < DOWN_BELOW ? kind.down : 0.0F);
                quad.addVertex(corner[0], corner[1], corner[2])
                        .setColor(-1)
                        .setUv(sprite.getU(u), sprite.getV(v))
                        .setNormal(normal.x, normal.y, normal.z);
            }
            quads.add(quad.bakeQuad());
        }
    }

    /** The kinds of tube as the original told them apart, and where on its sheet each one's picture is. */
    private enum Kind {
        PLAIN(0.0F, 0.0F),
        RESTRICT(0.0F, RESTRICT_DOWN),
        BUFFER(0.0F, BUFFER_DOWN),
        FILTER(FILTER_ACROSS, FILTER_DOWN);

        private final float across;
        private final float down;

        Kind(float across, float down) {
            this.across = across;
            this.down = down;
        }

        static Kind of(BlockState state) {
            var block = state.getBlock();
            if (block instanceof BufferTubeBlock) {
                return BUFFER;
            }
            if (block instanceof FilterTubeBlock) {
                return FILTER;
            }
            if (block instanceof RestrictTubeBlock) {
                return RESTRICT;
            }
            // the original's valve and one-way tubes wore the plain tube's picture, with their extras drawn over it
            return PLAIN;
        }
    }
}
