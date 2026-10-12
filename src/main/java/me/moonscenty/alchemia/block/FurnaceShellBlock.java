package me.moonscenty.alchemia.block;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * The shell of an infernal furnace: what was nether brick and obsidian round the lava, standing in for them while the
 * furnace stands. It draws nothing -- the heart's model is the whole furnace -- and breaking it takes the furnace
 * apart and gives back what it was.
 */
public class FurnaceShellBlock extends Block {
    public static final MapCodec<FurnaceShellBlock> CODEC = simpleCodec(FurnaceShellBlock::new);
    /** What it was: obsidian, or nether brick. */
    public static final BooleanProperty OBSIDIAN = BooleanProperty.create("obsidian");

    public FurnaceShellBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(OBSIDIAN, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OBSIDIAN);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    /** Broken, it takes the furnace it belongs to apart. */
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock()) && !replacement.is(net.minecraft.world.level.block.Blocks.OBSIDIAN)
                && !replacement.is(net.minecraft.world.level.block.Blocks.NETHER_BRICKS)) {
            for (BlockPos at : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
                BlockState there = level.getBlockState(at);
                if (there.is(ModBlocks.INFERNAL_FURNACE.get())) {
                    InfernalFurnaceBlock.undo(level, at.immutable(), there, false);
                    break;
                }
            }
        }
        super.onRemove(state, level, pos, replacement, moving);
    }
}
