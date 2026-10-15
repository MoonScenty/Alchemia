package me.moonscenty.alchemia.outer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import me.moonscenty.alchemia.aura.AuraGeneration;
import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.block.CrystalBlock;
import me.moonscenty.alchemia.block.CrystalType;
import me.moonscenty.alchemia.block.LootBlock;
import me.moonscenty.alchemia.block.eldritch.AncientBedrockBlock;
import me.moonscenty.alchemia.block.eldritch.CrabSpawnerBlockEntity;
import me.moonscenty.alchemia.block.eldritch.EldritchLockBlockEntity;
import me.moonscenty.alchemia.block.entity.ArcanePedestalBlockEntity;
import me.moonscenty.alchemia.block.taint.TaintFibreBlock;
import me.moonscenty.alchemia.entity.Champions;
import me.moonscenty.alchemia.entity.EldritchGuardian;
import me.moonscenty.alchemia.entity.cult.CultistPortal;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModEntities;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;

/**
 * Builds a chunk of a maze as the original built it: the rooms and passages of {@code GenCommon} and its kin, with
 * the same shapes, the same odds and the dice thrown in the same order.
 * <p>
 * Every cell is one chunk, built at height fifty. Like the original, a cell's openings reach one block into the
 * next chunk, and the four quarters of the great hall run into each other. Two dice are thrown: the chunk's own, for
 * the shape of things, and the world's, for what grows on them (the crust, the glyphs, the crystals, the urns); here
 * both are seeded from the world and the chunk.
 * <p>
 * Where the original left a doorway with no door to a great-hall quarter with no way in -- which put a stray door
 * frame at the corner of the world -- this leaves it out.
 */
public final class MazeBuilder {
    public static final int Y = 50;

    // what placeBlock was told to put
    private static final int PUNCTURE = 1;
    private static final int STONE = 2;
    private static final int CORNER_LOW_LEFT = 3;
    private static final int CORNER_LOW_RIGHT = 4;
    private static final int CORNER_HIGH_LEFT = 5;
    private static final int CORNER_HIGH_RIGHT = 6;
    private static final int GLOW = 7;
    private static final int ROCK = 8;
    private static final int AIR = 9;
    private static final int STAIRS = 10;
    private static final int STAIRS_HIGH = 11;
    private static final int DOORWAY = 15;
    private static final int LOCK = 16;
    private static final int DOOR = 17;
    private static final int INNER_ROCK = 18;
    private static final int TILE = 19;
    private static final int CRUST = 21;

    /** A tunnel's cross-section, rows top to bottom. */
    private static final int[][] CONNECT = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 8, 8, 8, 8, 8, 8, 8, 0, 0},
            {0, 8, 8, 2, 2, 2, 2, 2, 8, 8, 0},
            {0, 8, 2, 5, 9, 9, 9, 6, 2, 8, 0},
            {0, 8, 2, 9, 9, 9, 9, 9, 2, 8, 0},
            {0, 8, 2, 9, 9, 9, 9, 9, 2, 8, 0},
            {0, 8, 2, 9, 9, 9, 9, 9, 2, 8, 0},
            {0, 8, 2, 3, 9, 9, 9, 4, 2, 8, 0},
            {0, 8, 8, 2, 2, 2, 2, 2, 8, 8, 0},
            {0, 0, 8, 8, 8, 8, 8, 8, 8, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};

    /** The great hall's round door, across and up. */
    private static final int[][] DOORWAY_PATTERN = {
            {0, 2, 2, 2, 2, 2, 0},
            {2, 2, 9, 9, 9, 2, 2},
            {2, 9, 9, 9, 9, 9, 2},
            {2, 9, 9, 1, 9, 9, 2},
            {2, 9, 9, 9, 9, 9, 2},
            {2, 2, 9, 9, 9, 2, 2},
            {0, 2, 2, 2, 2, 2, 0}};

    /** The order the original went round a block's sides in. */
    private static final Direction[] SIDES = {Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH,
            Direction.WEST, Direction.EAST};

    private final WorldGenLevel level;
    private final Random random;
    private final Random world;
    private final Cell cell;
    private final List<BlockPos> decoCommon = new ArrayList<>();
    private final List<BlockPos> crabSpawner = new ArrayList<>();
    private final List<BlockPos> decoUrn = new ArrayList<>();
    private final List<BlockPos> punctures = new ArrayList<>();
    private final List<BlockPos> stairs = new ArrayList<>();

    private MazeBuilder(WorldGenLevel level, Random random, Random world, Cell cell) {
        this.level = level;
        this.random = random;
        this.world = world;
        this.cell = cell;
    }

    /** Builds this chunk's cell, if it is one. */
    public static void generate(WorldGenLevel level, Labyrinth labyrinth, int cx, int cz) {
        Cell cell = labyrinth.get(cx, cz);
        if (cell == null) {
            return;
        }
        long seed = level.getSeed() ^ (cx * 341873128712L + cz * 132897987541L);
        MazeBuilder builder = new MazeBuilder(level, new Random(seed), new Random(seed * 31L + 17L), cell);
        builder.build(cx, cz);
    }

    private void build(int cx, int cz) {
        switch (cell.feature) {
            case Cell.PORTAL -> portal(cx, cz);
            case Cell.BOSS_UPPER_LEFT, Cell.BOSS_UPPER_RIGHT, Cell.BOSS_LOWER_LEFT, Cell.BOSS_LOWER_RIGHT ->
                    bossRoom(cx, cz);
            case Cell.KEY_ROOM -> keyRoom(cx, cz);
            case Cell.NEST -> nest(cx, cz);
            case Cell.LIBRARY -> library(cx, cz);
            case Cell.TURRET, Cell.TURRET_PASSAGE -> turretRoom(cx, cz);
            case Cell.PASSAGE_ROOM -> passageRoom(cx, cz);
            default -> {
                random.nextInt(1);
                passage(cx, cz);
            }
        }
        decorate();
        for (BlockPos at : stairs) {
            BlockState there = level.getBlockState(at);
            if (there.getBlock() instanceof StairBlock) {
                level.setBlock(at, Block.updateFromNeighbourShapes(there, level, at), Block.UPDATE_CLIENTS);
            }
        }
    }

    // --- the blocks ----------------------------------------------------------

    private static BlockState ancientStone() {
        return ModBlocks.ANCIENT_STONE.block().get().defaultBlockState();
    }

    private static BlockState rock() {
        return ModBlocks.ANCIENT_ROCK.get().defaultBlockState();
    }

    private static BlockState tile() {
        return ModBlocks.ANCIENT_TILE.get().defaultBlockState();
    }

    private static BlockState glyphed() {
        return ModBlocks.GLYPHED_STONE.get().defaultBlockState();
    }

    private static BlockState slab(boolean top) {
        return ModBlocks.ANCIENT_STONE.slab().get().defaultBlockState()
                .setValue(net.minecraft.world.level.block.SlabBlock.TYPE, top ? SlabType.TOP : SlabType.BOTTOM);
    }

    /** The original's stairs by their old number: the low bits which way (east, west, south, north), four upside down. */
    private static BlockState stairs(int meta) {
        Direction facing = switch (meta & 3) {
            case 0 -> Direction.EAST;
            case 1 -> Direction.WEST;
            case 2 -> Direction.SOUTH;
            default -> Direction.NORTH;
        };
        return ModBlocks.ANCIENT_STONE.stairs().get().defaultBlockState().setValue(StairBlock.FACING, facing)
                .setValue(StairBlock.HALF, (meta & 4) != 0 ? Half.TOP : Half.BOTTOM);
    }

    /** The stone of the Outer Lands, any of it: what the decorations must touch to grow. */
    private static boolean isStone(BlockState state) {
        return state.is(ModBlocks.ANCIENT_STONE.block().get()) || state.is(ModBlocks.ANCIENT_ROCK.get())
                || state.is(ModBlocks.ANCIENT_TILE.get()) || state.is(ModBlocks.ANCIENT_DOORWAY.get())
                || state.is(ModBlocks.GLYPHED_STONE.get()) || state.is(ModBlocks.CRUSTED_STONE.get())
                || state.is(ModBlocks.GLOWING_CRUSTED_STONE.get()) || state.is(ModBlocks.ELDRITCH_STONE.block().get())
                || state.is(ModBlocks.ANCIENT_BEDROCK.get()) || state.is(ModBlocks.POROUS_STONE.get());
    }

    private void place(int x, int y, int z, int type) {
        place(x, y, z, type, null);
    }

    private void place(int x, int y, int z, int type, Direction dir) {
        BlockPos pos = new BlockPos(x, y, z);
        BlockState state = null;
        switch (type) {
            case DOORWAY -> {
                state = ModBlocks.ANCIENT_DOORWAY.get().defaultBlockState();
                forget(pos);
            }
            case LOCK -> {
                state = ModBlocks.ELDRITCH_LOCK.get().defaultBlockState();
                forget(pos);
            }
            case ROCK, INNER_ROCK -> state = rock();
            case DOOR -> state = ModBlocks.ELDRITCH_DOOR.get().defaultBlockState();
            case AIR -> {
                state = Blocks.AIR.defaultBlockState();
                forget(pos);
            }
            case PUNCTURE -> {
                state = Blocks.AIR.defaultBlockState();
                forget(pos);
                punctures.add(pos);
            }
            case STONE -> {
                if (cell.feature == Cell.NEST && world.nextInt(3) == 0) {
                    state = crust(pos);
                } else {
                    if (world.nextInt(25) == 0) {
                        boolean crab = cell.feature == Cell.NEST || world.nextInt(50) == 0;
                        if (crab && (cell.feature == Cell.PASSAGE || cell.feature == Cell.NEST)) {
                            crabSpawner.add(pos);
                        } else {
                            decoCommon.add(pos);
                        }
                    }
                    state = ancientStone();
                }
            }
            case CRUST -> state = crust(pos);
            case TILE -> {
                if (!level.getBlockState(pos).is(ModBlocks.ANCIENT_ROCK.get())) {
                    state = tile();
                }
            }
            case STAIRS -> state = stairs(switch (dir) {
                case NORTH -> 3;
                case SOUTH -> 2;
                case WEST -> 1;
                default -> 0;
            });
            case STAIRS_HIGH -> state = stairs(switch (dir) {
                case NORTH -> 7;
                case SOUTH -> 6;
                case WEST -> 5;
                default -> 4;
            });
            case CORNER_LOW_LEFT -> {
                if (world.nextFloat() < 0.005F) {
                    decoUrn.add(pos);
                }
                state = stairs(dir.getAxis() == Direction.Axis.Z ? 1 : 3);
            }
            case CORNER_LOW_RIGHT -> {
                if (world.nextFloat() < 0.005F) {
                    decoUrn.add(pos);
                }
                state = stairs(dir.getAxis() == Direction.Axis.Z ? 0 : 2);
            }
            case CORNER_HIGH_LEFT -> state = stairs(dir.getAxis() == Direction.Axis.Z ? 5 : 7);
            case CORNER_HIGH_RIGHT -> state = stairs(dir.getAxis() == Direction.Axis.Z ? 4 : 6);
            case GLOW -> state = ModBlocks.GLOWING_CRUSTED_STONE.get().defaultBlockState();
            default -> {
            }
        }
        if (state != null) {
            set(pos, state);
        }
    }

    private BlockState crust(BlockPos pos) {
        if (world.nextInt(25) == 0) {
            return ModBlocks.GLOWING_CRUSTED_STONE.get().defaultBlockState();
        }
        if (world.nextInt(25) == 0) {
            boolean crab = cell.feature == Cell.NEST || (cell.feature == Cell.CRUSTED && world.nextBoolean())
                    || world.nextInt(25) == 0;
            if (crab && (cell.feature == Cell.PASSAGE || cell.feature == Cell.NEST || cell.feature == Cell.CRUSTED)) {
                crabSpawner.add(pos);
            }
        }
        return ModBlocks.CRUSTED_STONE.get().defaultBlockState();
    }

    private void set(BlockPos pos, BlockState state) {
        level.setBlock(pos, state, Block.UPDATE_CLIENTS);
        if (state.getBlock() instanceof StairBlock) {
            stairs.add(pos);
        }
    }

    private void forget(BlockPos pos) {
        decoCommon.remove(pos);
        crabSpawner.remove(pos);
        decoUrn.remove(pos);
    }

    private void obelisk(BlockPos pos) {
        set(pos, ModBlocks.ELDRITCH_OBELISK.get().defaultBlockState());
        for (int up = 1; up <= 4; up++) {
            set(pos.above(up), ModBlocks.ELDRITCH_OBELISK_BODY.get().defaultBlockState());
        }
    }

    // --- what grows on it ----------------------------------------------------

    private void decorate() {
        for (BlockPos at : decoUrn) {
            if (level.isEmptyBlock(at.above())) {
                set(at, tile());
                float roll = world.nextFloat();
                int rarity = roll < 0.025F ? 2 : roll < 0.1F ? 1 : 0;
                set(at.above(), ModBlocks.LOOT_URN.get().defaultBlockState().setValue(LootBlock.RARITY, rarity));
            }
        }
        for (BlockPos at : decoCommon) {
            int exposed = exposed(at);
            if (exposed > 0 && (exposed == 1 || !voidInSight(at)) && touchesStone(at)) {
                int kind = world.nextInt(3) != 0 ? 0 : world.nextInt(8) != 0 ? 1 : 2;
                switch (kind) {
                    case 0 -> set(at, ModBlocks.ELDRITCH_STONE.block().get().defaultBlockState());
                    case 1 -> set(at, glyphed());
                    default -> {
                        for (Direction side : SIDES) {
                            if (level.isEmptyBlock(at.relative(side))) {
                                CrystalType type = CrystalType.values()[world.nextInt(7)];
                                level.setBlock(at.relative(side), ModBlocks.CRYSTALS.get(type).get().defaultBlockState()
                                        .setValue(CrystalBlock.FACING, side)
                                        .setValue(CrystalBlock.AGE, world.nextInt(3) + 1), Block.UPDATE_CLIENTS);
                                break;
                            }
                        }
                    }
                }
            }
        }
        for (BlockPos at : crabSpawner) {
            if (exposed(at) == 1 && touchesStone(at)) {
                set(at, ModBlocks.CRAB_SPAWNER.get().defaultBlockState());
                if (level.getBlockEntity(at) instanceof CrabSpawnerBlockEntity vent) {
                    for (Direction side : SIDES) {
                        if (level.isEmptyBlock(at.relative(side))) {
                            vent.setFacing(side);
                            break;
                        }
                    }
                }
            }
        }
        for (BlockPos at : punctures) {
            for (Direction side : SIDES) {
                if (level.getBlockState(at.relative(side)).is(ModBlocks.ANCIENT_ROCK.get())
                        && level.getBlockState(at.relative(side, 2)).is(ModBlocks.ANCIENT_BEDROCK.get())) {
                    level.setBlock(at.relative(side), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                    // the original's world told the bedrock behind it that it had been laid bare
                    AncientBedrockBlock.open(level, at.relative(side, 2));
                    break;
                }
            }
        }
    }

    /** How many of its sides are open to something it can be seen through. */
    private int exposed(BlockPos pos) {
        int open = 0;
        for (Direction side : SIDES) {
            BlockPos next = pos.relative(side);
            if (!level.getBlockState(next).isSolidRender(level, next)) {
                open++;
            }
        }
        return open;
    }

    private boolean touchesStone(BlockPos pos) {
        for (Direction side : SIDES) {
            if (isStone(level.getBlockState(pos.relative(side)))) {
                return true;
            }
        }
        return false;
    }

    private boolean voidInSight(BlockPos pos) {
        for (Direction side : SIDES) {
            BlockPos next = pos.relative(side);
            if (!level.getBlockState(next).isSolidRender(level, next)
                    && level.getBlockState(pos.relative(side.getOpposite())).is(ModBlocks.VACUUM.get())) {
                return true;
            }
        }
        return false;
    }

    private boolean nextToSolid(BlockPos pos) {
        for (Direction side : SIDES) {
            BlockPos next = pos.relative(side);
            if (level.getBlockState(next).isFaceSturdy(level, next, side.getOpposite())) {
                return true;
            }
        }
        return false;
    }

    /** Inclusive, as the original's helper. */
    private int between(int low, int high) {
        return low >= high ? low : world.nextInt(high - low + 1) + low;
    }

    // --- the shared shapes ---------------------------------------------------

    /** The tunnels out of a cell on its open sides, so deep; a room's taper at the inner end. */
    private void connections(int cx, int cz, int y, int depth, boolean tip) {
        int x = cx * 16;
        int z = cz * 16;
        if (cell.north) {
            connection(depth, tip, (w, h, d) -> place(x + 3 + w, y + 10 - h, z + d, CONNECT[h][w], Direction.NORTH));
        }
        if (cell.south) {
            connection(depth, tip,
                    (w, h, d) -> place(x + 3 + w, y + 10 - h, z + 16 - d, CONNECT[h][w], Direction.SOUTH));
        }
        if (cell.east) {
            connection(depth, tip,
                    (w, h, d) -> place(x + 16 - d, y + 10 - h, z + 3 + w, CONNECT[h][w], Direction.EAST));
        }
        if (cell.west) {
            connection(depth, tip, (w, h, d) -> place(x + d, y + 10 - h, z + 3 + w, CONNECT[h][w], Direction.WEST));
        }
    }

    @FunctionalInterface
    private interface Layer {
        void put(int w, int h, int d);
    }

    private static void connection(int depth, boolean tip, Layer layer) {
        for (int d = 0; d <= depth; d++) {
            int low = d == depth && tip ? 2 : d == depth - 1 && tip ? 1 : 0;
            int high = d == depth && tip ? 9 : d == depth - 1 && tip ? 10 : 11;
            for (int w = low; w < high; w++) {
                for (int h = low; h < high; h++) {
                    if (d == depth && tip && CONNECT[h][w] == ROCK) {
                        continue;
                    }
                    layer.put(w, h, d);
                }
            }
        }
    }

    /** The outer wall of a thirteen-square room, with a doorway on each open side. */
    private boolean outerWall(int a, int b, int c, int edge, int far, int gapLow, int gapHigh, int gapTop) {
        return (a == edge || a == far || b == edge || b == far)
                && (a != edge || b <= gapLow || b >= gapHigh || !cell.west || c >= gapTop)
                && (a != far || b <= gapLow || b >= gapHigh || !cell.east || c >= gapTop)
                && (b != edge || a <= gapLow || a >= gapHigh || !cell.north || c >= gapTop)
                && (b != far || a <= gapLow || a >= gapHigh || !cell.south || c >= gapTop);
    }

    private void shell(int x, int y, int z, int height) {
        for (int a = 2; a <= 14; a++) {
            for (int b = 2; b <= 14; b++) {
                for (int c = 1; c <= height; c++) {
                    place(x + a, y + c, z + b, AIR);
                }
            }
        }
        for (int a = 2; a <= 14; a++) {
            for (int b = 2; b <= 14; b++) {
                for (int c = 1; c <= height; c++) {
                    if (outerWall(a, b, c, 2, 14, 3, 12, 10)) {
                        place(x + a, y + c, z + b, ROCK);
                    }
                }
            }
        }
    }

    private void floorStairs(int x, int y, int z, int height, int type) {
        for (int g = 0; g <= 4; g++) {
            place(x + 6 + g, y + height, z + 4, type, Direction.NORTH);
            place(x + 6 + g, y + height, z + 12, type, Direction.SOUTH);
            place(x + 12, y + height, z + 6 + g, type, Direction.EAST);
            place(x + 4, y + height, z + 6 + g, type, Direction.WEST);
        }
    }

    /** A room with a rock ring and a window cross in each wall, and stepped floor and ceiling. */
    private void chamberA(int cx, int cz, int y) {
        int x = cx * 16;
        int z = cz * 16;
        shell(x, y, z, 11);
        for (int a = 3; a <= 13; a++) {
            for (int b = 3; b <= 13; b++) {
                for (int c = 2; c <= 10; c++) {
                    if (a == 3 || a == 13 || b == 3 || b == 13) {
                        if (c > 3 && c < 9 && (a == 8 || b == 8)
                                || c > 4 && c < 8 && (a == 7 || b == 7 || a == 9 || b == 9)) {
                            if (a != 8 && b != 8 || c != 6) {
                                place(x + a, y + c, z + b, TILE);
                            }
                        } else {
                            place(x + a, y + c, z + b, INNER_ROCK);
                        }
                    }
                }
            }
        }
        steppedFloor(x, y, z, STONE, 12);
        floorStairs(x, y, z, 2, STAIRS);
    }

    private void steppedFloor(int x, int y, int z, int floor, int top) {
        for (int a = 2; a <= 14; a++) {
            for (int b = 2; b <= 14; b++) {
                place(x + a, y, z + b, ROCK);
                place(x + a, y + 1, z + b, floor == TILE ? TILE : STONE);
                place(x + a, y + top, z + b, ROCK);
                place(x + a, y + top - 1, z + b, STONE);
                int step = Math.min(Math.abs(8 - a), Math.abs(8 - b));
                for (int g = 0; g < step - 1; g++) {
                    place(x + a, y + 1 + g, z + b, floor);
                }
                if (a > 3 && a < 13 && b > 3 && b < 13) {
                    for (int g = 0; g < step; g++) {
                        place(x + a, y + 11 - g, z + b, floor);
                    }
                }
            }
        }
    }

    /** A room with a stone ring, a lower ceiling and tiled pads in its corners. */
    private void chamberB(int cx, int cz, int y) {
        int x = cx * 16;
        int z = cz * 16;
        shell(x, y, z, 11);
        for (int a = 3; a <= 13; a++) {
            for (int b = 3; b <= 13; b++) {
                for (int c = 2; c <= 10; c++) {
                    if (a == 3 || a == 13 || b == 3 || b == 13) {
                        place(x + a, y + c, z + b, STONE);
                    }
                }
            }
        }
        for (int a = 2; a <= 14; a++) {
            for (int b = 2; b <= 14; b++) {
                place(x + a, y, z + b, ROCK);
                place(x + a, y + 1, z + b, STONE);
                place(x + a, y + 11, z + b, ROCK);
                place(x + a, y + 10, z + b, STONE);
                if (a > 3 && a < 13 && b > 3 && b < 13) {
                    if (a <= 5 && b <= 5 || a <= 5 && b >= 11 || a >= 11 && b <= 5 || a >= 11 && b >= 11) {
                        place(x + a, y + 2, z + b, STONE);
                        place(x + a, y + 9, z + b, STONE);
                    }
                    if (a == 5 && b == 5 || a == 5 && b == 11 || a == 11 && b == 5 || a == 11 && b == 11) {
                        set(new BlockPos(x + a, y + 3, z + b), tile());
                        set(new BlockPos(x + a, y + 8, z + b), tile());
                    }
                }
            }
        }
        floorStairs(x, y, z, 2, STAIRS);
        floorStairs(x, y, z, 9, STAIRS_HIGH);
    }

    // --- the rooms -----------------------------------------------------------

    /** A passage: a small box, tunnels out of its open sides, walls with fillets on the others. */
    private void passage(int cx, int cz) {
        int x = cx * 16;
        int z = cz * 16;
        int y = Y;
        for (int w = 2; w <= 6; w++) {
            for (int h = 2; h <= 6; h++) {
                for (int d = 1; d <= 6; d++) {
                    place(x + 4 + w, y + 2 + d, z + 4 + h, AIR);
                }
            }
        }
        connections(cx, cz, y, 4, false);
        int mod = cell.north && cell.south && cell.west && cell.east && random.nextBoolean() ? 1 : 0;
        for (int w = 1; w <= 7; w++) {
            for (int h = 1; h <= 7; h++) {
                if (w == 4 && h == 4 && mod == 1) {
                    place(x + 8, y + 2, z + 8, GLOW);
                    place(x + 8, y + 8, z + 8, GLOW);
                } else {
                    place(x + 4 + w, y + 2, z + 4 + h, punctured());
                    place(x + 4 + w, y + 8, z + 4 + h, punctured());
                }
                place(x + 4 + w, y + 1, z + 4 + h, ROCK);
                place(x + 4 + w, y + 9, z + 4 + h, ROCK);
            }
        }
        if (cell.north) {
            for (int w = 2 + mod; w <= 8 - mod; w++) {
                for (int h = 2 + mod; h <= 8 - mod; h++) {
                    place(x + 3 + w, y + 10 - h, z + 5, CONNECT[h][w], Direction.NORTH);
                }
            }
            if (mod == 0) {
                if (cell.west) {
                    place(x + 6, y + 3, z + 6, CORNER_LOW_LEFT, Direction.EAST);
                    place(x + 6, y + 7, z + 6, CORNER_HIGH_LEFT, Direction.EAST);
                }
                if (cell.east) {
                    place(x + 10, y + 3, z + 6, CORNER_LOW_LEFT, Direction.EAST);
                    place(x + 10, y + 7, z + 6, CORNER_HIGH_LEFT, Direction.EAST);
                }
            }
        } else {
            for (int w = 1; w <= 7; w++) {
                for (int h = 1; h <= 7; h++) {
                    place(x + 4 + w, y + 9 - h, z + 5, punctured());
                    place(x + 4 + w, y + 9 - h, z + 4, ROCK);
                    place(x + 4 + w, y + 9 - h, z + 3, PUNCTURE);
                }
            }
            for (int w = 2; w <= 6; w++) {
                place(x + 4 + w, y + 3, z + 6, CORNER_LOW_LEFT, Direction.EAST);
                place(x + 4 + w, y + 7, z + 6, CORNER_HIGH_LEFT, Direction.EAST);
            }
        }
        if (cell.south) {
            for (int w = 2 + mod; w <= 8 - mod; w++) {
                for (int h = 2 + mod; h <= 8 - mod; h++) {
                    place(x + 3 + w, y + 10 - h, z + 11, CONNECT[h][w], Direction.SOUTH);
                }
            }
            if (mod == 0) {
                if (cell.west) {
                    place(x + 6, y + 3, z + 10, CORNER_LOW_RIGHT, Direction.EAST);
                    place(x + 6, y + 7, z + 10, CORNER_HIGH_RIGHT, Direction.EAST);
                }
                if (cell.east) {
                    place(x + 10, y + 3, z + 10, CORNER_LOW_RIGHT, Direction.EAST);
                    place(x + 10, y + 7, z + 10, CORNER_HIGH_RIGHT, Direction.EAST);
                }
            }
        } else {
            for (int w = 1; w <= 7; w++) {
                for (int h = 1; h <= 7; h++) {
                    place(x + 4 + w, y + 9 - h, z + 11, punctured());
                    place(x + 4 + w, y + 9 - h, z + 12, ROCK);
                }
            }
            for (int w = 2; w <= 6; w++) {
                place(x + 4 + w, y + 3, z + 10, CORNER_LOW_RIGHT, Direction.EAST);
                place(x + 4 + w, y + 7, z + 10, CORNER_HIGH_RIGHT, Direction.EAST);
            }
        }
        if (cell.east) {
            for (int w = 2 + mod; w <= 8 - mod; w++) {
                for (int h = 2 + mod; h <= 8 - mod; h++) {
                    place(x + 11, y + 10 - h, z + 3 + w, CONNECT[h][w], Direction.EAST);
                }
            }
            if (mod == 0) {
                if (cell.north) {
                    place(x + 10, y + 3, z + 6, CORNER_LOW_RIGHT, Direction.NORTH);
                    place(x + 10, y + 7, z + 6, CORNER_HIGH_RIGHT, Direction.NORTH);
                }
                if (cell.south) {
                    place(x + 10, y + 3, z + 10, CORNER_LOW_RIGHT, Direction.NORTH);
                    place(x + 10, y + 7, z + 10, CORNER_HIGH_RIGHT, Direction.NORTH);
                }
            }
        } else {
            for (int w = 1; w <= 7; w++) {
                for (int h = 1; h <= 7; h++) {
                    place(x + 11, y + 9 - h, z + 4 + w, punctured());
                    place(x + 12, y + 9 - h, z + 4 + w, ROCK);
                }
            }
            for (int w = 2; w <= 6; w++) {
                place(x + 10, y + 3, z + 4 + w, CORNER_LOW_RIGHT, Direction.NORTH);
                place(x + 10, y + 7, z + 4 + w, CORNER_HIGH_RIGHT, Direction.NORTH);
            }
        }
        if (cell.west) {
            for (int w = 2 + mod; w <= 8 - mod; w++) {
                for (int h = 2 + mod; h <= 8 - mod; h++) {
                    place(x + 5, y + 10 - h, z + 3 + w, CONNECT[h][w], Direction.WEST);
                }
            }
            if (mod == 0) {
                if (cell.north) {
                    place(x + 6, y + 3, z + 6, CORNER_LOW_LEFT, Direction.NORTH);
                    place(x + 6, y + 7, z + 6, CORNER_HIGH_LEFT, Direction.NORTH);
                }
                if (cell.south) {
                    place(x + 6, y + 3, z + 10, CORNER_LOW_LEFT, Direction.NORTH);
                    place(x + 6, y + 7, z + 10, CORNER_HIGH_LEFT, Direction.NORTH);
                }
            }
        } else {
            for (int w = 1; w <= 7; w++) {
                for (int h = 1; h <= 7; h++) {
                    place(x + 5, y + 9 - h, z + 4 + w, punctured());
                    place(x + 4, y + 9 - h, z + 4 + w, ROCK);
                }
            }
            for (int w = 2; w <= 6; w++) {
                place(x + 6, y + 3, z + 4 + w, CORNER_LOW_LEFT, Direction.NORTH);
                place(x + 6, y + 7, z + 4 + w, CORNER_HIGH_LEFT, Direction.NORTH);
            }
        }
        if (mod == 1) {
            place(x + 5, y + 3, z + 5, CORNER_LOW_LEFT, Direction.EAST);
            place(x + 5, y + 7, z + 5, CORNER_HIGH_LEFT, Direction.EAST);
            place(x + 5, y + 3, z + 6, CORNER_LOW_LEFT, Direction.NORTH);
            place(x + 5, y + 7, z + 6, CORNER_HIGH_LEFT, Direction.NORTH);
            place(x + 11, y + 3, z + 5, CORNER_LOW_LEFT, Direction.EAST);
            place(x + 11, y + 7, z + 5, CORNER_HIGH_LEFT, Direction.EAST);
            place(x + 11, y + 3, z + 6, CORNER_LOW_RIGHT, Direction.NORTH);
            place(x + 11, y + 7, z + 6, CORNER_HIGH_RIGHT, Direction.NORTH);
            place(x + 5, y + 3, z + 11, CORNER_LOW_LEFT, Direction.NORTH);
            place(x + 5, y + 7, z + 11, CORNER_HIGH_LEFT, Direction.NORTH);
            place(x + 6, y + 3, z + 11, CORNER_LOW_RIGHT, Direction.EAST);
            place(x + 6, y + 7, z + 11, CORNER_HIGH_RIGHT, Direction.EAST);
            place(x + 11, y + 3, z + 11, CORNER_LOW_RIGHT, Direction.NORTH);
            place(x + 11, y + 7, z + 11, CORNER_HIGH_RIGHT, Direction.NORTH);
            place(x + 10, y + 3, z + 11, CORNER_LOW_RIGHT, Direction.EAST);
            place(x + 10, y + 7, z + 11, CORNER_HIGH_RIGHT, Direction.EAST);
        }
        if (cell.feature == Cell.OVERGROWN) {
            for (int w = -4; w <= 4; w++) {
                for (int h = -4; h <= 4; h++) {
                    for (int j = -4; j <= 4; j++) {
                        BlockPos at = new BlockPos(x + 8 + w, y + 4 + h, z + 8 + j);
                        BlockState there = level.getBlockState(at);
                        if ((level.isEmptyBlock(at) || isStone(there)
                                || there.is(ModBlocks.ANCIENT_STONE.stairs().get())) && random.nextBoolean()) {
                            place(at.getX(), at.getY(), at.getZ(), CRUST);
                        }
                    }
                }
            }
        }
        if (cell.feature == Cell.TAINTED) {
            for (int w = -4; w <= 4; w++) {
                for (int h = -3; h <= 3; h++) {
                    for (int j = -4; j <= 4; j++) {
                        BlockPos at = new BlockPos(x + 8 + w, y + 4 + h, z + 8 + j);
                        if (level.isEmptyBlock(at) && nextToSolid(at) && random.nextInt(3) != 0) {
                            level.setBlock(at, TaintFibreBlock.fitted(level, at,
                                    ModBlocks.TAINT_FIBRE.get().defaultBlockState()), Block.UPDATE_CLIENTS);
                        }
                    }
                }
            }
            AuraHandler.add(level, new BlockPos(x + 8, y + 4, z + 8), ModAspects.FLUX, AuraGeneration.BASE * 5);
        }
        if (cell.feature == Cell.WEBBED) {
            for (int w = -3; w <= 3; w++) {
                for (int h = -3; h <= 3; h++) {
                    for (int j = -3; j <= 3; j++) {
                        BlockPos at = new BlockPos(x + 8 + w, y + 4 + h, z + 8 + j);
                        if (level.isEmptyBlock(at) && random.nextFloat() < 0.35F) {
                            level.setBlock(at, Blocks.COBWEB.defaultBlockState(), Block.UPDATE_CLIENTS);
                        }
                    }
                }
            }
            BlockPos nest = new BlockPos(x + 8, y + 4, z + 8);
            level.setBlock(nest, Blocks.SPAWNER.defaultBlockState(), Block.UPDATE_CLIENTS);
            if (level.getBlockEntity(nest) instanceof SpawnerBlockEntity spawner) {
                spawner.setEntityId(ModEntities.MIND_SPIDER.get(), level.getRandom());
            }
        }
    }

    /** A punctured passage's walls, floor and ceiling are holed a third of the time. */
    private int punctured() {
        return cell.feature == Cell.PUNCTURED && random.nextInt(3) == 0 ? PUNCTURE : STONE;
    }

    private void turretRoom(int cx, int cz) {
        passage(cx, cz);
        int x = cx * 16;
        int z = cz * 16;
        int y = Y;
        if (random.nextBoolean()) {
            place(x + 8, y + 2, z + 8, DOORWAY);
            place(x + 8, y + 3, z + 8, DOORWAY);
            me.moonscenty.alchemia.entity.EldritchAutocaster.place(level, new BlockPos(x + 8, y + 4, z + 8),
                    Direction.UP);
        } else {
            place(x + 8, y + 8, z + 8, DOORWAY);
            place(x + 8, y + 7, z + 8, DOORWAY);
            me.moonscenty.alchemia.entity.EldritchAutocaster.place(level, new BlockPos(x + 8, y + 6, z + 8),
                    Direction.DOWN);
        }
    }

    private void passageRoom(int cx, int cz) {
        int x = cx * 16;
        int z = cz * 16;
        int y = Y;
        if (random.nextBoolean()) {
            chamberA(cx, cz, y);
        } else {
            chamberB(cx, cz, y);
        }
        connections(cx, cz, y, 3, true);
        switch (random.nextInt(8)) {
            case 0 -> {
                for (int q = 0; q < 16; q++) {
                    int dx = between(-3, 3);
                    int dz = between(-3, 3);
                    level.setBlock(new BlockPos(x + 8 + dx, y + 2 + q / 2, z + 8 + dz),
                            ModBlocks.TAINT_DUST.get().defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }
            case 1 -> guardians(x, y, z);
            case 2 -> obelisk(new BlockPos(x + 8, y + 4, z + 8));
            case 3, 4, 5 -> {
                int kind = random.nextInt(4);
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        BlockPos at = new BlockPos(x + 8 + i, y + 1, z + 8 + j);
                        forget(at);
                        level.setBlock(at, switch (kind) {
                            case 0 -> Blocks.LAVA.defaultBlockState();
                            case 1 -> ModBlocks.LIQUID_DEATH.get().defaultBlockState();
                            case 2 -> ModBlocks.PURIFYING_FLUID.get().defaultBlockState();
                            default -> Blocks.WATER.defaultBlockState();
                        }, Block.UPDATE_CLIENTS);
                    }
                }
            }
            case 6 -> {
                CultistPortal portal = ModEntities.CULTIST_PORTAL.get().create(level.getLevel());
                if (portal != null) {
                    portal.moveTo(x + 8.5, y + 2, z + 8.5, 0.0F, 0.0F);
                    portal.finalizeSpawn(level, level.getCurrentDifficultyAt(portal.blockPosition()),
                            MobSpawnType.STRUCTURE, null);
                    level.addFreshEntity(portal);
                }
            }
            default -> {
            }
        }
    }

    private int byDifficulty() {
        Difficulty difficulty = level.getDifficulty();
        return difficulty == Difficulty.HARD ? 2 : difficulty == Difficulty.NORMAL ? 1 : 0;
    }

    private void guardians(int x, int y, int z) {
        int count = 1 + byDifficulty();
        if (random.nextFloat() < count / 10.0F) {
            obelisk(new BlockPos(x + 8, y + 4, z + 8));
        }
        set(new BlockPos(x + 8, y + 2, z + 8), ModBlocks.LOOT_URN.get().defaultBlockState()
                .setValue(LootBlock.RARITY, 2));
        for (int i = 0; i < count; i++) {
            guardian(x, y, z, i == 0);
        }
    }

    private void guardian(int x, int y, int z, boolean champion) {
        EldritchGuardian guardian = ModEntities.ELDRITCH_GUARDIAN.get().create(level.getLevel());
        if (guardian == null) {
            return;
        }
        double gx = x + 8.5 + between(1, 3) * between(-1, 1);
        double gz = z + 8.5 + between(1, 3) * between(-1, 1);
        guardian.moveTo(gx, y + 3, gz, 0.0F, 0.0F);
        guardian.finalizeSpawn(level, level.getCurrentDifficultyAt(guardian.blockPosition()),
                MobSpawnType.STRUCTURE, null);
        guardian.restrictTo(new BlockPos(x + 8, y + 2, z + 8), 16);
        guardian.setPersistenceRequired();
        if (champion) {
            Champions.makeRandom(guardian);
        }
        level.addFreshEntity(guardian);
    }

    private void keyRoom(int cx, int cz) {
        int x = cx * 16;
        int z = cz * 16;
        int y = Y;
        chamberA(cx, cz, y);
        connections(cx, cz, y, 3, true);
        BlockPos stand = new BlockPos(x + 8, y + 2, z + 8);
        set(stand, ModBlocks.ANCIENT_PEDESTAL.get().defaultBlockState());
        if (level.getBlockEntity(stand) instanceof ArcanePedestalBlockEntity pedestal) {
            pedestal.hold(new ItemStack(ModItems.RUNED_TABLET.get()));
        }
        int count = 2 + byDifficulty();
        for (int i = 0; i < count; i++) {
            guardian(x, y, z, i == 0 || i == 3);
        }
    }

    private void library(int cx, int cz) {
        int x = cx * 16;
        int z = cz * 16;
        int y = Y;
        chamberB(cx, cz, y);
        int[][] corners = {{5, 5}, {5, 11}, {11, 5}, {11, 11}};
        for (int[] at : corners) {
            set(new BlockPos(x + at[0], y + 4, z + at[1]), glyphed());
            set(new BlockPos(x + at[0], y + 5, z + at[1]), slab(false));
        }
        for (int[] at : corners) {
            set(new BlockPos(x + at[0], y + 7, z + at[1]), glyphed());
            set(new BlockPos(x + at[0], y + 6, z + at[1]), slab(true));
        }
        set(new BlockPos(x + 8, y + 2, z + 8), tile());
        set(new BlockPos(x + 8, y + 3, z + 8), glyphed());
        set(new BlockPos(x + 8, y + 4, z + 8), slab(false));
        set(new BlockPos(x + 8, y + 9, z + 8), tile());
        set(new BlockPos(x + 8, y + 8, z + 8), glyphed());
        set(new BlockPos(x + 8, y + 7, z + 8), slab(true));
        connections(cx, cz, y, 3, true);
    }

    private void nest(int cx, int cz) {
        int x = cx * 16;
        int z = cz * 16;
        int y = Y;
        shell(x, y, z, 9);
        for (int a = 3; a <= 13; a++) {
            for (int b = 3; b <= 13; b++) {
                for (int c = 2; c <= 8; c++) {
                    if (a == 3 || a == 13 || b == 3 || b == 13) {
                        place(x + a, y + c, z + b, CRUST);
                    }
                    if ((a == 4 && !cell.west || a == 12 && !cell.east || b == 4 && !cell.north
                            || b == 12 && !cell.south) && random.nextBoolean()) {
                        place(x + a, y + c, z + b, CRUST);
                    }
                }
            }
        }
        for (int a = 2; a <= 14; a++) {
            for (int b = 2; b <= 14; b++) {
                place(x + a, y, z + b, ROCK);
                place(x + a, y + 1, z + b, CRUST);
                place(x + a, y + 10, z + b, ROCK);
                place(x + a, y + 9, z + b, CRUST);
                place(x + a, y + 8, z + b, CRUST);
            }
        }
        place(x + 8, y + 2, z + 8, CRUST);
        place(x + 8, y + 3, z + 8, CRUST);
        place(x + 8, y + 4, z + 8, CRUST);
        place(x + 7, y + 2, z + 8, CRUST);
        place(x + 8, y + 2, z + 7, CRUST);
        place(x + 9, y + 2, z + 8, CRUST);
        place(x + 8, y + 2, z + 9, CRUST);
        if (random.nextBoolean()) {
            place(x + 7, y + 3, z + 8, CRUST);
        }
        if (random.nextBoolean()) {
            place(x + 8, y + 3, z + 7, CRUST);
        }
        if (random.nextBoolean()) {
            place(x + 9, y + 3, z + 8, CRUST);
        }
        if (random.nextBoolean()) {
            place(x + 8, y + 3, z + 9, CRUST);
        }
        if (random.nextBoolean()) {
            place(x + 8, y + 5, z + 8, GLOW);
        }
        place(x + 8, y + 8, z + 8, CRUST);
        place(x + 8, y + 7, z + 8, CRUST);
        place(x + 8, y + 6, z + 8, CRUST);
        place(x + 7, y + 8, z + 8, CRUST);
        place(x + 8, y + 8, z + 7, CRUST);
        place(x + 9, y + 8, z + 8, CRUST);
        place(x + 8, y + 8, z + 9, CRUST);
        if (random.nextBoolean()) {
            place(x + 7, y + 7, z + 8, CRUST);
        }
        if (random.nextBoolean()) {
            place(x + 8, y + 7, z + 7, CRUST);
        }
        if (random.nextBoolean()) {
            place(x + 9, y + 7, z + 8, CRUST);
        }
        if (random.nextBoolean()) {
            place(x + 8, y + 7, z + 9, CRUST);
        }
        connections(cx, cz, y, 3, true);
        for (int a = -5; a <= 5; a++) {
            for (int b = -5; b <= 5; b++) {
                BlockPos at = new BlockPos(x + 8 + a, y + 2, z + 8 + b);
                if (random.nextFloat() < 0.15F && level.isEmptyBlock(at)) {
                    float roll = random.nextFloat();
                    int rarity = roll < 0.15F ? 2 : roll < 0.4F ? 1 : 0;
                    Block loot = random.nextFloat() < 0.2F ? ModBlocks.LOOT_CRATE.get() : ModBlocks.LOOT_URN.get();
                    set(at, loot.defaultBlockState().setValue(LootBlock.RARITY, rarity));
                }
            }
        }
    }

    /** The heart of a maze: the portal, on a cap, under an obelisk, in a room with doorway-stone corners. */
    private void portal(int cx, int cz) {
        int x = cx * 16;
        int z = cz * 16;
        int y = Y;
        shell(x, y, z, 11);
        for (int a = 3; a <= 13; a++) {
            for (int b = 3; b <= 13; b++) {
                for (int c = 2; c <= 10; c++) {
                    if ((a == 3 || a == 13 || b == 3 || b == 13) && (a > 4 || b > 4) && (a > 4 || b < 12)
                            && (a < 12 || b > 4) && (a < 12 || b < 12)) {
                        place(x + a, y + c, z + b, STONE);
                    }
                }
            }
        }
        steppedFloor(x, y, z, TILE, 12);
        floorStairs(x, y, z, 2, STAIRS);
        connections(cx, cz, y, 3, true);
        for (int a = 3; a <= 13; a++) {
            for (int b = 3; b <= 13; b++) {
                for (int c = 1; c <= 11; c++) {
                    if (a <= 4 && b <= 4 || a <= 4 && b >= 12 || a >= 12 && b <= 4 || a >= 12 && b >= 12) {
                        place(x + a, y + c, z + b, DOORWAY);
                    }
                }
            }
        }
        int[][][] wraps = {
                {{5, 5, 2}, {4, 5, 2}, {5, 4, 4}},
                {{12, 5, 2}, {11, 5, 2}, {11, 4, 5}},
                {{5, 11, 3}, {4, 11, 3}, {5, 12, 4}},
                {{12, 11, 3}, {11, 11, 3}, {11, 12, 5}}};
        for (int[][] corner : wraps) {
            for (int[] at : corner) {
                place(x + at[0], y + 3, z + at[1], STAIRS, Direction.from3DDataValue(at[2]));
            }
            for (int[] at : corner) {
                place(x + at[0], y + 8, z + at[1], STAIRS_HIGH, Direction.from3DDataValue(at[2]));
            }
        }
        set(new BlockPos(x + 8, y + 2, z + 8), ModBlocks.ELDRITCH_CAP.get().defaultBlockState());
        set(new BlockPos(x + 8, y + 3, z + 8), ModBlocks.ELDRITCH_PORTAL.get().defaultBlockState());
        obelisk(new BlockPos(x + 8, y + 4, z + 8));
    }

    /** One quarter of the great hall, and its round door if this quarter is the one with the way in. */
    private void bossRoom(int cx, int cz) {
        int x = cx * 16;
        int z = cz * 16;
        int y = Y;
        hallQuarter(cx, cz, y);
        Direction dir = null;
        if (cell.north) {
            dir = Direction.NORTH;
        }
        if (cell.south) {
            dir = Direction.SOUTH;
        }
        if (cell.east) {
            dir = Direction.EAST;
        }
        if (cell.west) {
            dir = Direction.WEST;
        }
        if (dir == null) {
            return;
        }
        for (int a = 0; a <= 6; a++) {
            for (int b = 0; b <= 6; b++) {
                int xx = switch (dir) {
                    case EAST -> x + 13;
                    case WEST -> x + 3;
                    default -> x + 5 + a;
                };
                int zz = switch (dir) {
                    case NORTH -> z + 3;
                    case SOUTH -> z + 13;
                    default -> z + 5 + a;
                };
                switch (DOORWAY_PATTERN[a][b]) {
                    case 1 -> {
                        place(xx, y + 2 + b, zz, LOCK);
                        if (level.getBlockEntity(new BlockPos(xx, y + 2 + b, zz)) instanceof EldritchLockBlockEntity lock) {
                            lock.setFacing(dir);
                        }
                    }
                    case 2 -> place(xx, y + 2 + b, zz, DOORWAY);
                    case 9 -> place(xx, y + 2 + b, zz, DOOR);
                    default -> {
                    }
                }
            }
        }
    }

    /** One of the four fourteen-square quarters of the twenty-eight-square hall. */
    private void hallQuarter(int cx, int cz, int y) {
        int x = cx * 16;
        int z = cz * 16;
        boolean left = cell.feature == Cell.BOSS_UPPER_LEFT || cell.feature == Cell.BOSS_LOWER_LEFT;
        boolean upper = cell.feature == Cell.BOSS_UPPER_LEFT || cell.feature == Cell.BOSS_UPPER_RIGHT;
        int aLow = left ? 2 : 0;
        int aHigh = left ? 15 : 14;
        int bLow = upper ? 2 : 0;
        int bHigh = upper ? 15 : 14;
        int wallA = left ? 2 : 14;
        int wallB = upper ? 2 : 14;
        int ringA = left ? 3 : 13;
        int ringB = upper ? 3 : 13;
        for (int a = aLow; a <= aHigh; a++) {
            for (int b = bLow; b <= bHigh; b++) {
                for (int c = 1; c <= 11; c++) {
                    place(x + a, y + c, z + b, AIR);
                }
            }
        }
        for (int a = aLow; a <= aHigh; a++) {
            for (int b = bLow; b <= bHigh; b++) {
                for (int c = 1; c <= 11; c++) {
                    boolean wall = a == wallA || b == wallB;
                    boolean holeA = a == wallA && b > 4 && b < 12 && (left ? cell.west : cell.east) && c < 10;
                    boolean holeB = b == wallB && a > 4 && a < 12 && (upper ? cell.north : cell.south) && c < 10;
                    if (wall && !holeA && !holeB) {
                        place(x + a, y + c, z + b, ROCK);
                    }
                }
            }
        }
        int rLowA = left ? 3 : 0;
        int rHighA = left ? 15 : 13;
        int rLowB = upper ? 3 : 0;
        int rHighB = upper ? 15 : 13;
        for (int a = rLowA; a <= rHighA; a++) {
            for (int b = rLowB; b <= rHighB; b++) {
                for (int c = 2; c <= 10; c++) {
                    if (a == ringA || b == ringB) {
                        place(x + a, y + c, z + b, INNER_ROCK);
                    }
                }
            }
        }
        for (int a = aLow; a <= aHigh; a++) {
            for (int b = bLow; b <= bHigh; b++) {
                place(x + a, y, z + b, ROCK);
                place(x + a, y + 1, z + b, TILE);
                place(x + a, y + 12, z + b, ROCK);
                place(x + a, y + 11, z + b, STONE);
            }
        }
        int frontZ = upper ? 4 : 12;
        Direction frontDir = upper ? Direction.NORTH : Direction.SOUTH;
        int sideX = left ? 4 : 12;
        Direction sideDir = left ? Direction.WEST : Direction.EAST;
        int frontLow = left ? 4 : 0;
        int frontHigh = left ? 15 : 11;
        for (int g = frontLow; g <= frontHigh; g++) {
            place(x + g, y + 2, z + frontZ, STAIRS, frontDir);
            place(x + g, y + 10, z + frontZ, STAIRS_HIGH, frontDir);
        }
        int sideLow = upper ? 4 : 0;
        int sideHigh = upper ? 15 : left ? 11 : 12;
        for (int g = sideLow; g <= sideHigh; g++) {
            place(x + sideX, y + 2, z + g, STAIRS, sideDir);
            place(x + sideX, y + 10, z + g, STAIRS_HIGH, sideDir);
        }
        connections(cx, cz, y, 3, true);
    }

}
