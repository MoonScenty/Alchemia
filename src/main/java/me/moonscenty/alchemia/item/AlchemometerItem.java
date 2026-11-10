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

public class AlchemometerItem extends Item {
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

    // original's order of what can be picked out; nothing first, meaning all of them
    public static java.util.List<net.minecraft.core.Holder<me.moonscenty.alchemia.aspect.Aspect>> auraAspects() {
        return java.util.List.of(me.moonscenty.alchemia.registry.ModAspects.AIR,
                me.moonscenty.alchemia.registry.ModAspects.FIRE, me.moonscenty.alchemia.registry.ModAspects.WATER,
                me.moonscenty.alchemia.registry.ModAspects.EARTH, me.moonscenty.alchemia.registry.ModAspects.ORDER,
                me.moonscenty.alchemia.registry.ModAspects.ENTROPY, me.moonscenty.alchemia.registry.ModAspects.FLUX);
    }

    public static int shown(ItemStack stack) {
        return stack.getOrDefault(me.moonscenty.alchemia.registry.ModDataComponents.AURA_SHOWN.get(), 0);
    }

    public static void cycle(net.minecraft.server.level.ServerPlayer player, ItemStack stack) {
        int next = (shown(stack) + 1) % (auraAspects().size() + 1);
        stack.set(me.moonscenty.alchemia.registry.ModDataComponents.AURA_SHOWN.get(), next);
        player.sendSystemMessage(next > 0
                ? Component.translatable("message.alchemia.alchemometer.one",
                        auraAspects().get(next - 1).value().displayName())
                : Component.translatable("message.alchemia.alchemometer.all"));
        player.level().playSound(null, player.blockPosition(), ModSounds.KEY.get(), SoundSource.PLAYERS, 0.5F, 1.0F);
    }

    // original: held, it reads the chunk once a second
    @Override
    public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity holder, int slot,
            boolean selected) {
        if (!selected || level.isClientSide || holder.tickCount % 20 != 0
                || !(holder instanceof net.minecraft.server.level.ServerPlayer player)) {
            return;
        }
        var aura = me.moonscenty.alchemia.aura.AuraHandler.at(level, player.blockPosition());
        java.util.List<Integer> amounts = auraAspects().stream().map(aura::get).toList();
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player,
                new me.moonscenty.alchemia.network.AuraReading(aura.base(), shown(stack), amounts));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        if (!level.isClientSide) {
            level.playSound(null, player.blockPosition(), ModSounds.SCAN.get(), SoundSource.PLAYERS, 0.5F, 1.0F);
        }
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

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
