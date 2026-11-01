package me.moonscenty.alchemia.client;

import me.moonscenty.alchemia.client.particle.GlowWispParticle;
import me.moonscenty.alchemia.entity.Wisp;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.RandomSource;

public final class WispEffects {
    private static final int PUFF = 8;

    private WispEffects() {
    }

    public static void trail(Wisp wisp) {
        if (!(wisp.level() instanceof ClientLevel level)) {
            return;
        }
        int colour = wisp.aspect() == null ? 0xFFFFFF : wisp.aspect().value().color();
        RandomSource random = wisp.getRandom();
        Minecraft.getInstance().particleEngine.add(new GlowWispParticle(level,
                wisp.getX() + (random.nextFloat() - random.nextFloat()) * 0.7F,
                wisp.getY() + 0.45F + (random.nextFloat() - random.nextFloat()) * 0.7F,
                wisp.getZ() + (random.nextFloat() - random.nextFloat()) * 0.7F, 0.1F,
                (colour >> 16 & 0xFF) / 255.0F, (colour >> 8 & 0xFF) / 255.0F, (colour & 0xFF) / 255.0F, false,
                0.0F));
    }

    public static void burst(Wisp wisp) {
        for (int mote = 0; mote < PUFF; mote++) {
            trail(wisp);
        }
    }
}
