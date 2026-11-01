package me.moonscenty.alchemia.entity;

import me.moonscenty.alchemia.registry.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;

public class AngryZombie extends Zombie {
    public AngryZombie(EntityType<? extends AngryZombie> type, Level level) {
        super(type, level);
    }

    public AngryZombie(Level level) {
        this(ModEntities.ANGRY_ZOMBIE.get(), level);
    }

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

    // the original predates drowned conversion
    @Override
    protected boolean convertsInWater() {
        return false;
    }
}
