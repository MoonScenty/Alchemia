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
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
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

public class CrucibleBlock extends BaseEntityBlock {
    private static final int BOTTLE = 250;

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof CrucibleBlockEntity crucible
                ? EssentiaSignal.of(crucible.dissolved().total(), CrucibleBlockEntity.CAPACITY) : 0;
    }
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 3);
    public static final int FULL = 3;

    public static final MapCodec<CrucibleBlock> CODEC = simpleCodec(CrucibleBlock::new);

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

    public static boolean heated(BlockGetter level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        if (!below.is(ModTags.Blocks.HEATS_CRUCIBLE)) {
            return false;
        }
        // a campfire only counts while lit, which the tag cannot express
        return !below.hasProperty(BlockStateProperties.LIT) || below.getValue(BlockStateProperties.LIT);
    }

    public static boolean boiling(BlockGetter level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof CrucibleBlockEntity crucible && crucible.working();
    }

    // a bucket of water refills and washes out whatever is dissolved
    @Override
    protected ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        int filled = state.getValue(LEVEL);

        if (!(level.getBlockEntity(pos) instanceof CrucibleBlockEntity crucible)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // original: water is never melted, even when the pot is too full to take it
        boolean bottle = held.is(Items.POTION)
                && held.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(Potions.WATER);
        if (held.is(Items.WATER_BUCKET) || bottle) {
            if (crucible.water() >= CrucibleBlockEntity.FULL) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
            if (!level.isClientSide) {
                crucible.pour(bottle ? BOTTLE : CrucibleBlockEntity.FULL, true);
                player.setItemInHand(hand, ItemUtils.createFilledResult(held, player,
                        new ItemStack(bottle ? Items.GLASS_BOTTLE : Items.BUCKET)));
                level.playSound(null, pos, SoundEvents.GENERIC_SWIM, SoundSource.BLOCKS, 0.33F,
                        1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.3F);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!held.isEmpty() && !player.isSecondaryUseActive() && hit.getDirection() == net.minecraft.core.Direction.UP
                && !(held.getItem() instanceof me.moonscenty.alchemia.item.WandItem) && !held.is(Items.BUCKET)
                && crucible.working()) {
            if (!level.isClientSide && crucible.putByHand(held, player) && !player.getAbilities().instabuild) {
                held.shrink(1);
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

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, net.minecraft.world.entity.Entity entity) {
        if (!level.isClientSide && entity instanceof net.minecraft.world.entity.LivingEntity living
                && level.getBlockEntity(pos) instanceof CrucibleBlockEntity crucible) {
            crucible.touched(living);
        }
    }

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
        return level.isClientSide
                ? createTickerHelper(type, ModBlockEntities.CRUCIBLE.get(), CrucibleEffects::tick)
                : createTickerHelper(type, ModBlockEntities.CRUCIBLE.get(), CrucibleBlockEntity::tick);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(10) == 0 && boiling(level, pos)) {
            level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.LAVA_POP, SoundSource.BLOCKS,
                    0.1F + random.nextFloat() * 0.1F, 1.2F + random.nextFloat() * 0.2F, false);
        }
    }
}
