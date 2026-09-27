package me.moonscenty.alchemia.block;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.block.entity.ArcanePedestalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A stone stand that holds up one thing.
 * <p>
 * It is not a container and has no opinion about what is put on it. What matters is that whatever is on it can be
 * seen from across the room and reached by a matrix that wants it: a ring of these is what an infusion is laid out
 * on, and reading the ring at a glance is how you tell whether you have laid it out right.
 * <p>
 * One thing, never a stack. Three ingots on a pedestal would look exactly like one and make a liar of every count
 * taken of the ring afterwards.
 */
public class ArcanePedestalBlock extends BaseEntityBlock {
    public static final MapCodec<ArcanePedestalBlock> CODEC = simpleCodec(ArcanePedestalBlock::new);

    /** The foot, the stem and the plate on top, which is what the model is made of. */
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0, 0, 0, 16, 4, 16),
            Block.box(4, 4, 4, 12, 12, 12),
            Block.box(2, 12, 2, 14, 16, 14));

    public ArcanePedestalBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<ArcanePedestalBlock> codec() {
        return CODEC;
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
        return new ArcanePedestalBlockEntity(pos, state);
    }

    /** Sets one thing down, or takes back what is already there. */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof ArcanePedestalBlockEntity stand)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!stand.held().isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (stack.isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide) {
            stand.hold(stack.split(1));
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.7F, 1.0F);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    /** Takes back what is standing on it. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof ArcanePedestalBlockEntity stand) || stand.held().isEmpty()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        ItemStack standing = stand.held();
        stand.hold(ItemStack.EMPTY);
        if (!player.getInventory().add(standing)) {
            player.drop(standing, false);
        }
        level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.7F, 1.0F);
        return InteractionResult.CONSUME;
    }

    /** Whatever was standing on it falls off when it goes. */
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock())
                && level.getBlockEntity(pos) instanceof ArcanePedestalBlockEntity stand
                && !stand.held().isEmpty()) {
            popResource(level, pos, stand.held());
            stand.hold(ItemStack.EMPTY);
        }
        super.onRemove(state, level, pos, replacement, moving);
    }
}
