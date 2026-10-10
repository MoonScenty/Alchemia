package me.moonscenty.alchemia.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;

/**
 * A soft ball of light, as the original's {@code FXWisp}: the glow in the bottom corner of the sheet, half-clear and
 * added to what is behind it. It either swells and fades back over its life, or dwindles from the start; it can sink
 * a little, and it settles on what it lands on.
 * <p>
 * Every number here is the original's.
 */
public class GlowWispParticle extends LegacyParticle {
    private static final float ALPHA = 0.5F;
    private static final double DRAG = 0.98;

    private final float full;
    private final boolean dwindles;
    private final float sinks;

    /**
     * @param size     how big against the original's own random size
     * @param dwindles whether it only shrinks, rather than swelling and shrinking
     * @param sinks    the original's gravity for it; nought hangs still
     */
    public GlowWispParticle(ClientLevel level, double x, double y, double z, float size, float r, float g, float b,
            boolean dwindles, float sinks) {
        super(level, x, y, z, LegacySheet.GLOW);
        this.rCol = r == 0.0F ? 1.0F : r;
        this.gCol = g;
        this.bCol = b;
        this.alpha = ALPHA;
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
        this.full = (random.nextFloat() * 0.5F + 0.5F) * 2.0F * size;
        this.dwindles = dwindles;
        this.sinks = sinks;
        this.lifetime = (int) (36.0 / (Math.random() * 0.3 + 0.7));
        this.hasPhysics = true;
        setSize(0.1F, 0.1F);
        area(0.0F, 0.875F, 0.125F, 1.0F);
        this.quadSize = 0.0F;
    }

    @Override
    public void tick() {
        xo = x;
        yo = y;
        zo = z;
        if (age++ >= lifetime) {
            remove();
            return;
        }
        yd -= 0.04 * sinks;
        move(xd, yd, zd);
        xd *= DRAG;
        yd *= DRAG;
        zd *= DRAG;
        if (onGround) {
            xd *= 0.7F;
            zd *= 0.7F;
        }
        float through;
        if (dwindles) {
            through = (lifetime - age) / (float) lifetime;
        } else {
            through = age / (float) (lifetime / 2);
            if (through > 1.0F) {
                through = 2.0F - through;
            }
        }
        quadSize = 0.5F * full * through;
    }

    @Override
    protected int getLightColor(float partial) {
        return FULL_BRIGHT;
    }
}
