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
 * @param turret   what an autocaster needs to know to shoot it, or nothing if no autocaster will take it. Seven
 *                 of the twelve are worth aiming at something; the other five only mean anything in a hand that
 *                 is choosing where to point them
 * @param spell    what it does when it is let off. {@link Spell#NOTHING} for the ones not yet written
 */
public record Focus(int colour, AspectList cost, boolean perTick, int cooldown, Turret turret, Spell spell) {

    /**
     * What an autocaster has to know about a focus bolted into it.
     *
     * @param range how far it will shoot, in blocks. It is also how far the thing looks for something to shoot at
     * @param lift  how far it aims above what it is shooting at, in degrees, to allow for the drop on the way.
     *              Scaled by how far off the target is, so a near one is not shot over the head
     */
    public record Turret(float range, float lift) {
        /** As far as anything with a focus in it sees, which is as far as most of them shoot. */
        public static final float SEES = 32.0F;
    }

    /** A focus that costs what it costs per use, comes back at once, is no good to a turret, and does nothing. */
    public static Focus of(int colour, AspectList cost) {
        return new Focus(colour, cost, false, 0, null, Spell.NOTHING);
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

    /** A different price, which is what most of the upgrades that change a focus outright come to. */
    public Focus costing(AspectList cost) {
        return new Focus(colour, cost, perTick, cooldown, turret, spell);
    }

    /** Charged once when it goes off after all, which is what becoming a thrown thing does to a held one. */
    public Focus letGo() {
        return new Focus(colour, cost, false, cooldown, turret, spell);
    }

    public Focus cooldown(int milliseconds) {
        return new Focus(colour, cost, perTick, milliseconds, turret, spell);
    }

    /** An autocaster will take this one, and it shoots as far and as flat as anything does. */
    public Focus inTurrets() {
        return inTurrets(Turret.SEES, 0.0F);
    }

    /** An autocaster will take this one. Named around the component for the same reason {@link #heldDown()} is. */
    public Focus inTurrets(float range, float lift) {
        return new Focus(colour, cost, perTick, cooldown, new Turret(range, lift), spell);
    }
}
