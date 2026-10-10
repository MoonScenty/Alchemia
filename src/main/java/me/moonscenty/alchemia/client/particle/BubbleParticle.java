package me.moonscenty.alchemia.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;

/**
 * A bubble on the crucible, as the original's {@code FXBubble}: a speck of light that drifts up off the surface, or
 * froth that sinks back into it or runs down the outside, and pops through three cells of the sheet in its last
 * ticks.
 * <p>
 * Every number here is the original's. Its life is counted down rather than up, as the original counted it.
 */
public class BubbleParticle extends LegacyParticle {
    /** The cell a bubble is drawn from, and the row it pops along. */
    private static final int FIRST = 16;
    /** How much of its speed it keeps each tick. */
    private static final double DRAG = 0.85F;
    /** How hard a rising bubble wanders sideways each tick. */
    private static final float WANDER = 0.01F;

    /** The kinds of bubble the original made. */
    public enum Kind {
        /** Rising off the surface. */
        BUBBLE,
        /** Froth on a boiling surface, sinking back. */
        FROTH,
        /** Froth running down the outside of an overfull pot. */
        FROTH_DOWN
    }

    private final double rise;
    private int left;
    private int frame = FIRST;

    /**
     * @param colour the bubble's colour, as {@code 0xRRGGBB}
     * @param lasts  the least it lives, to which a random share is added; the original's "age" argument
     * @param rise   how hard it is pushed up each tick; negative sinks it
     */
    public BubbleParticle(ClientLevel level, double x, double y, double z, int colour, int lasts, double rise,
            Kind kind) {
        super(level, x, y, z, LegacySheet.GLOW);
        this.rCol = ((colour >> 16) & 0xFF) / 255.0F;
        this.gCol = ((colour >> 8) & 0xFF) / 255.0F;
        this.bCol = (colour & 0xFF) / 255.0F;
        this.hasPhysics = false;
        float scale = (random.nextFloat() * 0.5F + 0.5F) * 2.0F * (random.nextFloat() * 0.3F + 0.2F);
        this.xd = (float) (Math.random() * 2.0 - 1.0) * 0.02F;
        this.yd = (float) Math.random() * 0.02F;
        this.zd = (float) (Math.random() * 2.0 - 1.0) * 0.02F;
        this.left = (int) ((lasts + 2) + 8.0 / (Math.random() * 0.8 + 0.2));
        if (kind != Kind.BUBBLE) {
            scale *= 0.75F;
            this.left = kind == Kind.FROTH ? 4 + random.nextInt(3) : 12 + random.nextInt(12);
            this.xd /= 5.0;
            this.yd /= 10.0;
            this.zd /= 5.0;
        }
        this.rise = kind == Kind.BUBBLE ? rise : kind == Kind.FROTH ? -0.001 : -0.005;
        this.quadSize = 0.1F * scale;
        this.lifetime = Integer.MAX_VALUE;
        cell(FIRST % CELLS, FIRST / CELLS);
    }

    @Override
    public void tick() {
        xo = x;
        yo = y;
        zo = z;
        yd += rise;
        if (rise > 0) {
            xd += (random.nextFloat() - random.nextFloat()) * WANDER;
            zd += (random.nextFloat() - random.nextFloat()) * WANDER;
        }
        x += xd;
        y += yd;
        z += zd;
        xd *= DRAG;
        yd *= DRAG;
        zd *= DRAG;
        if (left-- <= 0) {
            remove();
        } else if (left <= 2) {
            frame++;
            cell(frame % CELLS, frame / CELLS);
        }
    }

    @Override
    protected int getLightColor(float partial) {
        return FULL_BRIGHT;
    }
}
