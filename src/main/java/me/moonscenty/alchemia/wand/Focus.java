package me.moonscenty.alchemia.wand;

import me.moonscenty.alchemia.aspect.AspectList;

// cooldown is in milliseconds, as the original counted it
public record Focus(int colour, AspectList cost, boolean perTick, int cooldown, Turret turret, Spell spell,
        double reach, boolean blocksOnly) {

    // lift: degrees aimed above the target, scaled by its distance
    public record Turret(float range, float lift) {
        public static final float SEES = 32.0F;
    }

    public static Focus of(int colour, AspectList cost) {
        return new Focus(colour, cost, false, 0, null, Spell.NOTHING, Casting.REACH, false);
    }

    public Focus casts(Spell spell) {
        return new Focus(colour, cost, perTick, cooldown, turret, spell, reach, blocksOnly);
    }

    public Focus reaching(double reach, boolean blocksOnly) {
        return new Focus(colour, cost, perTick, cooldown, turret, spell, reach, blocksOnly);
    }

    public Focus heldDown() {
        return new Focus(colour, cost, true, cooldown, turret, spell, reach, blocksOnly);
    }

    public Focus costing(AspectList cost) {
        return new Focus(colour, cost, perTick, cooldown, turret, spell, reach, blocksOnly);
    }

    public Focus letGo() {
        return new Focus(colour, cost, false, cooldown, turret, spell, reach, blocksOnly);
    }

    public Focus cooldown(int milliseconds) {
        return new Focus(colour, cost, perTick, milliseconds, turret, spell, reach, blocksOnly);
    }

    public Focus inTurrets() {
        return inTurrets(Turret.SEES, 0.0F);
    }

    public Focus inTurrets(float range, float lift) {
        return new Focus(colour, cost, perTick, cooldown, new Turret(range, lift), spell, reach, blocksOnly);
    }
}
