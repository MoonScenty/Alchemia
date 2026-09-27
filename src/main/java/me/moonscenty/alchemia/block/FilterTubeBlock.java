package me.moonscenty.alchemia.block;

import java.util.Map;
import java.util.Optional;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.block.entity.FilterTubeBlockEntity;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * A length of pipe that lets one essentia by and turns the rest back.
 * <p>
 * What it lets by is told to it with a vis crystal, since a crystal is that aspect made solid and there is one for
 * each of the six primals. Crouch and click to make it forget again. Two filters set to different things in one run
 * pass nothing at all, which is the honest answer rather than a special case.
 */
public class FilterTubeBlock extends TubeBlock {
    public static final MapCodec<FilterTubeBlock> CODEC = simpleCodec(FilterTubeBlock::new);

    public FilterTubeBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<FilterTubeBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FilterTubeBlockEntity(pos, state);
    }

    @Override
    public Optional<Holder<Aspect>> insistsOn(BlockGetter level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof FilterTubeBlockEntity filter ? filter.only() : Optional.empty();
    }

    /** Set with a crystal of the aspect it is to let by. */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        Optional<Holder<Aspect>> aspect = crystallised(stack);
        if (aspect.isEmpty() || !(level.getBlockEntity(pos) instanceof FilterTubeBlockEntity filter)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide) {
            filter.only(aspect.get());
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.5F, 1.2F);
            say(player, filter);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    /** Says what it is set to, and forgets it if you crouch. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof FilterTubeBlockEntity filter)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            if (player.isSecondaryUseActive()) {
                filter.only(null);
            }
            say(player, filter);
        }
        return level.isClientSide ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }

    private static void say(Player player, FilterTubeBlockEntity filter) {
        player.displayClientMessage(filter.only()
                .map(aspect -> Component.translatable("block.alchemia.tube_filter.only", aspect.value().displayName()))
                .orElse(Component.translatable("block.alchemia.tube_filter.open"))
                .copy().withStyle(ChatFormatting.GRAY), true);
    }

    /** The aspect a vis crystal is made of, if the held thing is one. */
    private static Optional<Holder<Aspect>> crystallised(ItemStack stack) {
        for (Map.Entry<CrystalType, DeferredItem<Item>> entry : ModItems.SHARDS.entrySet()) {
            if (stack.is(entry.getValue().get())) {
                return Optional.of(entry.getKey().aspect());
            }
        }
        return Optional.empty();
    }
}
