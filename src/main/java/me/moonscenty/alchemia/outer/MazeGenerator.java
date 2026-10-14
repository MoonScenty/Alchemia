package me.moonscenty.alchemia.outer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Lays out one maze of the Outer Lands, a grid of chunks, as the original did and with the original's dice in the
 * original's order.
 * <p>
 * The great hall, two chunks by two, goes in a corner, and the portal in the middle. Holes of one to three cells are
 * scattered where the maze must go round. From the portal a growing tree is carved -- nine times in twenty it goes
 * on from the newest cell, as often from any, once in ten from the oldest -- and the portal is opened on a few
 * more sides. The hall is let in by one door; where no cell next to it was reached, a tunnel is dug out from it until
 * it meets the maze. Of the dead ends, one holds the key and most of the rest a nest, a library, a turret or a
 * room; half of the other passages get something of their own.
 * <p>
 * It can fail -- nothing carved, the hall shut out, no dead end -- and is then tried again with the next seed.
 */
public final class MazeGenerator {
    final int width;
    final int height;
    private final Random random;
    /** The grid, row by row: {@code grid[y][x]}. */
    final int[][] grid;

    MazeGenerator(int width, int height, long seed) {
        this.width = width;
        this.height = height;
        this.random = new Random(seed);
        this.grid = new int[height][width];
    }

    private record Loc(int x, int y) {
    }

    /** The first maze of this size that comes out of this seed or one after it. */
    static MazeGenerator make(int width, int height, long seed) {
        while (true) {
            MazeGenerator maze = new MazeGenerator(width, height, seed++);
            if (maze.generate()) {
                return maze;
            }
        }
    }

    static int dx(int direction) {
        return switch (direction) {
            case Cell.EAST -> 1;
            case Cell.WEST -> -1;
            default -> 0;
        };
    }

    static int dy(int direction) {
        return switch (direction) {
            case Cell.NORTH -> -1;
            case Cell.SOUTH -> 1;
            default -> 0;
        };
    }

    static int opposite(int direction) {
        return switch (direction) {
            case Cell.NORTH -> Cell.SOUTH;
            case Cell.SOUTH -> Cell.NORTH;
            case Cell.EAST -> Cell.WEST;
            default -> Cell.EAST;
        };
    }

    private int nextIndex(int size) {
        float roll = random.nextFloat();
        if (roll <= 0.45F) {
            return size - 1;
        }
        if (roll <= 0.9F) {
            return random.nextInt(size);
        }
        return 0;
    }

    private boolean inside(int x, int y) {
        return 0 < x && x < width - 1 && 0 < y && y < height - 1;
    }

    private static int feature(int packed) {
        return new Cell(packed).feature;
    }

    boolean generate() {
        int bx = 0;
        int by = 0;
        switch (random.nextInt(4)) {
            case 1 -> {
                bx = width - 2;
                by = height - 2;
            }
            case 2 -> bx = width - 2;
            case 3 -> by = height - 2;
            default -> {
            }
        }
        grid[by][bx] = Cell.BOSS_UPPER_LEFT << 8;
        grid[by][bx + 1] = Cell.BOSS_UPPER_RIGHT << 8;
        grid[by + 1][bx] = Cell.BOSS_LOWER_LEFT << 8;
        grid[by + 1][bx + 1] = Cell.BOSS_LOWER_RIGHT << 8;

        int px = 1 + width / 2;
        int py = 1 + height / 2;
        grid[py][px] = Cell.PORTAL << 8;

        List<Loc> frontier = new ArrayList<>();

        // the holes the maze goes round; a big one counts for two
        int count = (width + height) / 4;
        for (int k = 0; k < count; k++) {
            int size = 1 + random.nextInt(3);
            if (size > 2) {
                count--;
            }
            int sx = random.nextInt(width - size);
            int sy = random.nextInt(height - size);
            for (int x = sx; x < sx + size; x++) {
                for (int y = sy; y < sy + size; y++) {
                    if (grid[y][x] == 0) {
                        grid[y][x] = -1;
                    }
                }
            }
        }

        List<Integer> dirs = Arrays.asList(Cell.NORTH, Cell.SOUTH, Cell.EAST, Cell.WEST);
        Collections.shuffle(dirs, random);
        int first = dirs.get(0);
        int nx = px + dx(first);
        int ny = py + dy(first);
        grid[py][px] |= first;
        if (grid[ny][nx] < 0) {
            grid[ny][nx] = 0;
        }
        grid[ny][nx] |= opposite(first);
        frontier.add(new Loc(nx, ny));

        boolean carved = false;
        while (!frontier.isEmpty()) {
            int index = nextIndex(frontier.size());
            Loc at = frontier.get(index);
            Collections.shuffle(dirs, random);
            boolean found = false;
            for (int d : dirs) {
                int ex = at.x() + dx(d);
                int ey = at.y() + dy(d);
                if (inside(ex, ey)) {
                    if (grid[ey][ex] == 0) {
                        grid[at.y()][at.x()] |= d;
                        grid[ey][ex] |= opposite(d);
                        frontier.add(new Loc(ex, ey));
                        found = true;
                    }
                    if (found) {
                        carved = true;
                        break;
                    }
                }
            }
            if (!found) {
                frontier.remove(index);
            }
        }
        if (!carved) {
            return false;
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (grid[y][x] < 0) {
                    grid[y][x] = 0;
                }
            }
        }

        // a few more ways out of the portal
        Collections.shuffle(dirs, random);
        for (int d : dirs) {
            int ex = px + dx(d);
            int ey = py + dy(d);
            if (inside(ex, ey) && grid[ey][ex] > 0 && random.nextBoolean()) {
                grid[ey][ex] |= opposite(d);
                grid[py][px] |= d;
            }
        }

        // the one door into the hall
        Collections.shuffle(dirs, random);
        boolean connected = false;
        outer:
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                for (int d : dirs) {
                    int ex = bx + i + dx(d);
                    int ey = by + j + dy(d);
                    if (inside(ex, ey) && grid[ey][ex] > 0 && feature(grid[ey][ex]) == Cell.PASSAGE) {
                        grid[ey][ex] |= opposite(d);
                        grid[by + j][bx + i] |= d;
                        connected = true;
                        break outer;
                    }
                }
            }
        }
        if (!connected && !tunnel(bx, by, frontier, dirs)) {
            return false;
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Cell cell = new Cell(grid[y][x]);
                if (cell.feature == Cell.CARVING) {
                    cell.feature = Cell.PASSAGE;
                    grid[y][x] = cell.pack();
                }
            }
        }

        // the dead ends: one key room, and rooms in most of the rest
        List<int[]> deadEnds = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Cell cell = new Cell(grid[y][x]);
                if (cell.exits() == 1 && cell.feature == Cell.PASSAGE) {
                    deadEnds.add(new int[] {y, x});
                }
            }
        }
        if (deadEnds.isEmpty()) {
            return false;
        }
        int key = random.nextInt(deadEnds.size());
        setFeature(deadEnds.remove(key), Cell.KEY_ROOM);
        if (!deadEnds.isEmpty()) {
            int placed = 0;
            while (placed < deadEnds.size() * 0.75) {
                int r = random.nextInt(deadEnds.size());
                int[] at = deadEnds.get(r);
                if (new Cell(grid[at[0]][at[1]]).feature == Cell.PASSAGE) {
                    setFeature(at, switch (random.nextInt(5)) {
                        case 0 -> Cell.NEST;
                        case 1 -> Cell.LIBRARY;
                        case 2 -> Cell.TURRET;
                        default -> Cell.PASSAGE_ROOM;
                    });
                    deadEnds.remove(r);
                    placed++;
                }
            }
        }

        // and something in half the other passages
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Cell cell = new Cell(grid[y][x]);
                if (cell.feature == Cell.PASSAGE && (cell.north || cell.south || cell.west || cell.east)
                        && random.nextBoolean()) {
                    cell.feature = switch (random.nextInt(10)) {
                        case 0 -> Cell.LIBRARY;
                        case 1, 2 -> Cell.TURRET_PASSAGE;
                        case 3 -> Cell.PUNCTURED;
                        case 4 -> Cell.OVERGROWN;
                        case 5 -> Cell.TAINTED;
                        case 6 -> Cell.WEBBED;
                        default -> Cell.PASSAGE_ROOM;
                    };
                    grid[y][x] = cell.pack();
                }
            }
        }
        return true;
    }

    private void setFeature(int[] at, int feature) {
        Cell cell = new Cell(grid[at[0]][at[1]]);
        cell.feature = feature;
        grid[at[0]][at[1]] = cell.pack();
    }

    /** Digs out from the hall into what was not reached until it meets the maze; false when it never does. */
    private boolean tunnel(int bx, int by, List<Loc> frontier, List<Integer> dirs) {
        List<Integer> starts = Arrays.asList(Cell.NORTH, Cell.SOUTH, Cell.EAST, Cell.WEST);
        Collections.shuffle(starts, random);
        int marker = Cell.CARVING << 8;
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                for (int out : starts) {
                    int sx = bx + i + dx(out);
                    int sy = by + j + dy(out);
                    if (!inside(sx, sy) || grid[sy][sx] != 0) {
                        continue;
                    }
                    frontier.add(new Loc(sx, sy));
                    while (!frontier.isEmpty()) {
                        int index = nextIndex(frontier.size());
                        Loc at = frontier.get(index);
                        Collections.shuffle(dirs, random);
                        boolean found = false;
                        for (int d : dirs) {
                            int ex = at.x() + dx(d);
                            int ey = at.y() + dy(d);
                            if (!inside(ex, ey)) {
                                continue;
                            }
                            if (grid[ey][ex] == 0) {
                                grid[at.y()][at.x()] |= d | marker;
                                grid[ey][ex] |= opposite(d) | marker;
                                frontier.add(new Loc(ex, ey));
                                found = true;
                            } else if (feature(grid[ey][ex]) == Cell.PASSAGE) {
                                grid[at.y()][at.x()] |= d;
                                grid[ey][ex] |= opposite(d);
                                grid[sy][sx] |= opposite(out);
                                grid[by + j][bx + i] |= out;
                                frontier.clear();
                                return true;
                            }
                            if (found) {
                                break;
                            }
                        }
                        if (!found) {
                            frontier.remove(index);
                        }
                    }
                }
            }
        }
        return false;
    }
}
