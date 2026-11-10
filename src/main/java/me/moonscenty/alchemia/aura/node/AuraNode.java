package me.moonscenty.alchemia.aura.node;

import java.util.List;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aura.AuraGeneration;
import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModEntities;
import me.moonscenty.alchemia.research.NoteSolving;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

// An entity rather than a block: it drifts, can be pulled about, and merges with other nodes.
public class AuraNode extends Entity {
    private static final EntityDataAccessor<Integer> SIZE =
            SynchedEntityData.defineId(AuraNode.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> ASPECT =
            SynchedEntityData.defineId(AuraNode.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> TYPE =
            SynchedEntityData.defineId(AuraNode.class, EntityDataSerializers.INT);

    private static final double NEIGHBOUR_RANGE = 32.0;
    // ticks before the neighbour list is refreshed
    private static final int NEIGHBOUR_MEMORY = 750;
    // 1 in N nodes is not plain
    private static final int ODDITY = 10;
    // 1 in N has a compound aspect
    private static final int COMPOUND_CHANCE = 20;
    private static final double DRAG = 0.8;
    private static final double TOUCHING = 0.1;

    private boolean held;
    private int sincePeriod = -1;
    private int lookAgainAt = -1;
    private List<AuraNode> neighbours = List.of();

    public AuraNode(EntityType<? extends AuraNode> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    public AuraNode(Level level) {
        this(ModEntities.AURA_NODE.get(), level);
    }

    // held nodes neither move nor merge
    public boolean isHeld() {
        return held;
    }

    public void setHeld(boolean held) {
        this.held = held;
    }

    public int getSize() {
        return entityData.get(SIZE);
    }

    public void setSize(int size) {
        entityData.set(SIZE, Math.max(0, size));
    }

    public NodeType type() {
        return NodeType.values()[Mth.clamp(entityData.get(TYPE), 0, NodeType.values().length - 1)];
    }

    public void setType(NodeType type) {
        entityData.set(TYPE, type.ordinal());
    }

    public Holder<Aspect> aspect() {
        String id = entityData.get(ASPECT);
        if (id.isEmpty()) {
            return null;
        }
        return ModAspects.REGISTRY.getHolder(ResourceLocation.parse(id))
                .map(holder -> (Holder<Aspect>) holder).orElse(null);
    }

    public void setAspect(Holder<Aspect> aspect) {
        entityData.set(ASPECT, aspect == null ? "" : aspect.value().id().toString());
    }

    // compound nodes feed a random primal found by walking down their component tree
    public Holder<Aspect> feedsWith(RandomSource random) {
        Holder<Aspect> aspect = aspect();
        if (aspect == null) {
            return null;
        }
        // cap guards against a component tree that never reaches a primal
        for (int step = 0; step < 16 && !aspect.value().isPrimal(); step++) {
            List<Holder<Aspect>> parts = aspect.value().components().orElse(List.of());
            if (parts.isEmpty()) {
                break;
            }
            aspect = parts.get(random.nextInt(parts.size()));
        }
        return aspect.value().isPrimal() ? aspect : ModAspects.randomPrimal(random);
    }

    public void drawUp(RandomSource source) {
        int span = 2 + AuraGeneration.BASE / 3;
        setSize(span + source.nextInt(span));

        NodeType[] kinds = NodeType.values();
        NodeType type = source.nextInt(ODDITY) == 0 ? kinds[1 + source.nextInt(kinds.length - 1)] : NodeType.PLAIN;
        // original's: a third of the tainted ones come out plain after all
        if (type == NodeType.TAINTED && source.nextFloat() < 0.33F) {
            type = NodeType.PLAIN;
        }
        setType(type);

        List<Holder<Aspect>> pool = source.nextInt(COMPOUND_CHANCE) == 0
                ? ModAspects.compounds()
                : ModAspects.primals();
        setAspect(pool.isEmpty() ? ModAspects.AIR : pool.get(source.nextInt(pool.size())));
    }

    @Override
    public void tick() {
        if (getSize() == 0 && !level().isClientSide) {
            drawUp(random);
        }
        if (sincePeriod < 0) {
            sincePeriod = random.nextInt(NodeType.PERIOD);
        }

        if (level() instanceof ServerLevel server) {
            if (++sincePeriod > NodeType.PERIOD) {
                sincePeriod = 0;
                feed(server);
                type().doItsThing(this, server);
            }
            if (!held) {
                gatherTowardsNeighbours();
            }
        }

        // held, but still eased into its anchor spot
        if (getDeltaMovement().lengthSqr() > 1.0E-6) {
            setDeltaMovement(getDeltaMovement().scale(DRAG));
            setPos(position().add(getDeltaMovement()));
        }
    }

    private void feed(ServerLevel level) {
        if (!type().feeds()) {
            return;
        }
        Holder<Aspect> giving = feedsWith(random);
        // a tainted node feeds its own aspect at full strength; the fifth it puts out is the flux
        NodeType strength = type() == NodeType.TAINTED ? NodeType.PLAIN : type();
        if (giving != null) {
            AuraHandler.recharge(level, blockPosition(), giving, strength.strengthOf(this, level), random);
        }
    }

    private void gatherTowardsNeighbours() {
        if (tickCount > lookAgainAt) {
            neighbours = level().getEntitiesOfClass(AuraNode.class, getBoundingBox().inflate(NEIGHBOUR_RANGE),
                    other -> other != this && other.isAlive());
            lookAgainAt = tickCount + NEIGHBOUR_MEMORY;
        }

        for (AuraNode other : neighbours) {
            if (!other.isAlive() || !isAlive() || other.isHeld()) {
                continue;
            }
            Vec3 away = position().subtract(other.position());
            double apart = away.lengthSqr();
            double reach = (getSize() + other.getSize()) * 1.5;

            if (apart > TOUCHING && apart < reach) {
                double pull = other.getSize() / 50.0 / reach / apart;
                setDeltaMovement(getDeltaMovement().subtract(away.scale(pull)));
            } else if (apart <= TOUCHING && getSize() >= other.getSize()) {
                swallow(other);
            }
        }
    }

    private void swallow(AuraNode other) {
        setSize(getSize() + (int) Math.sqrt(other.getSize()));

        if (aspect() != null && other.aspect() != null && random.nextInt(100) < Math.sqrt(other.getSize())) {
            NoteSolving.mixOf(ModAspects.REGISTRY, aspect(), other.aspect()).ifPresent(this::setAspect);
        }

        boolean takesOn = type() == NodeType.PLAIN
                ? other.type() != NodeType.PLAIN && random.nextInt(3) == 0
                : other.type() != NodeType.PLAIN && random.nextInt(100) < Math.sqrt(other.getSize() / 2.0);
        if (takesOn) {
            setType(other.type());
        }
        other.discard();
    }

    // vanilla's size-based cull distance would hide a node at ~30 blocks
    private static final double SEEN_FROM = 96.0;

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < SEEN_FROM * SEEN_FROM;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void push(double x, double y, double z) {
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(SIZE, 0);
        builder.define(ASPECT, "");
        builder.define(TYPE, 0);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setSize(tag.getInt("size"));
        setType(NodeType.values()[Mth.clamp(tag.getInt("type"), 0, NodeType.values().length - 1)]);
        entityData.set(ASPECT, tag.getString("aspect"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("size", getSize());
        tag.putInt("type", type().ordinal());
        tag.putString("aspect", entityData.get(ASPECT));
    }
}
