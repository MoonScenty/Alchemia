package me.moonscenty.alchemia.item;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/** A scrap of somebody's learning. Read, it is four to eight points of experience. */
public class KnowledgeFragmentItem extends Item {
    /** What one is worth at least, and at most besides. The original's. */
    private static final int LEAST = 4;
    private static final int MORE = 5;

    public KnowledgeFragmentItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack fragment = player.getItemInHand(hand);
        if (level instanceof ServerLevel served) {
            fragment.consume(1, player);
            ExperienceOrb.award(served, player.position(), LEAST + level.random.nextInt(MORE));
        }
        return InteractionResultHolder.sidedSuccess(fragment, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("item.alchemia.knowledge_fragment.help").withStyle(ChatFormatting.GOLD));
    }
}
