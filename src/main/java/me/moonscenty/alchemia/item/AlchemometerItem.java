package me.moonscenty.alchemia.item;

import me.moonscenty.alchemia.registry.ModSounds;
import me.moonscenty.alchemia.research.ScanTarget;
import me.moonscenty.alchemia.research.Scanning;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/**
 * Held up to a thing and kept there, until it gives up what it is made of.
 */
public class AlchemometerItem extends Item {
    /** How long the player has to hold it steady before the reading takes. */
    public static final int SCAN_TICKS = 30;

    public AlchemometerItem(Properties properties) {
        super(properties);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        // the original's chirp, every time it is raised to look at something
        if (!level.isClientSide) {
            level.playSound(null, player.blockPosition(), ModSounds.SCAN.get(), SoundSource.PLAYERS, 0.5F, 1.0F);
        }
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    /** The reading lands the moment the scan is complete, rather than when the player lets go. */
    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remaining) {
        if (level.isClientSide || !(entity instanceof Player player)) {
            return;
        }
        if (getUseDuration(stack, entity) - remaining != SCAN_TICKS) {
            return;
        }

        ScanTarget target = Scanning.lookingAt(player).orElse(null);
        if (target == null) {
            player.displayClientMessage(Component.translatable("scan.alchemia.nothing_there"), true);
        } else {
            boolean learned = !Scanning.scan(player, target).isEmpty();
            if (learned) {
                level.playSound(null, player.blockPosition(), ModSounds.LEARN.get(), SoundSource.PLAYERS, 0.3F,
                        1.0F + level.random.nextFloat() * 0.1F);
            }
        }
        player.stopUsingItem();
        player.getCooldowns().addCooldown(this, 10);
    }
}
