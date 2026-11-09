package me.moonscenty.alchemia.player;

import java.util.Optional;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.player.effect.ModEffects;
import me.moonscenty.alchemia.registry.ModItems;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

// The counter winds up as warp is gained and unwinds as events fire, so a burst of warp brings trouble soon.
@EventBusSubscriber(modid = Alchemia.MODID)
public class WarpHandler {
    private static final int CHECK_INTERVAL = 2000;
    // caps the severity roll so the worst outcome is reachable but never certain
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
        // original's period
        if (player.tickCount % 10 == 0 && player.hasEffect(ModEffects.DEADLY_GAZE)) {
            gaze(player);
        }
    }

    public static void gaze(Player player) {
        MobEffectInstance gaze = player.getEffect(ModEffects.DEADLY_GAZE);
        if (gaze == null) {
            return;
        }
        // original's reach and cone
        int range = Math.min(8 + gaze.getAmplifier() * 3, 24);
        boolean pvp = player.level().getServer().isPvpAllowed();
        for (Entity entity : player.level().getEntities(player, player.getBoundingBox().inflate(range))) {
            if (entity.isPickable() && entity instanceof LivingEntity living && living.isAlive()
                    && inView(0.75F, player, living, range) && player.hasLineOfSight(living)
                    && (!(living instanceof Player) || pvp) && !living.hasEffect(MobEffects.WITHER)) {
                living.setLastHurtByMob(player);
                living.setLastHurtMob(player);
                if (living instanceof PathfinderMob mob) {
                    mob.setTarget(player);
                }
                living.addEffect(new MobEffectInstance(MobEffects.WITHER, 80));
            }
        }
    }

    private static boolean inView(float aperture, Player player, LivingEntity target, float range) {
        Vec3 toTarget = new Vec3(target.getX(), target.getY() + target.getBbHeight() / 2.0F, target.getZ())
                .subtract(player.getEyePosition());
        Vec3 axis = player.getLookAngle().scale(range);
        double along = toTarget.dot(axis) / axis.length();
        return along / toTarget.length() > Math.cos(aperture / 2.0F) && along < axis.length();
    }

    @SubscribeEvent
    public static void onFinishedUsing(LivingEntityUseItemEvent.Finish event) {
        if (!event.getEntity().level().isClientSide && event.getEntity() instanceof Player player) {
            ate(player, event.getItem());
        }
    }

    // original: flesh and brains take a level and 30 seconds off the hunger; other food only earns a warning
    public static void ate(Player player, ItemStack eaten) {
        MobEffectInstance hunger = player.getEffect(ModEffects.UNNATURAL_HUNGER);
        if (hunger == null) {
            return;
        }
        if (eaten.is(Items.ROTTEN_FLESH) || eaten.is(ModItems.ZOMBIE_BRAIN.get())) {
            player.removeEffect(ModEffects.UNNATURAL_HUNGER);
            if (hunger.getDuration() > 600 && hunger.getAmplifier() > 0) {
                MobEffectInstance eased = new MobEffectInstance(ModEffects.UNNATURAL_HUNGER, hunger.getDuration() - 600,
                        hunger.getAmplifier() - 1, true, false);
                eased.getCures().clear();
                player.addEffect(eased);
            }
            player.displayClientMessage(Component.translatable("warp.alchemia.hunger.fading")
                    .withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.ITALIC), false);
        } else if (eaten.getFoodProperties(player) != null) {
            player.displayClientMessage(Component.translatable("warp.alchemia.hunger.unsatisfied")
                    .withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);
        }
    }

    // worn and held warping gear counts too (see WarpingGear)
    public static void check(ServerPlayer player) {
        WarpData warp = WarpData.of(player);
        int gear = me.moonscenty.alchemia.item.WarpingGear.carried(player);
        RandomSource random = player.getRandom();
        if (triggers(warp, gear, random)) {
            player.setData(ModAttachments.WARP, afterEvent(warp, gear));
            hear(player, ModSounds.HEARTBEAT, player.getX(), player.getY(), player.getZ(), 1.0F, 1.0F);
            int severity = severityOf(warp, gear);
            // fortress mask easing; a roll that comes to nothing does nothing (original's rule)
            int roll = random.nextInt(Math.max(1, severity)) - me.moonscenty.alchemia.item.FortressMasks.easing(player);
            if (roll > 0) {
                inflict(player, severity, roll);
            }
            epiphany(player, warp.permanent() + warp.sticky());
        }
        // original: every check wears a point off, event or not
        player.setData(ModAttachments.WARP, WarpData.of(player).wornOff());
    }

    // original's thresholds: minor eldritch lore past 25 lasting warp, major past 50
    public static void epiphany(net.minecraft.world.entity.player.Player player, int lasting) {
        // past 10: the bath salts' clue
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

    public static void learn(net.minecraft.world.entity.player.Player player,
            net.minecraft.resources.ResourceLocation research) {
        me.moonscenty.alchemia.research.ResearchCompletion.complete(player, research);
    }

    public static boolean triggers(WarpData warp, RandomSource random) {
        return triggers(warp, 0, random);
    }

    public static boolean triggers(WarpData warp, int gear, RandomSource random) {
        return warp.counter() > 0 && warp.total() + gear > 0 && random.nextInt(100) <= Math.sqrt(warp.counter());
    }

    public static int severityOf(WarpData warp) {
        return severityOf(warp, 0);
    }

    public static int severityOf(WarpData warp, int gear) {
        return Math.min(MAX_SEVERITY, ((warp.total() + gear) * 2 + warp.counter()) / 3);
    }

    public static WarpData afterEvent(WarpData warp) {
        return afterEvent(warp, 0);
    }

    // gear slows the unwind by 2 per point carried, but never below the 5 any event takes
    public static WarpData afterEvent(WarpData warp, int gear) {
        int spent = (int) Math.max(5, Math.sqrt(warp.counter()) * 2 - gear * 2);
        return warp.withCounter(warp.counter() - spent);
    }

    public static void add(Player player, WarpData.Kind kind, int amount) {
        if (amount == 0) {
            return;
        }
        player.setData(ModAttachments.WARP, WarpData.of(player).add(kind, amount));
        if (!(player instanceof ServerPlayer server)) {
            return;
        }
        if (amount > 0 && kind != WarpData.Kind.TEMPORARY) {
            hear(server, ModSounds.WHISPERS, player.getX(), player.getY(), player.getZ(), 0.5F, 1.0F);
        }
        // temporary warp wearing off gets no message
        if (amount > 0 || kind != WarpData.Kind.TEMPORARY) {
            announce(server, kind, amount);
        }
    }

    private static void announce(ServerPlayer player, WarpData.Kind kind, int amount) {
        player.displayClientMessage(Component.translatable("warp.alchemia." + (amount > 0 ? "gained." : "lost.")
                + kind.name().toLowerCase(java.util.Locale.ROOT)).withStyle(net.minecraft.ChatFormatting.DARK_PURPLE),
                false);
    }

    // heard by this player only, as in the original
    private static void hear(ServerPlayer player, Holder<SoundEvent> sound, double x, double y, double z,
            float volume, float pitch) {
        player.connection.send(new ClientboundSoundPacket(sound, SoundSource.AMBIENT, x, y, z, volume, pitch,
                player.getRandom().nextLong()));
    }

    public static final int NO_MESSAGE = -1;
    private static final int CLARITY_ROLL = 76;

    public record Outcome(int message, Optional<Holder<MobEffect>> effect, int duration, int amplifier,
            Summons summons, boolean stubborn) {
        static Outcome whisper(int message) {
            return new Outcome(message, Optional.empty(), 0, 0, Summons.NOTHING, false);
        }

        static Outcome ail(int message, Holder<MobEffect> effect, int duration, int amplifier) {
            return new Outcome(message, Optional.of(effect), duration, amplifier, Summons.NOTHING, false);
        }

        // original: cleared of its curative items, so milk does not touch it
        static Outcome stubborn(int message, Holder<MobEffect> effect, int duration, int amplifier) {
            return new Outcome(message, Optional.of(effect), duration, amplifier, Summons.NOTHING, true);
        }

        static Outcome summon(int message, Summons summons) {
            return new Outcome(message, Optional.empty(), 0, 0, summons, false);
        }
    }

    public enum Summons {
        NOTHING,
        GUARDIAN,
        GUARDIANS,
        MANY_GUARDIANS,
        ILLUSORY_SPIDERS,
        SPIDERS,
        PORTAL
    }

    // original's outcome ladder: the roll picks the rung, warp sets how hard it bites
    public static Outcome outcomeFor(int warp, int roll) {
        int amp = Math.min(3, warp / 15);

        if (roll <= 8) {
            return Outcome.whisper(NO_MESSAGE);
        } else if (roll <= 12) {
            return Outcome.whisper(2);
        } else if (roll <= 16) {
            return Outcome.stubborn(3, ModEffects.FLUX_FLU, 5000, amp);
        } else if (roll <= 20) {
            return Outcome.stubborn(4, ModEffects.ALCHEDIARRHEA, Math.min(32000, 10 * warp), 0);
        } else if (roll <= 24) {
            return Outcome.stubborn(5, ModEffects.UNNATURAL_HUNGER, 5000, amp);
        } else if (roll <= 28) {
            return Outcome.whisper(6);
        } else if (roll <= 32) {
            return Outcome.summon(7, Summons.GUARDIAN);
        } else if (roll <= 36) {
            return Outcome.ail(NO_MESSAGE, ModEffects.BLURRED_VISION, Math.min(32000, 10 * warp), 0);
        } else if (roll <= 40) {
            return Outcome.stubborn(9, ModEffects.SUN_SCORNED, 5000, amp);
        } else if (roll <= 44) {
            return Outcome.ail(10, MobEffects.DIG_SLOWDOWN, 1200, amp);
        } else if (roll <= 48) {
            return Outcome.stubborn(11, ModEffects.FLUX_PHAGE, 6000, amp);
        } else if (roll <= 52) {
            return Outcome.ail(12, MobEffects.NIGHT_VISION, Math.min(40 * warp, 6000), 0);
        } else if (roll <= 56) {
            return Outcome.stubborn(13, ModEffects.DEADLY_GAZE, 6000, amp);
        } else if (roll <= 60) {
            return Outcome.summon(12, Summons.ILLUSORY_SPIDERS);
        } else if (roll <= 64) {
            return Outcome.whisper(13);
        } else if (roll <= 68) {
            return Outcome.summon(7, Summons.GUARDIANS);
        } else if (roll <= 72) {
            return Outcome.ail(NO_MESSAGE, MobEffects.BLINDNESS, Math.min(32000, 5 * warp), 0);
        } else if (roll == CLARITY_ROLL) {
            return Outcome.whisper(14);
        } else if (roll <= 80) {
            return Outcome.stubborn(5, ModEffects.UNNATURAL_HUNGER, 6000, amp);
        } else if (roll <= 88) {
            return Outcome.summon(15, Summons.PORTAL);
        } else if (roll <= 92) {
            return Outcome.summon(12, Summons.SPIDERS);
        }
        return Outcome.summon(7, Summons.MANY_GUARDIANS);
    }

    public static void inflict(ServerPlayer player, int warp, int roll) {
        Outcome outcome = outcomeFor(warp, roll);
        outcome.effect().ifPresent(effect -> {
            MobEffectInstance ailment = new MobEffectInstance(effect, outcome.duration(), outcome.amplifier(), true, true);
            if (outcome.stubborn()) {
                ailment.getCures().clear();
            }
            player.addEffect(ailment);
        });
        if (roll == CLARITY_ROLL && WarpData.of(player).sticky() > 0) {
            player.setData(ModAttachments.WARP, WarpData.of(player).eased());
            announce(player, WarpData.Kind.STICKY, -1);
        }
        // the two mildest are only sounds
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
            case PORTAL -> {
                if (!me.moonscenty.alchemia.entity.WarpSummons.portal(player)) {
                    return;
                }
            }
            default -> {
            }
        }
        whisper(player, outcome.message());
    }

    private static void whisper(ServerPlayer player, int line) {
        if (line == NO_MESSAGE) {
            return;
        }
        player.sendSystemMessage(Component.translatable("warp.alchemia.text." + Mth.clamp(line, 0, 15))
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
    }
}
