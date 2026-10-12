package me.moonscenty.alchemia.registry;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The craft's own sounds.
 *
 * <p>What each is made of is said in {@code sounds.json}, which names the original's recordings; those are read out
 * of the player's own copy of the original at start-up, as its pictures are, and never shipped here.
 */
public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT,
            Alchemia.MODID);

    /** A wisp drifting about, and one going out. */
    public static final DeferredHolder<SoundEvent, SoundEvent> WISP_LIVE = sound("wisp.live");
    public static final DeferredHolder<SoundEvent, SoundEvent> WISP_DEAD = sound("wisp.dead");
    /** A spark jumping to what it strikes. */
    public static final DeferredHolder<SoundEvent, SoundEvent> ZAP = sound("zap");
    /** A frosty crack, as of a pech mage's blast going off. */
    public static final DeferredHolder<SoundEvent, SoundEvent> ICE = sound("ice");
    /** A pech muttering, struck, dying, working itself up to a fight, and pleased with a trade. */
    public static final DeferredHolder<SoundEvent, SoundEvent> PECH_IDLE = sound("pech.idle");
    public static final DeferredHolder<SoundEvent, SoundEvent> PECH_HIT = sound("pech.hit");
    public static final DeferredHolder<SoundEvent, SoundEvent> PECH_DEATH = sound("pech.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> PECH_CHARGE = sound("pech.charge");
    public static final DeferredHolder<SoundEvent, SoundEvent> PECH_TRADE = sound("pech.trade");
    /** A pech rolling the bones over what it has been offered. */
    public static final DeferredHolder<SoundEvent, SoundEvent> PECH_DICE = sound("pech.dice");
    /** A handful of coins. */
    public static final DeferredHolder<SoundEvent, SoundEvent> COINS = sound("coins");

    private static DeferredHolder<SoundEvent, SoundEvent> sound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(Alchemia.id(name)));
    }

    private ModSounds() {
    }
}
