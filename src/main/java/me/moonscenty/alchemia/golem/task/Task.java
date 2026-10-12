package me.moonscenty.alchemia.golem.task;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import me.moonscenty.alchemia.golem.Golem;
import me.moonscenty.alchemia.golem.seal.SealEntity;
import me.moonscenty.alchemia.golem.seal.SealPos;
import me.moonscenty.alchemia.golem.seal.Seals;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;

/**
 * One thing that wants doing: going to a block, or going to something that moves, on behalf of the seal that asked.
 *
 * <p>A golem takes one on by reserving it, and gives it up when it is done or cannot go on. A task lives five
 * minutes, two more each time it is taken up or given up, and is let go of when that runs out or it is suspended.
 */
public final class Task {
    private static final AtomicInteger NEXT = new AtomicInteger();

    /** Whether the task is about a place or about something that moves. */
    public enum Kind {
        BLOCK,
        ENTITY
    }

    private final int id = NEXT.incrementAndGet();
    private final Kind kind;
    @Nullable
    private final SealPos seal;
    @Nullable
    private final BlockPos pos;
    @Nullable
    private final Entity entity;
    @Nullable
    private UUID golem;
    private boolean reserved;
    private boolean suspended;
    private boolean completed;
    private int data;
    /** In seconds, counted down once a second. */
    private int lifespan = 300;
    private int priority;

    public Task(@Nullable SealPos seal, BlockPos pos) {
        this.kind = Kind.BLOCK;
        this.seal = seal;
        this.pos = pos.immutable();
        this.entity = null;
    }

    public Task(@Nullable SealPos seal, Entity entity) {
        this.kind = Kind.ENTITY;
        this.seal = seal;
        this.pos = null;
        this.entity = entity;
    }

    public int id() {
        return id;
    }

    public Kind kind() {
        return kind;
    }

    @Nullable
    public SealPos seal() {
        return seal;
    }

    /** Where it is to be done: the block, or where the thing it is about stands now. */
    public BlockPos pos() {
        return kind == Kind.ENTITY ? entity.blockPosition() : pos;
    }

    @Nullable
    public Entity entity() {
        return entity;
    }

    /** Only this golem may take it, if any is named. */
    @Nullable
    public UUID golem() {
        return golem;
    }

    public void setGolem(@Nullable UUID golem) {
        this.golem = golem;
    }

    public int priority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean reserved() {
        return reserved;
    }

    /** Taking it up or giving it up gives it two minutes more to live. */
    public void setReserved(boolean reserved) {
        this.reserved = reserved;
        this.lifespan += 120;
    }

    public boolean suspended() {
        return suspended;
    }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    public boolean completed() {
        return completed;
    }

    public void setCompletion(boolean completed) {
        this.completed = completed;
        this.lifespan++;
    }

    public int lifespan() {
        return lifespan;
    }

    public void setLifespan(int lifespan) {
        this.lifespan = lifespan;
    }

    public int data() {
        return data;
    }

    public void setData(int data) {
        this.data = data;
    }

    /**
     * Whether a golem may do it: not if the seal is set for golems of another colour, and otherwise if the seal that
     * asked says so. A task no seal asked for any golem may do.
     */
    public boolean canBeDoneBy(Golem golem) {
        SealEntity asked = Seals.get(golem.level(), seal);
        if (asked == null) {
            return true;
        }
        if (golem.colour() > 0 && asked.colour() > 0 && golem.colour() != asked.colour()) {
            return false;
        }
        return asked.seal().canGolemPerform(golem, this);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Task task && task.id == id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
