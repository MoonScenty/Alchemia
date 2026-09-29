package me.moonscenty.alchemia.client.particle;

import me.moonscenty.alchemia.particle.MoteOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import org.joml.Vector3f;

/**
 * A mote of light: it rises, it gives out, and it is whatever colour it was given.
 * <p>
 * Made because nothing in the game does this. Dust carries a colour but is a hard speck that falls; the end rod
 * drifts up and gives out but is white and only white. Nitor comes in sixteen colours and wants both halves, so
 * it has one of its own.
 * <p>
 * Three things make it read as light rather than as confetti, and all three are the original's. It adds itself
 * to what is behind it, so a dozen gathered at one spot burn white in the middle and fade to colour at the rim.
 * It is drawn at full brightness whatever the hour, because a light that goes dark at night is not a light. And
 * it shrinks away over the whole of its life rather than blinking out, so the crowd at the middle is always a
 * mixture of new and nearly gone, which is what keeps it from pulsing.
 */
public class MoteParticle extends TextureSheetParticle {
    /** How long one lives, before the coin-toss that makes no two the same. The original's own numbers. */
    private static final int LIVES = 36;
    /** How see-through each one is. Low, because they are meant to be counted in dozens and add up. */
    private static final float THIN = 0.5F;
    /**
     * How hard it is pushed up each tick, and how much of its speed it keeps.
     * <p>
     * Both are small on purpose. A push that is kept settles at push times keep over one minus keep, and at a
     * fortieth of a block a tick that is a third of a block over a whole life -- which is a mote hanging about
     * its flame. Ten times that and it leaves before anybody sees it, which is how this was first written.
     */
    private static final double RISES = 0.0009;
    private static final double SLOWS = 0.92;

    /** The brightest the game can light anything, block and sky both. */
    private static final int DAYLIGHT = 0xF000F0;

    private final float grew;

    protected MoteParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd,
            MoteOptions colour, SpriteSet sprites) {
        // the four-part constructor and not the seven-part one: the longer one takes a speed and then throws
        // it away, scattering every particle in a random direction. A mote of a flame stays with its flame,
        // so the speed it was given is put on afterwards and kept
        super(level, x, y, z);
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        Vector3f tint = colour.colour();
        this.rCol = tint.x();
        this.gCol = tint.y();
        this.bCol = tint.z();
        this.quadSize *= colour.scale();
        this.grew = this.quadSize;
        this.alpha = THIN;
        // no two alike, so a crowd of them does not pulse together
        this.lifetime = (int) (LIVES / (this.random.nextDouble() * 0.3 + 0.7));
        this.hasPhysics = false;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        this.yd += RISES;
        this.xd *= SLOWS;
        this.yd *= SLOWS;
        this.zd *= SLOWS;
        this.move(this.xd, this.yd, this.zd);

        // it shrinks away over the whole of its life rather than going out at the end of it
        this.quadSize = this.grew * (1.0F - (float) this.age / this.lifetime);
    }

    /** Full brightness, whatever the hour. A light that goes dark at night is not a light. */
    @Override
    protected int getLightColor(float partial) {
        return DAYLIGHT;
    }

    /** Added to what is behind it, which is the whole of why a crowd of them burns white in the middle. */
    @Override
    public ParticleRenderType getRenderType() {
        return GlowSheet.INSTANCE;
    }

    public static class Maker implements ParticleProvider<MoteOptions> {
        private final SpriteSet sprites;

        public Maker(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(MoteOptions colour, ClientLevel level, double x, double y, double z,
                double xd, double yd, double zd) {
            return new MoteParticle(level, x, y, z, xd, yd, zd, colour, sprites);
        }
    }
}
