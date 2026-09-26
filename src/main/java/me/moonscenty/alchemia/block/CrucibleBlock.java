package me.moonscenty.alchemia.block;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.block.entity.CrucibleBlockEntity;
import me.moonscenty.alchemia.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A pot of water kept over a fire, for dissolving things into what they are made of.
 * <p>
 * Nothing happens in a cold one, and nothing happens in a dry one. Both are on purpose: an alchemist's first
 * problem is keeping a fire lit under a pot, and everything after that depends on it.
 */
public class CrucibleBlock extends BaseEntityBlock {
    /** How full it is. Zero is dry; three is up to the brim. */
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 3);
    public static final int FULL = 3;

    public static final MapCodec<CrucibleBlock> CODEC = simpleCodec(CrucibleBlock::new);

    // the pot, with the inside taken back out of it
    private static final VoxelShape SHAPE = Shapes.join(
            Shapes.block(),
            Shapes.or(Block.box(2, 4, 2, 14, 16, 14), Block.box(0, 0, 0, 16, 3, 16)),
            BooleanOp.ONLY_FIRST);

    public CrucibleBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(LEVEL, 0));
    }

    @Override
    protected MapCodec<CrucibleBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CrucibleBlockEntity(pos, state);
    }

    /**
     * Whether there is a fire under the pot.
     * <p>
     * A campfire counts only while it is lit, which no tag can say on its own, so the state is asked directly for
     * anything that has a light to it.
     */
    public static boolean heated(BlockGetter level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        if (!below.is(ModTags.Blocks.HEATS_CRUCIBLE)) {
            return false;
        }
        return !below.hasProperty(BlockStateProperties.LIT) || below.getValue(BlockStateProperties.LIT);
    }

    /** Whether the pot is doing anything: wet and hot at once. */
    public static boolean boiling(BlockGetter level, BlockPos pos, BlockState state) {
        return state.getValue(LEVEL) > 0 && heated(level, pos);
    }

    /**
     * Filling and emptying. A bucket of water fills it to the brim in one go; an empty bucket takes a full one back.
     * <p>
     * Filling a pot that already holds something washes what is in it away, since there is no way to pour water in
     * without doing that.
     */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        int filled = state.getValue(LEVEL);

        if (held.is(Items.WATER_BUCKET) && filled < FULL) {
            if (!level.isClientSide) {
                level.setBlockAndUpdate(pos, state.setValue(LEVEL, FULL));
                if (level.getBlockEntity(pos) instanceof CrucibleBlockEntity crucible) {
                    crucible.empty();
                }
                if (!player.getAbilities().instabuild) {
                    player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                }
                level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        if (held.is(Items.BUCKET) && filled == FULL) {
            if (!level.isClientSide) {
                level.setBlockAndUpdate(pos, state.setValue(LEVEL, 0));
                if (level.getBlockEntity(pos) instanceof CrucibleBlockEntity crucible) {
                    crucible.empty();
                }
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                    player.getInventory().placeItemBackInInventory(new ItemStack(Items.WATER_BUCKET));
                }
                level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    /** Rain fills a pot left out in it, a level at a time, the way a cauldron does. */
    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return state.getValue(LEVEL) < FULL;
    }

    @Override
    protected void randomTick(BlockState state, net.minecraft.server.level.ServerLevel level, BlockPos pos,
            RandomSource random) {
        if (level.isRainingAt(pos.above()) && random.nextFloat() < 0.05F && !heated(level, pos)) {
            level.setBlockAndUpdate(pos, state.setValue(LEVEL, state.getValue(LEVEL) + 1));
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!boiling(level, pos, state)) {
            return;
        }
        double top = pos.getY() + 0.3 + state.getValue(LEVEL) * 0.22;
        if (random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.BUBBLE_POP,
                    pos.getX() + 0.25 + random.nextDouble() * 0.5, top,
                    pos.getZ() + 0.25 + random.nextDouble() * 0.5, 0.0, 0.0, 0.0);
        }
        if (random.nextInt(8) == 0) {
            level.addParticle(ParticleTypes.SMOKE,
                    pos.getX() + 0.3 + random.nextDouble() * 0.4, top + 0.1,
                    pos.getZ() + 0.3 + random.nextDouble() * 0.4, 0.0, 0.02, 0.0);
        }
    }
}
