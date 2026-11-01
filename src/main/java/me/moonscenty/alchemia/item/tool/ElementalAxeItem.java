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

public class ElementalAxeItem extends AxeItem {
    // pull radius and strength, upward lift so items clear steps, and per-tick speed cap
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
