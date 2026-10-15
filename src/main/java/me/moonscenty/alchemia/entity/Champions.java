package me.moonscenty.alchemia.entity;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.AlchemiaConfig;
import me.moonscenty.alchemia.player.ModAttachments;
import me.moonscenty.alchemia.player.WarpData;
import me.moonscenty.alchemia.player.WarpHandler;
import me.moonscenty.alchemia.registry.ModSounds;
import me.moonscenty.alchemia.registry.ModTags;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * Champion mobs, as the original made them.
 *
 * <h3>Who becomes one</h3>
 * Every monster on the list ({@code alchemia:champion_candidates}) is rolled once, the first time it is in a world,
 * and never again. A roll of a hundred has to come out at nought or below: so one in a hundred on normal, three on
 * hard, none on easy, two more in the Nether, the End or somewhere spooky. Nothing with less than ten health is ever
 * made one. With champions turned off in the config the roll starts two higher, which leaves only those last few
 * places.
 *
 * <h3>What one is</h3>
 * A hundred more health (of which it gets twenty-five at once, not all), three times the bite, a name that says what
 * it is the champion of, and one of the thirteen {@link ChampionModifier modifiers}. It dies for five to seven more
 * experience than it would have, and a bag of loot: common or uncommon, and with looting now and then rare.
 *
 * <h3>Where this differs from the original</h3>
 * The original kept the roll in an attribute it gave every monster; here it is a number attached to the mob, saved
 * and sent to whoever can see it. An infested champion, struck, may burst a taint crawler out of itself.
 */
@EventBusSubscriber(modid = Alchemia.MODID)
public final class Champions {
    /** Never rolled. */
    public static final int UNROLLED = -2;
    /** Rolled, and nothing came of it. */
    public static final int ORDINARY = -1;

    private static final ResourceLocation HEALTH = Alchemia.id("champion_health");
    private static final ResourceLocation DAMAGE = Alchemia.id("champion_damage");
    private static final ResourceLocation BOLD = Alchemia.id("champion_bold");
    private static final ResourceLocation MIGHTY = Alchemia.id("champion_mighty");
    private static final ResourceLocation WARDED = Alchemia.id("champion_warded");

    private Champions() {
    }

    public static int of(Entity entity) {
        return entity.getData(ModAttachments.CHAMPION);
    }

    /** What it is the champion of, or null if it is no champion. */
    public static ChampionModifier modifier(Entity entity) {
        return ChampionModifier.of(of(entity));
    }

    public static boolean isChampion(Entity entity) {
        return of(entity) >= 0;
    }

    // --- the roll --------------------------------------------------------------

    @SubscribeEvent
    public static void joined(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide || !(event.getEntity() instanceof Monster mob) || of(mob) != UNROLLED) {
            return;
        }
        if (mob instanceof me.moonscenty.alchemia.entity.boss.ThaumcraftBoss boss) {
            makeBoss(boss);
            return;
        }
        roll(mob);
    }

    private static void roll(Monster mob) {
        boolean on = AlchemiaConfig.CHAMPION_MOBS.get();
        int roll = mob.getRandom().nextInt(100);
        Difficulty difficulty = mob.level().getDifficulty();
        if (difficulty == Difficulty.EASY || !on) {
            roll += 2;
        }
        if (difficulty == Difficulty.HARD && on) {
            roll -= 2;
        }
        Holder<Biome> biome = mob.level().getBiome(mob.blockPosition());
        if (biome.is(BiomeTags.IS_NETHER) || biome.is(BiomeTags.IS_END) || biome.is(Tags.Biomes.IS_SPOOKY)) {
            roll -= on ? 2 : 1;
        }
        if (mob.getType().is(ModTags.Entities.CHAMPION_CANDIDATES) && roll <= 0
                && mob.getAttribute(Attributes.MAX_HEALTH).getBaseValue() >= 10.0) {
            make(mob, ChampionModifier.values()[mob.getRandom().nextInt(ChampionModifier.values().length)]);
        } else {
            mob.setData(ModAttachments.CHAMPION, ORDINARY);
        }
    }

    /** A boss is a champion of something always, and gets only what that kind of champion adds. */
    public static void makeBoss(me.moonscenty.alchemia.entity.boss.ThaumcraftBoss boss) {
        ChampionModifier modifier = ChampionModifier.values()[boss.getRandom().nextInt(ChampionModifier.values().length)];
        boss.setData(ModAttachments.CHAMPION, modifier.ordinal());
        switch (modifier) {
            case BOLD -> add(boss, Attributes.MOVEMENT_SPEED, BOLD, 0.3, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
            case MIGHTY -> add(boss, Attributes.ATTACK_DAMAGE, MIGHTY, 2.0,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            case WARDED -> {
                int ward = ward(boss);
                add(boss, Attributes.MAX_ABSORPTION, WARDED, ward, AttributeModifier.Operation.ADD_VALUE);
                boss.setAbsorptionAmount(boss.getAbsorptionAmount() + ward);
            }
            default -> {
            }
        }
        boss.name();
    }

    /** Makes it a champion of something, by chance. */
    public static void makeRandom(Monster mob) {
        make(mob, ChampionModifier.values()[mob.getRandom().nextInt(ChampionModifier.values().length)]);
    }

    /** Makes it the champion of this, whatever it was before. */
    public static void make(Monster mob, ChampionModifier modifier) {
        if (mob instanceof net.minecraft.world.entity.monster.Creeper) {
            // a creeper is only ever bold
            modifier = ChampionModifier.BOLD;
        }
        mob.setData(ModAttachments.CHAMPION, modifier.ordinal());
        add(mob, Attributes.MAX_HEALTH, HEALTH, 100.0, AttributeModifier.Operation.ADD_VALUE);
        add(mob, Attributes.ATTACK_DAMAGE, DAMAGE, 2.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        mob.heal(25.0F);
        mob.setCustomName(Component.translatable(modifier.translationKey()).append(" ").append(mob.getName()));
        switch (modifier) {
            case BOLD -> add(mob, Attributes.MOVEMENT_SPEED, BOLD, 0.3, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
            case MIGHTY -> add(mob, Attributes.ATTACK_DAMAGE, MIGHTY, 2.0,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            case WARDED -> {
                int ward = ward(mob);
                add(mob, Attributes.MAX_ABSORPTION, WARDED, ward, AttributeModifier.Operation.ADD_VALUE);
                mob.setAbsorptionAmount(mob.getAbsorptionAmount() + ward);
            }
            default -> {
            }
        }
    }

    private static void add(LivingEntity mob, Holder<Attribute> attribute, ResourceLocation id, double amount,
            AttributeModifier.Operation operation) {
        AttributeInstance instance = mob.getAttribute(attribute);
        if (instance != null) {
            instance.addOrReplacePermanentModifier(new AttributeModifier(id, amount, operation));
        }
    }

    /** How much a warded champion's ward holds: half its health before it was made one. */
    private static int ward(LivingEntity mob) {
        return (int) mob.getAttribute(Attributes.MAX_HEALTH).getBaseValue() / 2;
    }

    // --- what it does ----------------------------------------------------------

    @SubscribeEvent
    public static void ticked(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Monster mob) || mob.level().isClientSide || mob.isDeadOrDying()) {
            return;
        }
        ChampionModifier modifier = modifier(mob);
        if (modifier == ChampionModifier.WARDED) {
            // a point back every second and a quarter while it has not been hit
            if (mob.invulnerableTime <= 0 && mob.tickCount % 25 == 0 && mob.getAbsorptionAmount() < ward(mob)) {
                mob.setAbsorptionAmount(mob.getAbsorptionAmount() + 1.0F);
            }
        } else if (modifier == ChampionModifier.UNDYING && mob.tickCount % 20 == 0) {
            mob.heal(1.0F);
        }
    }

    @SubscribeEvent
    public static void hurt(LivingIncomingDamageEvent event) {
        LivingEntity hurt = event.getEntity();
        DamageSource source = event.getSource();
        if (hurt.level().isClientSide) {
            return;
        }
        ChampionModifier defending = hurt instanceof Monster ? modifier(hurt) : null;
        if (defending != null) {
            if (defending == ChampionModifier.WARDED && hurt.getAbsorptionAmount() > 0.0F) {
                hurt.level().playSound(null, hurt.getX(), hurt.getY(), hurt.getZ(),
                        ModSounds.RUNIC_SHIELD_EFFECT.get(), SoundSource.HOSTILE, 0.66F,
                        1.1F + hurt.getRandom().nextFloat() * 0.1F);
            } else if (defending.when() == ChampionModifier.When.HURT
                    && source.getEntity() instanceof LivingEntity attacker) {
                event.setAmount(defend(defending, hurt, attacker, source, event.getAmount()));
            }
        }
        if (event.getAmount() > 0.0F && source.getEntity() instanceof Monster champion) {
            ChampionModifier attacking = modifier(champion);
            if (attacking != null && attacking.when() == ChampionModifier.When.HITS) {
                event.setAmount(attack(attacking, champion, hurt, event.getAmount()));
            }
        }
    }

    /** What a champion does to what hurts it. */
    private static float defend(ChampionModifier modifier, LivingEntity champion, LivingEntity attacker,
            DamageSource source, float amount) {
        switch (modifier) {
            case SPINED -> {
                if (!source.is(DamageTypes.THORNS)) {
                    attacker.hurt(champion.damageSources().thorns(champion), 1 + champion.getRandom().nextInt(3));
                    champion.level().playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(),
                            SoundEvents.THORNS_HIT, SoundSource.HOSTILE, 0.5F, 1.0F);
                }
            }
            case ARMORED -> {
                if (!source.is(DamageTypeTags.BYPASSES_ARMOR)) {
                    return amount * 19.0F / 25.0F;
                }
            }
            case INFESTED -> {
                if (champion.getRandom().nextFloat() < 0.4F && champion.level() instanceof ServerLevel served) {
                    TaintCrawler.spawnAt(served, champion.getX(), champion.getY() + champion.getBbHeight() / 2.0F,
                            champion.getZ());
                    served.playSound(null, champion.getX(), champion.getY(), champion.getZ(),
                            me.moonscenty.alchemia.registry.ModSounds.GORE.get(), SoundSource.HOSTILE, 0.5F, 1.0F);
                }
            }
            default -> {
            }
        }
        return amount;
    }

    /** What a champion does to what it hurts. */
    private static float attack(ChampionModifier modifier, Monster champion, LivingEntity target, float amount) {
        var random = champion.getRandom();
        switch (modifier) {
            case GRIM -> {
                if (random.nextFloat() < 0.4F) {
                    target.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 0), champion);
                }
            }
            case WARPED -> {
                if (random.nextFloat() < 0.33F && target instanceof ServerPlayer player) {
                    WarpHandler.add(player, WarpData.Kind.TEMPORARY, 1 + random.nextInt(3));
                }
            }
            case FIERY -> {
                if (random.nextFloat() < 0.4F) {
                    target.igniteForSeconds(4.0F);
                }
            }
            case SICKLY -> {
                if (random.nextFloat() < 0.4F) {
                    target.addEffect(new MobEffectInstance(MobEffects.HUNGER, 500, 0), champion);
                }
            }
            case VENOMOUS -> {
                if (random.nextFloat() < 0.4F) {
                    target.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0), champion);
                }
            }
            case VAMPIRIC -> champion.heal(Math.max(2.0F, amount / 2.0F));
            default -> {
            }
        }
        return amount;
    }

    // --- what it leaves ------------------------------------------------------

    @SubscribeEvent
    public static void died(LivingDropsEvent event) {
        LivingEntity dead = event.getEntity();
        if (!(dead.level() instanceof ServerLevel level) || !(dead instanceof Monster) || !isChampion(dead)
                || !event.isRecentlyHit() || event.getSource().getEntity() instanceof FakePlayer) {
            return;
        }
        ExperienceOrb.award(level, dead.position(), 5 + dead.getRandom().nextInt(3));
        int looting = 0;
        if (event.getSource().getEntity() instanceof LivingEntity killer) {
            looting = net.minecraft.world.item.enchantment.EnchantmentHelper.getEnchantmentLevel(level.registryAccess()
                    .registryOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                    .getHolderOrThrow(net.minecraft.world.item.enchantment.Enchantments.LOOTING), killer);
        }
        event.getDrops().add(new net.minecraft.world.entity.item.ItemEntity(level, dead.getX(), dead.getEyeY(),
                dead.getZ(), me.moonscenty.alchemia.item.LootBagItem.forChampion(dead.getRandom(), looting)));
    }
}
