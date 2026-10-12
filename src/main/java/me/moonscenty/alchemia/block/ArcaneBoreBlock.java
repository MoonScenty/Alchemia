package me.moonscenty.alchemia.block;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.block.entity.ArcaneBoreBlockEntity;
import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import me.moonscenty.alchemia.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * An arcane bore. It goes only directly above or below a base, faces the one who put it down as a piston does, and
 * digs the way it faces; a wand on any face turns it to dig that way. Drawn wholly by its renderer.
 */
public class ArcaneBoreBlock extends BaseEntityBlock {
    public static final MapCodec<ArcaneBoreBlock> CODEC = simpleCodec(ArcaneBoreBlock::new);
    /** The way it digs. */
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public ArcaneBoreBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    protected MapCodec<ArcaneBoreBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    /** Only against the top or bottom of a base. */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction side = context.getClickedFace();
        BlockPos against = context.getClickedPos().relative(side.getOpposite());
        if (side.getAxis().isHorizontal()
                || !context.getLevel().getBlockState(against).is(ModBlocks.ARCANE_BORE_BASE.get())) {
            return null;
        }
        return defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ArcaneBoreBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.ARCANE_BORE.get(), level.isClientSide
                ? ArcaneBoreBlockEntity::clientTick : ArcaneBoreBlockEntity::serverTick);
    }

    /** A wand goes on to the wand, which turns the bore; anything else opens it. */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        return stack.getItem() instanceof WandItem
                ? ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION
                : super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof ArcaneBoreBlockEntity bore)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            player.openMenu(bore, buffer -> buffer.writeBlockPos(pos));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock()) && level.getBlockEntity(pos) instanceof ArcaneBoreBlockEntity bore) {
            Containers.dropContents(level, pos, bore.contents());
        }
        super.onRemove(state, level, pos, replacement, moving);
    }
}
