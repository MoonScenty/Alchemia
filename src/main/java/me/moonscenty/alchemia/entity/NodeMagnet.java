package me.moonscenty.alchemia.entity;

import java.util.Comparator;
import java.util.List;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.aura.node.AuraNode;
import me.moonscenty.alchemia.block.CrystalType;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModSounds;
import me.moonscenty.alchemia.research.Clues;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * A magnet on a tripod that drags aura nodes about, the original's {@code EntityNodeMagnet}. It picks the nearest
 * node within thirty-two blocks that nothing is holding still, turns to face it, and draws it in, slower the bigger the
 * node, until it is a block off. It runs on vis it takes from the air a little at a time, and now and then the
 * pulling shakes loose a wisp or a point of flux.
 * <p>
 * It hums, and monsters for thirty-two blocks round take against it. It is fragile: a fall or a hard knock breaks it.
 */
public class NodeMagnet extends Construct {
    /** How much charge it holds, which a pull spends a point at a time. */
    private static final EntityDataAccessor<Byte> CHARGE =
            SynchedEntityData.defineId(NodeMagnet.class, EntityDataSerializers.BYTE);
    /** Whether it has hold of a node and the charge to pull it, which is all the client needs to spin it up. */
    private static final EntityDataAccessor<Boolean> PULLING =
            SynchedEntityData.defineId(NodeMagnet.class, EntityDataSerializers.BOOLEAN);

    private static final double LIVES = 15.0;
    private static final double REACH = 32.0;
    private static final int MENDS_EVERY = 50;
    /** Charge it tops up to, and what each primal it can draw from the air is worth. */
    private static final int TOPS_UP_TO = 10;
    private static final int PER_DROP = 3;
    /** How hard the magnet pulls, against how big the node is: the original's. */
    private static final double PULL = 200.0;
    private static final double PULL_PER_SIZE = 3.0;
    /** How fast the magnet's arms may spin, and how fast they come up to it and run down. */
    private static final float SPINS_AT_MOST = 36.0F;
    private static final float SPINS_UP = 0.36F;
    private static final float SPINS_DOWN = 0.5F;

    private AuraNode node;
    /** How fast the arms are spinning, and where they have got to. The client's alone. */
    private float spin;
    public float turned;

    public NodeMagnet(EntityType<? extends NodeMagnet> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, LIVES)
                .add(Attributes.FOLLOW_RANGE, REACH);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CHARGE, (byte) 0);
        builder.define(PULLING, false);
    }

    @Override
    protected ItemStack folded() {
        return new ItemStack(ModItems.NODE_MAGNET.get());
    }

    @Override
    protected int mendsEvery() {
        return MENDS_EVERY;
    }

    public int charge() {
        return entityData.get(CHARGE);
    }

    public void setCharge(int charge) {
        entityData.set(CHARGE, (byte) charge);
    }

    public boolean pulling() {
        return entityData.get(PULLING);
    }

    /** The node it has hold of, if any. */
    public AuraNode node() {
        return node;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            spin();
            return;
        }
        if (tickCount % MENDS_EVERY == 0) {
            stirUp();
        }
        if (tickCount % 10 == 0 && charge() < TOPS_UP_TO) {
            recharge();
        }
        if (node != null && (!node.isAlive() || node.isHeld())) {
            node = null;
        }
        if (node == null && tickCount % 20 == 0) {
            node = nearestFree();
        }
        if (node != null && charge() > 0) {
            pull();
        }
        entityData.set(PULLING, node != null && charge() > 0);
    }

    /** The hum sets the monsters round about against it, and its owner learns why. */
    private void stirUp() {
        for (Monster monster : level().getEntitiesOfClass(Monster.class, getBoundingBox().inflate(REACH),
                monster -> monster.distanceTo(this) <= REACH)) {
            if (random.nextBoolean() && monster.getTarget() == null) {
                monster.setTarget(this);
            }
        }
        if (getOwner() instanceof Player owner) {
            Clues.give(owner, Clues.NODE_MAGNET_DANGER);
        }
    }

    /** A point of each primal the air here can spare is worth three of charge. */
    private void recharge() {
        int charge = charge();
        for (Holder<Aspect> primal : ModAspects.primals()) {
            if (AuraHandler.drain(level(), blockPosition(), primal, 1)) {
                charge += PER_DROP;
            }
        }
        setCharge(charge);
    }

    private AuraNode nearestFree() {
        List<AuraNode> near = level().getEntitiesOfClass(AuraNode.class, getBoundingBox().inflate(REACH),
                found -> !found.isHeld() && found.distanceTo(this) <= REACH);
        return near.stream().min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
    }

    /**
     * One tick's pull on the node, for a point of charge: towards the magnet, weaker the bigger the node. Close
     * enough, or too far, and it lets go. One pull in a hundred shakes something loose: a wisp of the node's own
     * kind one time in five, otherwise a point of flux.
     */
    private void pull() {
        getLookControl().setLookAt(node.getX(), node.getY() + node.getEyeHeight(), node.getZ(), 10.0F,
                getMaxHeadXRot());
        double gap = distanceTo(node);
        if (gap < 1.0 || gap > REACH) {
            node = null;
            return;
        }
        Vec3 towards = position().subtract(node.position()).scale(1.0 / (gap * (PULL + node.getSize()
                * PULL_PER_SIZE)));
        node.setDeltaMovement(node.getDeltaMovement().add(towards));
        setCharge(charge() - 1);
        if (random.nextFloat() < 0.01F) {
            if (random.nextFloat() < 0.2F) {
                Wisp wisp = new Wisp(level());
                wisp.moveTo(node.getX(), node.getY(), node.getZ(), 0.0F, 0.0F);
                if (node.aspect() != null) {
                    wisp.setAspect(node.aspect());
                }
                level().addFreshEntity(wisp);
            } else {
                AuraHandler.add(level(), node.blockPosition(), ModAspects.FLUX, 1);
            }
        }
    }

    /** The arms spin up while it pulls and run down when it stops, with a pump's thump at every turn. */
    private void spin() {
        spin = Math.max(0.0F, Math.min(SPINS_AT_MOST, spin + (pulling() ? SPINS_UP : -SPINS_DOWN)));
        turned += spin;
        if (turned > 360.0F) {
            turned -= 360.0F;
            level().playLocalSound(getX(), getY(), getZ(), ModSounds.PUMP.get(), SoundSource.NEUTRAL, 0.7F, 1.0F,
                    false);
        }
    }

    // --- how fragile it is -------------------------------------------------

    @Override
    public void knockback(double strength, double x, double z) {
        super.knockback(strength, x, z);
        Vec3 moving = getDeltaMovement();
        if (moving.y > 0.1) {
            setDeltaMovement(moving.x, 0.1, moving.z);
        }
    }

    /** Moving at all is a risk to it, and moving fast breaks it outright. */
    @Override
    public void move(MoverType type, Vec3 by) {
        super.move(type, by);
        if (level().isClientSide || !isAlive()) {
            return;
        }
        Vec3 moving = getDeltaMovement();
        float jolt = (float) ((Math.abs(moving.x) + Math.abs(moving.y) + Math.abs(moving.z)) / 3.0);
        if (jolt > 0.25F || random.nextFloat() < jolt / 20.0F) {
            hurt(damageSources().fellOutOfWorld(), 40.0F);
        }
    }

    @Override
    protected void salvage() {
        salvage(ModItems.BIOALCHEMIC_MIND.get(), 0.2F);
        salvage(ModItems.MORPHIC_RESONATOR.get(), 0.2F);
        for (CrystalType crystal : CrystalType.values()) {
            salvage(ModBlocks.CRYSTALS.get(crystal).get(), 0.2F);
        }
        salvage(ModItems.BRASS_GEAR.get(), 0.5F);
        salvage(ModItems.BRASS_PLATE.get(), 0.5F);
        salvage(ModBlocks.GREATWOOD.planks().get(), 0.5F);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("charge", (byte) charge());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setCharge(tag.getByte("charge"));
    }
}
