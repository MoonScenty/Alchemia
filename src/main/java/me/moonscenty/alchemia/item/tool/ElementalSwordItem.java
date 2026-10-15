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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * The sword of the zephyr: an alchemium sword with air bound into it.
 *
 * <p>A blow carries on to whatever is standing beside what it struck, which is a working it comes with. Held up,
 * it raises a whirlwind round whoever is holding it: the air lifts them, slows their fall, and throws back
 * anything that comes too close. The wind wears the blade, a point a second.
 */
public class ElementalSwordItem extends SwordItem {
    /** How far the wind reaches, how hard it throws, and how hard it lifts. All the original's numbers. */
    private static final double REACHES = 2.5;
    private static final double THROWS = 2.5;
    private static final double LIFTS = 0.08;
    private static final double RISES_AT_MOST = 0.5;
    private static final double SETTLES_TO = 0.2;
    /** How much a fall is slowed each tick, as a share of itself. */
    private static final double CUSHIONS = 1.2;
    /** How often the wind is heard, and how often it costs the blade a point, in ticks. */
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

    /**
     * One tick of the whirlwind.
     * <p>
     * The holder is lifted: a fall is cushioned a fifth at a time and every tick adds a little rise, which settles
     * back if it gets going too fast. Anything alive within reach is pushed straight away from them, harder the
     * closer it is. Other players are left out of it, as the original left them.
     */
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
