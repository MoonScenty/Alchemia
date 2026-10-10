package me.moonscenty.alchemia.block;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.block.entity.NitorBlockEntity;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
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
 * <p>
 * Placed, it has no model at all, as the original had none: what is there to see is the original's wisps of light
 * welling up out of the spot ({@code NitorEffects}). The drawn pictures are what it looks like in a slot, where
 * nothing moves and something has to be shown.
 */
public class NitorBlock extends BaseEntityBlock {
    public static final MapCodec<NitorBlock> CODEC =
            simpleCodec(properties -> new NitorBlock(properties, DyeColor.WHITE));

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
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /** Nothing is drawn for it. Everything it looks like is its wisps. */
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NitorBlockEntity(pos, state);
    }

    /**
     * Only the client ticks a nitor, because only the client has anything to do about one.
     * <p>
     * The hook meant for idle scenery is no good here: it is called a few times a minute for any one block,
     * which is enough for a torch that is already drawn and nothing at all for a thing that is only its sparks.
     */
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return level.isClientSide
                ? createTickerHelper(type, ModBlockEntities.NITOR.get(), NitorBlockEntity::tick)
                : null;
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
