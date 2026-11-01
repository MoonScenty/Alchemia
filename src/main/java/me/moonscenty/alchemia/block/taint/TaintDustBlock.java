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

public class TaintDustBlock extends SettlingLayerBlock {
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 3);
    public static final int FULL = 3;

    // original's viscosity 8000 / 200
    private static final int SETTLES_EVERY = 40;
    // original's value
    private static final int WEATHERS_ONE_IN = 25;
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

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return SOLID[state.getValue(LEVEL)];
    }

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
