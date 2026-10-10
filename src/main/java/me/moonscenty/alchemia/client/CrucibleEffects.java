package me.moonscenty.alchemia.client;

import java.util.List;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.block.entity.CrucibleBlockEntity;
import me.moonscenty.alchemia.client.particle.BubbleParticle;
import me.moonscenty.alchemia.client.particle.GenericParticle;
import me.moonscenty.alchemia.client.particle.LegacySheet;
import me.moonscenty.alchemia.client.particle.WispParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * What a crucible shows, as the original showed it: froth on a boiling surface and froth running over the rim of a
 * full pot, a bubble now and then in the colour of something dissolved, and a boil-up when something goes in.
 * <p>
 * Drawn every tick on the client, while there is water in the pot.
 */
public final class CrucibleEffects {
    /** A surface bubble's chance in a tick is one in this. */
    private static final int BUBBLE_ODDS = 6;
    /** White, for a boil-up in a pot holding nothing. */
    private static final int WHITE = 0xFFFFFF;
    /** The froth's colour. */
    private static final int FROTH = 0x7F7FB2;

    private CrucibleEffects() {
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CrucibleBlockEntity crucible) {
        if (!(level instanceof ClientLevel client) || crucible.water() <= 0) {
            return;
        }
        RandomSource random = level.random;
        double top = pos.getY() + crucible.surface();
        if (crucible.working()) {
            add(new BubbleParticle(client, pos.getX() + 0.2 + random.nextFloat() * 0.6, top,
                    pos.getZ() + 0.2 + random.nextFloat() * 0.6, FROTH, -4, 0.0, BubbleParticle.Kind.FROTH));
            // a pot as full as it will go runs over at every edge of the rim
            if (crucible.dissolved().total() >= CrucibleBlockEntity.CAPACITY) {
                for (int i = 0; i < 2; i++) {
                    rimFroth(client, pos.getX(), pos.getZ() + random.nextFloat(), pos.getY() + 1);
                    rimFroth(client, pos.getX() + 1, pos.getZ() + random.nextFloat(), pos.getY() + 1);
                    rimFroth(client, pos.getX() + random.nextFloat(), pos.getZ(), pos.getY() + 1);
                    rimFroth(client, pos.getX() + random.nextFloat(), pos.getZ() + 1, pos.getY() + 1);
                }
            }
        }
        List<Holder<Aspect>> held = crucible.dissolved().sortedByAmount();
        if (random.nextInt(BUBBLE_ODDS) == 0 && !held.isEmpty()) {
            int colour = held.get(random.nextInt(held.size())).value().color();
            int across = 5 + random.nextInt(22);
            int down = 5 + random.nextInt(22);
            add(new BubbleParticle(client, pos.getX() + across / 32.0 + 1 / 64.0, top + 0.05,
                    pos.getZ() + down / 32.0 + 1 / 64.0, colour, 1, 0.002, BubbleParticle.Kind.BUBBLE));
        }
    }

    private static void rimFroth(ClientLevel level, double x, double z, double y) {
        add(new BubbleParticle(level, x, y, z, FROTH, -4, 0.0, BubbleParticle.Kind.FROTH_DOWN));
    }

    /**
     * Something went into the pot: it boils up, in the colours of what is in it. The harder the event, the higher
     * the bubbles are pushed; the original pushed a spill five times harder than a dissolving.
     */
    public static void boil(Level level, BlockPos pos, CrucibleBlockEntity crucible, int strength) {
        if (!(level instanceof ClientLevel client)) {
            return;
        }
        RandomSource random = level.random;
        List<Holder<Aspect>> held = crucible.dissolved().sortedByAmount();
        double top = pos.getY() + 0.1 + crucible.surface();
        for (int call = 0; call < 10; call++) {
            for (int i = 0; i < count(1); i++) {
                int colour = held.isEmpty() ? WHITE : held.get(random.nextInt(held.size())).value().color();
                add(new BubbleParticle(client, pos.getX() + 0.2 + random.nextFloat() * 0.6, top,
                        pos.getZ() + 0.2 + random.nextFloat() * 0.6, colour, 3, 0.003 * strength,
                        BubbleParticle.Kind.BUBBLE));
            }
        }
    }

    /** A recipe came out of the pot: the original's purple puff, with its flourish, rising out of the top. */
    public static void crafted(Level level, BlockPos pos) {
        if (level instanceof ClientLevel client) {
            bamf(client, pos.getX() + 0.5, pos.getY() + 1.25, pos.getZ() + 0.5, 0.5F, 0.1F, 0.6F, true, true);
        }
    }

    /**
     * The original's puff of magic ({@code drawBamf}): sparks flung out, with the flourish a few pale wisps and a
     * flash, and a little smoke that lingers. When it goes up the sparks are flung upwards and the smoke drifts up.
     */
    public static void bamf(ClientLevel level, double x, double y, double z, float r, float g, float b,
            boolean flair, boolean up) {
        RandomSource random = level.random;
        for (int i = 0; i < count(3) + random.nextInt(3) + 1; i++) {
            double xd = flung(random, 0.05F, 0.05F);
            double yd = flung(random, 0.05F, 0.05F);
            double zd = flung(random, 0.05F, 0.05F);
            if (up) {
                yd = Math.abs(yd) * (i + 2);
            }
            int lasts = 20 + random.nextInt(15);
            float scale = 4.0F + random.nextFloat() * 3.0F;
            add(new GenericParticle(level, x + xd * 2.0, y + (up ? -0.5F + random.nextFloat() * 0.1F : yd * 2.0),
                    z + zd * 2.0, xd / 2.0, yd / 2.0, zd / 2.0, LegacySheet.COVER_2, 8)
                    .lasts(lasts)
                    .colour(tinge(random, r), tinge(random, g), tinge(random, b))
                    .opacity(1.0F, 0.5F)
                    .cells(59, 5, 1)
                    .size(3.0F, scale)
                    .slows(0.7)
                    .spins(random.nextFloat() * 360.0F, random.nextBoolean() ? -1.0F : 1.0F)
                    .collides());
        }
        if (flair) {
            for (int i = 0; i < count(3) + random.nextInt(3); i++) {
                double xd = flung(random, 0.1F, 0.05F);
                double yd = flung(random, 0.1F, 0.05F);
                double zd = flung(random, 0.1F, 0.05F);
                add(new WispParticle(level, x + xd * 2.0, y + yd * 2.0, z + zd * 2.0, xd, yd, zd,
                        15 + random.nextInt(10), 0.5F));
            }
            add(new GenericParticle(level, x, y, z, 0.0, 0.0, 0.0, LegacySheet.GLOW_2, 8)
                    .lasts(10)
                    .colour(1.0F, 0.9F, 1.0F)
                    .opacity(1.0F, 0.0F)
                    .cells(37, 1, 1)
                    .size(10.0F + random.nextFloat() * 2.0F, 0.0F)
                    .spins(random.nextFloat() * 360.0F, (float) random.nextGaussian()));
        }
        for (int i = 0; i < count(flair ? 1 : 0) + random.nextInt(3); i++) {
            double xd = flung(random, 0.0025F, 0.005F);
            double yd = flung(random, 0.0025F, 0.005F);
            double zd = flung(random, 0.0025F, 0.005F);
            if (up) {
                yd = Math.abs(yd);
            }
            add(new GenericParticle(level, x + xd * 5.0, y + yd * 5.0, z + zd * 5.0, xd, yd, zd,
                    LegacySheet.GLOW_2, 8)
                    .lasts(75 + random.nextInt(75 + 20 * i))
                    .colours((0.9F + random.nextFloat() * 0.1F + r) / 2.0F, (0.1F + g) / 2.0F,
                            (0.5F + random.nextFloat() * 0.1F + b) / 2.0F,
                            0.1F + random.nextFloat() * 0.1F, 0.0F, 0.5F + random.nextFloat() * 0.1F)
                    .opacity(0.75F, 0.0F)
                    .cells(28 + random.nextInt(4), 1, 1)
                    .size(5.0F, 10.0F + random.nextFloat() * 4.0F)
                    .spins(random.nextFloat() * 360.0F, random.nextBoolean()
                            ? -3.0F - random.nextFloat() * 3.0F : 3.0F + random.nextFloat() * 3.0F)
                    .collides());
        }
    }

    /** A speed of at least {@code least} and up to {@code more} faster, either way. */
    private static double flung(RandomSource random, float least, float more) {
        return (least + random.nextFloat() * more) * (random.nextBoolean() ? -1 : 1);
    }

    /** A colour band a little off, one way or the other. */
    private static float tinge(RandomSource random, float band) {
        return Math.clamp(band * (1.0F + (float) random.nextGaussian() * 0.1F), 0.0F, 1.0F);
    }

    /** How many of something the original drew, by the player's particle setting. */
    static int count(int n) {
        return switch (Minecraft.getInstance().options.particles().get()) {
            case MINIMAL -> n / 2;
            case DECREASED -> n;
            case ALL -> n * 2;
        };
    }

    private static void add(Particle particle) {
        Minecraft.getInstance().particleEngine.add(particle);
    }
}
