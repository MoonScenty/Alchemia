package me.moonscenty.alchemia.block;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.block.entity.HungryChestBlockEntity;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A chest with an appetite: whatever is dropped on it, it eats.
 *
 * <p>It is a chest in every other way -- it opens, it holds twenty-seven stacks, a hopper can work it -- but loose
 * things that come to rest on its lid are swallowed whole, as much as there is room for, with a snap of the lid
 * and a sound of eating. What will not fit stays where it fell.
 *
 * <p>It never makes a double chest. Two of them side by side are two of them, each hungry on its own account.
 */
public class HungryChestBlock extends BaseEntityBlock {
    public static final MapCodec<HungryChestBlock> CODEC = simpleCodec(HungryChestBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 14.0, 15.0);

    public HungryChestBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH)
                .setValue(ChestBlock.TYPE, ChestType.SINGLE));
    }

    @Override
    protected MapCodec<HungryChestBlock> codec() {
        return CODEC;
    }

    /**
     * Facing, and the chest type that is always single. The game's chest keeps asking which half of a pair it is
     * when it plays its sounds, so it is given an answer that never changes.
     */
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ChestBlock.TYPE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /** Drawn by its renderer, the lid and all. */
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HungryChestBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return level.isClientSide
                ? createTickerHelper(type, ModBlockEntities.HUNGRY_CHEST.get(), HungryChestBlockEntity::lidTick)
                : null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof HungryChestBlockEntity chest) {
            player.openMenu(chest);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /**
     * Swallowing whatever has come to rest on it.
     * <p>
     * As a hopper takes things in: whole stacks where there is room, part of one where there is only room for
     * part, and nothing at all from a stack it cannot fit any of.
     */
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide || !(entity instanceof ItemEntity loose) || !loose.isAlive()
                || !(level.getBlockEntity(pos) instanceof HungryChestBlockEntity chest)) {
            return;
        }
        int before = loose.getItem().getCount();
        HopperBlockEntity.addItem(chest, loose);
        if (!loose.isAlive() || loose.getItem().getCount() != before) {
            level.playSound(null, pos, SoundEvents.GENERIC_EAT, SoundSource.BLOCKS, 0.25F,
                    (level.random.nextFloat() - level.random.nextFloat()) * 0.2F + 1.0F);
            level.blockEvent(pos, this, HungryChestBlockEntity.SNAPS, 0);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock()) && level.getBlockEntity(pos) instanceof HungryChestBlockEntity chest) {
            Containers.dropContents(level, pos, chest);
            level.updateNeighbourForOutputSignal(pos, this);
        }
        super.onRemove(state, level, pos, replacement, moving);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
    }
}
