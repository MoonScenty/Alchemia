package me.moonscenty.alchemia.item;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.living.ArmorHurtEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/**
 * What fortress armour does beyond having a lot of armour on it.
 *
 * <h3>A suit is worth more than its pieces</h3>
 * Every piece of it worn turns a further share of whatever got past the plate, so the fourth piece is worth more
 * than the first. That is the original's idea and the reason anybody built the whole suit rather than the one
 * piece that stops most.
 *
 * <h3>What the blow is made of matters</h3>
 * Plate is for being hit with. It turns fire and blasts best of all, a blade next, and a working worst -- there
 * is nothing about a steel shell that answers a curse. The original leaned the same way.
 *
 * <h3>Where this differs from the original</h3>
 * The original replaced the game's own armour arithmetic outright: each piece claimed a share of the blow and
 * the game's armour points were only a number on a bar. There is no longer any way to say that. So the armour
 * points do their ordinary work here and this is laid on top of what they leave, which means the numbers are
 * not the original's and cannot be -- they are chosen so that a whole suit is about as good as the original's
 * whole suit was, and so that nothing is ever made immune to anything.
 *
 * <p>Falling is the exception that carried over exactly: it does not wear the plate at all. Whatever the ground
 * does to the wearer, it does nothing to a suit built on an altar.
 */
@EventBusSubscriber(modid = Alchemia.MODID)
public final class FortressArmourEvents {
    /**
     * What one piece worn turns of what the plate let through, by what the blow is made of.
     * <p>
     * Eighths, sixteenths and thirty-seconds. There are three pieces, so a whole suit turns three eighths of a
     * fire or a blast, three sixteenths of a blow, and three thirty-seconds of a working.
     */
    public static final float TURNS_BURNING = 0.125F;
    public static final float TURNS_BLOWS = 0.0625F;
    public static final float TURNS_WORKINGS = 0.03125F;

    /**
     * What a mask on the helm is worth, counted as a share of another piece of the suit.
     * <p>
     * An eighth, which is next to nothing, and that is the original's number: it added a twentieth to a multiplier
     * that a whole suit had already carried to one and a quarter. Nobody builds a mask for the arithmetic.
     */
    public static final float A_MASK = 0.125F;

    /** All four, though only three of them can hold any of this today. A fourth piece would need no change here. */
    private static final EquipmentSlot[] WORN = {EquipmentSlot.HEAD, EquipmentSlot.CHEST,
            EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private FortressArmourEvents() {
    }

    /** How many pieces of it are on. */
    public static int worn(LivingEntity wearer) {
        int pieces = 0;
        for (EquipmentSlot slot : WORN) {
            if (wearer.getItemBySlot(slot).getItem() instanceof FortressArmorItem) {
                pieces++;
            }
        }
        return pieces;
    }

    /**
     * How much of a blow the whole suit turns aside: what is worn, counting a mask as a fraction of a piece,
     * against what the blow is made of.
     */
    public static float turnedAside(LivingEntity wearer, DamageSource source) {
        float pieces = worn(wearer);
        for (EquipmentSlot slot : WORN) {
            if (FortressArmorItem.fitting(wearer.getItemBySlot(slot)).map(HelmFitting::isMask).orElse(false)) {
                pieces += A_MASK;
            }
        }
        return pieces * turns(source);
    }

    /** What one piece is worth against this particular blow. */
    public static float turns(DamageSource source) {
        if (source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_EXPLOSION)) {
            return TURNS_BURNING;
        }
        return source.is(Tags.DamageTypes.IS_MAGIC) ? TURNS_WORKINGS : TURNS_BLOWS;
    }

    /**
     * Turns part of a blow aside.
     * <p>
     * Only what the plate did not already stop. Damage that goes through armour as a matter of course -- a fall
     * into the void, starving, a blow that was never meant to be stopped -- goes through this too.
     */
    @SubscribeEvent
    public static void turnsBlows(LivingDamageEvent.Pre event) {
        if (event.getSource().is(DamageTypeTags.BYPASSES_ARMOR)) {
            return;
        }
        float turned = turnedAside(event.getEntity(), event.getSource());
        if (turned <= 0.0F) {
            return;
        }
        event.setNewDamage(event.getNewDamage() * (1.0F - turned));
    }

    /** Falling does not wear the plate. Whatever the ground does to the wearer, it does nothing to this. */
    @SubscribeEvent
    public static void fallingDoesNotWearIt(ArmorHurtEvent event) {
        if (!event.getDamageSource().is(DamageTypeTags.IS_FALL)) {
            return;
        }
        event.getArmorMap().forEach((slot, entry) -> {
            ItemStack piece = entry.armorItemStack;
            if (piece.getItem() instanceof FortressArmorItem) {
                event.setNewDamage(slot, 0.0F);
            }
        });
    }
}
