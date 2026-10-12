package me.moonscenty.alchemia.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * An animal the taint has made over: the same shape, but a monster now, going for players, villagers and whatever
 * animals have not been made over yet. It comes into the world in a burst of taint, and its bite can carry taint
 * poison.
 *
 * <p>The tainted cow, pig, chicken, sheep and villager are all this, as the original's were all one pattern; each
 * says how big and hard it is, what it sounds like and what it leaves.
 */
public abstract class TaintedBeast extends Monster implements TaintedMob {
    protected TaintedBeast(EntityType<? extends TaintedBeast> type, Level level) {
        super(type, level);
        if (getNavigation() instanceof GroundPathNavigation ground) {
            ground.setCanOpenDoors(true);
        }
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        goalSelector.addGoal(5, new RandomStrollGoal(this, 1.0));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(0, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        if (huntsVillagers()) {
            targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        }
        if (huntsAnimals()) {
            targetSelector.addGoal(8, new NearestAttackableTargetGoal<>(this, Animal.class, false));
        }
    }

    /** Whether it goes after villagers, as all but a made-over villager do. */
    protected boolean huntsVillagers() {
        return true;
    }

    /** Whether it goes after animals that are not yet made over, as most of them do. */
    protected boolean huntsAnimals() {
        return true;
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !TaintedMob.allied(target) && super.canAttack(target);
    }

    @Override
    public boolean isAlliedTo(Entity other) {
        return TaintedMob.allied(other) || super.isAlliedTo(other);
    }

    /** It comes into the world in a burst of taint. */
    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide && tickCount < 5) {
            me.moonscenty.alchemia.client.TaintEffects.sploosh(this);
        }
    }

    @Override
    public boolean doHurtTarget(Entity victim) {
        if (!super.doHurtTarget(victim)) {
            return false;
        }
        TaintedMob.poison(this, victim);
        return true;
    }

    @Override
    public float getVoicePitch() {
        return 0.7F;
    }
}
