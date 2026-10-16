package me.moonscenty.alchemia.client;

import me.moonscenty.alchemia.client.particle.MoteParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;

/** The glimmer of the magical forest's grass at night. */
public final class MagicalForestEffects {
    private static final int MOTE_AGE = 400;
    private static final int TRAVEL_MOTE_AGE = 30;

    private MagicalForestEffects() {
    }

    /**
     * Now and then, the darker the night the more often, a mote rises off some grass within eight blocks: the
     * original's reckoning of how dark the sky over the grass is.
     */
    /** A pale mote rising off a paving stone of travel where something trod, as the original drew it. */
    public static void travelMote(Level level, BlockPos pos, RandomSource random) {
        if (level instanceof ClientLevel client) {
            Minecraft.getInstance().particleEngine.add(new MoteParticle(client, pos.getX() + random.nextFloat(),
                    pos.getY(), pos.getZ() + random.nextFloat(), TRAVEL_MOTE_AGE));
        }
    }

    public static void motes(Level level, BlockPos pos, RandomSource random) {
        if (!(level instanceof ClientLevel client)) {
            return;
        }
        float angle = level.getSunAngle(1.0F);
        float towards = angle < Mth.PI ? 0.0F : Mth.TWO_PI;
        angle += (towards - angle) * 0.2F;
        int light = level.getBrightness(LightLayer.SKY, pos.above()) - level.getSkyDarken();
        light = Mth.clamp(Math.round(light * Mth.cos(angle)), 0, 15);
        if (2 + light * 2 >= 1 + random.nextInt(13)) {
            return;
        }
        BlockPos at = pos.offset(Mth.nextInt(random, -8, 8), 5, Mth.nextInt(random, -8, 8));
        for (int step = 0; step < 10 && at.getY() > 50 && !level.getBlockState(at).is(Blocks.GRASS_BLOCK); step++) {
            at = at.below();
        }
        if (level.getBlockState(at).is(Blocks.GRASS_BLOCK)) {
            BlockPos over = at.above();
            Minecraft.getInstance().particleEngine.add(new MoteParticle(client, over.getX() + random.nextFloat(),
                    over.getY(), over.getZ() + random.nextFloat(), MOTE_AGE));
        }
    }
}
