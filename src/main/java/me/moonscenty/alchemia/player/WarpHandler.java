package me.moonscenty.alchemia.player;

import java.util.Optional;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.player.effect.ModEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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

    /** Rolls once against the counter, and sees an event through if it comes up. */
    public static void check(ServerPlayer player) {
        WarpData warp = WarpData.of(player);
        RandomSource random = player.getRandom();
        if (!triggers(warp, random)) {
            return;
        }

        player.setData(ModAttachments.WARP, afterEvent(warp));
        int severity = severityOf(warp);
        inflict(player, severity, random.nextInt(Math.max(1, severity)));
    }

    /**
     * Whether this check brings something on. The more warp has piled up unanswered, the likelier it is; a player
     * carrying no warp at all is never troubled.
     */
    public static boolean triggers(WarpData warp, RandomSource random) {
        return warp.counter() > 0 && warp.total() > 0 && random.nextInt(100) <= Math.sqrt(warp.counter());
    }

    /** How bad an event drawn at this much warp can get. Draws on the warp and the pent-up counter together. */
    public static int severityOf(WarpData warp) {
        return Math.min(MAX_SEVERITY, (warp.total() * 2 + warp.counter()) / 3);
    }

    /** The counter unwinds as the event plays out, and a little temporary warp works itself off with it. */
    public static WarpData afterEvent(WarpData warp) {
        int spent = (int) Math.max(5, Math.sqrt(warp.counter()) * 2);
        return warp.withCounter(warp.counter() - spent).add(WarpData.Kind.TEMPORARY, -1);
    }

    /** Adds warp of one kind and lets the player know something has shifted. */
    public static void add(Player player, WarpData.Kind kind, int amount) {
        if (amount == 0) {
            return;
        }
        player.setData(ModAttachments.WARP, WarpData.of(player).add(kind, amount));
    }

    /**
     * What a warp event comes to: a line of warning, and often an ailment with it.
     *
     * @param message which whisper the player hears
     * @param effect  what settles on them, if anything
     */
    public record Outcome(int message, Optional<Holder<MobEffect>> effect, int duration, int amplifier) {
        static Outcome whisper(int message) {
            return new Outcome(message, Optional.empty(), 0, 0);
        }

        static Outcome ail(int message, Holder<MobEffect> effect, int duration, int amplifier) {
            return new Outcome(message, Optional.of(effect), duration, amplifier);
        }
    }

    /**
     * Works through the same ladder of outcomes the original used. The roll decides how far up it lands; the warp
     * itself decides how hard the outcome bites.
     * <p>
     * A few rungs originally called something up out of the dark. Those have nothing to call yet, so for now they
     * only give the player warning; they get their teeth back in steps 11 and 13.
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
            return Outcome.whisper(7); // a mist, and something walking in it
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
            return Outcome.whisper(12); // spiders, or the idea of them
        } else if (roll <= 64) {
            return Outcome.whisper(13);
        } else if (roll <= 68) {
            return Outcome.whisper(7); // more mist, and more in it
        } else if (roll <= 72) {
            return Outcome.ail(10, MobEffects.BLINDNESS, Math.min(32000, 5 * warp), 0);
        } else if (roll <= 80) {
            return Outcome.ail(5, ModEffects.UNNATURAL_HUNGER, 6000, amp);
        } else if (roll <= 88) {
            return Outcome.whisper(14); // a way opening that should stay shut
        } else if (roll <= 92) {
            return Outcome.whisper(12); // spiders again, and these ones are real
        }
        return Outcome.whisper(14);
    }

    private static void inflict(ServerPlayer player, int warp, int roll) {
        Outcome outcome = outcomeFor(warp, roll);
        outcome.effect().ifPresent(effect ->
                player.addEffect(new MobEffectInstance(effect, outcome.duration(), outcome.amplifier(), true, true)));
        whisper(player, outcome.message());
    }

    /** Warp speaks to the player before it does anything, always in the same unsettling italics. */
    private static void whisper(ServerPlayer player, int line) {
        player.sendSystemMessage(Component.translatable("warp.alchemia.text." + Mth.clamp(line, 0, 14))
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
    }
}
