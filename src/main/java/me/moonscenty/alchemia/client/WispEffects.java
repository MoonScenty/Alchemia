package me.moonscenty.alchemia.client;

import me.moonscenty.alchemia.client.particle.GlowWispParticle;
import me.moonscenty.alchemia.entity.Wisp;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.RandomSource;

/** What a wisp leaves in the air: motes of its own colour as it drifts, and a puff of them coming and going. */
public final class WispEffects {
    /** How many motes make the puff a wisp comes and goes in. */
    private static final int PUFF = 8;

    private WispEffects() {
    }

    /** One mote, somewhere about the middle of it, in its colour. The original's wisp effect. */
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

    /**
     * A puff of motes where it comes into the world and where it goes out.
     * <p>
     * <b>Different from the original</b>, which drew a white flash off its node sheet; that sheet is drawn by the node
     * renderer here and is no particle's.
     */
    public static void burst(Wisp wisp) {
        for (int mote = 0; mote < PUFF; mote++) {
            trail(wisp);
        }
    }
}
