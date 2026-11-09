package me.moonscenty.alchemia.item;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.essentia.EssentiaHolder;
import me.moonscenty.alchemia.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

// a phial is always either a full DRAUGHT or empty
public class PhialItem extends Item implements me.moonscenty.alchemia.aspect.ContainedAspects {
    public static final int DRAUGHT = 8;

    public PhialItem(Properties properties) {
        super(properties);
    }

    public static Optional<Holder<Aspect>> inside(ItemStack stack) {
        return Optional.ofNullable(stack.get(ModDataComponents.ESSENTIA.get()));
    }

    @Override
    public me.moonscenty.alchemia.aspect.AspectList contained(ItemStack stack) {
        return inside(stack).map(aspect -> me.moonscenty.alchemia.aspect.AspectList.of(aspect, DRAUGHT))
                .orElse(me.moonscenty.alchemia.aspect.AspectList.EMPTY);
    }

    public static ItemStack filled(ItemStack from, Holder<Aspect> aspect) {
        ItemStack full = from.copyWithCount(1);
        full.set(ModDataComponents.ESSENTIA.get(), aspect);
        return full;
    }

    public static ItemStack emptied(ItemStack from) {
        ItemStack empty = from.copyWithCount(1);
        empty.remove(ModDataComponents.ESSENTIA.get());
        return empty;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null || !(level.getBlockEntity(context.getClickedPos()) instanceof EssentiaHolder vessel)) {
            return InteractionResult.PASS;
        }
        ItemStack held = context.getItemInHand();
        Optional<Holder<Aspect>> carrying = inside(held);

        if (level.isClientSide) {
            // client-side guess so the arm swings; the server decides
            return carrying.map(vessel::wants).orElseGet(() -> aFullDraughtIn(vessel).isPresent())
                    ? InteractionResult.SUCCESS
                    : InteractionResult.PASS;
        }

        ItemStack back;
        if (carrying.isPresent()) {
            if (!moved(vessel, carrying.get(), true)) {
                return InteractionResult.PASS;
            }
            back = emptied(held);
        } else {
            Optional<Holder<Aspect>> drawn = aFullDraughtIn(vessel);
            if (drawn.isEmpty() || !moved(vessel, drawn.get(), false)) {
                return InteractionResult.PASS;
            }
            back = filled(held, drawn.get());
        }
        swap(player, context.getHand(), held, back);
        level.playSound(null, context.getClickedPos(), SoundEvents.GENERIC_SWIM, SoundSource.BLOCKS, 0.25F, 1.0F);
        return InteractionResult.CONSUME;
    }

    private static Optional<Holder<Aspect>> aFullDraughtIn(EssentiaHolder vessel) {
        return vessel.held().asMap().entrySet().stream()
                .filter(entry -> entry.getValue() >= DRAUGHT)
                .map(Map.Entry::getKey)
                .findFirst();
    }

    // tried rather than checked first: vessels may refuse for their own reasons, so shortfalls are handed back
    private static boolean moved(EssentiaHolder vessel, Holder<Aspect> aspect, boolean pouring) {
        int done = 0;
        while (done < DRAUGHT && (pouring ? vessel.accept(aspect) : vessel.release(aspect))) {
            done++;
        }
        if (done == DRAUGHT) {
            return true;
        }
        for (int one = 0; one < done; one++) {
            if (pouring) {
                vessel.release(aspect);
            } else {
                vessel.accept(aspect);
            }
        }
        return false;
    }

    private static void swap(Player player, InteractionHand hand, ItemStack held, ItemStack back) {
        held.shrink(1);
        if (held.isEmpty()) {
            player.setItemInHand(hand, back);
        } else if (!player.getInventory().add(back)) {
            player.drop(back, false);
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        return inside(stack)
                .map(aspect -> (Component) Component.translatable(getDescriptionId() + ".filled",
                        aspect.value().displayName()))
                .orElseGet(() -> super.getName(stack));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        inside(stack).ifPresent(aspect -> lines.add(Component
                .translatable("item.alchemia.phial.holding", aspect.value().displayName(), DRAUGHT)
                .withStyle(ChatFormatting.GRAY)));
    }
}
