package me.moonscenty.alchemia.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;

/**
 * A particle drawn straight off one of the original's sheets, a cell at a time.
 * <p>
 * The original's sheets are sixteen cells by sixteen; a cell is picked by its column and row. Each kind of particle
 * says which layer it is drawn in and how it moves; this holds what they all share.
 */
public abstract class LegacyParticle extends SingleQuadParticle {
    /** Cells across and down a sheet. */
    protected static final int CELLS = 16;
    /** The brightest the game can light anything, block and sky both. */
    protected static final int FULL_BRIGHT = 0xF000F0;

    private final LegacySheet sheet;
    private float u0;
    private float u1;
    private float v0;
    private float v1;

    protected LegacyParticle(ClientLevel level, double x, double y, double z, LegacySheet sheet) {
        super(level, x, y, z);
        this.sheet = sheet;
        cell(0, 0);
    }

    /** Draw from this cell of the sheet. */
    protected void cell(int column, int row) {
        u0 = column / (float) CELLS;
        u1 = (column + 1) / (float) CELLS;
        v0 = row / (float) CELLS;
        v1 = (row + 1) / (float) CELLS;
    }

    /** Draw from the cell of this number, counting along the rows of a sheet cut into {@code grid} by grid. */
    protected void numbered(int index, int grid) {
        int column = index % grid;
        int row = index / grid;
        u0 = column / (float) grid;
        u1 = (column + 1) / (float) grid;
        v0 = row / (float) grid;
        v1 = (row + 1) / (float) grid;
    }

    /** Draw from this part of the sheet, as fractions of it. */
    protected void area(float left, float top, float right, float bottom) {
        u0 = left;
        u1 = right;
        v0 = top;
        v1 = bottom;
    }

    @Override
    protected float getU0() {
        return u0;
    }

    @Override
    protected float getU1() {
        return u1;
    }

    @Override
    protected float getV0() {
        return v0;
    }

    @Override
    protected float getV1() {
        return v1;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return sheet;
    }
}
