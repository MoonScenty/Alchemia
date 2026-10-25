package me.moonscenty.alchemia.entity;

import java.util.Objects;

import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * The automated crossbow plated in iron with a bioalchemic mind, the original's
 * {@code EntityTurretCrossbowAdvanced}: sturdier, quicker to reload, harder to shove about, and it can be told what to
 * shoot at as an autocaster can -- animals, monsters, people, and whether that means its owner's own.
 */
public class AdvancedAutomatedCrossbow extends AutomatedCrossbow {
    /** The four things it may be told to mind, one to a bit, numbered as the autocaster's. */
    private static final EntityDataAccessor<Byte> MINDS =
            SynchedEntityData.defineId(AdvancedAutomatedCrossbow.class, EntityDataSerializers.BYTE);

    private static final double LIVES = 40.0;
    private static final int PLATING = 8;

    public AdvancedAutomatedCrossbow(EntityType<? extends AdvancedAutomatedCrossbow> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return AutomatedCrossbow.attributes().add(Attributes.MAX_HEALTH, LIVES);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(MINDS, (byte) 0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new RangedAttackGoal(this, 0.0, 20, reloadsWithin(), 24.0F));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 5, true, false,
                this::worthShooting));
        mind(Autocaster.MONSTERS, true);
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

    /** Heavier on its feet than the basic one's slide, though less sure of it: a shove moves it half as far. */
    @Override
    protected double slides() {
        return 0.5;
    }

    @Override
    public void knockback(double strength, double x, double z) {
        super.knockback(strength / 3.0, x, z);
    }

    @Override
    public void tick() {
        super.tick();
        // a person it was set on, once the server stops allowing people to fight, is let be unless it is its owner
        if (!level().isClientSide && getTarget() instanceof Player player && !pvp()
                && !player.getUUID().equals(getOwnerUUID())) {
            setTarget(null);
        }
    }

    private boolean pvp() {
        MinecraftServer server = level().getServer();
        return server != null && server.isPvpAllowed();
    }

    /**
     * Whether this is something it has been told to mind: a monster, something alive that is not one, a person.
     * What is its owner's is passed over unless it has been set against friends, and people are fair game only where
     * the server lets people fight.
     */
    public boolean worthShooting(LivingEntity other) {
        boolean friendly = minds(Autocaster.FRIENDS);
        if (other instanceof Player player) {
            if (!minds(Autocaster.PLAYERS) || player.isCreative() || player.isSpectator()) {
                return false;
            }
            return player.getUUID().equals(getOwnerUUID()) ? friendly : pvp();
        }
        boolean monster = other.getType().getCategory() == MobCategory.MONSTER;
        if (!minds(monster ? Autocaster.MONSTERS : Autocaster.ANIMALS)) {
            return false;
        }
        if (other instanceof OwnableEntity owned && Objects.equals(owned.getOwnerUUID(), getOwnerUUID())) {
            return friendly;
        }
        return true;
    }

    public boolean minds(int what) {
        return (entityData.get(MINDS) & 1 << what) != 0;
    }

    /** Says what to mind, and forgets whatever it was aiming at, since it may no longer be fair game. */
    public void mind(int what, boolean on) {
        byte minds = entityData.get(MINDS);
        entityData.set(MINDS, (byte) (on ? minds | 1 << what : minds & ~(1 << what)));
        setTarget(null);
    }

    @Override
    protected void salvage() {
        salvage(ModItems.BIOALCHEMIC_MIND.get(), 0.2F);
        salvage(ModItems.BRASS_GEAR.get(), 0.5F);
        salvage(ModBlocks.GREATWOOD.planks().get(), 0.5F);
        salvage(ModBlocks.GREATWOOD.planks().get(), 0.5F);
        salvage(ModItems.BRASS_PLATE.get(), 0.3F);
        salvage(ModItems.IRON_PLATE.get(), 0.4F);
        salvage(ModItems.IRON_PLATE.get(), 0.4F);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("minds", entityData.get(MINDS));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(MINDS, tag.getByte("minds"));
    }
}
