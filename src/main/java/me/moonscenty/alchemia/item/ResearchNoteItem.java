package me.moonscenty.alchemia.item;

import java.util.List;

import me.moonscenty.alchemia.registry.ModDataComponents;
import me.moonscenty.alchemia.research.ResearchEntry;
import me.moonscenty.alchemia.research.ResearchNote;
import me.moonscenty.alchemia.player.ModAttachments;
import me.moonscenty.alchemia.player.PlayerKnowledge;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * A sheet with a research puzzle part-drawn on it.
 * <p>
 * A blank one is only paper; what makes it a note is the puzzle it carries, so the name and the description are read
 * off that rather than from the item itself.
 */
public class ResearchNoteItem extends Item {
    public ResearchNoteItem(Properties properties) {
        super(properties);
    }

    public static ResearchNote noteOn(ItemStack stack) {
        return stack.get(ModDataComponents.RESEARCH_NOTE.get());
    }

    /** Reading back a solved note is what actually teaches it; the sheet is used up in the doing. */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        ResearchNote note = noteOn(held);
        if (note == null || !note.complete()) {
            return InteractionResultHolder.pass(held);
        }
        if (!level.isClientSide && player instanceof ServerPlayer server) {
            PlayerKnowledge knowledge = PlayerKnowledge.of(server);
            if (knowledge.hasResearch(note.research())) {
                server.displayClientMessage(Component.translatable("note.alchemia.already_known"), true);
                return InteractionResultHolder.fail(held);
            }
            server.setData(ModAttachments.KNOWLEDGE, knowledge.withResearch(note.research()));
            server.displayClientMessage(Component.translatable("note.alchemia.learned",
                    ResearchEntry.displayName(note.research())), false);
            level.playSound(null, server.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.6F, 1.4F);
            held.shrink(1);
        }
        return InteractionResultHolder.sidedSuccess(held, level.isClientSide);
    }

    @Override
    public Component getName(ItemStack stack) {
        ResearchNote note = noteOn(stack);
        if (note == null) {
            return super.getName(stack);
        }
        return Component.translatable("item.alchemia.research_notes.on", ResearchEntry.displayName(note.research()));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        ResearchNote note = noteOn(stack);
        if (note == null) {
            return;
        }
        lines.add(Component.translatable(note.complete()
                ? "item.alchemia.research_notes.solved"
                : "item.alchemia.research_notes.unsolved")
                .withStyle(note.complete() ? ChatFormatting.GREEN : ChatFormatting.GRAY));
    }
}
