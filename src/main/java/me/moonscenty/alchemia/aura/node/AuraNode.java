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

/**
 * A knot of magic hanging in the air, feeding the land around it.
 * <p>
 * It is an entity rather than a block because it does not sit on the grid: it drifts, it can be pulled about, and
 * two that meet become one. Nothing can hurt it and it passes through walls.
 */
public class AuraNode extends Entity {
    private static final EntityDataAccessor<Integer> SIZE =
            SynchedEntityData.defineId(AuraNode.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> ASPECT =
            SynchedEntityData.defineId(AuraNode.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> TYPE =
            SynchedEntityData.defineId(AuraNode.class, EntityDataSerializers.INT);

    /** How far a node looks for others to drift towards. */
    private static final double NEIGHBOUR_RANGE = 32.0;
    /** How long it keeps that list before looking again. */
    private static final int NEIGHBOUR_MEMORY = 750;
    /** One in this many nodes is something other than plain. */
    private static final int ODDITY = 8;
    /** One in this many is made of something compound rather than one of the six. */
    private static final int COMPOUND_CHANCE = 20;
    /** How fast drifting dies down when nothing is pulling. */
    private static final double DRAG = 0.8;
    /** Close enough to be the same place. */
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

    // --- what it is --------------------------------------------------------

    /** Whether something is holding this node still. Nothing can move it or merge with it while it is. */
    public boolean isHeld() {
        return held;
    }

    /** Said by whatever is doing the holding, every tick it keeps hold. */
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

    /**
     * What the land gets fed: always one of the six.
     * <p>
     * A node made of something compound gives one of the aspects that compound is made from, following the tree
     * down until it reaches a primal, since what a compound is made of may well be compound itself.
     */
    public Holder<Aspect> feedsWith(RandomSource random) {
        Holder<Aspect> aspect = aspect();
        if (aspect == null) {
            return null;
        }
        // deep enough for any tree that terminates, and it gives up rather than looping on one that does not
        for (int step = 0; step < 16 && !aspect.value().isPrimal(); step++) {
            List<Holder<Aspect>> parts = aspect.value().components().orElse(List.of());
            if (parts.isEmpty()) {
                break;
            }
            aspect = parts.get(random.nextInt(parts.size()));
        }
        return aspect.value().isPrimal() ? aspect : ModAspects.randomPrimal(random);
    }

    /** Gives a fresh node its size, kind and aspect. */
    public void drawUp(RandomSource source) {
        int span = 2 + AuraGeneration.BASE / 3;
        setSize(span + source.nextInt(span));

        NodeType[] kinds = NodeType.values();
        setType(source.nextInt(ODDITY) == 0 ? kinds[1 + source.nextInt(kinds.length - 1)] : NodeType.PLAIN);

        List<Holder<Aspect>> pool = source.nextInt(COMPOUND_CHANCE) == 0
                ? ModAspects.compounds()
                : ModAspects.primals();
        setAspect(pool.isEmpty() ? ModAspects.AIR : pool.get(source.nextInt(pool.size())));
    }

    // --- living ------------------------------------------------------------

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

        // held still, but not frozen: it is still drawn in to the spot it is meant to sit
        if (getDeltaMovement().lengthSqr() > 1.0E-6) {
            setDeltaMovement(getDeltaMovement().scale(DRAG));
            setPos(position().add(getDeltaMovement()));
        }
    }

    /** Hands the land below a measure of what this node is made of. */
    private void feed(ServerLevel level) {
        if (!type().feeds() || type() == NodeType.TAINTED) {
            return;
        }
        Holder<Aspect> giving = feedsWith(random);
        if (giving != null) {
            AuraHandler.recharge(level, blockPosition(), giving, type().strengthOf(this, level), random);
        }
    }

    /**
     * Nodes pull on one another, and one that reaches another swallows it. What comes out is bigger, may have taken
     * on the other's kind, and may be made of the two of them mixed together.
     */
    private void gatherTowardsNeighbours() {
        if (tickCount > lookAgainAt) {
            neighbours = level().getEntitiesOfClass(AuraNode.class, getBoundingBox().inflate(NEIGHBOUR_RANGE),
                    other -> other != this && other.isAlive());
            lookAgainAt = tickCount + NEIGHBOUR_MEMORY;
        }

        for (AuraNode other : neighbours) {
            if (!other.isAlive() || !isAlive()) {
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

    // --- being left alone --------------------------------------------------

    /**
     * How far off a node may still be drawn.
     * <p>
     * The usual cutoff is worked out from how big a thing is, which would drop something this small at about thirty
     * blocks. A node is meant to be picked out across a valley by anyone with the lenses for it, so it says so.
     */
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

    // --- plumbing ----------------------------------------------------------

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
