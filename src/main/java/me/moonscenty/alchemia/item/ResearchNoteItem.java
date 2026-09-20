package me.moonscenty.alchemia.item;

import java.util.List;

import me.moonscenty.alchemia.registry.ModDataComponents;
import me.moonscenty.alchemia.research.ResearchEntry;
import me.moonscenty.alchemia.research.ResearchNote;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

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
