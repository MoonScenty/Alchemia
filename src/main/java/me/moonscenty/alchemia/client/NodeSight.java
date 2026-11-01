package me.moonscenty.alchemia.client;

import me.moonscenty.alchemia.aura.node.AuraNode;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.item.FortressArmorItem;
import me.moonscenty.alchemia.item.HelmFitting;
import me.moonscenty.alchemia.registry.ModTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class NodeSight {
    private static final double LENS_RANGE = 8000.0;
    private static final double INSTRUMENT_RANGE = 300.0;

    private static final double INSTRUMENT_AIM = 0.8;
    private static final double INSTRUMENT_SWEEP = 16.0;

    private static final double FADE_FROM = 0.9;

    private NodeSight() {
    }

    public static float clarity(Entity viewer, AuraNode node) {
        double range = rangeFor(viewer, node);
        if (range <= 0.0) {
            return 0.0F;
        }
        double away = node.distanceToSqr(viewer);
        if (away > range) {
            return 0.0F;
        }
        return (float) (1.0 - Math.min(1.0, away / (range * FADE_FROM)));
    }

    private static double rangeFor(Entity viewer, AuraNode node) {
        if (!(viewer instanceof Player player)) {
            return 0.0;
        }
        if (wearsLenses(player)) {
            return LENS_RANGE;
        }
        return holdingAlchemometer(player) && lookingAt(player, node) ? INSTRUMENT_RANGE : 0.0;
    }

    static boolean wearsLenses(Player player) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (reveals(player.getItemBySlot(slot))) {
                return true;
            }
        }
        return false;
    }

    private static boolean reveals(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        // a tag can't cover the helm: it depends on what was worked into it
        return stack.is(ModTags.Items.REVEALS)
                || FortressArmorItem.fitting(stack).map(HelmFitting::reveals).orElse(false);
    }

    static boolean holdingAlchemometer(Player player) {
        return player.getMainHandItem().is(ModItems.ALCHEMOMETER.get())
                || player.getOffhandItem().is(ModItems.ALCHEMOMETER.get());
    }

    private static boolean lookingAt(LivingEntity viewer, AuraNode node) {
        Vec3 towards = node.position().subtract(viewer.getEyePosition());
        double away = towards.length();
        if (away > INSTRUMENT_SWEEP) {
            return false;
        }
        return away < 1.0E-4 || viewer.getLookAngle().dot(towards.scale(1.0 / away)) > INSTRUMENT_AIM;
    }
}
