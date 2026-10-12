package me.moonscenty.alchemia.block;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.block.entity.GolemPressBlockEntity;
import me.moonscenty.alchemia.player.PlayerKnowledge;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModSounds;
import me.moonscenty.alchemia.item.VisHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The golem press, where the piston was: the heart of a two-by-two of piston, stone table, anvil and cauldron with
 * iron bars over the piston. Its renderer draws the whole press; the other four blocks are placeholders that draw
 * nothing, and any of them opens it.
 *
 * <p>It is made with a wand by somebody who knows the clockwork mind, out of a piston facing up with bars on it, a
 * stone table beside it, an anvil beside the table and a cauldron beside the anvil and the piston, round the square
 * clockwise. Break any of it and the rest goes back to what it was.
 */
public class GolemPressBlock extends BaseEntityBlock {
    public static final MapCodec<GolemPressBlock> CODEC = simpleCodec(GolemPressBlock::new);
    /** Towards the table. */
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    /** What it takes from a wand to make one: the original's price. */
    public static final java.util.function.Supplier<AspectList> COSTS =
            () -> AspectList.of(ModAspects.FIRE, 50).add(ModAspects.ORDER, 50).add(ModAspects.AIR, 50);
    /** Set while a press is taking itself apart, so that its parts do not each try to take it apart again. */
    private static boolean undoing;

    public GolemPressBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<GolemPressBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GolemPressBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.GOLEM_PRESS.get(),
                level.isClientSide ? GolemPressBlockEntity::clientTick : GolemPressBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        return open(level, pos, player);
    }

    /** Opens the press whose heart is here. */
    static InteractionResult open(Level level, BlockPos heart, Player player) {
        if (!level.isClientSide && level.getBlockEntity(heart) instanceof GolemPressBlockEntity press) {
            player.openMenu(press, heart);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock())) {
            if (level.getBlockEntity(pos) instanceof GolemPressBlockEntity press) {
                press.spill();
            }
            undo(level, pos, null);
        }
        super.onRemove(state, level, pos, replacement, moving);
    }

    // --- making and unmaking ----------------------------------------------------------------------------------

    /** Where the table is, beside the heart. */
    public static BlockPos table(BlockPos heart, Direction facing) {
        return heart.relative(facing);
    }

    /** Where the anvil is, beside the table. */
    public static BlockPos anvil(BlockPos heart, Direction facing) {
        return heart.relative(facing).relative(facing.getClockWise());
    }

    /** Where the cauldron is, beside the anvil and the heart. */
    public static BlockPos cauldron(BlockPos heart, Direction facing) {
        return heart.relative(facing.getClockWise());
    }

    /**
     * Takes the press apart round its heart, each placeholder back to what it stood in for and the heart back to a
     * piston facing up, unless it is the heart itself that was broken. The block that was broken is left alone: it
     * drops as what it stood in for, and putting it back as well would give it twice.
     */
    public static void undo(Level level, BlockPos heart, @Nullable BlockPos broken) {
        if (undoing || level.isClientSide) {
            return;
        }
        undoing = true;
        try {
            for (BlockPos at : BlockPos.betweenClosed(heart.offset(-1, 0, -1), heart.offset(1, 1, 1))) {
                if (at.equals(broken)) {
                    continue;
                }
                BlockState there = level.getBlockState(at);
                if (there.getBlock() instanceof GolemPressPartBlock) {
                    level.setBlock(at, there.getValue(GolemPressPartBlock.PART).original(), Block.UPDATE_ALL);
                }
            }
            if (broken != null && level.getBlockState(heart).is(ModBlocks.GOLEM_PRESS.get())) {
                level.setBlock(heart, Blocks.PISTON.defaultBlockState().setValue(PistonBaseBlock.FACING, Direction.UP),
                        Block.UPDATE_ALL);
            }
        } finally {
            undoing = false;
        }
    }

    static boolean undoing() {
        return undoing;
    }

    /**
     * Makes one out of the blocks round what was clicked, if they are laid out right, the wand can pay, and its
     * bearer knows the clockwork mind. Any of the five may be clicked.
     */
    public static boolean form(Level level, BlockPos clicked, Player player, ItemStack wand) {
        if (!PlayerKnowledge.of(player).hasResearch(Alchemia.id("clockwork_mind"))) {
            return false;
        }
        for (BlockPos at : BlockPos.betweenClosed(clicked.offset(-1, -1, -1), clicked.offset(1, 1, 1))) {
            Direction facing = fits(level, at);
            if (facing == null) {
                continue;
            }
            VisHolder holder = VisHolder.of(wand);
            AspectList cost = COSTS.get();
            if (holder == null || !holder.holds(wand, cost, player, VisHolder.Spending.CRAFTING)) {
                return false;
            }
            if (!level.isClientSide) {
                holder.take(wand, cost, player, VisHolder.Spending.CRAFTING);
                build(level, at.immutable(), facing);
                level.playSound(null, clicked, ModSounds.WAND.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return true;
        }
        return false;
    }

    /**
     * Which way a press with its heart here would face, if the blocks round it are laid out for one, or null. The
     * original looked for the table on each side in turn and took the first, so a heart with two tables beside it
     * faces whichever comes first.
     */
    @Nullable
    public static Direction fits(Level level, BlockPos heart) {
        BlockState piston = level.getBlockState(heart);
        if (!piston.is(Blocks.PISTON) || piston.getValue(PistonBaseBlock.FACING) != Direction.UP
                || piston.getValue(PistonBaseBlock.EXTENDED)
                || !level.getBlockState(heart.above()).is(Blocks.IRON_BARS)) {
            return null;
        }
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            if (level.getBlockState(table(heart, facing)).is(ModBlocks.STONE_TABLE.get())) {
                if (level.getBlockState(anvil(heart, facing)).is(Blocks.ANVIL)
                        && level.getBlockState(cauldron(heart, facing)).is(Blocks.CAULDRON)) {
                    return facing;
                }
                return null;
            }
        }
        return null;
    }

    /** Builds the press on a heart already known to fit, without asking anyone to pay for it. */
    public static void build(Level level, BlockPos heart, Direction facing) {
        undoing = true;
        try {
            level.setBlock(heart.above(), part(GolemPressPartBlock.Part.BARS), Block.UPDATE_ALL);
            level.setBlock(table(heart, facing), part(GolemPressPartBlock.Part.TABLE), Block.UPDATE_ALL);
            level.setBlock(anvil(heart, facing), part(GolemPressPartBlock.Part.ANVIL), Block.UPDATE_ALL);
            level.setBlock(cauldron(heart, facing), part(GolemPressPartBlock.Part.CAULDRON), Block.UPDATE_ALL);
            level.setBlock(heart, ModBlocks.GOLEM_PRESS.get().defaultBlockState().setValue(FACING, facing),
                    Block.UPDATE_ALL);
        } finally {
            undoing = false;
        }
    }

    private static BlockState part(GolemPressPartBlock.Part part) {
        return ModBlocks.GOLEM_PRESS_PART.get().defaultBlockState().setValue(GolemPressPartBlock.PART, part);
    }
}
