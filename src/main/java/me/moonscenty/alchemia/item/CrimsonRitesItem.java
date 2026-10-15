package me.moonscenty.alchemia.item;

import java.util.List;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.player.ModAttachments;
import me.moonscenty.alchemia.player.PlayerKnowledge;
import me.moonscenty.alchemia.player.WarpData;
import me.moonscenty.alchemia.player.WarpHandler;
import me.moonscenty.alchemia.research.ResearchEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * The Crimson Cult's book. Read, it teaches what little of the cult can be made out of it, and that leaves three
 * points of warp, two of them for good. It is not used up by reading.
 * <p>
 * The research it teaches is hidden: it is in nobody's book until the book has been read, and no table can work it
 * out. It teaches a second along with it, the minor eldritch lore, as the original did.
 */
public class CrimsonRitesItem extends Item {
    public static final ResourceLocation RESEARCH = Alchemia.id("crimson");
    public CrimsonRitesItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!level.isClientSide) {
            Player server = player;
            PlayerKnowledge knowledge = PlayerKnowledge.of(server);
            if (knowledge.hasResearch(RESEARCH)) {
                server.displayClientMessage(Component.translatable("note.alchemia.already_known"), true);
                return InteractionResultHolder.fail(held);
            }
            // its warp and the minor eldritch lore along with it come with the research
            me.moonscenty.alchemia.research.ResearchCompletion.complete(server, RESEARCH);
            level.playSound(null, server.blockPosition(), me.moonscenty.alchemia.registry.ModSounds.LEARN.get(),
                    SoundSource.PLAYERS, 0.75F, 1.0F);
        }
        return InteractionResultHolder.sidedSuccess(held, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("item.alchemia.crimson_rites.text.0").withStyle(ChatFormatting.DARK_PURPLE));
        lines.add(Component.translatable("item.alchemia.crimson_rites.text.1").withStyle(ChatFormatting.DARK_BLUE));
    }
}
