package me.moonscenty.alchemia.registry;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT,
            Alchemia.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> WISP_LIVE = sound("wisp.live");
    public static final DeferredHolder<SoundEvent, SoundEvent> WISP_DEAD = sound("wisp.dead");
    public static final DeferredHolder<SoundEvent, SoundEvent> ZAP = sound("zap");
    public static final DeferredHolder<SoundEvent, SoundEvent> ICE = sound("ice");
    public static final DeferredHolder<SoundEvent, SoundEvent> PECH_IDLE = sound("pech.idle");
    public static final DeferredHolder<SoundEvent, SoundEvent> PECH_HIT = sound("pech.hit");
    public static final DeferredHolder<SoundEvent, SoundEvent> PECH_DEATH = sound("pech.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> PECH_CHARGE = sound("pech.charge");
    public static final DeferredHolder<SoundEvent, SoundEvent> PECH_TRADE = sound("pech.trade");
    public static final DeferredHolder<SoundEvent, SoundEvent> PECH_DICE = sound("pech.dice");
    public static final DeferredHolder<SoundEvent, SoundEvent> SWARM = sound("swarm");
    public static final DeferredHolder<SoundEvent, SoundEvent> SWARM_ATTACK = sound("swarm.attack");
    public static final DeferredHolder<SoundEvent, SoundEvent> FLY = sound("fly");
    public static final DeferredHolder<SoundEvent, SoundEvent> ROOTS = sound("roots");
    public static final DeferredHolder<SoundEvent, SoundEvent> TENTACLE = sound("tentacle");
    public static final DeferredHolder<SoundEvent, SoundEvent> GORE = sound("gore");
    public static final DeferredHolder<SoundEvent, SoundEvent> COINS = sound("coins");
    public static final DeferredHolder<SoundEvent, SoundEvent> JACOBS = sound("jacobs");
    public static final DeferredHolder<SoundEvent, SoundEvent> RUNIC_SHIELD_EFFECT = sound("runicshieldeffect");
    public static final DeferredHolder<SoundEvent, SoundEvent> WIND = sound("wind");
    public static final DeferredHolder<SoundEvent, SoundEvent> PUMP = sound("pump");
    public static final DeferredHolder<SoundEvent, SoundEvent> RUNIC_SHIELD_CHARGE = sound("runicshieldcharge");
    public static final DeferredHolder<SoundEvent, SoundEvent> EVIL_PORTAL = sound("evilportal");
    public static final DeferredHolder<SoundEvent, SoundEvent> URN_BREAK = sound("urnbreak");

    public static final DeferredHolder<SoundEvent, SoundEvent> CAMERA_CLACK = sound("cameraclack");
    public static final DeferredHolder<SoundEvent, SoundEvent> TOOL = sound("tool");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCAN = sound("scan");
    public static final DeferredHolder<SoundEvent, SoundEvent> WAND = sound("wand");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRAFT_START = sound("craftstart");

    public static final DeferredHolder<SoundEvent, SoundEvent> CHANT = sound("chant");
    public static final DeferredHolder<SoundEvent, SoundEvent> MONOLITH = sound("monolith");
    public static final DeferredHolder<SoundEvent, SoundEvent> WAND_FAIL = sound("wandfail");
    public static final DeferredHolder<SoundEvent, SoundEvent> EG_ATTACK = sound("egattack");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHOCK = sound("shock");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRAB_TALK = sound("crabtalk");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRAB_CLAW = sound("crabclaw");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRAB_DEATH = sound("crabdeath");
    public static final DeferredHolder<SoundEvent, SoundEvent> EG_IDLE = sound("egidle");
    public static final DeferredHolder<SoundEvent, SoundEvent> EG_SCREECH = sound("egscreech");
    public static final DeferredHolder<SoundEvent, SoundEvent> EG_DEATH = sound("egdeath");
    public static final DeferredHolder<SoundEvent, SoundEvent> LEARN = sound("learn");

    public static final DeferredHolder<SoundEvent, SoundEvent> KEY = sound("key");

    public static final DeferredHolder<SoundEvent, SoundEvent> POOF = sound("poof");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPILL = sound("spill");
    public static final DeferredHolder<SoundEvent, SoundEvent> BUBBLE = sound("bubble");

    public static final DeferredHolder<SoundEvent, SoundEvent> JAR = sound("jar");
    public static final DeferredHolder<SoundEvent, SoundEvent> ALEMBIC_KNOCK = sound("alembicknock");
    public static final DeferredHolder<SoundEvent, SoundEvent> PAGE = sound("page");
    public static final DeferredHolder<SoundEvent, SoundEvent> CRYSTAL = sound("crystal");

    public static final DeferredHolder<SoundEvent, SoundEvent> CRAFT_FAIL = sound("craftfail");

    public static final DeferredHolder<SoundEvent, SoundEvent> CAMERA_TICKS = sound("cameraticks");
    public static final DeferredHolder<SoundEvent, SoundEvent> SQUEEK = sound("squeek");
    public static final DeferredHolder<SoundEvent, SoundEvent> CREAK = sound("creak");

    public static final DeferredHolder<SoundEvent, SoundEvent> RUMBLE = sound("rumble");
    public static final DeferredHolder<SoundEvent, SoundEvent> FIRE_LOOP = sound("fireloop");
    public static final DeferredHolder<SoundEvent, SoundEvent> BRAIN = sound("brain");

    public static final DeferredHolder<SoundEvent, SoundEvent> INFUSER_START = sound("infuserstart");
    public static final DeferredHolder<SoundEvent, SoundEvent> INFUSER = sound("infuser");

    public static final DeferredHolder<SoundEvent, SoundEvent> HH_ON = sound("hhon");
    public static final DeferredHolder<SoundEvent, SoundEvent> HH_OFF = sound("hhoff");
    public static final DeferredHolder<SoundEvent, SoundEvent> WRITE = sound("write");
    public static final DeferredHolder<SoundEvent, SoundEvent> ERASE = sound("erase");
    public static final DeferredHolder<SoundEvent, SoundEvent> SWING = sound("swing");

    public static final DeferredHolder<SoundEvent, SoundEvent> HEARTBEAT = sound("heartbeat");
    public static final DeferredHolder<SoundEvent, SoundEvent> WHISPERS = sound("whispers");

    private static DeferredHolder<SoundEvent, SoundEvent> sound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(Alchemia.id(name)));
    }

    private ModSounds() {
    }
}
