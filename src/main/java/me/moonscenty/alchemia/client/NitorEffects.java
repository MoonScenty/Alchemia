package me.moonscenty.alchemia.client;

import me.moonscenty.alchemia.client.particle.GlowWispParticle;
import me.moonscenty.alchemia.client.particle.ParticleBudget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;

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
        if (random.nextInt(9 - ParticleBudget.scale(2)) == 0) {
            int colour = dye.getMapColor().col;
            add(new GlowWispParticle(client, x, y, z, 0.5F, ClientColours.red(colour),
                    ClientColours.green(colour), ClientColours.blue(colour), true, -0.025F)
                    .towards(pos.getX() + 0.3 + random.nextFloat() * 0.4, y,
                            pos.getZ() + 0.3 + random.nextFloat() * 0.4));
        }
        if (random.nextInt(15 - ParticleBudget.scale(4)) == 0) {
            // the original's second kind of wisp, which is always this pale yellow
            add(new GlowWispParticle(client, x, y, z, 0.25F, 0.5F + random.nextFloat() * 0.3F,
                    0.5F + random.nextFloat() * 0.3F, 0.2F, true, -0.02F)
                    .towards(pos.getX() + 0.4 + random.nextFloat() * 0.2, y,
                            pos.getZ() + 0.4 + random.nextFloat() * 0.2));
        }
    }

    private static void add(GlowWispParticle particle) {
        Minecraft.getInstance().particleEngine.add(particle);
    }
}
