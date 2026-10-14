package me.moonscenty.alchemia.item.bauble;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.network.ShieldRunes;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * The runic shield, as the original kept it: golden hearts that are not food's but the gear's.
 * <p>
 * Everything worn -- the four pieces of armour and every bauble -- is added up every five seconds, or at once when
 * a bauble goes on or comes off; that is the most the shield holds, and its hearts are the player's absorption. A
 * point of it comes back every two seconds, for a point each of air and earth out of the aura where the player
 * stands, and none while the aura is to be spared. Once it has been beaten down to nothing it waits four seconds
 * before it begins again. A charged ring brings each point a quarter second sooner.
 * <p>
 * When a blow beats the shield down: a kinetic girdle throws out a blast that breaks nothing, a revitalising ring
 * mends its wearer for twelve seconds, an emergency amulet puts eight points back at once -- each no oftener than
 * its own while. Whoever is near sees the runes flare where the blow landed.
 * <p>
 * The original timed all this by the clock on the wall; here it is in ticks. Its emergency amulet only played its
 * sound and forgot to give the eight points its research promised; here it gives them.
 */
@EventBusSubscriber(modid = Alchemia.MODID)
public final class RunicShield {
    /** How long between points, and how long after it breaks before it mends, in ticks. */
    public static final int RECHARGE = 40;
    public static final int CHARGED_RING_QUICKER = 10;
    public static final int BROKEN_WAIT = 80;
    private static final int RECOUNT = 100;
    private static final ResourceLocation HOLDS = Alchemia.id("runic_shield");

    /** What a shield's bauble adds to it, beyond its points. */
    public enum Upgrade {
        NONE, CHARGED, KINETIC, HEALING, EMERGENCY
    }

    /** One player's shield: what it holds, what it has, and when it may next do what. */
    private static final class State {
        int most;
        int charged;
        int kinetic;
        int healing;
        int emergency;
        int last;
        long next;
        boolean dirty = true;
        final long[] cooldown = new long[Upgrade.values().length];
    }

    private static final Map<UUID, State> STATES = new HashMap<>();

    private RunicShield() {
    }

    /** Recounts the player's shield on its next tick. */
    public static void markDirty(LivingEntity wearer) {
        if (wearer instanceof Player player && !player.level().isClientSide) {
            STATES.computeIfAbsent(player.getUUID(), id -> new State()).dirty = true;
        }
    }

    /** The most the player's shield holds, as last counted. */
    public static int most(Player player) {
        State state = STATES.get(player.getUUID());
        return state == null ? 0 : state.most;
    }

    @SubscribeEvent
    public static void loggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        STATES.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void ticked(PlayerTickEvent.Post event) {
        tick(event.getEntity());
    }

    /** One tick of a player's shield: counted again if it is time, and a point back if one is due. */
    public static void tick(Player player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        State state = STATES.computeIfAbsent(player.getUUID(), id -> new State());
        if (state.dirty || player.tickCount % RECOUNT == 0) {
            state.dirty = false;
            recount(player, state);
        }
        if (state.most <= 0) {
            return;
        }
        long now = level.getGameTime();
        int current = (int) player.getAbsorptionAmount();
        if (current == 0 && state.last > 0) {
            state.next = now + BROKEN_WAIT;
            state.last = 0;
        }
        if (current < state.most && state.next < now
                && !AuraHandler.shouldSpare(level, player.blockPosition(), ModAspects.AIR)
                && !AuraHandler.shouldSpare(level, player.blockPosition(), ModAspects.EARTH)
                && AuraHandler.get(level, player.blockPosition(), ModAspects.AIR) >= 1
                && AuraHandler.get(level, player.blockPosition(), ModAspects.EARTH) >= 1) {
            AuraHandler.drainAvailable(level, player.blockPosition(), ModAspects.AIR, 1);
            AuraHandler.drainAvailable(level, player.blockPosition(), ModAspects.EARTH, 1);
            state.next = now + Math.max(0, RECHARGE - state.charged * CHARGED_RING_QUICKER);
            player.setAbsorptionAmount(current + 1);
            state.last = current + 1;
        }
    }

    /** Adds up what is worn, and lets the player's hearts hold that much. */
    private static void recount(Player player, State state) {
        int most = 0;
        int charged = 0;
        int kinetic = 0;
        int healing = 0;
        int emergency = 0;
        for (EquipmentSlot slot : new EquipmentSlot[] {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
                EquipmentSlot.FEET}) {
            most += RunicGear.charge(player.getItemBySlot(slot));
        }
        var curios = CuriosApi.getCuriosInventory(player);
        if (curios.isPresent()) {
            var worn = curios.get().getEquippedCurios();
            for (int i = 0; i < worn.getSlots(); i++) {
                ItemStack bauble = worn.getStackInSlot(i);
                most += RunicGear.charge(bauble);
                if (bauble.getItem() instanceof RunicGear gear) {
                    switch (gear.upgrade(bauble)) {
                        case CHARGED -> charged++;
                        case KINETIC -> kinetic++;
                        case HEALING -> healing++;
                        case EMERGENCY -> emergency++;
                        default -> {
                        }
                    }
                }
            }
        }
        if (most < state.most) {
            player.setAbsorptionAmount(Math.max(0.0F, player.getAbsorptionAmount() - (state.most - most)));
        }
        state.most = most;
        state.charged = charged;
        state.kinetic = kinetic;
        state.healing = healing;
        state.emergency = emergency;
        AttributeInstance hearts = player.getAttribute(Attributes.MAX_ABSORPTION);
        if (hearts != null) {
            hearts.removeModifier(HOLDS);
            if (most > 0) {
                hearts.addTransientModifier(new AttributeModifier(HOLDS, most, AttributeModifier.Operation.ADD_VALUE));
            }
        }
    }

    /** A blow on the shield: the runes flare, and if it breaks, the upgrades answer. */
    @SubscribeEvent
    public static void struck(LivingIncomingDamageEvent event) {
        LivingEntity struck = event.getEntity();
        if (!(struck.level() instanceof ServerLevel level) || struck.getAbsorptionAmount() <= 0) {
            return;
        }
        int target = event.getSource().getEntity() != null ? event.getSource().getEntity().getId() : -1;
        if (event.getSource().is(DamageTypes.FALL)) {
            target = -2;
        }
        if (event.getSource().is(DamageTypes.FALLING_BLOCK) || event.getSource().is(DamageTypes.FALLING_ANVIL)) {
            target = -3;
        }
        if (!(struck instanceof Player player)) {
            // a warded champion or something of the Outer Lands behind its own shield
            if (struck instanceof net.minecraft.world.entity.monster.Monster && (me.moonscenty.alchemia.entity.Champions
                    .modifier(struck) == me.moonscenty.alchemia.entity.ChampionModifier.WARDED
                    || struck instanceof me.moonscenty.alchemia.entity.EldritchMob)) {
                PacketDistributor.sendToPlayersNear(level, null, struck.getX(), struck.getY(), struck.getZ(), 32.0,
                        new ShieldRunes(struck.getId(), target));
                level.playSound(null, struck.getX(), struck.getY(), struck.getZ(), ModSounds.RUNIC_SHIELD_EFFECT.get(),
                        SoundSource.HOSTILE, 0.66F, 1.1F + level.random.nextFloat() * 0.1F);
            }
            return;
        }
        State state = STATES.get(player.getUUID());
        if (state == null || state.most <= 0) {
            return;
        }
        PacketDistributor.sendToPlayersNear(level, null, player.getX(), player.getY(), player.getZ(), 32.0,
                new ShieldRunes(player.getId(), target));
        int charge = (int) player.getAbsorptionAmount();
        if (charge > event.getAmount()) {
            return;
        }
        long now = level.getGameTime();
        if (state.kinetic > 0 && ready(state, Upgrade.KINETIC, now, 400)) {
            level.explode(player, player.getX(), player.getY() + player.getBbHeight() / 2.0, player.getZ(),
                    1.5F + state.kinetic * 0.5F, false, Level.ExplosionInteraction.NONE);
        }
        if (state.healing > 0 && ready(state, Upgrade.HEALING, now, 400)) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 240, state.healing));
            level.playSound(null, player, ModSounds.RUNIC_SHIELD_EFFECT.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        if (state.emergency > 0 && ready(state, Upgrade.EMERGENCY, now, 1200)) {
            float restored = Math.min(state.most, 8 * state.emergency);
            // after the blow is taken, so the eight points stand against the next one
            level.getServer().execute(() -> player.setAbsorptionAmount(Math.max(player.getAbsorptionAmount(),
                    restored)));
            level.playSound(null, player, ModSounds.RUNIC_SHIELD_CHARGE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    /** What any runic gear says of itself: how much shield it holds. */
    @SubscribeEvent
    public static void tooltip(net.neoforged.neoforge.event.entity.player.ItemTooltipEvent event) {
        int charge = RunicGear.charge(event.getItemStack());
        if (charge > 0) {
            event.getToolTip().add(net.minecraft.network.chat.Component.translatable("item.alchemia.runic_charge", charge)
                    .withStyle(net.minecraft.ChatFormatting.GOLD));
        }
    }

    private static boolean ready(State state, Upgrade upgrade, long now, int cooldown) {
        if (state.cooldown[upgrade.ordinal()] >= now) {
            return false;
        }
        state.cooldown[upgrade.ordinal()] = now + cooldown;
        return true;
    }
}
