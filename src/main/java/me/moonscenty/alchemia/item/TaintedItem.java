package me.moonscenty.alchemia.item;

import me.moonscenty.alchemia.player.effect.ModEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class TaintedItem extends Item {
    // original's values: the bigger the stack, the likelier
    private static final int ONE_IN = 4321;
    private static final int LASTS = 120;

    public TaintedItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity holder, int slot, boolean selected) {
        super.inventoryTick(stack, level, holder, slot, selected);
        if (level.isClientSide || !(holder instanceof LivingEntity living) || living.isInvertedHealAndHarm()
                || living.hasEffect(ModEffects.TAINT_POISON) || level.random.nextInt(ONE_IN) > stack.getCount()) {
            return;
        }
        living.addEffect(new MobEffectInstance(ModEffects.TAINT_POISON, LASTS, 0, false, true));
        if (living instanceof Player player) {
            player.sendSystemMessage(Component.translatable("message.alchemia.taint_item_poison",
                    stack.getHoverName().copy().withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC)));
            stack.shrink(1);
        }
    }
}
