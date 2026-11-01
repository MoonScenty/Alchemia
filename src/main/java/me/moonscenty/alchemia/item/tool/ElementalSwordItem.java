package me.moonscenty.alchemia.item.tool;

import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ElementalSwordItem extends SwordItem {
    // original's values
    private static final double REACHES = 2.5;
    private static final double THROWS = 2.5;
    private static final double LIFTS = 0.08;
    private static final double RISES_AT_MOST = 0.5;
    private static final double SETTLES_TO = 0.2;
    // falling speed and fall distance are divided by this each tick
    private static final double CUSHIONS = 1.2;
    // sound and durability interval, in ticks
    private static final int EVERY = 20;

    public ElementalSwordItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BLOCK;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity holder) {
        return ElementalTools.HELD_FOREVER;
    }

    // other players are not pushed, as in the original
    @Override
    public void onUseTick(Level level, LivingEntity holder, ItemStack stack, int left) {
        int held = getUseDuration(stack, holder) - left;
        Vec3 moving = holder.getDeltaMovement();
        double up = moving.y < 0.0 ? moving.y / CUSHIONS : moving.y;
        up += LIFTS;
        if (up > RISES_AT_MOST) {
            up = SETTLES_TO;
        }
        holder.setDeltaMovement(moving.x, up, moving.z);
        holder.fallDistance /= (float) CUSHIONS;
        holder.hurtMarked = true;

        for (LivingEntity near : level.getEntitiesOfClass(LivingEntity.class,
                holder.getBoundingBox().inflate(REACHES),
                other -> other != holder && !(other instanceof Player) && other.isAlive()
                        && other != holder.getVehicle())) {
            Vec3 away = near.position().subtract(holder.position());
            double far = away.length() + 0.1;
            near.setDeltaMovement(near.getDeltaMovement().add(away.scale(1.0 / THROWS / far)));
            near.hurtMarked = true;
        }

        if (level instanceof ServerLevel served) {
            served.sendParticles(ParticleTypes.CLOUD, holder.getX(), holder.getY() + 0.1, holder.getZ(),
                    2, 0.6, 0.1, 0.6, 0.02);
            if (held % EVERY == 0) {
                served.playSound(null, holder.getX(), holder.getY(), holder.getZ(), ModSounds.WIND.get(),
                        SoundSource.PLAYERS, 0.5F, 0.9F + served.random.nextFloat() * 0.2F);
                stack.hurtAndBreak(1, holder, LivingEntity.getSlotForHand(holder.getUsedItemHand()));
            }
        }
    }
}
