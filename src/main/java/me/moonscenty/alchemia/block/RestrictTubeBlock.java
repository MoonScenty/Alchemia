package me.moonscenty.alchemia.block;

import com.mojang.serialization.MapCodec;

/**
 * A length of pipe with the way through it narrowed.
 * <p>
 * Nothing is turned back, but a run that comes through one waits a turn between loads, so half as much arrives in
 * the same time. Put one on the branch you care less about and the rest of the works keeps the lion's share.
 */
public class RestrictTubeBlock extends TubeBlock {
    public static final MapCodec<RestrictTubeBlock> CODEC = simpleCodec(RestrictTubeBlock::new);

    public RestrictTubeBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<RestrictTubeBlock> codec() {
        return CODEC;
    }

    @Override
    public int holdsUp() {
        return 1;
    }
}
