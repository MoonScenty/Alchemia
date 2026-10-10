package me.moonscenty.alchemia.wand;

import me.moonscenty.alchemia.aspect.AspectList;

/**
 * What a focus is, apart from what it does.
 * <p>
 * A focus screws onto the end of a wand and decides what happens when the wand is pointed at something. What it
 * does is a dozen different things and belongs a dozen different places; what every one of them has in common is
 * here — a price, a colour, and how long to wait before asking again.
 *
 * @param colour   the colour of what comes out of the wand, not of the focus in the hand. The original passed it
 *                 when it registered each focus and used it to tint the bolt, the orb or the shard
 * @param cost     what one use is charged, in whole vis
 * @param perTick  whether that is the price of a use or the price of a tick of holding it down
 * @param cooldown how long before the wand will do it again, in milliseconds as the original counted it
 * @param turret   whether an autocaster will take it. Seven of the twelve are worth aiming at something; the
 *                 other five only mean anything in a hand that is choosing where to point them
 * @param spell    what it does when it is let off. {@link Spell#NOTHING} for the ones not yet written
 */
public record Focus(int colour, AspectList cost, boolean perTick, int cooldown, boolean turret, Spell spell) {

    /** A focus that costs what it costs per use, comes back at once, is no good to a turret, and does nothing. */
    public static Focus of(int colour, AspectList cost) {
        return new Focus(colour, cost, false, 0, false, Spell.NOTHING);
    }

    /** What it does when it is let off. */
    public Focus casts(Spell spell) {
        return new Focus(colour, cost, perTick, cooldown, turret, spell);
    }

    /**
     * Charged for every tick it is held rather than once when it is let go.
     * <p>
     * Not called {@code perTick()}: that name belongs to the component this sets, and a record will not have one
     * name mean two things.
     */
    public Focus heldDown() {
        return new Focus(colour, cost, true, cooldown, turret, spell);
    }

    public Focus cooldown(int milliseconds) {
        return new Focus(colour, cost, perTick, milliseconds, turret, spell);
    }

    /** An autocaster will take this one. Named around the component for the same reason {@link #heldDown()} is. */
    public Focus inTurrets() {
        return new Focus(colour, cost, perTick, cooldown, true, spell);
    }
}
