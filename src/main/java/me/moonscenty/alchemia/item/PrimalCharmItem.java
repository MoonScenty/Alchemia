package me.moonscenty.alchemia.item;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.player.PlayerKnowledge;
import me.moonscenty.alchemia.research.Clues;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class PrimalCharmItem extends Item {
    // original's odds per tick carried
    private static final int ONE_IN = 20000;

    public PrimalCharmItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity holder, int slot, boolean selected) {
        if (level.isClientSide || !(holder instanceof Player player) || level.random.nextInt(ONE_IN) != 0) {
            return;
        }
        PlayerKnowledge knowledge = PlayerKnowledge.of(player);
        if (knowledge.hasResearch(Alchemia.id("focus_primal")) || knowledge.hasResearch(Clues.FOCUS_PRIMAL)) {
            return;
        }
        player.sendSystemMessage(Component.translatable("message.alchemia.primal_charm")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        PlayerKnowledge.update(player, known -> known.withResearch(Clues.FOCUS_PRIMAL));
    }
}
