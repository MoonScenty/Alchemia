package me.moonscenty.alchemia.block;

import com.mojang.serialization.MapCodec;

/**
 * A void jar, the original's {@code TileJarFillableVoid}: a warded jar with a little of the void worked into it. It
 * goes on taking essentia when it is full and lets what will not fit go, now and then as a point of flux in the air
 * about it. It is the end a pipe run drains into when nothing else will have what is in it.
 */
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
