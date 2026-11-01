package me.moonscenty.alchemia.block;

import java.util.List;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.Direction;

// passive: the smelter pours once more for each pump facing it
// stays put if the smelter is removed, as the original's did
public class SmelterAuxBlock extends SmelterSideBlock {
    public static final MapCodec<SmelterAuxBlock> CODEC = simpleCodec(SmelterAuxBlock::new);
    // original's order; decides which pump or vent goes first
    public static final List<Direction> SIDES = List.of(Direction.SOUTH, Direction.WEST, Direction.NORTH,
            Direction.EAST);

    public SmelterAuxBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends SmelterAuxBlock> codec() {
        return CODEC;
    }
}
