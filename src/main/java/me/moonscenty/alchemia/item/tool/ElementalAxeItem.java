package me.moonscenty.alchemia.item.tool;

import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * The axe of the stream: an alchemium axe with water bound into it.
 *
 * <p>Water is what moves things along, and what the axe does is move things along. It comes with two workings
 * built in -- a tree comes down from its top rather than its foot, and what it drops comes to whoever felled it --
 * and holding the use button draws every loose thing nearby in as well, felled or not.
 */
public class ElementalAxeItem extends AxeItem {
    /** How far it reaches for loose things, how hard it pulls, and the most any one thing is pulled in a tick. */
    private static final double REACHES = 10.0;
    private static final double PULLS = 0.3;
    private static final double LIFTS = 0.1;
    private static final double AT_MOST = 0.25;

    public ElementalAxeItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity holder) {
        return ElementalTools.HELD_FOREVER;
    }

    /**
     * Everything loose within ten blocks drifts towards whoever is holding the button down.
     * <p>
     * A little at a time and never faster than a quarter of a block a tick, so it arrives rather than hits, and
     * a little upward as well so it comes over the edge of a step rather than piling against it.
     */
    @Override
    public void onUseTick(Level level, LivingEntity holder, ItemStack stack, int left) {
        Vec3 middle = holder.position().add(0.0, holder.getBbHeight() / 2.0, 0.0);
        for (ItemEntity loose : level.getEntitiesOfClass(ItemEntity.class,
                new AABB(middle, middle).inflate(REACHES), ItemEntity::isAlive)) {
            Vec3 away = loose.position().subtract(middle);
            double far = away.length();
            if (far < 1.0E-4) {
                continue;
            }
            Vec3 pull = away.scale(-PULLS / far);
            Vec3 now = loose.getDeltaMovement().add(pull.x, pull.y + LIFTS, pull.z);
            loose.setDeltaMovement(Mth.clamp(now.x, -AT_MOST, AT_MOST), Mth.clamp(now.y, -AT_MOST, AT_MOST),
                    Mth.clamp(now.z, -AT_MOST, AT_MOST));
        }
    }
}
