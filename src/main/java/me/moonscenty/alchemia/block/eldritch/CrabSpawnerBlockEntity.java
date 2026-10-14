package me.moonscenty.alchemia.block.eldritch;

import me.moonscenty.alchemia.client.EldritchEffects;
import me.moonscenty.alchemia.entity.EldritchCrab;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import me.moonscenty.alchemia.registry.ModEntities;
import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * A crab vent in the crust of the Outer Lands. While someone is within sixteen blocks and there are no more than five
 * crabs within thirty-two, it hisses and, a little after, pushes out a crab -- without a helmet -- every seven to
 * twelve seconds.
 */
public class CrabSpawnerBlockEntity extends BlockEntity {
    private int count = 150;
    /** For the drawing: an animation counter with a random start, and how much longer it is venting hard. */
    public int ticks;
    private int venting;
    private Direction facing = Direction.UP;

    public CrabSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRAB_SPAWNER.get(), pos, state);
    }

    public Direction facing() {
        return facing;
    }

    public void setFacing(Direction facing) {
        this.facing = facing;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CrabSpawnerBlockEntity vent) {
        if (vent.ticks == 0) {
            vent.ticks = level.random.nextInt(500);
        }
        vent.ticks++;
        if (level instanceof ServerLevel served) {
            vent.count--;
            if (vent.count < 0) {
                vent.count = 50 + level.random.nextInt(50);
                return;
            }
            if (vent.count == 15 && vent.active(level) && !vent.crowded(level)) {
                level.blockEvent(pos, state.getBlock(), 1, 0);
                level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 1.0F);
            }
            if (vent.count <= 0 && vent.active(level) && !vent.crowded(level)) {
                vent.count = 150 + level.random.nextInt(100);
                vent.spawn(served);
                level.playSound(null, pos, ModSounds.GORE.get(), SoundSource.BLOCKS, 0.5F, 1.0F);
            }
        } else if (vent.venting > 0) {
            vent.venting--;
            for (int i = 0; i < 3; i++) {
                EldritchEffects.vent(level, pos, vent.facing);
            }
        } else if (level.random.nextInt(20) == 0) {
            EldritchEffects.vent(level, pos, vent.facing);
        }
    }

    @Override
    public boolean triggerEvent(int id, int type) {
        if (id == 1) {
            venting = 20;
            return true;
        }
        return super.triggerEvent(id, type);
    }

    private boolean active(Level level) {
        return level.getNearestPlayer(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5,
                worldPosition.getZ() + 0.5, 16.0, false) != null;
    }

    private boolean crowded(Level level) {
        return level.getEntitiesOfClass(EldritchCrab.class, new AABB(worldPosition).inflate(32.0)).size() > 5;
    }

    private void spawn(ServerLevel level) {
        EldritchCrab crab = ModEntities.ELDRITCH_CRAB.get().create(level);
        if (crab == null) {
            return;
        }
        BlockPos out = worldPosition.relative(facing);
        crab.moveTo(out.getX() + 0.5, out.getY() + 0.5, out.getZ() + 0.5, 0.0F, 0.0F);
        crab.finalizeSpawn(level, level.getCurrentDifficultyAt(worldPosition), MobSpawnType.SPAWNER, null);
        crab.setHelm(false);
        crab.setDeltaMovement(facing.getStepX() * 0.2, facing.getStepY() * 0.2, facing.getStepZ() * 0.2);
        level.addFreshEntity(crab);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putByte("facing", (byte) facing.get3DDataValue());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        facing = Direction.from3DDataValue(tag.getByte("facing"));
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
}
