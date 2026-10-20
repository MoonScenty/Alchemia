package me.moonscenty.alchemia.player;

import java.util.Optional;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.player.effect.ModEffects;
import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Decides when a player's warp catches up with them, and what it does when it does.
 * <p>
 * A check comes round every hundred seconds. Whether anything happens depends on the counter, which winds up as warp is
 * gained and unwinds as events fire — so a burst of warp brings trouble soon, and then things settle.
 */
@EventBusSubscriber(modid = Alchemia.MODID)
public class WarpHandler {
    /** How often a player is checked, in ticks. */
    private static final int CHECK_INTERVAL = 2000;
    /** Severity is drawn from the player's warp, capped here so the very worst is always reachable but never certain. */
    private static final int MAX_SEVERITY = 100;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide || !(player instanceof ServerPlayer server)) {
            return;
        }
        if (player.tickCount > 0 && player.tickCount % CHECK_INTERVAL == 0 && !player.hasEffect(ModEffects.WARP_WARD)) {
            check(server);
        }
    }

    /**
     * Rolls once against the counter, and sees an event through if it comes up.
     * <p>
     * What the player is holding and wearing counts as well, while they are holding and wearing it. See
     * {@link me.moonscenty.alchemia.item.WarpingGear}.
     */
    public static void check(ServerPlayer player) {
        WarpData warp = WarpData.of(player);
        int gear = me.moonscenty.alchemia.item.WarpingGear.carried(player);
        RandomSource random = player.getRandom();
        if (!triggers(warp, gear, random)) {
            return;
        }

        player.setData(ModAttachments.WARP, afterEvent(warp, gear));
        // the heart pounds before anything else happens, and only the one it is happening to hears it
        hear(player, ModSounds.HEARTBEAT, player.getX(), player.getY(), player.getZ(), 1.0F, 1.0F);
        int severity = severityOf(warp, gear);
        // a grinning devil on the helm takes the edge off; what comes to nothing is nothing, as in the original
        int roll = random.nextInt(Math.max(1, severity)) - me.moonscenty.alchemia.item.FortressMasks.easing(player);
        if (roll > 0) {
            inflict(player, severity, roll);
        }
        epiphany(player, warp.permanent() + warp.sticky());
    }

    /**
     * Warp that has settled in teaches. Past twenty-five of it that will not pass, a warp event leaves the minor
     * eldritch lore behind it; past fifty, the major. The original's thresholds.
     */
    public static void epiphany(net.minecraft.world.entity.player.Player player, int lasting) {
        // past ten, the headaches want something done about them: the bath salts' clue
        net.minecraft.resources.ResourceLocation salts = me.moonscenty.alchemia.Alchemia.id("bath_salts");
        if (lasting > 10 && !PlayerKnowledge.of(player).hasResearch(salts)
                && !PlayerKnowledge.of(player).hasResearch(me.moonscenty.alchemia.research.Clues.BATH_SALTS)) {
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable("warp.alchemia.headaches")
                    .withStyle(net.minecraft.ChatFormatting.DARK_PURPLE, net.minecraft.ChatFormatting.ITALIC), false);
            me.moonscenty.alchemia.research.Clues.give(player, me.moonscenty.alchemia.research.Clues.BATH_SALTS);
        }
        if (lasting > 25) {
            learn(player, me.moonscenty.alchemia.Alchemia.id("eldritch_minor"));
        }
        if (lasting > 50) {
            learn(player, me.moonscenty.alchemia.Alchemia.id("eldritch_major"));
        }
    }

    /** Teaches a player a piece of research, with all that comes of learning it, unless they knew it. */
    public static void learn(net.minecraft.world.entity.player.Player player,
            net.minecraft.resources.ResourceLocation research) {
        me.moonscenty.alchemia.research.ResearchCompletion.complete(player, research);
    }

    /**
     * Whether this check brings something on. The more warp has piled up unanswered, the likelier it is; a player
     * carrying no warp at all is never troubled.
     */
    public static boolean triggers(WarpData warp, RandomSource random) {
        return triggers(warp, 0, random);
    }

    /** The same, with warp carried in gear counted on top of the player's own. */
    public static boolean triggers(WarpData warp, int gear, RandomSource random) {
        return warp.counter() > 0 && warp.total() + gear > 0 && random.nextInt(100) <= Math.sqrt(warp.counter());
    }

    /** How bad an event drawn at this much warp can get. Draws on the warp and the pent-up counter together. */
    public static int severityOf(WarpData warp) {
        return severityOf(warp, 0);
    }

    public static int severityOf(WarpData warp, int gear) {
        return Math.min(MAX_SEVERITY, ((warp.total() + gear) * 2 + warp.counter()) / 3);
    }

    /** The counter unwinds as the event plays out, and a little temporary warp works itself off with it. */
    public static WarpData afterEvent(WarpData warp) {
        return afterEvent(warp, 0);
    }

    /**
     * The same, for a player carrying warp in their gear: it bleeds off more slowly, two less for every point
     * carried, though never less than the five any event takes.
     */
    public static WarpData afterEvent(WarpData warp, int gear) {
        int spent = (int) Math.max(5, Math.sqrt(warp.counter()) * 2 - gear * 2);
        return warp.withCounter(warp.counter() - spent).add(WarpData.Kind.TEMPORARY, -1);
    }

    /** Adds warp of one kind and lets the player know something has shifted. */
    public static void add(Player player, WarpData.Kind kind, int amount) {
        if (amount == 0) {
            return;
        }
        player.setData(ModAttachments.WARP, WarpData.of(player).add(kind, amount));
        if (!(player instanceof ServerPlayer server)) {
            return;
        }
        // warp that settles in is heard settling
        if (amount > 0 && kind != WarpData.Kind.TEMPORARY) {
            hear(server, ModSounds.WHISPERS, player.getX(), player.getY(), player.getZ(), 0.5F, 1.0F);
        }
        // and told of; temporary warp wearing off is not worth a word
        if (amount > 0 || kind != WarpData.Kind.TEMPORARY) {
            server.displayClientMessage(Component.translatable("warp.alchemia." + (amount > 0 ? "gained." : "lost.")
                    + kind.name().toLowerCase(java.util.Locale.ROOT)).withStyle(net.minecraft.ChatFormatting.DARK_PURPLE),
                    false);
        }
    }

    /** A sound only this player hears, as the original's warp sounds were played on the client alone. */
    private static void hear(ServerPlayer player, Holder<SoundEvent> sound, double x, double y, double z,
            float volume, float pitch) {
        player.connection.send(new ClientboundSoundPacket(sound, SoundSource.AMBIENT, x, y, z, volume, pitch,
                player.getRandom().nextLong()));
    }

    /**
     * What a warp event comes to: a line of warning, and often an ailment with it.
     *
     * @param message which whisper the player hears
     * @param effect  what settles on them, if anything
     */
    public record Outcome(int message, Optional<Holder<MobEffect>> effect, int duration, int amplifier,
            Summons summons) {
        static Outcome whisper(int message) {
            return new Outcome(message, Optional.empty(), 0, 0, Summons.NOTHING);
        }

        static Outcome ail(int message, Holder<MobEffect> effect, int duration, int amplifier) {
            return new Outcome(message, Optional.of(effect), duration, amplifier, Summons.NOTHING);
        }

        static Outcome summon(int message, Summons summons) {
            return new Outcome(message, Optional.empty(), 0, 0, summons);
        }
    }

    /** What a warp event calls up out of the dark, if anything. */
    public enum Summons {
        NOTHING,
        /** A fog, and a guardian in it. */
        GUARDIAN,
        /** A fog, and a guardian in it for every thirty of warp. */
        GUARDIANS,
        /** A fog, and a guardian in it for every fifteen of warp. */
        MANY_GUARDIANS,
        /** Spiders only the player can see. */
        ILLUSORY_SPIDERS,
        /** Spiders. */
        SPIDERS,
        /** A lesser crimson portal somewhere near. */
        PORTAL
    }

    /**
     * Works through the same ladder of outcomes the original used. The roll decides how far up it lands; the warp
     * itself decides how hard the outcome bites.
     * <p>
     * A few rungs call something up out of the dark: a fog with guardians in it, spiders real or not, a portal.
     */
    public static Outcome outcomeFor(int warp, int roll) {
        int amp = Math.min(3, warp / 15);

        if (roll <= 4) {
            return Outcome.whisper(0);
        } else if (roll <= 8) {
            return Outcome.whisper(1);
        } else if (roll <= 12) {
            return Outcome.whisper(2);
        } else if (roll <= 16) {
            return Outcome.ail(3, ModEffects.FLUX_FLU, 5000, amp);
        } else if (roll <= 20) {
            return Outcome.ail(4, ModEffects.ALCHEDIARRHEA, Math.min(32000, 10 * warp), 0);
        } else if (roll <= 24) {
            return Outcome.ail(5, ModEffects.UNNATURAL_HUNGER, 5000, amp);
        } else if (roll <= 28) {
            return Outcome.whisper(6);
        } else if (roll <= 32) {
            return Outcome.summon(7, Summons.GUARDIAN); // a mist, and something walking in it
        } else if (roll <= 36) {
            return Outcome.ail(8, ModEffects.BLURRED_VISION, Math.min(32000, 10 * warp), 0);
        } else if (roll <= 40) {
            return Outcome.ail(9, ModEffects.SUN_SCORNED, 5000, amp);
        } else if (roll <= 44) {
            return Outcome.ail(10, MobEffects.DIG_SLOWDOWN, 1200, amp);
        } else if (roll <= 48) {
            return Outcome.ail(11, ModEffects.FLUX_PHAGE, 6000, amp);
        } else if (roll <= 52) {
            // seeing in the dark is its own kind of wrong
            return Outcome.ail(12, MobEffects.NIGHT_VISION, Math.min(40 * warp, 6000), 0);
        } else if (roll <= 56) {
            return Outcome.ail(13, ModEffects.DEADLY_GAZE, 6000, amp);
        } else if (roll <= 60) {
            return Outcome.summon(12, Summons.ILLUSORY_SPIDERS); // spiders, or the idea of them
        } else if (roll <= 64) {
            return Outcome.whisper(13);
        } else if (roll <= 68) {
            return Outcome.summon(7, Summons.GUARDIANS); // more mist, and more in it
        } else if (roll <= 72) {
            return Outcome.ail(10, MobEffects.BLINDNESS, Math.min(32000, 5 * warp), 0);
        } else if (roll <= 80) {
            return Outcome.ail(5, ModEffects.UNNATURAL_HUNGER, 6000, amp);
        } else if (roll <= 88) {
            return Outcome.summon(15, Summons.PORTAL); // chanting, somewhere close
        } else if (roll <= 92) {
            return Outcome.summon(12, Summons.SPIDERS); // spiders again, and these ones are real
        }
        return Outcome.summon(7, Summons.MANY_GUARDIANS);
    }

    private static void inflict(ServerPlayer player, int warp, int roll) {
        Outcome outcome = outcomeFor(warp, roll);
        outcome.effect().ifPresent(effect ->
                player.addEffect(new MobEffectInstance(effect, outcome.duration(), outcome.amplifier(), true, true)));
        whisper(player, outcome.message());
        // the two mildest are only noises: a creeper hissing at your back, or something going off out of sight
        RandomSource random = player.getRandom();
        if (roll <= 4) {
            hear(player, BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.CREEPER_PRIMED),
                    player.getX(), player.getY(), player.getZ(), 1.0F, 0.5F);
        } else if (roll <= 8) {
            hear(player, SoundEvents.GENERIC_EXPLODE, player.getX() + (random.nextFloat() - 0.5F) * 20.0F,
                    player.getY() + (random.nextFloat() - 0.5F) * 20.0F,
                    player.getZ() + (random.nextFloat() - 0.5F) * 20.0F, 4.0F,
                    (1.0F + (random.nextFloat() - random.nextFloat()) * 0.2F) * 0.7F);
        }
        switch (outcome.summons()) {
            case GUARDIAN -> me.moonscenty.alchemia.entity.WarpSummons.mist(player, 1);
            case GUARDIANS -> me.moonscenty.alchemia.entity.WarpSummons.mist(player, warp / 30);
            case MANY_GUARDIANS -> me.moonscenty.alchemia.entity.WarpSummons.mist(player, warp / 15);
            case ILLUSORY_SPIDERS -> me.moonscenty.alchemia.entity.WarpSummons.spiders(player, warp, false);
            case SPIDERS -> me.moonscenty.alchemia.entity.WarpSummons.spiders(player, warp, true);
            case PORTAL -> me.moonscenty.alchemia.entity.WarpSummons.portal(player);
            default -> {
            }
        }
    }

    /** Warp speaks to the player before it does anything, always in the same unsettling italics. */
    private static void whisper(ServerPlayer player, int line) {
        player.sendSystemMessage(Component.translatable("warp.alchemia.text." + Mth.clamp(line, 0, 15))
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
    }
}
