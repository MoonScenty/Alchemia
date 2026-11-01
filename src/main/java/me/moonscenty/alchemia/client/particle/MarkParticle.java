package me.moonscenty.alchemia.client.particle;

import me.moonscenty.alchemia.particle.MarkOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;

// not drawn through walls: particles share one pass that can't drop depth testing
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
