package me.moonscenty.alchemia.client.particle;

import org.joml.Vector3f;

import me.moonscenty.alchemia.particle.SparkleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.Mth;

/**
 * A spark of vis drawn out of the air and into whatever is pulling on it, as the original's {@code FXVisSparkle}: it
 * wanders off a little, then steers hard for its target, twinkling through a row of the sheet as it goes, and
 * dwindles away as it arrives.
 * <p>
 * Every number here is the original's.
 */
public class SparkleParticle extends LegacyParticle {
    /** How hard it steers for its target each tick, and the fastest it may go along any one axis. */
    private static final double STEER = 0.1F;
    private static final float TOP_SPEED = 0.1F;
    /** How much of its speed it keeps each tick. */
    private static final double DRAG = 0.985;
    /** Within this it starts to dwindle, and within this it is home. */
    private static final double NEAR = 2.0;
    private static final double HOME = 0.2;
    private static final float DWINDLE = 0.95F;
    /** For its first ticks it grows in at one over this, picked anew for each spark. */
    private static final int GROWS_FOR = 10;
    /** The row of the sheet it twinkles through, a cell a tick. */
    private static final int ROW = 8;
    private static final float ALPHA = 0.5F;

    private final Vector3f target;
    private final float growth;
    private float scale;

    protected SparkleParticle(ClientLevel level, double x, double y, double z, SparkleOptions options) {
        super(level, x, y, z, LegacySheet.GLOW);
        this.target = options.target();
        this.rCol = ((options.colour() >> 16) & 0xFF) / 255.0F;
        this.gCol = ((options.colour() >> 8) & 0xFF) / 255.0F;
        this.bCol = (options.colour() & 0xFF) / 255.0F;
        this.alpha = ALPHA;
        this.lifetime = 1000;
        this.hasPhysics = false;
        this.xd = random.nextGaussian() * 0.01F;
        this.yd = random.nextGaussian() * 0.01F;
        this.zd = random.nextGaussian() * 0.01F;
        this.growth = 45 + random.nextInt(15);
        this.scale = 0.0F;
        this.quadSize = 0.0F;
        cell(0, ROW);
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
        move(xd, yd, zd);
        xd *= DRAG;
        yd *= DRAG;
        zd *= DRAG;
        double dx = target.x() - x;
        double dy = target.y() - y;
        double dz = target.z() - z;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (distance < NEAR) {
            scale *= DWINDLE;
        }
        if (distance < HOME) {
            lifetime = age;
        }
        if (age < GROWS_FOR) {
            scale = age / growth;
        }
        if (distance > 0) {
            xd = Mth.clamp((float) (xd + dx / distance * STEER), -TOP_SPEED, TOP_SPEED);
            yd = Mth.clamp((float) (yd + dy / distance * STEER), -TOP_SPEED, TOP_SPEED);
            zd = Mth.clamp((float) (zd + dz / distance * STEER), -TOP_SPEED, TOP_SPEED);
        }
        cell(age % CELLS, ROW);
        // it pulses a little as it twinkles
        quadSize = 0.1F * scale * (Mth.sin(age / 3.0F) * 0.3F + 6.0F);
    }

    @Override
    protected int getLightColor(float partial) {
        return FULL_BRIGHT;
    }

    public static class Maker implements ParticleProvider<SparkleOptions> {
        @Override
        public Particle createParticle(SparkleOptions options, ClientLevel level, double x, double y, double z,
                double xd, double yd, double zd) {
            return new SparkleParticle(level, x, y, z, options);
        }
    }
}
