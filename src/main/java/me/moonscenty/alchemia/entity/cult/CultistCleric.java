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
 * <h3>At an altar</h3>
 * The clerics an altar calls up stand about it in its rite, floating a little off the ground with a dark thread
 * running from each to the altar, still and chanting. Any hurt at all breaks a cleric's rite, as does wandering more
 * than four blocks from the altar or the altar going; so does being called on for help often enough -- five times, or
 * one time in three each time.
 */
public class CultistCleric extends Cultist implements RangedAttackMob {
    private static final net.minecraft.network.syncher.EntityDataAccessor<Byte> FLAGS =
            net.minecraft.network.syncher.SynchedEntityData.defineId(CultistCleric.class,
                    net.minecraft.network.syncher.EntityDataSerializers.BYTE);
    private static final net.minecraft.network.syncher.EntityDataAccessor<java.util.Optional<net.minecraft.core.BlockPos>> ALTAR =
            net.minecraft.network.syncher.SynchedEntityData.defineId(CultistCleric.class,
                    net.minecraft.network.syncher.EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final int RITUAL = 1;
    /** How often it has been called on for help while in its rite. */
    public int rage;
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
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FLAGS, (byte) 0);
        builder.define(ALTAR, java.util.Optional.empty());
    }

    // --- its rite ------------------------------------------------------------

    public boolean isRitualist() {
        return (entityData.get(FLAGS) & RITUAL) != 0;
    }

    public void setRitualist(boolean ritualist) {
        byte flags = entityData.get(FLAGS);
        entityData.set(FLAGS, (byte) (ritualist ? flags | RITUAL : flags & ~RITUAL));
    }

    /** The altar it attends, if it is in a rite. */
    public java.util.Optional<net.minecraft.core.BlockPos> altar() {
        return entityData.get(ALTAR);
    }

    /** Sets it to stand in the rite of the altar there. */
    public void attendAltar(net.minecraft.core.BlockPos altar) {
        entityData.set(ALTAR, java.util.Optional.of(altar));
        setRitualist(true);
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        if (isInvulnerableTo(source)) {
            return false;
        }
        setRitualist(false);
        return super.hurt(source, amount);
    }

    @Override
    public void tick() {
        super.tick();
        if (!isRitualist()) {
            return;
        }
        if (!level().isClientSide) {
            if (rage >= 5) {
                setRitualist(false);
            }
            return;
        }
        // in its rite it looks to the altar
        altar().ifPresent(at -> getLookControl().setLookAt(at.getX() + 0.5, at.getY() + 1.5, at.getZ() + 0.5,
                40.0F, 10.0F));
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return !isRitualist() && super.removeWhenFarAway(distance);
    }

    /** One more call for help in the rite; one time in three, or the fifth, it breaks off and goes. */
    public void calledOn(net.minecraft.world.entity.LivingEntity attacker) {
        rage++;
        level().broadcastEntityEvent(this, (byte) 19);
        if (random.nextInt(3) == 0) {
            setRitualist(false);
            setTarget(attacker);
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 19) {
            for (int i = 0; i < 3; i++) {
                level().addParticle(net.minecraft.core.particles.ParticleTypes.ANGRY_VILLAGER, getRandomX(1.0),
                        getY() + 0.5 + random.nextDouble() * getBbHeight(), getRandomZ(1.0),
                        random.nextGaussian() * 0.02, random.nextGaussian() * 0.02, random.nextGaussian() * 0.02);
            }
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("Flags", entityData.get(FLAGS));
        altar().ifPresent(at -> tag.putLong("Altar", at.asLong()));
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(FLAGS, tag.getByte("Flags"));
        if (tag.contains("Altar")) {
            entityData.set(ALTAR, java.util.Optional.of(net.minecraft.core.BlockPos.of(tag.getLong("Altar"))));
        }
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new AltarFocus(this));
        goalSelector.addGoal(2, new Casting(this));
        goalSelector.addGoal(3, new me.moonscenty.alchemia.entity.QuickMeleeGoal(this, 1.0, false));
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

    /**
     * Stands still in its rite, doing nothing else; and every two seconds makes sure it still has a rite to be in --
     * that it is within four blocks of an altar still standing.
     */
    static class AltarFocus extends net.minecraft.world.entity.ai.goal.Goal {
        private final CultistCleric cleric;

        AltarFocus(CultistCleric cleric) {
            this.cleric = cleric;
            setFlags(java.util.EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            return cleric.isRitualist() && cleric.altar().isPresent();
        }

        @Override
        public void tick() {
            if (cleric.tickCount % 40 != 0) {
                return;
            }
            net.minecraft.core.BlockPos at = cleric.altar().orElse(null);
            if (at == null || at.distToCenterSqr(cleric.position()) > 16.0
                    || !(cleric.level().getBlockEntity(at)
                            instanceof me.moonscenty.alchemia.block.eldritch.EldritchAltarBlockEntity)) {
                cleric.setRitualist(false);
            }
        }
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
