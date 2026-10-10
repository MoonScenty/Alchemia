package me.moonscenty.alchemia.client.particle;

import me.moonscenty.alchemia.particle.MarkOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;

/**
 * A sounding tool's mark on an ore it heard, as the original drew it: a block-sized blue-white sparkle that spins
 * through a row of the sheet, shrinking and fading over two seconds, and shows only once the wave from the struck
 * block has reached it.
 * <p>
 * The original drew these through the walls between the player and the ore. The game's particles share one pass
 * that cannot be left with depth testing off without upsetting everything drawn after it, so these are drawn
 * where they can be seen, as other particles are.
 */
public final class MarkParticle {
    private MarkParticle() {
    }

    public static class Maker implements ParticleProvider<MarkOptions> {
        @Override
        public Particle createParticle(MarkOptions options, ClientLevel level, double x, double y, double z,
                double xd, double yd, double zd) {
            return new GenericParticle(level, x, y, z, 0.0, 0.0, 0.0, LegacySheet.GLOW, 16)
                    .lasts(40)
                    .colour(0.5F, 0.5F, 1.0F)
                    .opacity(0.66F, 0.0F)
                    .cells(80, 16, 1)
                    .loops()
                    .size(5.0F, 1.0F)
                    .waits(options.waits());
        }
    }
}
