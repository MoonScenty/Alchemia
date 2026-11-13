package me.moonscenty.alchemia.entity;

import java.util.UUID;

import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
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
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public abstract class Construct extends PathfinderMob implements OwnableEntity {
    private static final EntityDataAccessor<String> OWNER =
            SynchedEntityData.defineId(Construct.class, EntityDataSerializers.STRING);
    // one bit per mind, indices below
    private static final EntityDataAccessor<Byte> MINDS =
            SynchedEntityData.defineId(Construct.class, EntityDataSerializers.BYTE);

    // bit order matches the original's buttons
    public static final int ANIMALS = 0;
    public static final int MONSTERS = 1;
    public static final int PLAYERS = 2;
    public static final int FRIENDS = 3;

    // original's values
    private static final int CLACKS_EVERY = 240;
    private static final float LOOTING_HELPS = 0.15F;

    private int looting;

    protected Construct(EntityType<? extends Construct> type, Level level) {
        super(type, level);
        setPersistenceRequired();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(OWNER, "");
        builder.define(MINDS, (byte) 0);
    }

    protected abstract ItemStack folded();

    protected abstract int mendsEvery();

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            // body follows the head: it has no separate body angle
            setYRot(yHeadRot);
            if (tickCount % mendsEvery() == 0) {
                heal(1.0F);
            }
        }
    }

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

    public boolean owns(Player player) {
        return getOwnerUUID() == null || player.getUUID().equals(getOwnerUUID());
    }

    @Override
    public net.minecraft.world.scores.PlayerTeam getTeam() {
        LivingEntity owner = getOwner();
        return owner != null ? owner.getTeam() : super.getTeam();
    }

    public boolean minds(int what) {
        return (entityData.get(MINDS) & 1 << what) != 0;
    }

    public void mind(int what, boolean on) {
        byte minds = entityData.get(MINDS);
        entityData.set(MINDS, (byte) (on ? minds | 1 << what : minds & ~(1 << what)));
        setTarget(null);
    }

    // the original's canAttackClass: villagers count as animals, constructs as neither
    protected boolean mindsKind(LivingEntity other) {
        if (other instanceof Player) {
            if (!minds(PLAYERS)) {
                return false;
            }
            if (!pvp() && !minds(FRIENDS)) {
                mind(PLAYERS, false);
                return false;
            }
            return true;
        }
        if (other instanceof Enemy) {
            return minds(MONSTERS);
        }
        return minds(ANIMALS) && (other instanceof Animal || other instanceof AmbientCreature
                || other instanceof WaterAnimal || other instanceof AbstractGolem
                || other instanceof net.minecraft.world.entity.npc.Npc);
    }

    // minding friends turns every rule about sides around
    public boolean worthShooting(LivingEntity other) {
        boolean friendly = minds(FRIENDS);
        if (!mindsKind(other) || other instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return false;
        }
        var team = getTeam();
        if (team != null && (other.getTeam() == team) != friendly) {
            return false;
        }
        UUID owner = getOwnerUUID();
        if (other instanceof Player player) {
            return player.getUUID().equals(owner) ? friendly : pvp();
        }
        if (owner == null) {
            return true;
        }
        return other instanceof OwnableEntity owned ? owner.equals(owned.getOwnerUUID()) == friendly : !friendly;
    }

    protected void forgetStrangers() {
        if (getTarget() instanceof Player player && !pvp() && !player.getUUID().equals(getOwnerUUID())) {
            setTarget(null);
        }
    }

    // it does not turn on its owner or what they own unless told to mind friends
    @Override
    public boolean isAlliedTo(Entity other) {
        UUID owner = getOwnerUUID();
        if (owner != null && !minds(FRIENDS) && (owner.equals(other.getUUID())
                || other instanceof OwnableEntity owned && owner.equals(owned.getOwnerUUID()))) {
            return true;
        }
        return super.isAlliedTo(other);
    }

    protected boolean pvp() {
        MinecraftServer server = level().getServer();
        return server != null && server.isPvpAllowed();
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (letsHandle(player, hand)) {
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
            takeDown(player, hand);
            return InteractionResult.CONSUME;
        }
        return opened(player);
    }

    protected boolean letsHandle(Player player, InteractionHand hand) {
        return player.isSecondaryUseActive() || player.getItemInHand(hand).is(net.minecraft.world.item.Items.NAME_TAG);
    }

    protected void takeDown(Player player, InteractionHand hand) {
        playSound(ModSounds.ZAP.get(), 1.0F, 1.0F);
        foldUp();
        player.swing(hand, true);
    }

    protected final void foldUp() {
        emptyOut();
        spawnAtLocation(folded(), 0.5F);
        discard();
    }

    protected InteractionResult opened(Player player) {
        return InteractionResult.CONSUME;
    }

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

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource cause, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, cause, recentlyHit);
        looting = Looting.of(cause);
        salvage();
    }

    protected abstract void salvage();

    protected void salvage(ItemLike part, float chance) {
        if (salvaged(chance)) {
            spawnAtLocation(new ItemStack(part), 0.5F);
        }
    }

    protected boolean salvaged(float chance) {
        return getRandom().nextFloat() < chance + looting * LOOTING_HELPS;
    }

    protected boolean salvagedHalfHelped(float chance) {
        return getRandom().nextFloat() < chance + looting * LOOTING_HELPS / 2.0F;
    }

    // a hit knocks its aim off
    @Override
    public boolean hurt(DamageSource source, float amount) {
        setYRot((float) (getYRot() + getRandom().nextGaussian() * 45.0));
        setXRot((float) (getXRot() + getRandom().nextGaussian() * 20.0));
        return super.hurt(source, amount);
    }

    // the original's tripods slid a fifth of a shove
    protected double slides() {
        return 0.2;
    }

    @Override
    public void move(net.minecraft.world.entity.MoverType type, Vec3 by) {
        super.move(type, new Vec3(by.x * slides(), by.y, by.z * slides()));
    }

    protected boolean hops() {
        return true;
    }

    @Override
    public void knockback(double strength, double x, double z) {
        super.knockback(strength, x, z);
        Vec3 moving = getDeltaMovement();
        if (!hops() && moving.y > 0.1) {
            setDeltaMovement(moving.x, 0.1, moving.z);
        }
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

    // it aims rather than looks, so may point straight down
    @Override
    public int getMaxHeadXRot() {
        return 90;
    }

    @Override
    protected int decreaseAirSupply(int air) {
        return air;
    }

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

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("owner", entityData.get(OWNER));
        tag.putByte("minds", entityData.get(MINDS));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(OWNER, tag.getString("owner"));
        entityData.set(MINDS, tag.getByte("minds"));
    }
}
