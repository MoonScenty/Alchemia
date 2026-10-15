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

/**
 * A redstone relay: a repeater with two toggles on it, one for how strong a signal it takes to set it off and one
 * for how strong a signal it gives when it is.
 *
 * <p>It is put down and wired as a repeater is, its output the end with the torch on it, and it answers a tenth of a
 * second after its input changes. The toggle at the back sets the least it answers to; the one beside the torch, what
 * it gives out. Each steps from one to fifteen and round again.
 */
public class RedstoneRelayBlock extends DiodeBlock {
    public static final MapCodec<RedstoneRelayBlock> CODEC = simpleCodec(RedstoneRelayBlock::new);
    /** The least signal it answers to, and what it gives when it does. */
    public static final IntegerProperty IN = IntegerProperty.create("in", 1, 15);
    public static final IntegerProperty OUT = IntegerProperty.create("out", 1, 15);
    /** How long it takes to answer, in ticks. The original's. */
    private static final int DELAY = 2;

    /**
     * Where the toggles are, across the top: how far toward the input (positive) or the output, and how far to the
     * input's right hand (positive) or left, from the middle. The original's.
     */
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

    /** As a repeater is put down, its output away from whoever puts it down; crouching turns it round. */
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

    /** Wire runs into it only at its two ends, as into a repeater. */
    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return direction != null && direction.getAxis() == state.getValue(FACING).getAxis();
    }

    /** A toggle clicked on steps on; anywhere else, the click goes on to whatever is in hand. */
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

    /** The slab, with the two toggles standing on it so that a click finds them. */
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
