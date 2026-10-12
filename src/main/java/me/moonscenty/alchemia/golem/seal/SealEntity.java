package me.moonscenty.alchemia.golem.seal;

import java.util.UUID;

import javax.annotation.Nullable;

import me.moonscenty.alchemia.golem.task.Tasks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * A seal stuck somewhere: what kind it is, and everything its owner has set on it. Priority runs from five below to
 * five above; colour is nought for every golem or one more than a dye's number for golems of that colour only. A
 * locked seal is worked only by its owner's golems; a seal minding redstone stops while it is powered.
 */
public final class SealEntity {
    public static final int LOWEST = -5;
    public static final int HIGHEST = 5;
    /** The widest an area runs each way. */
    public static final int WIDEST = 8;

    private final SealPos pos;
    private final Seal seal;
    private int priority;
    private int colour;
    private boolean locked;
    private boolean redstone;
    @Nullable
    private UUID owner;
    /** How far it reaches across, up and along, for a seal that works over an area. */
    private BlockPos area;
    private boolean stopped;

    public SealEntity(SealPos pos, Seal seal) {
        this.pos = pos;
        this.seal = seal;
        // an area seal starts three across each way along its face and one block out from it
        Direction face = pos.face();
        this.area = new BlockPos(face.getStepX() == 0 ? 3 : 1, face.getStepY() == 0 ? 3 : 1,
                face.getStepZ() == 0 ? 3 : 1);
    }

    public SealPos pos() {
        return pos;
    }

    public Seal seal() {
        return seal;
    }

    public int priority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = Math.clamp(priority, LOWEST, HIGHEST);
    }

    public int colour() {
        return colour;
    }

    public void setColour(int colour) {
        this.colour = Math.clamp(colour, 0, 16);
    }

    public boolean locked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean redstone() {
        return redstone;
    }

    public void setRedstone(boolean redstone) {
        this.redstone = redstone;
    }

    @Nullable
    public UUID owner() {
        return owner;
    }

    public void setOwner(@Nullable UUID owner) {
        this.owner = owner;
    }

    public BlockPos area() {
        return area;
    }

    public void setArea(BlockPos area) {
        this.area = new BlockPos(Math.clamp(area.getX(), 1, WIDEST), Math.clamp(area.getY(), 1, WIDEST),
                Math.clamp(area.getZ(), 1, WIDEST));
    }

    /** Whether it is set to mind redstone and the block it is on, or the one in front of it, is powered. */
    public boolean stoppedByRedstone(Level level) {
        return redstone && (level.hasNeighborSignal(pos.pos()) || level.hasNeighborSignal(pos.front()));
    }

    /** Every tick: stopped by redstone it calls off its tasks; otherwise its seal goes about its work. */
    public void tick(ServerLevel level) {
        if (stoppedByRedstone(level)) {
            if (!stopped) {
                Tasks.suspendFor(level, pos);
            }
            stopped = true;
            return;
        }
        stopped = false;
        seal.tick(level, this);
    }

    /**
     * The blocks it works over: starting in front of its face and running as far out as its area says, and as far
     * either side across the face.
     */
    public AABB bounds() {
        Direction face = pos.face();
        AABB box = new AABB(pos.pos()).move(face.getStepX(), face.getStepY(), face.getStepZ());
        box = box.expandTowards(face.getStepX() != 0 ? (area.getX() - 1) * face.getStepX() : 0,
                face.getStepY() != 0 ? (area.getY() - 1) * face.getStepY() : 0,
                face.getStepZ() != 0 ? (area.getZ() - 1) * face.getStepZ() : 0);
        return box.inflate(face.getStepX() == 0 ? area.getX() - 1 : 0, face.getStepY() == 0 ? area.getY() - 1 : 0,
                face.getStepZ() == 0 ? area.getZ() - 1 : 0);
    }

    // --- saving ---------------------------------------------------------------------------------------------

    public CompoundTag save(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.put("pos", NbtUtils.writeBlockPos(pos.pos()));
        tag.putByte("face", (byte) pos.face().get3DDataValue());
        tag.putString("type", seal.key());
        tag.putByte("priority", (byte) priority);
        tag.putByte("colour", (byte) colour);
        tag.putBoolean("locked", locked);
        tag.putBoolean("redstone", redstone);
        if (owner != null) {
            tag.putUUID("owner", owner);
        }
        tag.put("area", NbtUtils.writeBlockPos(area));
        seal.save(tag, registries);
        return tag;
    }

    /** A seal as saved, or null if it was of a kind there no longer is. */
    @Nullable
    public static SealEntity load(CompoundTag tag, HolderLookup.Provider registries) {
        Seal seal = SealTypes.make(tag.getString("type"));
        BlockPos at = NbtUtils.readBlockPos(tag, "pos").orElse(null);
        if (seal == null || at == null) {
            return null;
        }
        SealEntity entity = new SealEntity(new SealPos(at, Direction.from3DDataValue(tag.getByte("face"))), seal);
        entity.priority = tag.getByte("priority");
        entity.colour = tag.getByte("colour");
        entity.locked = tag.getBoolean("locked");
        entity.redstone = tag.getBoolean("redstone");
        entity.owner = tag.hasUUID("owner") ? tag.getUUID("owner") : null;
        NbtUtils.readBlockPos(tag, "area").ifPresent(entity::setArea);
        seal.load(tag, registries);
        return entity;
    }
}
