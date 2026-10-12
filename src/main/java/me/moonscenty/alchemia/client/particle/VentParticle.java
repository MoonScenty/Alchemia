package me.moonscenty.alchemia.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;

/**
 * A puff of steam let off by a machine, as the original's {@code FXVent}: it shoots off the way it is blown, slows
 * fast and rises a little, swelling by a sixth each tick until it is full size and gone, thinning out as it grows.
 * It plays through the first few cells of the sheet's top row as it swells.
 *
 * <p>Every number here is the original's.
 */
public class VentParticle extends LegacyParticle {
    private static final float OPACITY = 0.4F;
    private static final float SPEED = 0.125F;
    private static final float SPREAD = 5.0F;

    /** How big it gets before it is gone. */
    private float full = 1.0F;
    private float scale;

    public VentParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, int colour) {
        super(level, x, y, z, LegacySheet.COVER);
        this.scale = random.nextFloat() * 0.1F + 0.05F;
        this.rCol = (colour >> 16 & 0xFF) / 255.0F;
        this.gCol = (colour >> 8 & 0xFF) / 255.0F;
        this.bCol = (colour & 0xFF) / 255.0F;
        this.hasPhysics = false;
        this.gravity = 0.0F;
        this.lifetime = 1000;
        heading(xd, yd, zd);
        look();
    }

    /** Bigger or smaller than an ordinary puff, start and end alike. */
    public VentParticle scaled(float by) {
        scale *= by;
        full *= by;
        look();
        return this;
    }

    private void heading(double xd, double yd, double zd) {
        double length = Math.sqrt(xd * xd + yd * yd + zd * zd);
        if (length == 0) {
            length = 1;
        }
        this.xd = (xd / length + wobble()) * SPEED;
        this.yd = (yd / length + wobble()) * SPEED;
        this.zd = (zd / length + wobble()) * SPEED;
    }

    private double wobble() {
        return random.nextGaussian() * (random.nextBoolean() ? -1 : 1) * 0.0075F * SPREAD;
    }

    @Override
    public void tick() {
        xo = x;
        yo = y;
        zo = z;
        age++;
        if (scale >= full) {
            remove();
            return;
        }
        yd += 0.0025;
        move(xd, yd, zd);
        xd *= 0.85;
        yd *= 0.85;
        zd *= 0.85;
        if (scale < full) {
            scale = Math.min(full, scale * 1.15F);
        }
        if (onGround) {
            xd *= 0.7F;
            zd *= 0.7F;
        }
        look();
    }

    private void look() {
        quadSize = 0.3F * scale;
        alpha = OPACITY * Mth.clamp((full - scale) / full, 0.0F, 1.0F);
        cell((int) (1.0F + scale / full * 4.0F) % CELLS, 0);
    }
}
