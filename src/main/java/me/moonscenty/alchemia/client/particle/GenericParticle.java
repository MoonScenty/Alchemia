package me.moonscenty.alchemia.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;

/**
 * The original's general-purpose particle, {@code FXGeneric}: a run of cells it plays through once over its life or
 * loops, a colour it fades from one to another, an opacity and a size it eases from one to another, a spin, and how
 * quickly it slows. Most of the original's effects are one of these with its own numbers.
 * <p>
 * The original eased opacity and size a step every frame drawn, so how far they had come depended on how fast the
 * game was running and was only right at twenty frames a second. Here they ease by the tick, over the life it was
 * given, as the original meant them to.
 * <p>
 * It is lit by the world, unlike the original's own glowing particles. Like the original, it draws its first and
 * last ticks at half opacity, so it does not pop in or out.
 */
public class GenericParticle extends LegacyParticle {
    private final int grid;
    private int first;
    private int frames = 1;
    private int every = 1;
    private boolean loops;
    private float r0;
    private float g0;
    private float b0;
    private float r1;
    private float g1;
    private float b1;
    private float alpha0 = 1.0F;
    private float alpha1 = 1.0F;
    private float size0 = 1.0F;
    private float size1 = 1.0F;
    private float spin;
    private double slows = 0.98;
    private int waits;

    /**
     * @param sheet which sheet and which way it is mixed in
     * @param grid  how many cells across and down that sheet is cut into
     */
    public GenericParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd,
            LegacySheet sheet, int grid) {
        super(level, x, y, z, sheet);
        this.grid = grid;
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.hasPhysics = false;
        this.gravity = 0.0F;
        this.lifetime = 1;
        colours(1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F);
    }

    public GenericParticle lasts(int ticks) {
        lifetime = Math.max(1, ticks);
        return this;
    }

    /** One colour for the whole of its life. */
    public GenericParticle colour(float r, float g, float b) {
        return colours(r, g, b, r, g, b);
    }

    /** A colour it starts in and one it fades to. */
    public GenericParticle colours(float r0, float g0, float b0, float r1, float g1, float b1) {
        this.r0 = r0;
        this.g0 = g0;
        this.b0 = b0;
        this.r1 = r1;
        this.g1 = g1;
        this.b1 = b1;
        rCol = r0;
        gCol = g0;
        bCol = b0;
        return this;
    }

    public GenericParticle opacity(float from, float to) {
        alpha0 = from;
        alpha1 = to;
        alpha = from;
        return this;
    }

    /** How big, in the original's units: a tenth of a block from the middle to an edge for each one. */
    public GenericParticle size(float from, float to) {
        size0 = from;
        size1 = to;
        quadSize = 0.1F * from;
        return this;
    }

    /** The cells it plays through, by number along the rows. */
    public GenericParticle cells(int first, int frames, int every) {
        this.first = first;
        this.frames = Math.max(1, frames);
        this.every = Math.max(1, every);
        numbered(first, grid);
        return this;
    }

    /** Plays its cells round and round rather than once over its life. */
    public GenericParticle loops() {
        loops = true;
        return this;
    }

    /** Turned to start with, in degrees, and how far it turns each tick. */
    public GenericParticle spins(float from, float perTick) {
        roll = from * Mth.DEG_TO_RAD;
        oRoll = roll;
        spin = perTick * Mth.DEG_TO_RAD;
        return this;
    }

    /** How much of its speed it keeps each tick. */
    public GenericParticle slows(double keeps) {
        slows = keeps;
        return this;
    }

    /**
     * Shows only after this many ticks, and lives that much longer, as the original's delay did. Asked for last,
     * since {@link #lasts}, {@link #opacity} and {@link #size} would otherwise undo it.
     */
    public GenericParticle waits(int ticks) {
        waits = Math.max(0, ticks);
        lifetime += waits;
        alpha = 0.0F;
        quadSize = 0.0F;
        return this;
    }

    /** Stops at what it runs into, rather than passing through. */
    public GenericParticle collides() {
        hasPhysics = true;
        return this;
    }

    @Override
    public void tick() {
        xo = x;
        yo = y;
        zo = z;
        oRoll = roll;
        if (age++ >= lifetime) {
            remove();
            return;
        }
        roll += spin;
        move(xd, yd, zd);
        xd *= slows;
        yd *= slows;
        zd *= slows;
        if (onGround && slows != 1.0) {
            xd *= 0.7F;
            zd *= 0.7F;
        }

        if (age < waits) {
            // not there yet: it takes up its life only once it shows
            alpha = 0.0F;
            quadSize = 0.0F;
            return;
        }
        float through = Math.min(1.0F, (age - waits) / (float) (lifetime - waits));
        int frame = loops ? first + (age / every) % frames
                : (int) (first + Math.min(frames * through, frames - 1));
        numbered(frame, grid);
        rCol = Mth.lerp(through, r0, r1);
        gCol = Mth.lerp(through, g0, g1);
        bCol = Mth.lerp(through, b0, b1);
        float faded = Mth.clamp(Mth.lerp(through, alpha0, alpha1), 0.0F, 1.0F);
        alpha = age - waits <= 1 || age >= lifetime - 1 ? faded / 2.0F : faded;
        quadSize = 0.1F * Math.max(0.0F, Mth.lerp(through, size0, size1));
    }
}
