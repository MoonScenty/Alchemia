package me.moonscenty.alchemia.item;

import java.util.List;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.block.entity.JarBlockEntity;
import me.moonscenty.alchemia.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import me.moonscenty.alchemia.block.entity.AlembicBlockEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class JarBlockItem extends BlockItem implements me.moonscenty.alchemia.aspect.ContainedAspects {
    public JarBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public AspectList contained(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.CONTENTS.get(), AspectList.EMPTY);
    }

    // original: a jar held against an alembic scoops it out
    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null || !(level.getBlockEntity(context.getClickedPos()) instanceof AlembicBlockEntity alembic)
                || alembic.amount() <= 0) {
            return InteractionResult.PASS;
        }
        Holder<Aspect> kind = alembic.holding().orElseThrow();
        Holder<Aspect> label = stack.get(ModDataComponents.ESSENTIA.get());
        AspectList inside = stack.getOrDefault(ModDataComponents.CONTENTS.get(), AspectList.EMPTY);
        int taken = Math.min(alembic.amount(), JarBlockEntity.CAPACITY - inside.total());
        if (label != null && label.value() != kind.value() || !inside.isEmpty() && inside.get(kind) <= 0 || taken <= 0) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        alembic.draw(taken);
        ItemStack filled = stack.copyWithCount(1);
        filled.set(ModDataComponents.CONTENTS.get(), AspectList.of(kind, inside.total() + taken));
        if (stack.getCount() > 1) {
            stack.shrink(1);
            if (!player.getInventory().add(filled)) {
                player.drop(filled, false);
            }
        } else {
            player.setItemInHand(context.getHand(), filled);
        }
        level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_SWIM, SoundSource.PLAYERS, 0.25F, 1.0F);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        AspectList inside = stack.get(ModDataComponents.CONTENTS.get());
        if (inside != null && !inside.isEmpty()) {
            Holder<Aspect> holding = inside.sortedByAmount().getFirst();
            lines.add(Component.translatable("block.alchemia.jar.holding", holding.value().displayName(),
                    inside.get(holding), JarBlockEntity.CAPACITY).withStyle(ChatFormatting.GRAY));
        }
        Holder<Aspect> label = stack.get(ModDataComponents.ESSENTIA.get());
        if (label != null) {
            lines.add(Component.translatable("item.alchemia.jar.labelled", label.value().displayName())
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
