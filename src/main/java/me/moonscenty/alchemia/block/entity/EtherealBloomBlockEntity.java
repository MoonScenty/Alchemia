package me.moonscenty.alchemia.block.entity;

import me.moonscenty.alchemia.aura.BiomePaint;
import me.moonscenty.alchemia.block.taint.Tainted;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * What an ethereal bloom does, the original's {@code TileEtherealBloom}. Once a second it casts a line of light seven
 * blocks out each way through itself, turning a little further round each time and slowly tilting, and where the
 * line comes down on the ground it puts the land back as it was made and kills whatever taint is there. After fifteen
 * seconds of finding nothing it goes to sleep for a minute, and starts again.
 */
public class EtherealBloomBlockEntity extends BlockEntity {
    /** How long it searches in vain before it sleeps, in passes, and how long it sleeps, in ticks. */
    public static final int RESTLESS = 300;
    public static final int SLEEPS = RESTLESS * 4;
    /** How far its line reaches each way. */
    public static final int REACH = 8;
    /** How long a freshly planted one takes to grow, in ticks; the drawing grows with it. */
    public static final int GROWS = 100;

    public int counter;
    private int rad;
    private int rad1;
    public int growthCounter = GROWS;
    private int foundTaint = RESTLESS;
    private int sleepCounter = SLEEPS;
    private boolean sleep;

    public EtherealBloomBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ETHEREAL_BLOOM.get(), pos, state);
    }

    /** Planted just now: it grows in from nothing. */
    public void planted() {
        growthCounter = 0;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    private void wake() {
        foundTaint = RESTLESS;
        sleepCounter = SLEEPS;
        sleep = false;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EtherealBloomBlockEntity bloom) {
        if (bloom.counter == 0) {
            bloom.counter = level.random.nextInt(100);
            bloom.rad = bloom.counter;
        }
        bloom.counter++;
        if (bloom.foundTaint == 0) {
            bloom.sleepCounter--;
            bloom.sleep = bloom.sleepCounter != 0;
            if (!bloom.sleep) {
                bloom.counter = 0;
                bloom.sleepCounter = SLEEPS;
            }
        }
        if (level instanceof ServerLevel served && bloom.counter % 20 == 0 && !bloom.sleep) {
            bloom.sweep(served, pos);
        }
        if (level.isClientSide && bloom.growthCounter == 0) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, ModSounds.ROOTS.get(),
                    SoundSource.BLOCKS, 1.0F, 0.6F, false);
        }
        bloom.growthCounter++;
    }

    /** One pass of the line: further round, a little more tilted, and down to the ground at each step out. */
    private void sweep(ServerLevel level, BlockPos pos) {
        rad = (int) (rad + 5.0 + Math.sqrt(1 + rad1) * 5.0 + level.random.nextInt(5));
        if (rad > 360) {
            rad -= 360;
            rad1 = rad1 + 5 + level.random.nextInt(5);
            if (rad1 > 87) {
                rad1 -= 87;
            }
        }
        Vec3 source = Vec3.atCenterOf(pos);
        boolean found = false;
        for (int q = 1; q < REACH; q++) {
            // out along x, tipped up by rad1 and turned round by rad, as the original turned its vector
            Vec3 out = new Vec3(q, 0.0, 0.0).zRot(rad1 * Mth.DEG_TO_RAD).yRot(rad * Mth.DEG_TO_RAD);
            if (clear(level, ground(level, source.add(out))) | clear(level, ground(level, source.subtract(out)))) {
                found = true;
            }
            if (found) {
                wake();
                break;
            }
        }
        if (foundTaint > 0 && !found) {
            foundTaint--;
        }
    }

    private static BlockPos ground(Level level, Vec3 at) {
        BlockPos pos = BlockPos.containing(at);
        while (level.isEmptyBlock(pos) && pos.getY() > level.getMinBuildHeight()) {
            pos = pos.below();
        }
        return pos;
    }

    /** Puts the land back as it was made, and kills any taint standing there; whether it did either. */
    public static boolean clear(ServerLevel level, BlockPos at) {
        boolean cleared = false;
        var made = BiomePaint.generated(level, at);
        if (level.hasChunkAt(at) && !BiomePaint.isAlready(level, at, made)) {
            cleared = BiomePaint.paint(level, at, made);
        }
        BlockState there = level.getBlockState(at);
        if (there.getBlock() instanceof Tainted tainted) {
            tainted.die(level, at, there);
            cleared = true;
        }
        return cleared;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        growthCounter = tag.contains("growth") ? tag.getInt("growth") : GROWS;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("growth", Math.min(growthCounter, GROWS));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
