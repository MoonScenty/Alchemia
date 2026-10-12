package me.moonscenty.alchemia.client;

import me.moonscenty.alchemia.client.particle.GenericParticle;
import me.moonscenty.alchemia.client.particle.LegacySheet;
import me.moonscenty.alchemia.entity.FireBat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.RandomSource;

/** What a firebat leaves in the air that the game has no particle for: the sparks off a bomb's fuse. */
public final class FireBatEffects {
    private FireBatEffects() {
    }

    /** One spark a tick off a bomb, the original's nine-cell run, white and short-lived. */
    public static void fuse(FireBat bat) {
        if (!(bat.level() instanceof ClientLevel level)) {
            return;
        }
        RandomSource random = bat.getRandom();
        float size = 1.0F + random.nextFloat() * 0.5F;
        Minecraft.getInstance().particleEngine.add(new GenericParticle(level,
                bat.xo + (random.nextFloat() - random.nextFloat()) * 0.1F, bat.yo + bat.getBbHeight() / 2.0F,
                bat.zo + (random.nextFloat() - random.nextFloat()) * 0.1F, 0.0, 0.0, 0.0, LegacySheet.GLOW, 16)
                .lasts(7 + random.nextInt(5))
                .colour(1.0F, 1.0F, 1.0F)
                .opacity(0.8F, 0.8F)
                .cells(151, 9, 1)
                .size(size, size));
    }
}
