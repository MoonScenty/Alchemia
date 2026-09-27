package me.moonscenty.alchemia.item;

import java.util.List;

import me.moonscenty.alchemia.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * A slip of paper that says what belongs in a jar.
 * <p>
 * Sticking one on is what turns a jar from a pot into a place: a labelled jar takes the one thing it is labelled
 * with and refuses everything else, empty or not, so a wall of them can be piped from a single run without the
 * first jar in the row swallowing whatever comes past.
 * <p>
 * A blank one takes its word from whatever the jar already holds. One written on beforehand -- by holding it up to
 * a phial at the bench -- can be stuck on an empty jar, which is how a place is set aside for something you have
 * not distilled yet.
 * <p>
 * It carries no essentia itself, only the name of one. The aspect on it is a word, not a measure, which is why it
 * is an ordinary item rather than something the pipes will talk to.
 */
public class JarLabelItem extends Item {
    public JarLabelItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        return PhialItem.inside(stack)
                .map(aspect -> (Component) Component.translatable(getDescriptionId() + ".written",
                        aspect.value().displayName()))
                .orElseGet(() -> super.getName(stack));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        if (stack.has(ModDataComponents.ESSENTIA.get())) {
            return;
        }
        lines.add(Component.translatable("item.alchemia.jar_label.blank").withStyle(ChatFormatting.GRAY));
    }
}
