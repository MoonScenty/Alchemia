package me.moonscenty.alchemia.block;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.block.entity.CrucibleBlockEntity;
import me.moonscenty.alchemia.client.CrucibleEffects;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import me.moonscenty.alchemia.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
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

    /** Whether the pot is doing anything: wet, and hot enough for long enough. */
    public static boolean boiling(BlockGetter level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof CrucibleBlockEntity crucible && crucible.working();
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

        if (!(level.getBlockEntity(pos) instanceof CrucibleBlockEntity crucible)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (held.is(Items.WATER_BUCKET) && filled < FULL) {
            if (!level.isClientSide) {
                crucible.fill();
                if (!player.getAbilities().instabuild) {
                    player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                }
                level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        if (held.is(Items.BUCKET) && filled == FULL) {
            if (!level.isClientSide) {
                crucible.drain();
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
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.isRainingAt(pos.above()) && random.nextFloat() < 0.05F && !heated(level, pos)
                && level.getBlockEntity(pos) instanceof CrucibleBlockEntity crucible) {
            crucible.fill();
        }
    }

    /** A pot broken with something in it lets it all go into the air at once. */
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock()) && level.getBlockEntity(pos) instanceof CrucibleBlockEntity crucible) {
            crucible.spillAll();
        }
        super.onRemove(state, level, pos, replacement, moving);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        // the client draws the froth and the bubbles every tick, as the original did
        return level.isClientSide
                ? createTickerHelper(type, ModBlockEntities.CRUCIBLE.get(), CrucibleEffects::tick)
                : createTickerHelper(type, ModBlockEntities.CRUCIBLE.get(), CrucibleBlockEntity::tick);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // the bubbles are drawn every tick by CrucibleEffects; here, as in the original, only the odd pop is heard
        if (random.nextInt(10) == 0 && boiling(level, pos)) {
            level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.LAVA_POP, SoundSource.BLOCKS,
                    0.1F + random.nextFloat() * 0.1F, 1.2F + random.nextFloat() * 0.2F, false);
        }
    }
}
