package me.moonscenty.alchemia.outer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

// Read from chunk-generator threads, hence the concurrent maps and the static instance.
public class Labyrinth extends SavedData {
    private static final String NAME = Alchemia.MODID + "_labyrinth";
    private static volatile Labyrinth current;

    private final Map<Long, Short> cells = new ConcurrentHashMap<>();
    // exits come out at the recorded portal; the original searched 128 blocks and could miss
    private final Map<Long, Long> links = new ConcurrentHashMap<>();
    private int bossCount;

    public static SavedData.Factory<Labyrinth> factory() {
        return new SavedData.Factory<>(Labyrinth::new, Labyrinth::load, null);
    }

    public static Labyrinth of(MinecraftServer server) {
        Labyrinth labyrinth = server.overworld().getDataStorage().computeIfAbsent(factory(), NAME);
        current = labyrinth;
        return labyrinth;
    }

    public static Labyrinth current() {
        return current;
    }

    public static void forget() {
        current = null;
    }

    private static long key(int x, int z) {
        return ChunkPos.asLong(x, z);
    }

    public Cell get(int x, int z) {
        Short packed = cells.get(key(x, z));
        return packed == null ? null : new Cell(packed);
    }

    void put(int x, int z, short packed) {
        cells.put(key(x, z), packed);
        setDirty();
    }

    public boolean inRange(int x, int z, int width, int height) {
        for (int i = -width; i <= width; i++) {
            for (int j = -height; j <= height; j++) {
                if (cells.containsKey(key(x + i, z + j))) {
                    return true;
                }
            }
        }
        return false;
    }

    // true when a maze was already there, as the original's
    public boolean ensureMaze(BlockPos altar, RandomSource random) {
        int width = 15 + random.nextInt(8) * 2;
        int height = 15 + random.nextInt(8) * 2;
        int cx = altar.getX() >> 4;
        int cz = altar.getZ() >> 4;
        if (inRange(cx, cz, width, height)) {
            return true;
        }
        place(cx, cz, MazeGenerator.make(width, height, random.nextLong()));
        links.put(key(cx, cz), altar.above().asLong());
        setDirty();
        return false;
    }

    void place(int x, int z, MazeGenerator maze) {
        int ox = x - (1 + maze.width / 2);
        int oz = z - (1 + maze.height / 2);
        for (int i = 0; i < maze.width; i++) {
            for (int j = 0; j < maze.height; j++) {
                if (maze.grid[j][i] > 0) {
                    cells.put(key(ox + i, oz + j), (short) maze.grid[j][i]);
                }
            }
        }
        setDirty();
    }

    public ChunkPos nearestPortal(int x, int z) {
        ChunkPos best = null;
        long bestDistance = Long.MAX_VALUE;
        for (Map.Entry<Long, Short> entry : cells.entrySet()) {
            if (new Cell(entry.getValue()).feature != Cell.PORTAL) {
                continue;
            }
            ChunkPos at = new ChunkPos(entry.getKey());
            long distance = (long) (at.x - x) * (at.x - x) + (long) (at.z - z) * (at.z - z);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = at;
            }
        }
        return best;
    }

    public BlockPos linkOf(ChunkPos portalCell) {
        Long link = links.get(portalCell.toLong());
        return link == null ? null : BlockPos.of(link);
    }

    // counts up by 1, or by 2 one time in four; the boss kind cycles through four
    public int nextBoss(RandomSource random) {
        bossCount++;
        if (random.nextFloat() < 0.25F) {
            bossCount++;
        }
        setDirty();
        return bossCount % 4;
    }

    private static Labyrinth load(CompoundTag tag, HolderLookup.Provider registries) {
        Labyrinth labyrinth = new Labyrinth();
        for (Tag entry : tag.getList("cells", Tag.TAG_COMPOUND)) {
            CompoundTag cell = (CompoundTag) entry;
            labyrinth.cells.put(key(cell.getInt("x"), cell.getInt("z")), cell.getShort("cell"));
        }
        for (Tag entry : tag.getList("links", Tag.TAG_COMPOUND)) {
            CompoundTag link = (CompoundTag) entry;
            labyrinth.links.put(link.getLong("cell"), link.getLong("portal"));
        }
        labyrinth.bossCount = tag.getInt("bossCount");
        return labyrinth;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        cells.forEach((at, packed) -> {
            ChunkPos pos = new ChunkPos(at);
            CompoundTag cell = new CompoundTag();
            cell.putInt("x", pos.x);
            cell.putInt("z", pos.z);
            cell.putShort("cell", packed);
            list.add(cell);
        });
        tag.put("cells", list);
        ListTag linked = new ListTag();
        links.forEach((cell, portal) -> {
            CompoundTag link = new CompoundTag();
            link.putLong("cell", cell);
            link.putLong("portal", portal);
            linked.add(link);
        });
        tag.put("links", linked);
        tag.putInt("bossCount", bossCount);
        return tag;
    }
}
