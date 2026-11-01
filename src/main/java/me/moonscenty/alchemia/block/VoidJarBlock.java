package me.moonscenty.alchemia.block;

import com.mojang.serialization.MapCodec;

public class VoidJarBlock extends JarBlock {
    public static final MapCodec<VoidJarBlock> CODEC = simpleCodec(VoidJarBlock::new);

    public VoidJarBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends JarBlock> codec() {
        return CODEC;
    }
}
