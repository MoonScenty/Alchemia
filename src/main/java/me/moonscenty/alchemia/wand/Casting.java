package me.moonscenty.alchemia.wand;

import java.util.function.Predicate;

import me.moonscenty.alchemia.item.FocusItem;
import me.moonscenty.alchemia.item.VisHolder;
import me.moonscenty.alchemia.item.WandItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

// price is checked before the spell and taken only if it did something; original's order
public final class Casting {
    // original's value
    public static final double REACH = 20.0;

    // focus cooldowns are in milliseconds
    private static final int A_SECOND = 1000;
    private static final int TICKS_A_SECOND = 20;

    private Casting() {
    }

    public static FocusItem fittedTo(ItemStack wand) {
        return WandItem.focus(wand).getItem() instanceof FocusItem focus ? focus : null;
    }

    public static Focus fitted(ItemStack wand) {
        ItemStack focus = WandItem.focus(wand);
        return focus.getItem() instanceof FocusItem item ? item.focus(focus) : null;
    }

    public static int worked(ItemStack wand, FocusUpgrade upgrade) {
        ItemStack focus = WandItem.focus(wand);
        int level = FocusUpgrades.level(focus, upgrade);
        if (upgrade == FocusUpgrade.POTENCY && !focus.isEmpty() && wand.getItem() instanceof WandItem
                && WandItem.rod(wand).potent()) {
            level++;
        }
        return level;
    }

    public static boolean has(ItemStack wand, FocusUpgrade upgrade) {
        return worked(wand, upgrade) > 0;
    }

    public static boolean cast(ServerLevel level, Player caster, ItemStack wand, int heldFor) {
        Focus focus = fitted(wand);
        if (focus == null || caster.getCooldowns().isOnCooldown(wand.getItem())) {
            return false;
        }
        if (!VisHolder.canPay(wand, focus.cost(), caster, VisHolder.Spending.WORKING)) {
            return false;
        }
        if (!focus.spell().cast(level, caster, wand, aim(caster), heldFor)) {
            return false;
        }

        VisHolder holder = VisHolder.of(wand);
        if (holder != null) {
            holder.take(wand, focus.cost(), caster, VisHolder.Spending.WORKING);
        }
        waitAfter(caster, wand, focus);
        return true;
    }

    private static void waitAfter(Player caster, ItemStack wand, Focus focus) {
        if (focus.cooldown() <= 0) {
            return;
        }
        int ticks = Math.max(1, focus.cooldown() * TICKS_A_SECOND / A_SECOND);
        caster.getCooldowns().addCooldown(wand.getItem(), ticks);
    }

    public static HitResult aim(LivingEntity caster) {
        return aim(caster, REACH, 1.0, Casting::catchable);
    }

    // slack: how far either side of the line of sight an entity is still caught
    public static HitResult aim(LivingEntity caster, double reach, double slack, Predicate<Entity> filter) {
        Vec3 eye = caster.getEyePosition();
        Vec3 along = caster.getViewVector(1.0F).scale(reach);
        Vec3 far = eye.add(along);

        BlockHitResult blocks = caster.level().clip(new ClipContext(eye, far,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
        Vec3 stop = blocks.getType() == HitResult.Type.MISS ? far : blocks.getLocation();

        AABB swept = caster.getBoundingBox().expandTowards(along).inflate(slack);
        EntityHitResult struck = ProjectileUtil.getEntityHitResult(caster.level(), caster, eye, stop, swept, filter);
        return struck != null ? struck : blocks;
    }

    private static boolean catchable(Entity entity) {
        return !entity.isSpectator() && entity.isAlive() && entity.isPickable();
    }

    public static ServerLevel served(Level level) {
        return level instanceof ServerLevel server ? server : null;
    }
}
