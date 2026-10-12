package me.moonscenty.alchemia.client;

import me.moonscenty.alchemia.client.particle.GlowWispParticle;
import me.moonscenty.alchemia.registry.ModAspects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

/** The wisp a full brain in a jar lets off, as the original's: the nitor's big wisp, in the colour of death. */
public final class BrainJarEffects {
    private BrainJarEffects() {
    }

    public static void full(Level level, BlockPos pos) {
        if (!(level instanceof ClientLevel client)) {
            return;
        }
        RandomSource random = level.random;
        int colour = ModAspects.DEATH.value().color();
        double y = pos.getY() + 0.8;
        Minecraft.getInstance().particleEngine.add(new GlowWispParticle(client, pos.getX() + 0.5, y,
                pos.getZ() + 0.5, 0.5F, ((colour >> 16) & 0xFF) / 255.0F, ((colour >> 8) & 0xFF) / 255.0F,
                (colour & 0xFF) / 255.0F, true, -0.025F)
                .towards(pos.getX() + 0.3 + random.nextFloat() * 0.4, y, pos.getZ() + 0.3 + random.nextFloat() * 0.4));
    }
}
