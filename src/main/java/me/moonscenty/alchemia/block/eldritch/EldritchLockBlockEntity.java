package me.moonscenty.alchemia.block.eldritch;

import me.moonscenty.alchemia.outer.BossRooms;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * The lock on the great hall's door: which way the door faces, and, once a runed tablet is in it, how long it has
 * been turning. It pumps every quarter second for five seconds, then whatever is in the hall wakes, the door melts
 * away and the lock with it.
 */
public class EldritchLockBlockEntity extends BlockEntity {
    /** How long the lock turns before the hall opens. */
    public static final int TURNING = 100;

    /** -1 while it waits for a tablet; then the ticks since. */
    private int count = -1;
    private Direction facing = Direction.NORTH;

    public EldritchLockBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELDRITCH_LOCK.get(), pos, state);
    }

    public int count() {
        return count;
    }

    public Direction facing() {
        return facing;
    }

    public void setFacing(Direction facing) {
        this.facing = facing;
        changed();
    }

    /** Whether a tablet can go in: only while it is still shut. */
    public boolean waiting() {
        return count < 0;
    }

    public void turn() {
        count = 0;
        changed();
    }

    private void changed() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EldritchLockBlockEntity lock) {
        if (lock.count == -1) {
            return;
        }
        lock.count++;
        if (level.isClientSide) {
            return;
        }
        if (lock.count % 5 == 0) {
            level.playSound(null, pos, ModSounds.PUMP.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        if (lock.count > TURNING && level instanceof ServerLevel served) {
            level.playSound(null, pos, ModSounds.ICE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            BossRooms.wake(served, pos);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putByte("facing", (byte) facing.get3DDataValue());
        tag.putShort("count", (short) count);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        facing = Direction.from3DDataValue(tag.getByte("facing"));
        count = tag.contains("count") ? tag.getShort("count") : -1;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition).inflate(2.25);
    }
}
