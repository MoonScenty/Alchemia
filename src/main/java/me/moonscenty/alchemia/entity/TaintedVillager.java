package me.moonscenty.alchemia.entity;

import me.moonscenty.alchemia.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A villager the taint has made over. It still keeps to doors and houses, and now and then has a coin about it.
 */
public class TaintedVillager extends TaintedBeast {
    public TaintedVillager(EntityType<? extends TaintedVillager> type, Level level) {
        super(type, level);
    }

    public TaintedVillager(Level level) {
        this(ModEntities.TAINTED_VILLAGER.get(), level);
    }

    /** The original's numbers. */
    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ARMOR, 0.0);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.VILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.VILLAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.VILLAGER_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(SoundEvents.ZOMBIE_VILLAGER_STEP, 0.15F, 1.0F);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        spawnAtLocation(TaintedMob.gooOrTendril(random, 1.0F / 3.0F), getBbHeight() / 2.0F);
        int looting = source.getEntity() instanceof net.minecraft.world.entity.LivingEntity killer
                ? net.minecraft.world.item.enchantment.EnchantmentHelper.getEnchantmentLevel(level.registryAccess()
                        .registryOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                        .getHolderOrThrow(net.minecraft.world.item.enchantment.Enchantments.LOOTING), killer)
                : 0;
        if (random.nextInt(8) < 1 + looting) {
            spawnAtLocation(new net.minecraft.world.item.ItemStack(me.moonscenty.alchemia.registry.ModItems.COIN.get()),
                    1.5F);
        }
    }

    /** It keeps to houses as it did: through doors, home again, looking at people. It hunts no animals. */
    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(4, new net.minecraft.world.entity.ai.goal.OpenDoorGoal(this, true));
        goalSelector.addGoal(5, new net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal(this, 1.0));
        goalSelector.addGoal(9, new net.minecraft.world.entity.ai.goal.LookAtPlayerGoal(this,
                net.minecraft.world.entity.npc.AbstractVillager.class, 5.0F, 0.02F));
        goalSelector.addGoal(10, new net.minecraft.world.entity.ai.goal.LookAtPlayerGoal(this,
                net.minecraft.world.entity.LivingEntity.class, 8.0F));
    }

    @Override
    protected boolean huntsAnimals() {
        return false;
    }

    @Override
    protected boolean huntsVillagers() {
        return false;
    }
}
