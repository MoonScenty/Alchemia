package me.moonscenty.alchemia.item;

import me.moonscenty.alchemia.registry.ModSounds;
import me.moonscenty.alchemia.research.ResearchCompletion;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class AlchemonomiconItem extends Item {
    // set by the client at startup; stays a no-op on the server
    public static Runnable opener = () -> {
    };

    public AlchemonomiconItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) {
            opener.run();
            level.playLocalSound(player.getX(), player.getY(), player.getZ(), ModSounds.PAGE.get(),
                    SoundSource.PLAYERS, 1.0F, 1.0F, false);
        } else {
            ResearchCompletion.collectSiblings(player);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }
}
