package me.moonscenty.alchemia.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;

/**
 * A little wisp of light in some pale colour, as the original's {@code FXWispyOrb}: it drifts up and about, flickers
 * through a row of the sheet a cell a tick, and fades out evenly over its life.
 * <p>
 * Every number here is the original's.
 */
public class WispParticle extends LegacyParticle {
    /** The row it flickers along. */
    private static final int ROW = 8;
    private static final double DRAG = 0.85F;
    private static final double RISE = 0.002;
    private static final float WANDER = 0.01F;

    private final float fades;
    private int left;
    private int frame;

    /**
     * @param lasts how long it lives at the least; up to half as long again is added
     * @param alpha how opaque it starts
     */
    public WispParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd,
            int lasts, float alpha) {
        super(level, x, y, z, LegacySheet.GLOW);
        this.rCol = 0.25F + random.nextFloat() * 0.75F;
        this.gCol = 0.25F + random.nextFloat() * 0.75F;
        this.bCol = 0.25F + random.nextFloat() * 0.75F;
        this.hasPhysics = false;
        float scale = (random.nextFloat() * 0.5F + 0.5F) * 2.0F * (random.nextFloat() * 0.5F + 0.3F);
        this.quadSize = 0.1F * scale;
        this.xd = xd * 0.2F + (float) (Math.random() * 2.0 - 1.0) * 0.02F;
        this.yd = yd * 0.2F + (float) Math.random() * 0.02F;
        this.zd = zd * 0.2F + (float) (Math.random() * 2.0 - 1.0) * 0.02F;
        this.left = (int) (lasts + (lasts / 2) * random.nextFloat());
        this.lifetime = Integer.MAX_VALUE;
        this.alpha = alpha;
        this.fades = alpha / Math.max(1, left);
        cell(0, ROW);
    }

    @Override
    public void tick() {
        xo = x;
        yo = y;
        zo = z;
        yd += RISE;
        xd += (random.nextFloat() - random.nextFloat()) * WANDER;
        zd += (random.nextFloat() - random.nextFloat()) * WANDER;
        x += xd;
        y += yd;
        z += zd;
        xd *= DRAG;
        yd *= DRAG;
        zd *= DRAG;
        if (left-- <= 0) {
            remove();
        }
        frame = (frame + 1) % CELLS;
        cell(frame, ROW);
        alpha = Math.max(0.0F, alpha - fades);
    }

    @Override
    protected int getLightColor(float partial) {
        return FULL_BRIGHT;
    }
}
