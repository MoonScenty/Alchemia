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
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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

public class EssentiaSmelterBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final MapCodec<EssentiaSmelterBlock> CODEC =
            simpleCodec(properties -> new EssentiaSmelterBlock(properties, Tier.BASIC));

    // original's values; the void tier pours no faster than basic, as in the original's code despite its description
    public enum Tier {
        BASIC(0.80F, 40),
        ALCHEMIUM(0.85F, 25),
        VOID(0.95F, 40);

        private final float efficiency;
        private final int poursEvery;

        Tier(float efficiency, int poursEvery) {
            this.efficiency = efficiency;
            this.poursEvery = poursEvery;
        }

        public float efficiency() {
            return efficiency;
        }

        public int poursEvery() {
            return poursEvery;
        }
    }

    private final Tier tier;

    public EssentiaSmelterBlock(Properties properties, Tier tier) {
        super(properties);
        this.tier = tier;
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LIT, false));
    }

    public Tier tier() {
        return tier;
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        if (id != EssentiaSmelterBlockEntity.VENTED) {
            return super.triggerEvent(state, level, pos, id, param);
        }
        if (level.isClientSide) {
            Direction out = Direction.from3DDataValue(param);
            RandomSource random = level.random;
            double x = pos.getX() + 0.5 + out.getStepX();
            double y = pos.getY() + 0.5;
            double z = pos.getZ() + 0.5 + out.getStepZ();
            level.playLocalSound(x, y, z, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.25F,
                    2.6F + (random.nextFloat() - random.nextFloat()) * 0.8F, false);
            for (int puff = 0; puff < 4; puff++) {
                level.addParticle(ParticleTypes.SMOKE, x + jitter(random), y + jitter(random), z + jitter(random),
                        out.getStepX() / 4.0 + jitter(random), jitter(random), out.getStepZ() / 4.0 + jitter(random));
            }
        }
        return true;
    }

    private static double jitter(RandomSource random) {
        return 0.1 - random.nextFloat() * 0.2;
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

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof EssentiaSmelterBlockEntity smelter)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            player.openMenu(smelter, buffer -> buffer.writeBlockPos(pos));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) {
            return;
        }
        Direction facing = state.getValue(FACING);
        if (random.nextInt(5) == 0) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 0.6F, 1.0F, false);
        }
        if (facing.getAxis().isVertical()) {
            return;
        }
        double across = random.nextDouble() * 0.5 - 0.25;
        double x = pos.getX() + 0.5 + facing.getStepX() * 0.52 + facing.getStepZ() * across;
        double y = pos.getY() + 0.2 + random.nextDouble() * 5.0 / 16.0;
        double z = pos.getZ() + 0.5 + facing.getStepZ() * 0.52 + facing.getStepX() * across;
        level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.0, 0.0);
        level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0, 0.0, 0.0);
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
