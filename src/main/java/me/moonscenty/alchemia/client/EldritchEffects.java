package me.moonscenty.alchemia.client;

import me.moonscenty.alchemia.client.particle.GenericParticle;
import me.moonscenty.alchemia.client.particle.LegacySheet;
import me.moonscenty.alchemia.entity.EldritchGuardian;
import me.moonscenty.alchemia.entity.EldritchOrb;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.RandomSource;

/** What the Outer Lands' creatures leave in the air: the guardian's trail of black smoke and the orb's dark burst. */
public final class EldritchEffects {
    /** The original's dark wisp run: thirteen frames on the fourth row of the old sheet. */
    private static final int WISP_CELLS = 48;
    private static final int WISP_FRAMES = 13;

    private EldritchEffects() {
    }

    /** A wisp or two of black smoke off the waist each tick, as many as the particle setting allows. */
    public static void smoke(EldritchGuardian guardian) {
        if (!(guardian.level() instanceof ClientLevel level)) {
            return;
        }
        ParticleStatus status = Minecraft.getInstance().options.particles().get();
        int count = status == ParticleStatus.ALL ? 2 : status == ParticleStatus.DECREASED ? 1 : 0;
        RandomSource random = guardian.getRandom();
        for (int i = 0; i < count; i++) {
            float shade = random.nextFloat() * 0.05F;
            Minecraft.getInstance().particleEngine.add(new GenericParticle(level,
                    guardian.getX() + (random.nextFloat() - random.nextFloat()) * 0.2,
                    guardian.getY() + 0.22 * guardian.getBbHeight(),
                    guardian.getZ() + (random.nextFloat() - random.nextFloat()) * 0.2,
                    random.nextGaussian() * 0.03, -0.05, random.nextGaussian() * 0.03, LegacySheet.COVER, 16)
                    .lasts((int) (40 / (random.nextFloat() * 0.3F + 0.7F)))
                    .colour(shade, shade, shade)
                    .opacity(0.15F, 0.0F)
                    .cells(WISP_CELLS, WISP_FRAMES, 1)
                    .loops()
                    .slows(0.98)
                    .size(0.4F, 0.4F));
        }
    }

    /** A dark wisp from somewhere on an obelisk's shaft down to something from the Outer Lands it is keeping. */
    public static void obeliskWisp(net.minecraft.world.level.Level world, net.minecraft.core.BlockPos pos,
            net.minecraft.world.entity.LivingEntity kept) {
        if (!(world instanceof ClientLevel level)) {
            return;
        }
        RandomSource random = level.random;
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.0 + random.nextFloat() * 3.0;
        double z = pos.getZ() + 0.5;
        int lasts = 20 + random.nextInt(10);
        double tx = kept.getX();
        double ty = kept.getY() + kept.getBbHeight() / 2.0;
        double tz = kept.getZ();
        float shade = random.nextFloat() * 0.1F;
        Minecraft.getInstance().particleEngine.add(new GenericParticle(level, x, y, z, (tx - x) / lasts,
                (ty - y) / lasts, (tz - z) / lasts, LegacySheet.COVER, 16)
                .lasts(lasts)
                .colour(shade, shade, shade)
                .opacity(0.5F, 0.0F)
                .cells(WISP_CELLS, WISP_FRAMES, 1)
                .loops()
                .size(1.0F, 0.2F));
    }

    /** A purple bubble rising off liquid death, lower the further it has flowed. */
    public static void deathBubble(net.minecraft.world.level.Level world, net.minecraft.core.BlockPos pos, int flowed,
            RandomSource random) {
        if (!(world instanceof ClientLevel level)) {
            return;
        }
        int red = (int) ((0.3F - random.nextFloat() * 0.1F) * 255);
        int blue = (int) ((0.4F + random.nextFloat() * 0.1F) * 255);
        Minecraft.getInstance().particleEngine.add(new me.moonscenty.alchemia.client.particle.BubbleParticle(level,
                pos.getX() + random.nextFloat(), pos.getY() + 0.1F + 0.225F * (4 - Math.min(flowed, 3)),
                pos.getZ() + random.nextFloat(), red << 16 | blue, 1, 0.002,
                me.moonscenty.alchemia.client.particle.BubbleParticle.Kind.BUBBLE));
    }

    /** A puff out of a crab vent, the way it faces. */
    public static void vent(net.minecraft.world.level.Level world, net.minecraft.core.BlockPos pos,
            net.minecraft.core.Direction facing) {
        if (!(world instanceof ClientLevel level)) {
            return;
        }
        RandomSource random = level.random;
        float jx = 0.15F - random.nextFloat() * 0.3F;
        float jy = 0.15F - random.nextFloat() * 0.3F;
        float jz = 0.15F - random.nextFloat() * 0.3F;
        float vx = 0.1F - random.nextFloat() * 0.2F;
        float vy = 0.1F - random.nextFloat() * 0.2F;
        float vz = 0.1F - random.nextFloat() * 0.2F;
        Minecraft.getInstance().particleEngine.add(new me.moonscenty.alchemia.client.particle.VentParticle(level,
                pos.getX() + 0.5 + jx + facing.getStepX() / 2.1F, pos.getY() + 0.5 + jy + facing.getStepY() / 2.1F,
                pos.getZ() + 0.5 + jz + facing.getStepZ() / 2.1F, facing.getStepX() / 3.0F + vx,
                facing.getStepY() / 3.0F + vy, facing.getStepZ() / 3.0F + vz, 0x9988AA).scaled(2.0F));
    }

    /** A faint dark stream from somewhere in a void down to where it pours from, and now and then the wind. */
    public static void voidDraw(net.minecraft.world.level.Level world, net.minecraft.core.BlockPos from,
            net.minecraft.core.BlockPos to) {
        if (!(world instanceof ClientLevel level)) {
            return;
        }
        RandomSource random = level.random;
        double x = from.getX() + random.nextFloat();
        double y = from.getY() + random.nextFloat();
        double z = from.getZ() + random.nextFloat();
        double tx = to.getX() + 0.5;
        double ty = to.getY() + 0.5;
        double tz = to.getZ() + 0.5;
        int lasts = 20 + (int) Math.sqrt(from.distSqr(to)) * 4;
        Minecraft.getInstance().particleEngine.add(new GenericParticle(level, x, y, z, (tx - x) / lasts,
                (ty - y) / lasts, (tz - z) / lasts, LegacySheet.COVER, 16)
                .lasts(lasts)
                .colour(0.0F, 0.0F, 0.0F)
                .opacity(0.4F, 0.4F)
                .cells(48, 14, 1)
                .loops()
                .size(3.0F, 3.0F));
        if (random.nextInt(33) == 0) {
            level.playLocalSound(from, me.moonscenty.alchemia.registry.ModSounds.WIND.get(),
                    net.minecraft.sounds.SoundSource.AMBIENT, 0.33F, 1.0F, false);
        }
    }

    /** Thirty dark wisps thrown out from where an orb burst, shrinking as they go. */
    public static void burst(EldritchOrb orb) {
        if (!(orb.level() instanceof ClientLevel level)) {
            return;
        }
        RandomSource random = orb.getRandom();
        for (int i = 0; i < 30; i++) {
            float fx = (random.nextFloat() - random.nextFloat()) * 0.3F;
            float fy = (random.nextFloat() - random.nextFloat()) * 0.3F;
            float fz = (random.nextFloat() - random.nextFloat()) * 0.3F;
            int lasts = (int) (36 / (random.nextFloat() * 0.3F + 0.7F));
            float shade = random.nextFloat() * 0.1F;
            Minecraft.getInstance().particleEngine.add(new GenericParticle(level, orb.getX() + fx,
                    orb.getY() + fy, orb.getZ() + fz, fx * 7.0 / lasts, fy * 7.0 / lasts, fz * 7.0 / lasts,
                    LegacySheet.COVER, 16)
                    .lasts(lasts)
                    .colour(shade, shade, shade)
                    .opacity(0.75F, 0.75F)
                    .cells(WISP_CELLS, WISP_FRAMES, 1)
                    .loops()
                    .size(3.0F, 0.0F));
        }
    }
}
