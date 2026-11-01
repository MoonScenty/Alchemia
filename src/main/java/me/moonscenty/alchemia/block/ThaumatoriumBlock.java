package me.moonscenty.alchemia.block;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.block.entity.ThaumatoriumBlockEntity;
import me.moonscenty.alchemia.item.VisHolder;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.player.PlayerKnowledge;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModSounds;
import me.moonscenty.alchemia.Alchemia;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ThaumatoriumBlock extends BaseEntityBlock {
    public static final MapCodec<ThaumatoriumBlock> CODEC = simpleCodec(ThaumatoriumBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty TOP = BooleanProperty.create("top");
    public static final java.util.function.Supplier<AspectList> COSTS = () -> AspectList.of(ModAspects.FIRE, 50)
            .add(ModAspects.ORDER, 100).add(ModAspects.WATER, 100);

    public ThaumatoriumBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(TOP, false));
    }

    @Override
    protected MapCodec<ThaumatoriumBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, TOP);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ThaumatoriumBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.THAUMATORIUM.get(), level.isClientSide
                ? ThaumatoriumBlockEntity::clientTick : ThaumatoriumBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof ThaumatoriumBlockEntity half) || half.main() == null) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            ThaumatoriumBlockEntity main = half.main();
            player.openMenu(main, buffer -> buffer.writeBlockPos(main.getBlockPos()));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbour,
            LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        boolean whole = state.getValue(TOP)
                ? level.getBlockState(pos.below()).is(this)
                : level.getBlockState(pos.above()).is(this)
                        && level.getBlockState(pos.below()).is(ModBlocks.CRUCIBLE.get());
        return whole ? state : ModBlocks.ALCHEMICAL_CONSTRUCT.get().defaultBlockState();
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock()) && !state.getValue(TOP)
                && level.getBlockEntity(pos) instanceof ThaumatoriumBlockEntity main) {
            Containers.dropContents(level, pos, main.contents());
        }
        super.onRemove(state, level, pos, replacement, moving);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof ThaumatoriumBlockEntity half && !state.getValue(TOP)
                ? net.minecraft.world.inventory.AbstractContainerMenu.getRedstoneSignalFromContainer(half) : 0;
    }

    public static boolean form(Level level, BlockPos clicked, Direction side, Player player, ItemStack wand) {
        if (!side.getAxis().isHorizontal()
                || !PlayerKnowledge.of(player).hasResearch(Alchemia.id("thaumatorium"))) {
            return false;
        }
        BlockPos base;
        if (construct(level, clicked.above()) && level.getBlockState(clicked.below()).is(ModBlocks.CRUCIBLE.get())
                && construct(level, clicked)) {
            base = clicked;
        } else if (construct(level, clicked.below()) && construct(level, clicked)
                && level.getBlockState(clicked.below(2)).is(ModBlocks.CRUCIBLE.get())) {
            base = clicked.below();
        } else if (level.getBlockState(clicked).is(ModBlocks.CRUCIBLE.get()) && construct(level, clicked.above())
                && construct(level, clicked.above(2))) {
            base = clicked.above();
        } else {
            return false;
        }
        if (!VisHolder.pay(wand, COSTS.get(), player, VisHolder.Spending.CRAFTING)) {
            return false;
        }
        if (level.isClientSide) {
            return true;
        }
        BlockState made = ModBlocks.THAUMATORIUM.get().defaultBlockState().setValue(FACING, side);
        level.setBlock(base, made, Block.UPDATE_CLIENTS);
        level.setBlock(base.above(), made.setValue(TOP, true), Block.UPDATE_ALL);
        level.playSound(null, base, ModSounds.WAND.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        for (BlockPos puff : new BlockPos[] {base, base.above()}) {
            level.playSound(null, puff, ModSounds.POOF.get(), SoundSource.BLOCKS, 0.4F,
                    1.0F + (float) level.random.nextGaussian() * 0.05F);
        }
        return true;
    }

    private static boolean construct(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(ModBlocks.ALCHEMICAL_CONSTRUCT.get());
    }
}
