package me.moonscenty.alchemia.block;

import java.util.List;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.Direction;

/**
 * An auxiliary slurry pump: set against the side of a smelter, it draws on what the smelter holds and pours it up
 * into vessels stacked on itself, one more column of them working off the same fire.
 * <p>
 * It does nothing of its own. The smelter looks round at every pour for pumps facing it, and pours once more for
 * each, so a smelter with three is a smelter with four columns. It goes on any side but the front, where the fire is
 * fed, and only against a smelter; once there, it stays if the smelter is taken away, as the original's did.
 */
public class SmelterAuxBlock extends SmelterSideBlock {
    public static final MapCodec<SmelterAuxBlock> CODEC = simpleCodec(SmelterAuxBlock::new);
    /** The sides a smelter looks round, in the original's order, which settles which pump or vent goes first. */
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
