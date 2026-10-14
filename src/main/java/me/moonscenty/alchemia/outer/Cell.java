package me.moonscenty.alchemia.outer;

/**
 * One chunk of a maze in the Outer Lands: which of its four sides open onto the next, and what it holds. Kept, as
 * the original kept it, packed into a short -- the sides in the low bits (north 1, south 2, east 4, west 8) and the
 * feature in the high byte.
 */
public final class Cell {
    public static final int NORTH = 1;
    public static final int SOUTH = 2;
    public static final int EAST = 4;
    public static final int WEST = 8;

    /** What a cell holds. */
    public static final int PASSAGE = 0;
    public static final int PORTAL = 1;
    /** The four quarters of the great hall at the end: upper left, upper right, lower left, lower right. */
    public static final int BOSS_UPPER_LEFT = 2;
    public static final int BOSS_UPPER_RIGHT = 3;
    public static final int BOSS_LOWER_LEFT = 4;
    public static final int BOSS_LOWER_RIGHT = 5;
    public static final int KEY_ROOM = 6;
    public static final int NEST = 7;
    public static final int LIBRARY = 8;
    public static final int TURRET = 9;
    public static final int TURRET_PASSAGE = 10;
    public static final int PASSAGE_ROOM = 11;
    public static final int CRUSTED = 12;
    public static final int PUNCTURED = 21;
    public static final int OVERGROWN = 22;
    public static final int TAINTED = 23;
    public static final int WEBBED = 24;
    /** Only while a maze is being carved: the tunnel that brings the hall in. */
    static final int CARVING = 99;

    public boolean north;
    public boolean south;
    public boolean east;
    public boolean west;
    public int feature;

    public Cell(int packed) {
        north = (packed & NORTH) != 0;
        south = (packed & SOUTH) != 0;
        east = (packed & EAST) != 0;
        west = (packed & WEST) != 0;
        feature = (byte) (packed >> 8);
    }

    public short pack() {
        int packed = (north ? NORTH : 0) | (south ? SOUTH : 0) | (east ? EAST : 0) | (west ? WEST : 0);
        return (short) (packed | feature << 8);
    }

    public int exits() {
        return (north ? 1 : 0) + (south ? 1 : 0) + (east ? 1 : 0) + (west ? 1 : 0);
    }

    public boolean isBossRoom() {
        return feature >= BOSS_UPPER_LEFT && feature <= BOSS_LOWER_RIGHT;
    }
}
