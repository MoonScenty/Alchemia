package me.moonscenty.alchemia.block;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.block.entity.InfusionMatrixBlockEntity;
import me.moonscenty.alchemia.item.WandItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The runic matrix: eight stones turning in the air with nothing holding them up.
 * <p>
 * There is no model. What you see is drawn a frame at a time by the renderer, because the whole of it is movement
 * -- it turns, it leans, and it shakes itself apart as the work goes wrong. A still picture of it would be a lie
 * about what the thing is.
 * <p>
 * It is worked by pointing a wand at it. Everything else about it -- what is being made, what is still owed, how
 * badly it is going -- lives in the block entity.
 */
public class InfusionMatrixBlock extends BaseEntityBlock {
    public static final MapCodec<InfusionMatrixBlock> CODEC = simpleCodec(InfusionMatrixBlock::new);

    /** What you can hit: the space the stones turn in, which is nearly the whole block. */
    private static final VoxelShape SHAPE = Block.box(1, 1, 1, 15, 15, 15);

    public InfusionMatrixBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<InfusionMatrixBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /** Nothing at all is drawn by the block itself. */
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new InfusionMatrixBlockEntity(pos, state);
    }

    /**
     * Pointing a wand at it sets it going.
     * <p>
     * Nothing is taken out of the wand for it. What an infusion costs is the essentia in the jars and the things
     * on the pedestals, and charging for the match as well would only be a toll on remembering to bring the wand.
     */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(stack.getItem() instanceof WandItem)
                || !(level.getBlockEntity(pos) instanceof InfusionMatrixBlockEntity matrix)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        switch (matrix.wake(player)) {
            case WOKEN -> {
                say(player, "block.alchemia.infusion_matrix.woken");
                level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 0.8F, 1.0F);
            }
            case UNBUILT -> {
                say(player, "block.alchemia.infusion_matrix.unbuilt");
                level.playSound(null, pos, SoundEvents.DISPENSER_FAIL, SoundSource.BLOCKS, 0.6F, 1.0F);
            }
            case BUSY -> say(player, "block.alchemia.infusion_matrix.busy");
            case NOTHING -> {
                say(player, "block.alchemia.infusion_matrix.nothing");
                level.playSound(null, pos, SoundEvents.DISPENSER_FAIL, SoundSource.BLOCKS, 0.6F, 1.0F);
            }
            default -> {
            }
        }
        return ItemInteractionResult.CONSUME;
    }

    /** Says what the work is still waiting on, which is nearly always a jar that has run dry. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof InfusionMatrixBlockEntity matrix)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!matrix.awake()) {
            say(player, "block.alchemia.infusion_matrix.asleep");
        } else if (!matrix.busy()) {
            // said before the working rather than during it, since this is the one thing you can still fix
            int lopsided = matrix.symmetry(level);
            player.displayClientMessage((lopsided == 0
                    ? Component.translatable("block.alchemia.infusion_matrix.steady")
                    : Component.translatable("block.alchemia.infusion_matrix.lopsided", lopsided))
                    .withStyle(ChatFormatting.GRAY), true);
        } else if (matrix.owed().isEmpty()) {
            say(player, "block.alchemia.infusion_matrix.gathering");
        } else {
            player.displayClientMessage(Component.translatable("block.alchemia.infusion_matrix.owed",
                    owing(matrix)).withStyle(ChatFormatting.GRAY), true);
        }
        return InteractionResult.CONSUME;
    }

    /** What is still to be drunk, most of it first. */
    private static MutableComponent owing(InfusionMatrixBlockEntity matrix) {
        MutableComponent out = Component.empty();
        boolean first = true;
        for (Holder<Aspect> aspect : matrix.owed().sortedByAmount()) {
            if (!first) {
                out.append(", ");
            }
            out.append(aspect.value().displayName()).append(" " + matrix.owed().get(aspect));
            first = false;
        }
        return out;
    }

    private static void say(Player player, String key) {
        player.displayClientMessage(Component.translatable(key).withStyle(ChatFormatting.GRAY), true);
    }

    /** Taking the matrix away takes the altar with it: the corners are arcane stone again. */
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock())
                && level.getBlockEntity(pos) instanceof InfusionMatrixBlockEntity matrix && matrix.awake()) {
            matrix.sleep(level);
        }
        super.onRemove(state, level, pos, replacement, moving);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return level.isClientSide ? null
                : createTickerHelper(type, me.moonscenty.alchemia.registry.ModBlockEntities.INFUSION_MATRIX.get(),
                        InfusionMatrixBlockEntity::tick);
    }
}
