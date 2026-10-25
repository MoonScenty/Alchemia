package me.moonscenty.alchemia.entity;

import me.moonscenty.alchemia.registry.ModEntities;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

/**
 * A rabbit the taint has made over. It still hops as a rabbit does, of whatever colour it was, but it goes for
 * players and animals now, and does not breed. A killer rabbit made over is harder still, and bites twice as hard.
 */
public class TaintedRabbit extends Rabbit implements TaintedMob {
    public TaintedRabbit(EntityType<? extends TaintedRabbit> type, Level level) {
        super(type, level);
    }

    public TaintedRabbit(Level level) {
        this(ModEntities.TAINTED_RABBIT.get(), level);
    }

    /** The original's numbers. */
    public static AttributeSupplier.Builder attributes() {
        return Rabbit.createAttributes()
                .add(Attributes.MAX_HEALTH, 15.0)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3);
    }

    /** None of a rabbit's own ways: only hunting. */
    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new QuickMeleeGoal(this, 1.4, false));
        goalSelector.addGoal(5, new RandomStrollGoal(this, 0.6));
        goalSelector.addGoal(11, new LookAtPlayerGoal(this, Player.class, 10.0F));
        targetSelector.addGoal(0, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        targetSelector.addGoal(8, new NearestAttackableTargetGoal<>(this, Animal.class, false));
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !TaintedMob.allied(target) && super.canAttack(target);
    }

    @Override
    public boolean isAlliedTo(Entity other) {
        return TaintedMob.allied(other) || super.isAlliedTo(other);
    }

    /** Armoured as a killer rabbit is, or a little less. */
    @Override
    public int getArmorValue() {
        return getVariant() == Variant.EVIL ? 8 : 3;
    }

    /** Twice as hard for a killer, and with the taint's poison in it. */
    @Override
    public boolean doHurtTarget(Entity target) {
        float damage = (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (getVariant() == Variant.EVIL) {
            damage *= 2.0F;
        }
        playSound(SoundEvents.SLIME_ATTACK, 1.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
        DamageSource source = damageSources().mobAttack(this);
        boolean hit = target.hurt(source, damage);
        if (hit) {
            TaintedMob.poison(this, target);
            if (level() instanceof ServerLevel served) {
                EnchantmentHelper.doPostAttackEffects(served, target, source);
            }
        }
        return hit;
    }

    @Override
    public Rabbit getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return null;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    /** A little goo, more with looting. */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        int looting = source.getEntity() instanceof LivingEntity killer
                ? EnchantmentHelper.getEnchantmentLevel(level.registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                        .getHolderOrThrow(Enchantments.LOOTING), killer)
                : 0;
        int goo = random.nextInt(2) + random.nextInt(1 + looting);
        for (int bit = 0; bit < goo; bit++) {
            spawnAtLocation(new ItemStack(ModItems.TAINTED_GOO.get()));
        }
    }
}
