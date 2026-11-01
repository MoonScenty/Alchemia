package me.moonscenty.alchemia.block;

import java.util.Map;
import java.util.Optional;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.block.entity.FilterTubeBlockEntity;
import me.moonscenty.alchemia.item.PhialItem;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
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

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        Optional<Holder<Aspect>> aspect = PhialItem.inside(stack).or(() -> crystallised(stack));
        if (aspect.isEmpty() || !(level.getBlockEntity(pos) instanceof FilterTubeBlockEntity filter)) {
            return super.useItemOn(stack, state, level, pos, player, hand, hit);
        }
        if (!level.isClientSide) {
            filter.only(aspect.get());
            level.playSound(null, pos, ModSounds.PAGE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            say(player, filter);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof FilterTubeBlockEntity filter)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            if (player.isSecondaryUseActive()) {
                if (filter.only().isPresent()) {
                    level.playSound(null, pos, ModSounds.PAGE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                }
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

    private static Optional<Holder<Aspect>> crystallised(ItemStack stack) {
        for (Map.Entry<CrystalType, DeferredItem<Item>> entry : ModItems.SHARDS.entrySet()) {
            if (stack.is(entry.getValue().get())) {
                return Optional.of(entry.getKey().aspect());
            }
        }
        return Optional.empty();
    }
}
