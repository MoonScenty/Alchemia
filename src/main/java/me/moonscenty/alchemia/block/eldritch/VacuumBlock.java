package me.moonscenty.alchemia.block.eldritch;

import me.moonscenty.alchemia.client.EldritchEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The deadly void, that the ancient bedrock gives way to where it is laid open.
 * <p>
 * Its source floods the open air next to it, one block at a time every quarter second, each block a step further
 * from the source, out to twelve. A block that is cut off from anything nearer the source wastes away a step at a
 * time and is gone. It cannot be seen, struck or walked on, and it is dark.
 * <p>
 * It pulls whatever is in it toward the source, things that fall harder than creatures. The source does ten damage
 * as if from falling out of the world; the rest now and then one, and less often the further out. Creatures that
 * breathe water are safe from all but the source.
 */
public class VacuumBlock extends Block {
    public static final int FURTHEST = 12;
    public static final IntegerProperty SPREAD = IntegerProperty.create("spread", 0, FURTHEST);

    public VacuumBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(SPREAD, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SPREAD);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(SPREAD) == 0 ? 0 : 15;
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return state.getValue(SPREAD) > 0;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        check(level, pos, state);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        check(level, pos, state);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos from,
            boolean moving) {
        if (!level.isClientSide) {
            check(level, pos, state);
        }
    }

    private void check(Level level, BlockPos pos, BlockState state) {
        int spread = state.getValue(SPREAD);
        boolean fed = spread == 0;
        if (!fed) {
            for (Direction side : Direction.values()) {
                BlockPos next = pos.relative(side);
                if (!level.isLoaded(next)) {
                    continue;
                }
                BlockState there = level.getBlockState(next);
                if (there.is(this) && there.getValue(SPREAD) < spread) {
                    fed = true;
                    break;
                }
            }
        }
        if (fed) {
            if (spread < FURTHEST) {
                for (Direction side : Direction.values()) {
                    BlockPos next = pos.relative(side);
                    if (!level.isLoaded(next)) {
                        continue;
                    }
                    BlockState there = level.getBlockState(next);
                    if ((!there.is(this) && level.isEmptyBlock(next))
                            || (there.is(this) && there.getValue(SPREAD) > spread + 1)) {
                        level.setBlockAndUpdate(next, defaultBlockState().setValue(SPREAD, spread + 1));
                        level.scheduleTick(next, this, 5);
                        break;
                    }
                }
            }
        } else if (spread < FURTHEST) {
            level.setBlockAndUpdate(pos, state.setValue(SPREAD, spread + 1));
            level.scheduleTick(pos, this, 5);
        } else {
            level.removeBlock(pos, false);
        }
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity instanceof Player player && player.getAbilities().instabuild) {
            return;
        }
        if (entity.isInvulnerableTo(level.damageSources().fellOutOfWorld())) {
            return;
        }
        int spread = state.getValue(SPREAD);
        if (!level.isClientSide && !(spread != 0 && entity instanceof Mob mob && mob.canBreatheUnderwater())
                && (spread == 0 || level.random.nextInt(2000) < FURTHEST - spread)) {
            entity.hurt(level.damageSources().fellOutOfWorld(), spread == 0 ? 10.0F : 1.0F);
        }
        if (spread > 0 && entity.isPushable() || spread > 0 && entity instanceof ItemEntity) {
            for (Direction side : Direction.values()) {
                BlockState there = level.getBlockState(pos.relative(side));
                if (there.is(this) && there.getValue(SPREAD) < spread) {
                    float upward = entity instanceof ItemEntity ? 1.0F : 10.0F;
                    entity.setDeltaMovement(entity.getDeltaMovement().add(
                            side.getStepX() / (25.0F * (spread + 1)),
                            side.getStepY() / (upward * (spread + 1)),
                            side.getStepZ() / (25.0F * (spread + 1))));
                }
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        int spread = state.getValue(SPREAD);
        if (spread > 3 && random.nextInt(500) < spread) {
            BlockPos source = source(level, pos, spread);
            if (!source.equals(pos)) {
                EldritchEffects.voidDraw(level, pos, source);
            }
        }
    }

    /** Down the steps to where it comes from. */
    private BlockPos source(Level level, BlockPos pos, int spread) {
        for (int step = 0; step < FURTHEST + 1; step++) {
            boolean moved = false;
            for (Direction side : Direction.values()) {
                BlockState there = level.getBlockState(pos.relative(side));
                if (there.is(this) && there.getValue(SPREAD) < spread) {
                    pos = pos.relative(side);
                    spread = there.getValue(SPREAD);
                    moved = true;
                    break;
                }
            }
            if (!moved) {
                break;
            }
        }
        return pos;
    }
}
