package me.moonscenty.alchemia.block;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.block.entity.InfusionMatrixBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

public class ArcanePillarBlock extends Block {
    public static final MapCodec<ArcanePillarBlock> CODEC = simpleCodec(ArcanePillarBlock::new);
    public static final net.minecraft.world.level.block.state.properties.EnumProperty<PillarKind> KIND =
            net.minecraft.world.level.block.state.properties.EnumProperty.create("kind", PillarKind.class);

    // matrix is two above the foot or one above the head; either half may be broken
    private static final Vec3i[] MATRICES = {
            new Vec3i(1, 2, 1), new Vec3i(1, 2, -1), new Vec3i(-1, 2, 1), new Vec3i(-1, 2, -1),
            new Vec3i(1, 1, 1), new Vec3i(1, 1, -1), new Vec3i(-1, 1, 1), new Vec3i(-1, 1, -1)};

    public ArcanePillarBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(KIND, PillarKind.NORMAL));
    }

    @Override
    protected void createBlockStateDefinition(
            net.minecraft.world.level.block.state.StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(KIND);
    }

    @Override
    protected MapCodec<ArcanePillarBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

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
