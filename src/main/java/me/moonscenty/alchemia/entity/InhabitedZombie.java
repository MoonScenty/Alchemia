package me.moonscenty.alchemia.entity;

import me.moonscenty.alchemia.entity.cult.Cultist;
import me.moonscenty.alchemia.registry.ModEntities;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;

/**
 * A shambling husk: a dead man in crimson plate with an eldritch crab living in him. A zombie otherwise -- a tougher
 * one, at 30 health and a bite of 5 -- that talks like a crab.
 * <p>
 * It always has the cult's helm on and, more often than not, the chestplate and greaves. When it dies none of it is
 * left: the body bursts and the crab comes out of it in its shell. Out of the Outer Lands, where one walks now and then
 * and never within thirty-two blocks of another, only an egg brings one.
 */
public class InhabitedZombie extends Zombie implements EldritchMob {
    public InhabitedZombie(EntityType<? extends InhabitedZombie> type, Level level) {
        super(type, level);
    }

    public InhabitedZombie(Level level) {
        this(ModEntities.INHABITED_ZOMBIE.get(), level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.SPAWN_REINFORCEMENTS_CHANCE, 0.0);
    }

    /** Somewhere one may walk: dark, as for any monster, and no other husk anywhere near. */
    public static boolean checkSpawnRules(EntityType<InhabitedZombie> type, ServerLevelAccessor level,
            MobSpawnType reason, BlockPos pos, RandomSource random) {
        return level.getEntitiesOfClass(InhabitedZombie.class, new AABB(pos).inflate(32.0, 16.0, 32.0)).isEmpty()
                && Monster.checkMonsterSpawnRules(type, level, reason, pos, random);
    }

    @Override
    protected void addBehaviourGoals() {
        super.addBehaviourGoals();
        targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Cultist.class, true));
    }

    @Override
    protected boolean convertsInWater() {
        return false;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
            SpawnGroupData data) {
        float chance = level.getDifficulty() == Difficulty.HARD ? 0.9F : 0.6F;
        setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.CRIMSON.get("crimson_plate_helm").get()));
        if (random.nextFloat() <= chance) {
            setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.CRIMSON.get("crimson_plate_chest").get()));
        }
        if (random.nextFloat() <= chance) {
            setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.CRIMSON.get("crimson_plate_legs").get()));
        }
        return super.finalizeSpawn(level, difficulty, reason, data);
    }

    /** Nothing it wore or carried is left: it bursts. */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
    }

    /** No slow fall: the body bursts at once and the crab in it comes out shelled. */
    @Override
    protected void tickDeath() {
        if (!level().isClientSide && !isRemoved()) {
            EldritchCrab crab = new EldritchCrab(level());
            crab.moveTo(getX(), getEyeY(), getZ(), getYRot(), getXRot());
            crab.setHelm(true);
            level().addFreshEntity(crab);
            ((ServerLevel) level()).sendParticles(ParticleTypes.POOF, getX(), getY() + getBbHeight() / 2.0, getZ(),
                    20, getBbWidth() / 2.0, getBbHeight() / 2.0, getBbWidth() / 2.0, 0.02);
            remove(Entity.RemovalReason.KILLED);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.CRAB_TALK.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.HOSTILE_HURT;
    }
}
