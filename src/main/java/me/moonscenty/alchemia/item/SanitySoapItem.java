package me.moonscenty.alchemia.item;

import me.moonscenty.alchemia.player.WarpData;
import me.moonscenty.alchemia.player.WarpHandler;
import me.moonscenty.alchemia.player.effect.ModEffects;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModSounds;
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
 * Sanitizing soap, the original's {@code ItemSanitySoap}: held up and scrubbed with for ten seconds, it washes all
 * temporary warp away and, one time in three, a point of the warp that sticks. Warp ward makes that a good deal
 * likelier, and so does standing in purifying fluid while you do it. Put it down early and nothing comes of it.
 * What has been written into you for good it does not touch.
 */
public class SanitySoapItem extends Item {
    /** How long a wash is held for, and how far into it it counts as done. */
    public static final int WASH = 200;
    public static final int DONE = 195;
    private static final float CHANCE = 0.33F;
    private static final float HELP = 0.25F;

    public SanitySoapItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return WASH;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BLOCK;
    }

    /** Lathering: a rustle now and then and a froth about whoever is washing, and done once it has been long enough. */
    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remaining) {
        if (WASH - remaining > DONE) {
            user.releaseUsingItem();
            return;
        }
        if (level.isClientSide) {
            if (level.random.nextFloat() < 0.2F) {
                level.playLocalSound(user.getX(), user.getY(), user.getZ(), ModSounds.ROOTS.get(),
                        SoundSource.PLAYERS, 0.1F, 1.5F + level.random.nextFloat() * 0.2F, false);
            }
            me.moonscenty.alchemia.client.SoapEffects.lather(level, user, 5, 0.5, 0xFFCCE5);
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity user, int remaining) {
        if (WASH - remaining <= DONE) {
            return;
        }
        stack.consume(1, user);
        if (level.isClientSide) {
            level.playLocalSound(user.getX(), user.getY(), user.getZ(), ModSounds.CRAFT_START.get(),
                    SoundSource.PLAYERS, 0.25F, 1.0F, false);
            me.moonscenty.alchemia.client.SoapEffects.lather(level, user, 20, 1.5, 0xFFB2E5);
            return;
        }
        if (user instanceof Player player) {
            wash(player, level.random.nextFloat());
        }
    }

    /** What a finished wash does, given the roll it was made with. */
    public static void wash(Player player, float roll) {
        float chance = CHANCE;
        if (player.hasEffect(ModEffects.WARP_WARD)) {
            chance += HELP;
        }
        if (player.level().getBlockState(player.blockPosition()).is(ModBlocks.PURIFYING_FLUID.get())) {
            chance += HELP;
        }
        if (roll < chance && WarpData.of(player).of(WarpData.Kind.STICKY) > 0) {
            WarpHandler.add(player, WarpData.Kind.STICKY, -1);
        }
        int temporary = WarpData.of(player).of(WarpData.Kind.TEMPORARY);
        if (temporary > 0) {
            WarpHandler.add(player, WarpData.Kind.TEMPORARY, -temporary);
        }
    }
}
