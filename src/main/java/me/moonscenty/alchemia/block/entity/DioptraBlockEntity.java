package me.moonscenty.alchemia.block.entity;

import java.util.Arrays;

import org.joml.Vector3f;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aura.AuraChunk;
import me.moonscenty.alchemia.aura.AuraGeneration;
import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.aura.ShardTuning;
import me.moonscenty.alchemia.aura.node.AuraNode;
import me.moonscenty.alchemia.block.DioptraBlock;
import me.moonscenty.alchemia.particle.SparkleOptions;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * A dioptra reading the aura: thirteen chunks by thirteen round it, once a second, for the one aspect its shard is
 * tuned to -- or, tuned by a balanced shard, for whichever primal is strongest in each chunk.
 *
 * <p>Each chunk is written down as how much there is, against half again the usual aura and never past a hundred and
 * ten in a hundred, and which aspect it was. A chunk with a node in it is marked as well, and sparkles on the map.
 */
public class DioptraBlockEntity extends BlockEntity {
    /** How many chunks across the map is, and how often it is read. The original's. */
    public static final int ACROSS = 13;
    public static final int MIDDLE = ACROSS / 2;
    private static final int EVERY = 20;
    /** The most a chunk is drawn at, in hundredths of half again the usual aura. */
    public static final int TALLEST = 110;
    /** Added to a chunk's aspect where there is a node. */
    public static final int NODE = 10;
    /** Half again the usual aura: what a hundred on the map stands for. */
    public static final float FULL = AuraGeneration.BASE * 1.5F;

    private int tuning = ShardTuning.NONE;
    private int counter;
    private byte[] amounts = new byte[ACROSS * ACROSS];
    private byte[] kinds = blank();

    public DioptraBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DIOPTRA.get(), pos, state);
    }

    private static byte[] blank() {
        byte[] kinds = new byte[ACROSS * ACROSS];
        Arrays.fill(kinds, (byte) ShardTuning.NONE);
        return kinds;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, DioptraBlockEntity dioptra) {
        dioptra.counter++;
        if (!state.getValue(DioptraBlock.ENABLED) || dioptra.tuning == ShardTuning.NONE) {
            dioptra.counter = 0;
        } else if (!level.isClientSide) {
            if (dioptra.counter % EVERY == 0) {
                dioptra.read(level);
            }
        } else {
            dioptra.sparkle(level);
        }
    }

    /** Every chunk of the map read over. */
    public void read(Level level) {
        amounts = new byte[ACROSS * ACROSS];
        kinds = blank();
        ChunkPos middle = new ChunkPos(worldPosition);
        boolean[] nodes = nodes(level, middle);
        for (int x = 0; x < ACROSS; x++) {
            for (int z = 0; z < ACROSS; z++) {
                BlockPos at = new ChunkPos(middle.x + x - MIDDLE, middle.z + z - MIDDLE).getMiddleBlockPosition(0);
                AuraChunk aura = AuraHandler.at(level, at);
                if (!aura.exists() || aura.aspects().isEmpty()) {
                    continue;
                }
                Holder<Aspect> shown = ShardTuning.aspect(tuning);
                if (shown == null) {
                    shown = aura.aspects().remove(ModAspects.FLUX).sortedByAmount().stream().findFirst()
                            .orElse(null);
                    if (shown == null) {
                        continue;
                    }
                }
                int cell = x + z * ACROSS;
                kinds[cell] = (byte) (code(shown) + (nodes[cell] ? NODE : 0));
                amounts[cell] = (byte) Math.min(TALLEST, aura.get(shown) / FULL * 100.0F);
            }
        }
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
    }

    /** Which chunks of the map have a node in them. */
    private static boolean[] nodes(Level level, ChunkPos middle) {
        boolean[] found = new boolean[ACROSS * ACROSS];
        int west = (middle.x - MIDDLE) << 4;
        int north = (middle.z - MIDDLE) << 4;
        AABB map = new AABB(west, level.getMinBuildHeight(), north, west + ACROSS * 16, level.getMaxBuildHeight(),
                north + ACROSS * 16);
        for (AuraNode node : level.getEntitiesOfClass(AuraNode.class, map)) {
            int x = (node.getBlockX() >> 4) - middle.x + MIDDLE;
            int z = (node.getBlockZ() >> 4) - middle.z + MIDDLE;
            if (x >= 0 && x < ACROSS && z >= 0 && z < ACROSS) {
                found[x + z * ACROSS] = true;
            }
        }
        return found;
    }

    /** The tuning code an aspect is drawn by: its own, for a primal or flux. */
    private static int code(Holder<Aspect> aspect) {
        for (int tuning = 1; tuning <= 7; tuning++) {
            if (aspect.equals(ShardTuning.aspect(tuning))) {
                return tuning;
            }
        }
        return ShardTuning.BALANCED;
    }

    /** Now and then a sparkle over a chunk with a node in it, at the height the map is drawn there. */
    private void sparkle(Level level) {
        RandomSource random = level.random;
        for (int x = 0; x < ACROSS; x++) {
            for (int z = 0; z < ACROSS; z++) {
                int cell = x + z * ACROSS;
                if (kinds[cell] <= NODE || amounts[cell] <= 0 || random.nextInt(3) != 0) {
                    continue;
                }
                double px = worldPosition.getX() + 0.5 + (x - MIDDLE) * 0.075
                        + (random.nextFloat() - random.nextFloat()) * 0.04;
                double py = worldPosition.getY() + 1 + amounts[cell] / FULL;
                double pz = worldPosition.getZ() + 0.5 + (z - MIDDLE) * 0.075
                        + (random.nextFloat() - random.nextFloat()) * 0.04;
                level.addParticle(new SparkleOptions(0xE0E0E0, new Vector3f((float) px, (float) py + 0.15F,
                        (float) pz)), px, py, pz, 0, 0, 0);
            }
        }
    }

    /** Tuned to a new shard, or to nothing; the map is cleared until it is next read. */
    public void tune(int tuning) {
        this.tuning = tuning;
        amounts = new byte[ACROSS * ACROSS];
        kinds = blank();
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public int tuning() {
        return tuning;
    }

    public int counter() {
        return counter;
    }

    /** How much of a chunk's aspect there is, in hundredths of half again the usual aura. */
    public int amount(int x, int z) {
        return amounts[x + z * ACROSS];
    }

    /** The aspect a chunk is drawn in, or none where nothing was read. */
    public Holder<Aspect> aspect(int x, int z) {
        int kind = kinds[x + z * ACROSS];
        return ShardTuning.aspect(kind > NODE ? kind - NODE : kind);
    }

    /** How strong the aura is in its own chunk, for a comparator. */
    public int signal() {
        float share = amount(MIDDLE, MIDDLE) / (float) TALLEST;
        return (int) Math.floor(share * 14.0F) + (share > 0 ? 1 : 0);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        tuning = tag.contains("tuning") ? tag.getByte("tuning") : ShardTuning.NONE;
        byte[] readAmounts = tag.getByteArray("amounts");
        byte[] readKinds = tag.getByteArray("kinds");
        if (readAmounts.length == ACROSS * ACROSS && readKinds.length == ACROSS * ACROSS) {
            amounts = readAmounts;
            kinds = readKinds;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putByte("tuning", (byte) tuning);
        tag.putByteArray("amounts", amounts);
        tag.putByteArray("kinds", kinds);
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
