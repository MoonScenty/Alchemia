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

/**
 * A little glass bottle that holds eight of one essentia.
 * <p>
 * It is the only way to carry essentia about by hand. Everything else in the works is plumbed -- a jar sits where it
 * was put and a tube goes where it was laid -- so a phial is what you use when the thing you want it in is across
 * the room, or not built yet.
 * <p>
 * Eight at a time and nothing in between. A phial is either full or empty, which is why no amount is written on it:
 * the aspect is the whole of what it knows.
 */
public class PhialItem extends Item {
    /** What one holds, and so what it takes and what it gives. */
    public static final int DRAUGHT = 8;

    public PhialItem(Properties properties) {
        super(properties);
    }

    /** What is in it, if anything. */
    public static Optional<Holder<Aspect>> inside(ItemStack stack) {
        return Optional.ofNullable(stack.get(ModDataComponents.ESSENTIA.get()));
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

    /**
     * Fills from a vessel, or pours into one.
     * <p>
     * Which way round is decided by the phial rather than by the player: a full one pours and an empty one draws.
     * Nothing happens by halves -- a vessel with seven in it will not fill a phial, and one with seven places left
     * will not take one. Anything short of a whole draught is put back where it came from.
     */
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
            // a guess, only so that the arm swings: the server decides whether anything actually moves
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
        level.playSound(null, context.getClickedPos(), SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 0.6F, 1.0F);
        return InteractionResult.CONSUME;
    }

    /** The first thing in the vessel there is a whole draught of. */
    private static Optional<Holder<Aspect>> aFullDraughtIn(EssentiaHolder vessel) {
        return vessel.held().asMap().entrySet().stream()
                .filter(entry -> entry.getValue() >= DRAUGHT)
                .map(Map.Entry::getKey)
                .findFirst();
    }

    /**
     * Moves a whole draught one way or the other, and says whether it managed the lot.
     * <p>
     * Asking first would mean asking eight times and hoping nothing changed in between, and a vessel is allowed to
     * refuse for reasons of its own -- a braced jar gives nothing up however much is in it. So it is tried, and
     * anything short of a draught is handed straight back.
     */
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

    /** Hands back one bottle in place of the other, and finds the spare a home. */
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
