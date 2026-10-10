package me.moonscenty.alchemia.client;

import java.util.List;

import javax.annotation.Nullable;

import me.moonscenty.alchemia.client.legacy.LegacyModels;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;

/**
 * Our block model, gone when the original's mesh for the same block is there.
 * <p>
 * For a block the original drew wholly in its renderer, and never as a block: with its jar, the renderer draws the
 * original's and our model would only stand in the way. The particles a broken block throws still come from ours.
 */
public class LegacyHiddenModel extends BakedModelWrapper<BakedModel> {
    private final String mesh;

    public LegacyHiddenModel(BakedModel ours, String mesh) {
        super(ours);
        this.mesh = mesh;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random,
            ModelData data, @Nullable RenderType renderType) {
        return LegacyModels.mesh(mesh).isPresent() ? List.of()
                : super.getQuads(state, side, random, data, renderType);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random) {
        return LegacyModels.mesh(mesh).isPresent() ? List.of() : super.getQuads(state, side, random);
    }
}
