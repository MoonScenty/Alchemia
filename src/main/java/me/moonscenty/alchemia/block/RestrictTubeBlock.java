package me.moonscenty.alchemia.block;

import com.mojang.serialization.MapCodec;

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
