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
    /** A taint swarm humming, biting, and one of its flies buzzing past. */
    public static final DeferredHolder<SoundEvent, SoundEvent> SWARM = sound("swarm");
    public static final DeferredHolder<SoundEvent, SoundEvent> SWARM_ATTACK = sound("swarm.attack");
    public static final DeferredHolder<SoundEvent, SoundEvent> FLY = sound("fly");
    /** A taintacle creaking like roots, and lashing. */
    public static final DeferredHolder<SoundEvent, SoundEvent> ROOTS = sound("roots");
    public static final DeferredHolder<SoundEvent, SoundEvent> TENTACLE = sound("tentacle");
    /** Something bursting wetly. */
    public static final DeferredHolder<SoundEvent, SoundEvent> GORE = sound("gore");
    /** A handful of coins. */
    public static final DeferredHolder<SoundEvent, SoundEvent> COINS = sound("coins");

    /** A shutter clacking: a golem going about, or hurt. */
    public static final DeferredHolder<SoundEvent, SoundEvent> CAMERA_CLACK = sound("cameraclack");
    /** Tools going down: a golem falling apart. */
    public static final DeferredHolder<SoundEvent, SoundEvent> TOOL = sound("tool");
    /** A rising chirp, as a golem takes in what it is told. */
    public static final DeferredHolder<SoundEvent, SoundEvent> SCAN = sound("scan");
    /** A wand's flourish, as something is made. */
    public static final DeferredHolder<SoundEvent, SoundEvent> WAND = sound("wand");
    /** Something starting to be made. */
    public static final DeferredHolder<SoundEvent, SoundEvent> CRAFT_START = sound("craftstart");

    /** The Crimson Cult: a cleric's chanting, a portal's hum, a summons gone through, an orb cast and landing. */
    public static final DeferredHolder<SoundEvent, SoundEvent> CHANT = sound("chant");
    public static final DeferredHolder<SoundEvent, SoundEvent> MONOLITH = sound("monolith");
    public static final DeferredHolder<SoundEvent, SoundEvent> WAND_FAIL = sound("wandfail");
    public static final DeferredHolder<SoundEvent, SoundEvent> EG_ATTACK = sound("egattack");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHOCK = sound("shock");
    /** The eldritch crab's chatter, the snap of its claws and its last. */
    public static final DeferredHolder<SoundEvent, SoundEvent> CRAB_TALK = sound("crabtalk");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRAB_CLAW = sound("crabclaw");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRAB_DEATH = sound("crabdeath");
    /** The guardian's breathing, its screech and its last. */
    public static final DeferredHolder<SoundEvent, SoundEvent> EG_IDLE = sound("egidle");
    public static final DeferredHolder<SoundEvent, SoundEvent> EG_SCREECH = sound("egscreech");
    public static final DeferredHolder<SoundEvent, SoundEvent> EG_DEATH = sound("egdeath");
    /** Something learnt off a page. */
    public static final DeferredHolder<SoundEvent, SoundEvent> LEARN = sound("learn");

    private static DeferredHolder<SoundEvent, SoundEvent> sound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(Alchemia.id(name)));
    }

    private ModSounds() {
    }
}
