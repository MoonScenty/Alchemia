package me.moonscenty.alchemia.entity.cult;

import me.moonscenty.alchemia.registry.ModEntities;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.phys.Vec3;

/**
 * The cult's priest: 24 health, the crimson robe, no weapon, and chanting. Further off than two blocks it casts --
 * every one to two seconds, sooner the nearer its target -- either a red orb that follows what it was thrown at (one
 * time in three) or three small fireballs. Closer, it lays about with its hands like any knight.
 * <p>
 * The original's clerics also stood in a ring about an altar, floating and chanting into it. That comes with the
 * altar.
 */
public class CultistCleric extends Cultist implements RangedAttackMob {
    public CultistCleric(EntityType<? extends CultistCleric> type, Level level) {
        super(type, level);
    }

    public CultistCleric(Level level) {
        this(ModEntities.CULTIST_CLERIC.get(), level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Cultist.attributes().add(Attributes.MAX_HEALTH, 24.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new Casting(this));
        goalSelector.addGoal(3, new CultistKnight.QuickMelee(this));
        goalSelector.addGoal(5, new OpenDoorGoal(this, true));
        goalSelector.addGoal(6, new MoveTowardsRestrictionGoal(this, 0.8));
        goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new CallForHelp(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void dress(ServerLevelAccessor level, DifficultyInstance difficulty) {
        setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.CRIMSON.get("crimson_robe_helm").get()));
        setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.CRIMSON.get("crimson_robe_chest").get()));
        setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.CRIMSON.get("crimson_robe_legs").get()));
        if (random.nextFloat() < (level.getDifficulty() == Difficulty.HARD ? 0.3F : 0.1F)) {
            setItemSlot(EquipmentSlot.FEET, new ItemStack(ModItems.CRIMSON.get("crimson_boots").get()));
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distance) {
        double dx = target.getX() - getX();
        double dy = target.getBoundingBox().minY + target.getBbHeight() / 2.0F - (getY() + getBbHeight() / 2.0F);
        double dz = target.getZ() - getZ();
        swing(InteractionHand.MAIN_HAND);
        if (random.nextFloat() > 0.66F) {
            GolemOrb orb = new GolemOrb(level(), this, target, true);
            orb.setPos(orb.getX() + orb.getDeltaMovement().x / 2.0, orb.getY(),
                    orb.getZ() + orb.getDeltaMovement().z / 2.0);
            orb.shoot(dx, dy + 2.0, dz, 0.66F, 3.0F);
            playSound(ModSounds.EG_ATTACK.get(), 1.0F, 1.0F + random.nextFloat() * 0.1F);
            level().addFreshEntity(orb);
        } else {
            float spread = (float) Math.sqrt(distance) * 0.5F;
            level().levelEvent(null, LevelEvent.SOUND_BLAZE_FIREBALL, blockPosition(), 0);
            for (int i = 0; i < 3; i++) {
                SmallFireball fireball = new SmallFireball(level(), this, new Vec3(
                        dx + random.nextGaussian() * spread, dy, dz + random.nextGaussian() * spread).normalize());
                fireball.setPos(fireball.getX(), getY() + getBbHeight() / 2.0F + 0.5, fireball.getZ());
                level().addFreshEntity(fireball);
            }
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.CHANT.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 500;
    }

    /** Casts from further off than two blocks; nearer, the hands are quicker. */
    static class Casting extends RangedAttackGoal {
        private final CultistCleric cleric;

        Casting(CultistCleric cleric) {
            super(cleric, 1.0, 20, 40, 24.0F);
            this.cleric = cleric;
        }

        @Override
        public boolean canUse() {
            if (!super.canUse()) {
                return false;
            }
            LivingEntity target = cleric.getTarget();
            if (target == null || !target.isAlive()) {
                cleric.setTarget(null);
                return false;
            }
            return cleric.distanceToSqr(target.getX(), target.getBoundingBox().minY, target.getZ()) >= 4.0;
        }
    }
}
