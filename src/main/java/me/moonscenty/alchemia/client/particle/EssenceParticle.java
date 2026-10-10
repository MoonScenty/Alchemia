package me.moonscenty.alchemia.client.particle;

import org.joml.Vector3f;

import me.moonscenty.alchemia.particle.EssenceOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.Mth;

/**
 * A drop of essentia making its way to whatever drew on it, as the original's {@code FXEssentiaTrail}: a little
 * sphere of its aspect's colour that wobbles off, then is pulled in, harder the nearer it is, and dwindles away as it
 * arrives.
 * <p>
 * The original moved essentia between things as a snaking tube drawn with a geometry library it carried with it;
 * this simpler drop it also had, and is drawn here instead. Every number in it is the original's.
 */
public class EssenceParticle extends LegacyParticle {
    /** The droplet's cell on the sheet. */
    private static final int CELL = 25;
    private static final double DRAG = 0.985;
    private static final double TOP_SPEED = 0.05;
    private static final double PULL = 0.01;
    private static final double RISE = 0.01 * 0.2F;
    private static final double NEAR = 2.0;
    private static final float DWINDLE = 0.98F;
    private static final float GONE = 0.2F;
    private static final float ALPHA = 0.5F;

    private final Vector3f target;
    private final int count;
    private float scale;

    protected EssenceParticle(ClientLevel level, double x, double y, double z, EssenceOptions options) {
        super(level, x, y, z, LegacySheet.COVER);
        this.target = options.target();
        this.count = options.count();
        this.scale = Mth.sin(count / 2.0F) * 0.1F + 1.0F;
        double distance = Math.sqrt(distanceToSqr(target.x(), target.y(), target.z()));
        int span = Math.max(1, (int) (distance * 30.0F));
        this.lifetime = span / 2 + random.nextInt(span);
        this.xd = Mth.sin(count / 4.0F) * 0.015F + random.nextGaussian() * 0.002;
        this.yd = Mth.sin(count / 3.0F) * 0.015F + random.nextGaussian() * 0.002;
        this.zd = Mth.sin(count / 2.0F) * 0.015F + random.nextGaussian() * 0.002;
        float r = ((options.colour() >> 16) & 0xFF) / 255.0F;
        float g = ((options.colour() >> 8) & 0xFF) / 255.0F;
        float b = (options.colour() & 0xFF) / 255.0F;
        this.rCol = r * 0.8F + random.nextFloat() * r * 0.2F;
        this.gCol = g * 0.8F + random.nextFloat() * g * 0.2F;
        this.bCol = b * 0.8F + random.nextFloat() * b * 0.2F;
        this.alpha = ALPHA;
        this.hasPhysics = true;
        numbered(CELL, CELLS);
        size();
    }

    private double distanceToSqr(double x, double y, double z) {
        return Mth.square(this.x - x) + Mth.square(this.y - y) + Mth.square(this.z - z);
    }

    private void size() {
        quadSize = 0.1F * scale * (1.0F + Mth.sin((age - count) / 5.0F) * 0.25F);
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
        yd += RISE;
        move(xd, yd, zd);
        xd = Mth.clamp(xd * DRAG, -TOP_SPEED, TOP_SPEED);
        yd = Mth.clamp(yd * DRAG, -TOP_SPEED, TOP_SPEED);
        zd = Mth.clamp(zd * DRAG, -TOP_SPEED, TOP_SPEED);
        double dx = target.x() - x;
        double dy = target.y() - y;
        double dz = target.z() - z;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (distance < NEAR) {
            scale *= DWINDLE;
        }
        if (scale < GONE) {
            remove();
            return;
        }
        if (distance > 0) {
            double pull = PULL / Math.min(1.0, distance);
            xd += dx / distance * pull;
            yd += dy / distance * pull;
            zd += dz / distance * pull;
        }
        size();
    }

    @Override
    protected int getLightColor(float partial) {
        return FULL_BRIGHT;
    }

    public static class Maker implements ParticleProvider<EssenceOptions> {
        @Override
        public Particle createParticle(EssenceOptions options, ClientLevel level, double x, double y, double z,
                double xd, double yd, double zd) {
            return new EssenceParticle(level, x, y, z, options);
        }
    }
}
