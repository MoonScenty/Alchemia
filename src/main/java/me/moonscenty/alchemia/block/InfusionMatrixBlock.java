package me.moonscenty.alchemia.block;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.block.entity.InfusionMatrixBlockEntity;
import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.registry.ModSounds;
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

public class InfusionMatrixBlock extends BaseEntityBlock {
    public static final MapCodec<InfusionMatrixBlock> CODEC = simpleCodec(InfusionMatrixBlock::new);

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

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new InfusionMatrixBlockEntity(pos, state);
    }

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
        switch (matrix.wake(player, stack)) {
            case WOKEN -> {
                say(player, "block.alchemia.infusion_matrix.woken");
                level.playSound(null, pos, ModSounds.WAND.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                level.playSound(null, pos, ModSounds.POOF.get(), SoundSource.BLOCKS, 0.4F,
                        1.0F + (float) level.random.nextGaussian() * 0.05F);
            }
            case UNBUILT -> {
                say(player, "block.alchemia.infusion_matrix.unbuilt");
                level.playSound(null, pos, SoundEvents.DISPENSER_FAIL, SoundSource.BLOCKS, 0.6F, 1.0F);
            }
            case UNPAID -> level.playSound(null, pos, SoundEvents.DISPENSER_FAIL, SoundSource.BLOCKS, 0.6F, 1.0F);
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
            int leaning = matrix.symmetry(level);
            player.displayClientMessage((leaning > 0
                    ? Component.translatable("block.alchemia.infusion_matrix.lopsided", leaning)
                    : leaning < 0
                            ? Component.translatable("block.alchemia.infusion_matrix.steadied", -leaning)
                            : Component.translatable("block.alchemia.infusion_matrix.steady"))
                    .withStyle(ChatFormatting.GRAY), true);
        } else if (matrix.owed().isEmpty()) {
            say(player, "block.alchemia.infusion_matrix.gathering");
        } else {
            player.displayClientMessage(Component.translatable("block.alchemia.infusion_matrix.owed",
                    owing(matrix)).withStyle(ChatFormatting.GRAY), true);
        }
        return InteractionResult.CONSUME;
    }

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

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock())
                && level.getBlockEntity(pos) instanceof InfusionMatrixBlockEntity matrix && matrix.awake()) {
            matrix.collapse(level);
        }
        super.onRemove(state, level, pos, replacement, moving);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return level.isClientSide
                ? createTickerHelper(type, me.moonscenty.alchemia.registry.ModBlockEntities.INFUSION_MATRIX.get(),
                        InfusionMatrixBlockEntity::clientTick)
                : createTickerHelper(type, me.moonscenty.alchemia.registry.ModBlockEntities.INFUSION_MATRIX.get(),
                        InfusionMatrixBlockEntity::tick);
    }
}
