package me.moonscenty.alchemia.block.taint;

import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Degraded taint dust, the original's {@code BlockTaintDust}: what is left where taint has died back -- the crust
 * and the tainted soil and the tainted logs crumble into it. It is a dry, finite heap in four depths, a quarter block
 * each, that runs downhill and levels out like sand poured from a sack, and now and then, one random tick in
 * twenty-five, it weathers away a depth. Dug up, it gives nothing back, but the deeper it was, the better the chance
 * of finding bone meal or a taint tendril in it.
 */
public class TaintDustBlock extends SettlingLayerBlock {
    /** How deep the heap is, a quarter block to a whole one. */
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 3);
    public static final int FULL = 3;

    /** How often it settles, in ticks: the original's thick fluid, eight thousand of viscosity over two hundred. */
    private static final int SETTLES_EVERY = 40;
    /** One random tick in this many, it weathers away a depth. The original's default. */
    private static final int WEATHERS_ONE_IN = 25;
    /** The chance, per depth, of something found in it when it is dug. */
    private static final float FIND_PER_DEPTH = 0.0333F;

    private static final VoxelShape[] OUTLINES = new VoxelShape[FULL + 1];
    private static final VoxelShape[] SOLID = new VoxelShape[FULL + 1];

    static {
        for (int level = 0; level <= FULL; level++) {
            OUTLINES[level] = Block.box(0, 0, 0, 16, 4 * (level + 1), 16);
            SOLID[level] = level == 0 ? Shapes.empty() : Block.box(0, 0, 0, 16, 4 * level, 16);
        }
    }

    public TaintDustBlock(Properties properties) {
        super(properties, SETTLES_EVERY);
    }

    @Override
    protected IntegerProperty depthProperty() {
        return LEVEL;
    }

    @Override
    protected int full() {
        return FULL;
    }

    public static BlockState of(Block dust, int level) {
        return dust.defaultBlockState().setValue(LEVEL, Math.clamp(level, 0, FULL));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return OUTLINES[state.getValue(LEVEL)];
    }

    /** Stood on a quarter lower than it shows, as the original's was. */
    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return SOLID[state.getValue(LEVEL)];
    }

    /** Weathers a depth away now and then, the last of it to nothing; otherwise it settles. */
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(WEATHERS_ONE_IN) != 0) {
            settle(state, level, pos, random);
            return;
        }
        int depth = state.getValue(LEVEL);
        if (depth == 0) {
            level.removeBlock(pos, false);
        } else {
            level.setBlock(pos, of(this, depth - 1), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        int depth = state.getValue(LEVEL);
        if (!level.isClientSide && depth > 0 && level.random.nextFloat() < depth * FIND_PER_DEPTH) {
            popResource(level, pos, new ItemStack(level.random.nextBoolean()
                    ? Items.BONE_MEAL : ModItems.TAINT_TENDRIL.get()));
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
