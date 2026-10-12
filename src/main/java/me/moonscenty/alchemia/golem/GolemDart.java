package me.moonscenty.alchemia.golem;

import me.moonscenty.alchemia.registry.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * A dart from a golem's launchers: an arrow in all but size, made as it is fired and never picked up again. Stuck
 * in something, it is gone after a second.
 */
public class GolemDart extends AbstractArrow {
    private static final int STAYS = 20;

    public GolemDart(EntityType<? extends GolemDart> type, Level level) {
        super(type, level);
        pickup = Pickup.DISALLOWED;
    }

    public GolemDart(Level level, LivingEntity shooter) {
        super(ModEntities.GOLEM_DART.get(), shooter, level, new ItemStack(Items.ARROW), null);
        pickup = Pickup.DISALLOWED;
    }

    @Override
    public void tick() {
        super.tick();
        if (inGround && inGroundTime >= STAYS) {
            discard();
        }
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }
}
