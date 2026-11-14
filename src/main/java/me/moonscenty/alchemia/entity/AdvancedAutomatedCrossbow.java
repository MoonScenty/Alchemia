package me.moonscenty.alchemia.entity;

import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class AdvancedAutomatedCrossbow extends AutomatedCrossbow {
    private static final double LIVES = 40.0;
    private static final int PLATING = 8;

    public AdvancedAutomatedCrossbow(EntityType<? extends AdvancedAutomatedCrossbow> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return AutomatedCrossbow.attributes().add(Attributes.MAX_HEALTH, LIVES);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new ArrowAttackGoal(this, 0.0, 20, reloadsWithin(), 24.0F));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 5, true, false,
                this::worthShooting));
        mind(MONSTERS, true);
    }

    @Override
    protected int reloadsWithin() {
        return 40;
    }

    @Override
    protected ItemStack folded() {
        return new ItemStack(ModItems.ADVANCED_AUTOMATED_CROSSBOW.get());
    }

    @Override
    public int getArmorValue() {
        return PLATING;
    }

    @Override
    protected double slides() {
        return 0.5;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            forgetStrangers();
        }
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return mindsKind(target) && super.canAttack(target);
    }

    @Override
    protected void salvage() {
        salvage(ModItems.BIOTHAUMIC_MIND.get(), 0.2F);
        salvage(ModItems.BRASS_GEAR.get(), 0.5F);
        salvage(ModBlocks.GREATWOOD.planks().get(), 0.5F);
        salvage(ModBlocks.GREATWOOD.planks().get(), 0.5F);
        salvage(ModItems.BRASS_PLATE.get(), 0.3F);
        salvage(ModItems.IRON_PLATE.get(), 0.4F);
        salvage(ModItems.IRON_PLATE.get(), 0.4F);
    }
}
