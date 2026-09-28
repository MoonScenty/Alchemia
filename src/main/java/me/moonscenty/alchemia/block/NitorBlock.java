package me.moonscenty.alchemia.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A flame held still: light with nothing burning under it.
 * <p>
 * It is the one light in the mod that costs no fuel and goes out for no reason, which is what makes it worth the
 * glowstone it is boiled from. It hangs where it is put -- floor, wall, ceiling or open air -- because a light
 * that needs something to stand on is a torch, and torches are not worth boiling anything for.
 * <p>
 * There are sixteen of these, one to a dye. They share one picture, drawn in grey, and each is painted by the
 * colour its own kind gives out; the red bead in the middle is a second picture that no dye touches. Sixteen
 * pictures would have been sixteen chances for one of them to drift away from the others.
 */
public class NitorBlock extends Block {
    public static final MapCodec<NitorBlock> CODEC = simpleCodec(properties -> new NitorBlock(properties, DyeColor.WHITE));

    /** A small flame in the middle of the block: it is a light, not a thing to walk on. */
    private static final VoxelShape SHAPE = Block.box(5.0, 5.0, 5.0, 11.0, 11.0, 11.0);

    private final DyeColor colour;

    public NitorBlock(Properties properties, DyeColor colour) {
        super(properties);
        this.colour = colour;
    }

    /** What the grey flame is painted with. */
    public DyeColor colour() {
        return colour;
    }

    @Override
    protected MapCodec<? extends NitorBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /** It hangs in the air if that is where it was put. Nothing holds a flame up. */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return true;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState beside, Direction side) {
        return beside.is(this) || super.skipRendering(state, beside, side);
    }
}
