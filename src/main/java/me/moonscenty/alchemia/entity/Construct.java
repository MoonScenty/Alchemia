package me.moonscenty.alchemia.entity;

import java.util.UUID;

import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Something somebody built and set down, the original's {@code EntityOwnedConstruct}: an automated crossbow, a node
 * magnet. It belongs to whoever put it there, fights on their side, never wanders off or despawns, clacks like a
 * camera, and is taken down again by its owner with a wand, which hands back the thing it was set down from.
 * <p>
 * The autocaster came first and is its own class; this is for the ones that came after it.
 */
public abstract class Construct extends PathfinderMob implements OwnableEntity {
    /** Whose it is, written as a string the way the game writes a tamed animal's. */
    private static final EntityDataAccessor<String> OWNER =
            SynchedEntityData.defineId(Construct.class, EntityDataSerializers.STRING);
    /** How often it clacks, the original's. */
    private static final int CLACKS_EVERY = 240;
    /** What looting adds to the odds of each part surviving it being broken, the original's. */
    private static final float LOOTING_HELPS = 0.15F;

    /** The looting of whatever broke it, while its parts are being handed out. */
    private int looting;

    protected Construct(EntityType<? extends Construct> type, Level level) {
        super(type, level);
        setPersistenceRequired();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(OWNER, "");
    }

    /** The thing it folds back up into when its owner takes it down. */
    protected abstract ItemStack folded();

    /** How long between its knitting itself back together a point at a time, in ticks. */
    protected abstract int mendsEvery();

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            // it has no body to speak of; whatever draws it wants one angle for the whole thing
            setYRot(yHeadRot);
            if (tickCount % mendsEvery() == 0) {
                heal(1.0F);
            }
        }
    }

    // --- whose it is -------------------------------------------------------

    @Override
    public UUID getOwnerUUID() {
        String written = entityData.get(OWNER);
        try {
            return written.isEmpty() ? null : UUID.fromString(written);
        } catch (IllegalArgumentException malformed) {
            return null;
        }
    }

    public void setOwner(UUID owner) {
        entityData.set(OWNER, owner == null ? "" : owner.toString());
    }

    /** Whether this person may work it. One nobody set down is anybody's. */
    public boolean owns(Player player) {
        return getOwnerUUID() == null || player.getUUID().equals(getOwnerUUID());
    }

    /** It fights on its owner's side, so its owner's team is its team. */
    @Override
    public net.minecraft.world.scores.PlayerTeam getTeam() {
        LivingEntity owner = getOwner();
        return owner != null ? owner.getTeam() : super.getTeam();
    }

    /**
     * A wand takes it down; anything else is {@link #opened}. Only for whoever put it there: somebody else's is
     * somebody else's, and saying so is kinder than letting them find out.
     */
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (player.isSecondaryUseActive() || player.getItemInHand(hand).is(net.minecraft.world.item.Items.NAME_TAG)) {
            return super.mobInteract(player, hand);
        }
        if (level().isClientSide || isRemoved()) {
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        if (!owns(player)) {
            player.displayClientMessage(Component.translatable("entity.alchemia.autocaster.not_yours"), true);
            return InteractionResult.CONSUME;
        }
        if (player.getItemInHand(hand).getItem() instanceof WandItem) {
            playSound(ModSounds.ZAP.get(), 1.0F, 1.0F);
            emptyOut();
            spawnAtLocation(folded(), 0.5F);
            discard();
            player.swing(hand, true);
            return InteractionResult.CONSUME;
        }
        return opened(player);
    }

    /** What an owner's empty hand does to it. */
    protected InteractionResult opened(Player player) {
        return InteractionResult.CONSUME;
    }

    /** Whatever it is carrying falls out, when it is taken down or broken. */
    protected void emptyOut() {
    }

    @Override
    public void die(DamageSource cause) {
        OwnedDeaths.tell(this);
        super.die(cause);
        if (!level().isClientSide) {
            emptyOut();
        }
    }

    // --- what is left of a broken one --------------------------------------

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource cause, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, cause, recentlyHit);
        looting = cause.getEntity() instanceof LivingEntity killer
                ? EnchantmentHelper.getEnchantmentLevel(
                        level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING),
                        killer)
                : 0;
        salvage();
    }

    /** Hands out its parts, each by {@link #salvage(ItemLike, float)}. */
    protected abstract void salvage();

    /** One part of it, at the odds the original gave, a little better for every level of looting. */
    protected void salvage(ItemLike part, float chance) {
        if (getRandom().nextFloat() < chance + looting * LOOTING_HELPS) {
            spawnAtLocation(new ItemStack(part), 0.5F);
        }
    }

    // --- how it takes a knock ----------------------------------------------

    /** A blow knocks it off true, so a thing being shot at can at least put the aim out. */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        setYRot((float) (getYRot() + getRandom().nextGaussian() * 45.0));
        setXRot((float) (getXRot() + getRandom().nextGaussian() * 20.0));
        return super.hurt(source, amount);
    }

    /** How much of a sideways shove it actually goes along with: the original's tripods slid a fifth of it. */
    protected double slides() {
        return 0.2;
    }

    @Override
    public void move(net.minecraft.world.entity.MoverType type, Vec3 by) {
        super.move(type, new Vec3(by.x * slides(), by.y, by.z * slides()));
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity other) {
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    /** It aims rather than looks, and may aim straight down at something under it. */
    @Override
    public int getMaxHeadXRot() {
        return 90;
    }

    /** It never needs air. */
    @Override
    protected int decreaseAirSupply(int air) {
        return air;
    }

    // --- what it sounds like -----------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.CAMERA_CLACK.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.CAMERA_CLACK.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.TOOL.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return CLACKS_EVERY;
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.NEUTRAL;
    }

    // --- saving ------------------------------------------------------------

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("owner", entityData.get(OWNER));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(OWNER, tag.getString("owner"));
    }
}
