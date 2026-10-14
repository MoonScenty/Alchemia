package me.moonscenty.alchemia.client;

import me.moonscenty.alchemia.client.particle.GenericParticle;
import me.moonscenty.alchemia.client.particle.LegacySheet;
import me.moonscenty.alchemia.entity.cult.Cultist;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.RandomSource;

/** What the cult leaves in the air: the red puff a cultist steps out of. */
public final class CultEffects {
    private CultEffects() {
    }

    /** Twenty puffs about the body, white going to blood red, off the original's six-frame run. */
    public static void arrive(Cultist cultist) {
        if (!(cultist.level() instanceof ClientLevel level)) {
            return;
        }
        RandomSource random = cultist.getRandom();
        float width = cultist.getBbWidth();
        for (int i = 0; i < 20; i++) {
            double vx = random.nextGaussian() * 0.05;
            double vy = random.nextGaussian() * 0.05;
            double vz = random.nextGaussian() * 0.05;
            float size = 3.0F + random.nextFloat() * 2.0F;
            Minecraft.getInstance().particleEngine.add(new GenericParticle(level,
                    cultist.getX() + random.nextFloat() * width * 2.0F - width + vx * 2.0,
                    cultist.getY() + random.nextFloat() * cultist.getBbHeight() + vy * 2.0,
                    cultist.getZ() + random.nextFloat() * width * 2.0F - width + vz * 2.0,
                    vx, vy, vz, LegacySheet.COVER_2, 16)
                    .lasts(10 + random.nextInt(10))
                    .colours(1.0F, 1.0F, 1.0F, 0.6F, 0.0F, 0.0F)
                    .opacity(0.8F, 0.8F)
                    .cells(160, 6, 1)
                    .size(size, size));
        }
    }
}
