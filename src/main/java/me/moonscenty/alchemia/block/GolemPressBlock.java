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

public class GolemPressBlock extends BaseEntityBlock {
    public static final MapCodec<GolemPressBlock> CODEC = simpleCodec(GolemPressBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final java.util.function.Supplier<AspectList> COSTS =
            () -> AspectList.of(ModAspects.FIRE, 50).add(ModAspects.ORDER, 50).add(ModAspects.AIR, 50);
    // guards against each part re-triggering undo while the press comes apart
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

    public static BlockPos table(BlockPos heart, Direction facing) {
        return heart.relative(facing);
    }

    public static BlockPos anvil(BlockPos heart, Direction facing) {
        return heart.relative(facing).relative(facing.getClockWise());
    }

    public static BlockPos cauldron(BlockPos heart, Direction facing) {
        return heart.relative(facing.getClockWise());
    }

    // the broken block is skipped: it drops as what it stood in for
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

    public static boolean form(Level level, BlockPos clicked, Player player, ItemStack wand) {
        if (!PlayerKnowledge.of(player).hasResearch(Alchemia.id("clockwork_mind"))) {
            return false;
        }
        for (BlockPos at : BlockPos.betweenClosed(clicked.offset(-1, -1, -1), clicked.offset(1, 1, 1))) {
            Direction facing = fits(level, at);
            if (facing == null) {
                continue;
            }
            if (!VisHolder.pay(wand, COSTS.get(), player, VisHolder.Spending.CRAFTING)) {
                return false;
            }
            if (!level.isClientSide) {
                build(level, at.immutable(), facing);
                level.playSound(null, clicked, ModSounds.WAND.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return true;
        }
        return false;
    }

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
