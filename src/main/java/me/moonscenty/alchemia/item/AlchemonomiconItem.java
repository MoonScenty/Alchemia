package me.moonscenty.alchemia.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * The book of everything worked out so far. Opening it is a client matter, so the client hands in what to do on
 * startup and a server is left with nothing to run.
 */
public class AlchemonomiconItem extends Item {
    /** Set by the client when it starts. Stays a no-op on a server, which has no screens to open. */
    public static Runnable opener = () -> {
    };

    public AlchemonomiconItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) {
            opener.run();
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }
}
