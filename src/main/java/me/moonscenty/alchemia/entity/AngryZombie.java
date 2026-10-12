package me.moonscenty.alchemia.entity;

import me.moonscenty.alchemia.registry.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;

/**
 * A zombie that still has its brain, and is angrier for it. The original's was a zombie with more health, a harder
 * bite and a little more hide, which turned on whatever hurt it, and everything else was the zombie's own.
 * <p>
 * What it drops is its loot table: flesh, and its brain more often than not.
 */
public class AngryZombie extends Zombie {
    public AngryZombie(EntityType<? extends AngryZombie> type, Level level) {
        super(type, level);
    }

    public AngryZombie(Level level) {
        this(ModEntities.ANGRY_ZOMBIE.get(), level);
    }

    /** A zombie's, with 25 health, a bite of 5 and three more points of armour than a zombie's two. */
    public static AttributeSupplier.Builder attributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 25.0)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.ARMOR, 5.0)
                .add(Attributes.SPAWN_REINFORCEMENTS_CHANCE, 0.0);
    }

    @Override
    protected void addBehaviourGoals() {
        super.addBehaviourGoals();
        // whatever hurt it, without calling the others in
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    /** The original was from before zombies drowned into something else; it stays what it is. */
    @Override
    protected boolean convertsInWater() {
        return false;
    }
}
