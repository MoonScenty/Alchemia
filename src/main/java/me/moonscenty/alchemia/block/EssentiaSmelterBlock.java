package me.moonscenty.alchemia.block;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.block.entity.EssentiaSmelterBlockEntity;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * A furnace that makes no ingots.
 * <p>
 * What it burns comes apart into what it is made of, and that goes up through the hole in its top into whatever is
 * stacked there. A smelter with nothing above it fills up and stops, which is the whole reason for stacking.
 * <p>
 * It is worked by hand or by hopper rather than through a screen. A screen would want a panel drawn for it, and
 * there is nothing to show on one that the block does not already say by burning or not burning.
 */
public class EssentiaSmelterBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public static final MapCodec<EssentiaSmelterBlock> CODEC = simpleCodec(EssentiaSmelterBlock::new);

    public EssentiaSmelterBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LIT, false));
    }

    @Override
    protected MapCodec<EssentiaSmelterBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EssentiaSmelterBlockEntity(pos, state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    /**
     * Puts what is held into whichever slot will take it.
     * <p>
     * Anything that burns is fuel and anything else is work, which leaves nothing to choose and so nothing to get
     * wrong. Coal is never smelted here even though it has aspects of its own; that is the price of not asking.
     */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (held.isEmpty() || !(level.getBlockEntity(pos) instanceof EssentiaSmelterBlockEntity smelter)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        int slot = held.getBurnTime(net.minecraft.world.item.crafting.RecipeType.SMELTING) > 0
                ? EssentiaSmelterBlockEntity.SLOT_FUEL
                : EssentiaSmelterBlockEntity.SLOT_INPUT;
        if (!smelter.canPlaceItem(slot, held)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        ItemStack there = smelter.getItem(slot);
        if (!there.isEmpty() && (!ItemStack.isSameItemSameComponents(there, held)
                || there.getCount() >= there.getMaxStackSize())) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide) {
            if (there.isEmpty()) {
                smelter.setItem(slot, held.split(1));
            } else {
                there.grow(1);
                held.shrink(1);
                smelter.setChanged();
            }
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.5F, 1.0F);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) {
            return;
        }
        Direction facing = state.getValue(FACING);
        double drift = random.nextDouble() * 0.6 - 0.3;
        if (random.nextInt(5) == 0) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 0.6F, 1.0F, false);
        }
        // fire at the mouth, and what has been boiled off rising out of the hole in the top
        level.addParticle(ParticleTypes.SMALL_FLAME,
                pos.getX() + 0.5 + facing.getStepX() * 0.52 + (facing.getAxis().isVertical() ? 0 : drift * facing.getStepZ()),
                pos.getY() + 0.15,
                pos.getZ() + 0.5 + facing.getStepZ() * 0.52 + (facing.getAxis().isVertical() ? 0 : drift * facing.getStepX()),
                0.0, 0.0, 0.0);
        if (random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.CLOUD,
                    pos.getX() + 0.5 + drift * 0.3, pos.getY() + 1.05, pos.getZ() + 0.5 + drift * 0.3,
                    0.0, 0.03, 0.0);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock())
                && level.getBlockEntity(pos) instanceof EssentiaSmelterBlockEntity smelter) {
            smelter.spill();
            Containers.dropContents(level, pos, smelter.contents());
        }
        super.onRemove(state, level, pos, replacement, moving);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return level.isClientSide ? null
                : createTickerHelper(type, ModBlockEntities.ESSENTIA_SMELTER.get(),
                        EssentiaSmelterBlockEntity::tick);
    }
}
