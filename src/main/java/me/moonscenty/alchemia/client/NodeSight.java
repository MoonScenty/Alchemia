package me.moonscenty.alchemia.client;

import me.moonscenty.alchemia.aura.node.AuraNode;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Whether a node can be seen at all, and how plainly.
 * <p>
 * A node is not part of the ordinary world. It is there for anyone carrying something that reads the aura and for
 * nobody else, which is what makes hunting one an errand rather than a walk. An alchemometer will pick one out at
 * arm's length, and only while it is being pointed at; proper lenses show them across a valley without being asked.
 */
public final class NodeSight {
    /** How far lenses reach, and how far an alchemometer does, as squared distances. */
    private static final double LENS_RANGE = 8000.0;
    private static final double INSTRUMENT_RANGE = 300.0;

    /** An alchemometer only reads what it is held up to, so a node behind the player stays hidden. */
    private static final double INSTRUMENT_AIM = 0.8;
    private static final double INSTRUMENT_SWEEP = 16.0;

    /** The last stretch of the range is where a node fades rather than blinking out. */
    private static final double FADE_FROM = 0.9;

    private NodeSight() {
    }

    /**
     * How plainly a node shows to a viewer, from nothing at all to full.
     *
     * @return 0 when the node cannot be seen, otherwise the alpha to draw it at
     */
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

    /** How far the viewer can see nodes, squared, or zero if they are carrying nothing that shows them. */
    private static double rangeFor(Entity viewer, AuraNode node) {
        if (!(viewer instanceof Player player)) {
            return 0.0;
        }
        if (wearsLenses(player)) {
            return LENS_RANGE;
        }
        return holdingInstrument(player) && lookingAt(player, node) ? INSTRUMENT_RANGE : 0.0;
    }

    /** Anything in the tag shows nodes just by being worn or carried. Nothing is in it until the goggles exist. */
    private static boolean wearsLenses(Player player) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (reveals(player.getItemBySlot(slot))) {
                return true;
            }
        }
        return false;
    }

    private static boolean reveals(ItemStack stack) {
        return !stack.isEmpty() && stack.is(ModTags.Items.REVEALS);
    }

    private static boolean holdingInstrument(Player player) {
        return player.getMainHandItem().is(ModItems.ALCHEMOMETER.get())
                || player.getOffhandItem().is(ModItems.ALCHEMOMETER.get());
    }

    /** Roughly pointed at it and near enough to read: the instrument is a lens, not a map. */
    private static boolean lookingAt(LivingEntity viewer, AuraNode node) {
        Vec3 towards = node.position().subtract(viewer.getEyePosition());
        double away = towards.length();
        if (away > INSTRUMENT_SWEEP) {
            return false;
        }
        // a node sitting on the player is pointed at by definition, and normalising it would divide by nothing
        return away < 1.0E-4 || viewer.getLookAngle().dot(towards.scale(1.0 / away)) > INSTRUMENT_AIM;
    }
}
