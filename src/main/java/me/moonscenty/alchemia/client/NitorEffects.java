package me.moonscenty.alchemia.client;

import me.moonscenty.alchemia.client.particle.GlowWispParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;

/**
 * A nitor's flame, as the original's ticking block let it off: two sorts of the original's wisp welling up from the
 * middle of the block. Now and then a big one in the colour of the nitor's dye, and less often a small pale yellow
 * one, both drifting a little way upwards and out as they go.
 * <p>
 * How often turns on the particle setting, as it did in the original: the fewer particles asked for, the rarer.
 */
public final class NitorEffects {
    private NitorEffects() {
    }

    public static void burn(Level level, BlockPos pos, DyeColor dye) {
        if (!(level instanceof ClientLevel client)) {
            return;
        }
        RandomSource random = level.random;
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;
        if (random.nextInt(9 - scaled(2)) == 0) {
            int colour = dye.getMapColor().col;
            add(new GlowWispParticle(client, x, y, z, 0.5F, ((colour >> 16) & 0xFF) / 255.0F,
                    ((colour >> 8) & 0xFF) / 255.0F, (colour & 0xFF) / 255.0F, true, -0.025F)
                    .towards(pos.getX() + 0.3 + random.nextFloat() * 0.4, y,
                            pos.getZ() + 0.3 + random.nextFloat() * 0.4));
        }
        if (random.nextInt(15 - scaled(4)) == 0) {
            // the original's second kind of wisp, which is always this pale yellow
            add(new GlowWispParticle(client, x, y, z, 0.25F, 0.5F + random.nextFloat() * 0.3F,
                    0.5F + random.nextFloat() * 0.3F, 0.2F, true, -0.02F)
                    .towards(pos.getX() + 0.4 + random.nextFloat() * 0.2, y,
                            pos.getZ() + 0.4 + random.nextFloat() * 0.2));
        }
    }

    /** The original's measure of how many particles are wanted: half at the least setting, double at all. */
    private static int scaled(int count) {
        ParticleStatus status = Minecraft.getInstance().options.particles().get();
        return switch (status) {
            case MINIMAL -> count / 2;
            case DECREASED -> count;
            case ALL -> count * 2;
        };
    }

    private static void add(GlowWispParticle particle) {
        Minecraft.getInstance().particleEngine.add(particle);
    }
}
