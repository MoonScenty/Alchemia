package me.moonscenty.alchemia.block;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RedstoneRelayBlock extends DiodeBlock {
    public static final MapCodec<RedstoneRelayBlock> CODEC = simpleCodec(RedstoneRelayBlock::new);
    public static final IntegerProperty IN = IntegerProperty.create("in", 1, 15);
    public static final IntegerProperty OUT = IntegerProperty.create("out", 1, 15);
    // original's value
    private static final int DELAY = 2;

    // {toward input (+) / output, toward input's right (+) / left} offsets from centre; original's values
    private static final double[] OUT_TOGGLE = {-0.4375, -0.0625, 0.0625, 0.4375};
    private static final double[] IN_TOGGLE = {0.0625, 0.4375, -0.1875, 0.1875};

    public RedstoneRelayBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, false)
                .setValue(IN, 1).setValue(OUT, 15));
    }

    @Override
    protected MapCodec<RedstoneRelayBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED, IN, OUT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction looking = context.getHorizontalDirection();
        boolean crouching = context.getPlayer() != null && context.getPlayer().isShiftKeyDown();
        return defaultBlockState().setValue(FACING, crouching ? looking : looking.getOpposite());
    }

    @Override
    protected int getDelay(BlockState state) {
        return DELAY;
    }

    @Override
    protected boolean shouldTurnOn(Level level, BlockPos pos, BlockState state) {
        return getInputSignal(level, pos, state) >= state.getValue(IN);
    }

    @Override
    protected int getOutputSignal(BlockGetter level, BlockPos pos, BlockState state) {
        return state.getValue(OUT);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return direction != null && direction.getAxis() == state.getValue(FACING).getAxis();
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        if (!player.getAbilities().mayBuild) {
            return InteractionResult.PASS;
        }
        Direction facing = state.getValue(FACING);
        Vec3 from = hit.getLocation().subtract(pos.getCenter());
        double toward = from.x * facing.getStepX() + from.z * facing.getStepZ();
        Direction right = facing.getClockWise();
        double across = from.x * right.getStepX() + from.z * right.getStepZ();
        IntegerProperty toggle = on(OUT_TOGGLE, toward, across) ? OUT : on(IN_TOGGLE, toward, across) ? IN : null;
        if (toggle == null) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            BlockState stepped = state.cycle(toggle);
            level.setBlock(pos, stepped, Block.UPDATE_ALL);
            if (toggle == IN) {
                checkTickOnNeighbor(level, pos, stepped);
            } else if (stepped.getValue(POWERED)) {
                updateNeighborsInFront(level, pos, stepped);
            }
            player.displayClientMessage(Component.translatable("block.alchemia.redstone_relay." + toggle.getName(),
                    stepped.getValue(toggle)), true);
            level.playSound(null, pos, ModSounds.KEY.get(), SoundSource.BLOCKS, 0.5F, 1.0F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static boolean on(double[] toggle, double toward, double across) {
        return toward >= toggle[0] && toward <= toggle[1] && across >= toggle[2] && across <= toggle[3];
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        return Shapes.or(SHAPE, toggle(facing, OUT_TOGGLE), toggle(facing, IN_TOGGLE));
    }

    private static VoxelShape toggle(Direction facing, double[] toggle) {
        Direction right = facing.getClockWise();
        double minX = 1;
        double maxX = 0;
        double minZ = 1;
        double maxZ = 0;
        for (double toward : new double[] {toggle[0], toggle[1]}) {
            for (double across : new double[] {toggle[2], toggle[3]}) {
                double x = 0.5 + toward * facing.getStepX() + across * right.getStepX();
                double z = 0.5 + toward * facing.getStepZ() + across * right.getStepZ();
                minX = Math.min(minX, x);
                maxX = Math.max(maxX, x);
                minZ = Math.min(minZ, z);
                maxZ = Math.max(maxZ, z);
            }
        }
        return Shapes.box(minX, 0.0, minZ, maxX, 0.25, maxZ);
    }
}
