package me.moonscenty.alchemia.entity.cult;

import me.moonscenty.alchemia.item.CrimsonRitesItem;
import me.moonscenty.alchemia.player.PlayerKnowledge;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.util.FakePlayer;

/**
 * A member of the Crimson Cult: the knight or the cleric. Never anything by itself.
 *
 * <h3>What they share</h3>
 * They are the size of a player, walk a little faster than one and open doors. They never turn on each other, and
 * one that is hurt calls every other cultist within sixty-four blocks to it. They are not out after dark of their own
 * accord -- nothing calls them up but a portal or an altar.
 *
 * <h3>What they leave</h3>
 * One in five leaves a void seed; otherwise a coin, two times in ten and once more for each level of looting. Killed
 * by a player, now and then a knowledge fragment, and the cult's book: one time in four until the killer has read it,
 * one in fifty after, one in a hundred while they carry a copy. Each piece they wear falls one time in twenty.
 */
public abstract class Cultist extends Monster {
    /** How far a hurt cultist's cry carries, out and up. The original's: twice its following range, and ten. */
    private static final double CALLS_ACROSS = 64.0;
    private static final double CALLS_UP = 10.0;

    protected Cultist(EntityType<? extends Cultist> type, Level level) {
        super(type, level);
        xpReward = 10;
        if (getNavigation() instanceof GroundPathNavigation ground) {
            ground.setCanOpenDoors(true);
        }
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            setDropChance(slot, 0.05F);
        }
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 4.0);
    }

    // --- one of us -----------------------------------------------------------

    @Override
    public boolean isAlliedTo(Entity other) {
        return other instanceof Cultist || super.isAlliedTo(other);
    }

    @Override
    public boolean canPickUpLoot() {
        return false;
    }

    // --- dressing ------------------------------------------------------------

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
            SpawnGroupData data) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data);
        dress(level, difficulty);
        return result;
    }

    /** What this kind of cultist goes about in. The game's own random armour is not theirs, so it is never rolled. */
    protected abstract void dress(ServerLevelAccessor level, DifficultyInstance difficulty);

    @Override
    protected void populateDefaultEquipmentSlots(net.minecraft.util.RandomSource random,
            DifficultyInstance difficulty) {
    }

    @Override
    protected void populateDefaultEquipmentEnchantments(ServerLevelAccessor level,
            net.minecraft.util.RandomSource random, DifficultyInstance difficulty) {
    }

    // --- what they leave -----------------------------------------------------

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        int looting = 0;
        if (source.getEntity() instanceof LivingEntity killer) {
            looting = EnchantmentHelper.getEnchantmentLevel(level.registryAccess()
                    .registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.LOOTING), killer);
        }
        int roll = random.nextInt(10);
        if (roll <= 1) {
            spawnAtLocation(new ItemStack(ModItems.VOID_SEED.get()), 1.5F);
        } else if (roll <= 3 + looting) {
            spawnAtLocation(new ItemStack(ModItems.COIN.get()), 1.5F);
        }
        if (recentlyHit && random.nextFloat() < 0.025F + looting * 0.01F) {
            spawnAtLocation(new ItemStack(ModItems.KNOWLEDGE_FRAGMENT.get()), 1.0F);
        }
        if (source.getEntity() instanceof Player killer && !(killer instanceof FakePlayer)) {
            int odds = PlayerKnowledge.of(killer).hasResearch(CrimsonRitesItem.RESEARCH) ? 50 : 4;
            if (killer.getInventory().contains(new ItemStack(ModItems.CRIMSON_RITES.get()))) {
                odds = 100;
            }
            if (random.nextInt(odds) == 0) {
                spawnAtLocation(new ItemStack(ModItems.CRIMSON_RITES.get()), getEyeHeight());
            }
        }
    }

    // --- a summons -----------------------------------------------------------

    /** The red puff a cultist arrives in. */
    @Override
    public void spawnAnim() {
        if (level().isClientSide) {
            me.moonscenty.alchemia.client.CultEffects.arrive(this);
        } else {
            level().broadcastEntityEvent(this, (byte) 20);
        }
    }

    // --- where it keeps to ---------------------------------------------------

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (hasRestriction()) {
            BlockPos home = getRestrictCenter();
            tag.putInt("HomeD", (int) getRestrictRadius());
            tag.putInt("HomeX", home.getX());
            tag.putInt("HomeY", home.getY());
            tag.putInt("HomeZ", home.getZ());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("HomeD")) {
            restrictTo(new BlockPos(tag.getInt("HomeX"), tag.getInt("HomeY"), tag.getInt("HomeZ")),
                    tag.getInt("HomeD"));
        }
    }

    /**
     * Turns on whatever hurt it, and calls every cultist in reach that has nothing better to do to turn on it too.
     */
    protected static class CallForHelp extends TargetGoal {
        private int lastHurtAt = Integer.MIN_VALUE;

        public CallForHelp(Cultist cultist) {
            super(cultist, false);
            setFlags(java.util.EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            LivingEntity attacker = mob.getLastHurtByMob();
            return mob.getLastHurtByMobTimestamp() != lastHurtAt && attacker != null
                    && canAttack(attacker, TargetingConditions.forCombat().ignoreLineOfSight());
        }

        @Override
        public void start() {
            LivingEntity attacker = mob.getLastHurtByMob();
            mob.setTarget(attacker);
            targetMob = attacker;
            lastHurtAt = mob.getLastHurtByMobTimestamp();
            unseenMemoryTicks = 300;
            AABB reach = new AABB(mob.blockPosition()).inflate(CALLS_ACROSS, CALLS_UP, CALLS_ACROSS);
            for (Cultist other : mob.level().getEntitiesOfClass(Cultist.class, reach)) {
                if (other != mob && other.getTarget() == null && !other.isAlliedTo(attacker)) {
                    other.setTarget(attacker);
                }
            }
            super.start();
        }
    }
}
