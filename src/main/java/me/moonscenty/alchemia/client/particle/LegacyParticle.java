package me.moonscenty.alchemia.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;

public abstract class LegacyParticle extends SingleQuadParticle {
    protected static final int CELLS = 16;
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

    protected void cell(int column, int row) {
        u0 = column / (float) CELLS;
        u1 = (column + 1) / (float) CELLS;
        v0 = row / (float) CELLS;
        v1 = (row + 1) / (float) CELLS;
    }

    protected void numbered(int index, int grid) {
        int column = index % grid;
        int row = index / grid;
        u0 = column / (float) grid;
        u1 = (column + 1) / (float) grid;
        v0 = row / (float) grid;
        v1 = (row + 1) / (float) grid;
    }

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
