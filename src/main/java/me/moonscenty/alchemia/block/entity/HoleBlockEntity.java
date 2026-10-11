package me.moonscenty.alchemia.block.entity;

import me.moonscenty.alchemia.block.HoleBlock;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * One square of a portable hole: what used to be here, and how long until it is here again.
 *
 * <p>The first square bored is the only one that knows anything. It carries the direction the hole is going and
 * how many squares deep it is allowed to get, and on its first tick it opens the eight squares around it and one
 * more square deeper. Those know nothing and only wait. A tunnel therefore opens a layer a tick, which is what
 * makes it look bored rather than deleted.
 *
 * <p>Every square counts down on its own and puts back exactly what it took. They were all opened in the same
 * tick band, so they close together without anybody having to arrange it.
 */
public class HoleBlockEntity extends BlockEntity {
    /** How wide a tunnel is: three by three, bored from the middle out. */
    private static final int ACROSS = 3;

    private BlockState was = Blocks.STONE.defaultBlockState();
    private int closesIn = 120;
    private int open;
    /** How much deeper this hole may still go. One means this is the end of it. */
    private int deeper = 1;
    private Direction into;

    public HoleBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HOLE.get(), pos, state);
    }

    /** What this square is standing in for, and for how long. */
    public void holding(BlockState was, int closesIn, int deeper, Direction into) {
        this.was = was;
        this.closesIn = closesIn;
        this.deeper = deeper;
        this.into = into;
        setChanged();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, HoleBlockEntity hole) {
        if (hole.open == 0 && hole.deeper > 1 && hole.into != null) {
            hole.widen(level, pos);
            hole.bore(level, pos);
        }
        hole.open++;
        if (hole.open >= hole.closesIn) {
            hole.fillIn(level, pos);
        }
    }

    /** The eight squares around this one, across the way the tunnel is going. None of them goes any deeper. */
    private void widen(Level level, BlockPos pos) {
        for (int across = 0; across < ACROSS * ACROSS; across++) {
            int one = across / ACROSS - 1;
            int other = across % ACROSS - 1;
            if (one == 0 && other == 0) {
                continue;
            }
            bore(level, beside(pos, one, other), closesIn, 1, null);
        }
    }

    /** One square further into the wall, carrying the direction and one less of the depth. */
    private void bore(Level level, BlockPos pos) {
        if (!bore(level, pos.relative(into), closesIn, deeper - 1, into)) {
            deeper = 0;
        }
    }

    /** A square to the side of this one, measured across whichever way the tunnel is going. */
    private BlockPos beside(BlockPos pos, int one, int other) {
        return switch (into.getAxis()) {
            case X -> pos.offset(0, one, other);
            case Y -> pos.offset(one, 0, other);
            case Z -> pos.offset(one, other, 0);
        };
    }

    /**
     * Opens one square, if there is anything there worth opening.
     *
     * @return whether a hole was made, which is how the tunnel knows it has reached the far side
     */
    public static boolean bore(Level level, BlockPos pos, int closesIn, int deeper, Direction into) {
        BlockState there = level.getBlockState(pos);
        if (level.getBlockEntity(pos) != null || !HoleBlock.borable(there)
                || there.getDestroySpeed(level, pos) < 0.0F) {
            return false;
        }
        level.setBlock(pos, me.moonscenty.alchemia.registry.ModBlocks.HOLE.get().defaultBlockState(), 3);
        if (level.getBlockEntity(pos) instanceof HoleBlockEntity hole) {
            hole.holding(there, closesIn, deeper, into);
        }
        return true;
    }

    /**
     * Puts back what was here.
     * <p>
     * Called when the time is up, and again if the hole is taken out of the world some other way -- a piston,
     * a blast, somebody in creative. A hole that vanished without doing this would take a block with it, and
     * this is the only copy of what that block was.
     */
    public void fillIn(Level level, BlockPos pos) {
        level.setBlock(pos, was, 3);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        was = NbtUtils.readBlockState(registries.lookupOrThrow(net.minecraft.core.registries.Registries.BLOCK),
                tag.getCompound("was"));
        closesIn = tag.getInt("closes_in");
        open = tag.getInt("open");
        deeper = tag.getInt("deeper");
        into = tag.contains("into") ? Direction.byName(tag.getString("into")) : null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("was", NbtUtils.writeBlockState(was));
        tag.putInt("closes_in", closesIn);
        tag.putInt("open", open);
        tag.putInt("deeper", deeper);
        if (into != null) {
            tag.putString("into", into.getName());
        }
    }

    /** What is waiting to come back, which only a test and the block itself have any business knowing. */
    public BlockState was() {
        return was;
    }
}
