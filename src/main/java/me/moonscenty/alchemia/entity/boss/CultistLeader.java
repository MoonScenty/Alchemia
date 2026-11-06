package me.moonscenty.alchemia.entity.boss;

import java.util.List;

import me.moonscenty.alchemia.entity.ChampionModifier;
import me.moonscenty.alchemia.entity.cult.Cultist;
import me.moonscenty.alchemia.entity.cult.GolemOrb;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;

public class CultistLeader extends ThaumcraftBoss implements RangedAttackMob {
    public static final List<String> TITLES = List.of("Alberic", "Anselm", "Bastian", "Beturian", "Chabier",
            "Chorache", "Chuse", "Dodorol", "Ebardo", "Ferrando", "Fertus", "Guillen", "Larpe", "Obano", "Zelipe");

    public CultistLeader(EntityType<? extends CultistLeader> type, Level level) {
        super(type, level);
        xpReward = 40;
    }

    public static AttributeSupplier.Builder attributes() {
        return bossAttributes()
                .add(Attributes.MAX_HEALTH, 150.0)
                .add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.ATTACK_DAMAGE, 5.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new LongRangeAttackGoal(this, 16.0, 1.0, 30, 40, 24.0F));
        goalSelector.addGoal(3, new me.moonscenty.alchemia.entity.QuickMeleeGoal(this, 1.1, false));
        goalSelector.addGoal(6, new MoveTowardsRestrictionGoal(this, 0.8));
        goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new CallsTheCult(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected List<String> titles() {
        return TITLES;
    }

    @Override
    public Component title(ChampionModifier modifier) {
        return Component.translatable("entity.alchemia.cultist_leader.titled", titled(),
                Component.translatable(modifier.translationKey()));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
            SpawnGroupData data) {
        setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.CRIMSON.get("crimson_praetor_helm").get()));
        setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.CRIMSON.get("crimson_praetor_chest").get()));
        setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.CRIMSON.get("crimson_praetor_legs").get()));
        setItemSlot(EquipmentSlot.FEET, new ItemStack(ModItems.CRIMSON.get("crimson_boots").get()));
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(level.getDifficulty() == Difficulty.EASY
                ? ModItems.METAL_TOOLS.get("void_sword").get() : ModItems.CRIMSON_BLADE.get()));
        float clamped = difficulty.getSpecialMultiplier();
        if (random.nextFloat() < 0.5F * clamped) {
            setItemSlot(EquipmentSlot.MAINHAND, net.minecraft.world.item.enchantment.EnchantmentHelper.enchantItem(
                    random, getMainHandItem(), (int) (7.0F + clamped * random.nextInt(22)),
                    level.registryAccess(), java.util.Optional.empty()));
        }
        return super.finalizeSpawn(level, difficulty, reason, data);
    }

    // only the cult: unlike the other bosses the original's praetor has no eldritch allies
    @Override
    public boolean isAlliedTo(Entity other) {
        return other instanceof Cultist || other instanceof CultistLeader || isAlliedTo(other.getTeam());
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !(target instanceof Cultist) && !(target instanceof CultistLeader) && super.canAttack(target);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        for (Cultist near : level().getEntitiesOfClass(Cultist.class, new AABB(blockPosition()).inflate(8.0))) {
            if (!near.hasEffect(MobEffects.REGENERATION)) {
                near.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 1));
            }
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        if (!getSensing().hasLineOfSight(target)) {
            return;
        }
        swing(InteractionHand.MAIN_HAND);
        getLookControl().setLookAt(target.getX(), target.getBoundingBox().minY + target.getBbHeight() / 2.0,
                target.getZ(), 30.0F, 30.0F);
        GolemOrb.lob(this, target);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack worn = getItemBySlot(slot);
            if (!worn.isEmpty() && random.nextFloat() < 0.085F) {
                spawnAtLocation(worn.copy());
            }
        }
        ItemEntity bag = spawnAtLocation(new ItemStack(ModItems.LOOT_BAG_RARE.get()), 1.5F);
        if (bag != null) {
            bag.setUnlimitedLifetime();
        }
    }

    private static class CallsTheCult extends HurtByTargetGoal {
        CallsTheCult(CultistLeader leader) {
            super(leader);
        }

        @Override
        public void start() {
            super.start();
            LivingEntity attacker = mob.getLastHurtByMob();
            if (attacker == null) {
                return;
            }
            Cultist.callTheCult(mob, attacker, mob.getAttributeValue(Attributes.FOLLOW_RANGE) * 2.0, 10.0);
        }
    }
}
