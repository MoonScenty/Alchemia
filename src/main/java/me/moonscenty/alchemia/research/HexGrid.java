package me.moonscenty.alchemia.research;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;

/**
 * Axial coordinates on a hex board, and the handful of shapes a research note is cut from.
 * <p>
 * A cell is named by two numbers rather than three: the third is always {@code -q - r}, so keeping it would only be
 * something else to get wrong.
 */
public final class HexGrid {
    /** The six ways out of a cell, in order, so that walking them traces a ring. */
    private static final int[][] NEIGHBOURS = {{1, 0}, {1, -1}, {0, -1}, {-1, 0}, {-1, 1}, {0, 1}};
    public static final int SIDES = 6;

    private HexGrid() {
    }

    public record Hex(int q, int r) {
        public static final Codec<Hex> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("q").forGetter(Hex::q),
                Codec.INT.fieldOf("r").forGetter(Hex::r))
                .apply(instance, Hex::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, Hex> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Hex::q,
                ByteBufCodecs.VAR_INT, Hex::r,
                Hex::new);

        public Hex neighbour(int side) {
            int[] step = NEIGHBOURS[Math.floorMod(side, SIDES)];
            return new Hex(q + step[0], r + step[1]);
        }

        public List<Hex> neighbours() {
            List<Hex> around = new ArrayList<>(SIDES);
            for (int side = 0; side < SIDES; side++) {
                around.add(neighbour(side));
            }
            return around;
        }

        /** How many steps apart two cells are, walking from one to the other. */
        public int distanceTo(Hex other) {
            return (Math.abs(q - other.q) + Math.abs(r - other.r) + Math.abs(q + r - other.q - other.r)) / 2;
        }
    }

    public static final Hex CENTRE = new Hex(0, 0);

    /** The cells exactly {@code radius} steps from the middle, in order around the edge. */
    public static List<Hex> ring(int radius) {
        if (radius <= 0) {
            return List.of(CENTRE);
        }
        Hex walker = CENTRE;
        for (int step = 0; step < radius; step++) {
            walker = walker.neighbour(4);
        }

        List<Hex> ring = new ArrayList<>(SIDES * radius);
        for (int side = 0; side < SIDES; side++) {
            for (int step = 0; step < radius; step++) {
                ring.add(walker);
                walker = walker.neighbour(side);
            }
        }
        return ring;
    }

    /** Every cell out to {@code radius}, middle included. */
    public static Set<Hex> filled(int radius) {
        Set<Hex> cells = new LinkedHashSet<>();
        for (int step = 0; step <= radius; step++) {
            cells.addAll(ring(step));
        }
        return cells;
    }

    /**
     * Spaces a number of cells evenly around a ring, starting somewhere random. Evenly rather than at random, so a
     * note never opens with two endpoints sat next to each other and the puzzle already half solved.
     */
    public static List<Hex> spaceAroundRing(int radius, int count, RandomSource random) {
        List<Hex> ring = ring(radius);
        if (count <= 0 || ring.isEmpty()) {
            return List.of();
        }

        float spacing = (float) ring.size() / count;
        int start = random.nextInt(ring.size());
        List<Hex> picked = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            picked.add(ring.get((start + Math.round(index * spacing)) % ring.size()));
        }
        return picked;
    }
}
