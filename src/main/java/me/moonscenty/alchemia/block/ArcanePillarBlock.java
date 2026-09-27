package me.moonscenty.alchemia.block;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.block.entity.InfusionMatrixBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A corner of an altar: stone that has been told what it is for.
 * <p>
 * It draws nothing. What you see standing on it is a picture the matrix above draws, the same as the eight stones
 * turning in the air, and for the same reason: the pillar is two blocks tall and a block cannot be hit above
 * itself. So the picture floats and this -- a plain cube where the foot of it is -- is the part you can break.
 * <p>
 * It exists at all only because the stone has to stop being drawn. A pillar drawn over an arcane stone leaves the
 * top half of the stone standing out around the shaft, and no amount of moving the picture about hides a whole
 * block behind a tapering one.
 */
public class ArcanePillarBlock extends Block {
    public static final MapCodec<ArcanePillarBlock> CODEC = simpleCodec(ArcanePillarBlock::new);

    /**
     * Where a matrix would be, if this corner belongs to one.
     * <p>
     * Eight places, not four: a corner stands two blocks high and either of them may be the one broken, so the
     * matrix is two up from the foot or one up from the head.
     */
    private static final Vec3i[] MATRICES = {
            new Vec3i(1, 2, 1), new Vec3i(1, 2, -1), new Vec3i(-1, 2, 1), new Vec3i(-1, 2, -1),
            new Vec3i(1, 1, 1), new Vec3i(1, 1, -1), new Vec3i(-1, 1, 1), new Vec3i(-1, 1, -1)};

    public ArcanePillarBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<ArcanePillarBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    /**
     * Breaking a corner takes the altar with it, at once.
     * <p>
     * The matrix looks round once a second anyway, but a second is long enough for somebody to break a second
     * corner wondering why the first did nothing, and then a third.
     */
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock())) {
            for (Vec3i where : MATRICES) {
                if (level.getBlockEntity(pos.offset(where)) instanceof InfusionMatrixBlockEntity matrix
                        && matrix.awake()) {
                    matrix.sleep(level);
                }
            }
        }
        super.onRemove(state, level, pos, replacement, moving);
    }
}
