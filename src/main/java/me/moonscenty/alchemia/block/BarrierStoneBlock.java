package me.moonscenty.alchemia.block;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.block.entity.BarrierStoneBlockEntity;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import me.moonscenty.alchemia.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;

/**
 * A barrier stone, the original's barrier paving stone: arcane brick worked into a ward. It keeps the two blocks over
 * it filled with a warding aura that no creature but a player walks through, and pushes back anything that tries to
 * jump or fly over. A redstone signal turns it off.
 * <p>
 * Its runes tell how it is: blue while a signal holds it off, red while something sits in the space it should ward,
 * purple while it is holding back a creature.
 */
public class BarrierStoneBlock extends BaseEntityBlock {
    public static final MapCodec<BarrierStoneBlock> CODEC = simpleCodec(BarrierStoneBlock::new);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    /** A paving stone stands a sixteenth short of a full block, the way farmland does; it is walked on as one. */
    private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 15.0, 16.0);

    private static final Vector3f BLUE = new Vector3f(0.4F, 0.15F, 0.9F);
    private static final Vector3f RED = new Vector3f(0.95F, 0.15F, 0.15F);
    private static final Vector3f PURPLE = new Vector3f(0.8F, 0.0F, 0.65F);

    public BarrierStoneBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(POWERED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(POWERED, context.getLevel().hasNeighborSignal(context.getClickedPos()));
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbor, BlockPos from,
            boolean moving) {
        boolean powered = level.hasNeighborSignal(pos);
        if (powered != state.getValue(POWERED)) {
            level.setBlock(pos, state.setValue(POWERED, powered), Block.UPDATE_ALL);
        }
    }

    /** Whether a ward at this place is up: a barrier stone below it, one or two down, with no signal on it. */
    public static boolean warding(BlockGetter level, BlockPos ward) {
        for (int down = 1; down <= 2; down++) {
            BlockState below = level.getBlockState(ward.below(down));
            if (below.is(ModBlocks.BARRIER_STONE.get())) {
                return !below.getValue(POWERED);
            }
        }
        return false;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return Shapes.block();
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BarrierStoneBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return level.isClientSide ? null
                : createTickerHelper(type, ModBlockEntities.BARRIER_STONE.get(), BarrierStoneBlockEntity::serverTick);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        Vector3f colour = null;
        int runes = 0;
        if (state.getValue(POWERED)) {
            colour = BLUE;
            runes = 2;
        } else if (blocked(level, pos.above()) || blocked(level, pos.above(2))) {
            colour = RED;
            runes = 3;
        } else if (!level.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(1.0),
                living -> !(living instanceof Player)).isEmpty()) {
            colour = PURPLE;
            runes = 1;
        }
        for (int i = 0; i < runes; i++) {
            level.addParticle(new DustParticleOptions(colour, 0.6F), pos.getX() + random.nextDouble(),
                    pos.getY() + 1.0 + random.nextDouble() * 1.8, pos.getZ() + random.nextDouble(), 0.0, 0.02, 0.0);
        }
    }

    /** Something other than the ward, or the open air it will soon fill, where the ward should be. */
    private static boolean blocked(Level level, BlockPos at) {
        BlockState there = level.getBlockState(at);
        return !there.isAir() && !there.is(ModBlocks.WARDING_AURA.get());
    }
}
