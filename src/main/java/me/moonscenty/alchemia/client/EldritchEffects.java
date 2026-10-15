package me.moonscenty.alchemia.client;

import me.moonscenty.alchemia.client.particle.GenericParticle;
import me.moonscenty.alchemia.client.particle.LegacySheet;
import me.moonscenty.alchemia.entity.EldritchGuardian;
import me.moonscenty.alchemia.entity.EldritchOrb;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
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

    /** A dark wisp drifting up off the Warden, every tick. */
    public static void wardenWisp(me.moonscenty.alchemia.entity.boss.EldritchWarden warden) {
        if (!(warden.level() instanceof ClientLevel level)) {
            return;
        }
        RandomSource random = level.random;
        double x = warden.getX() + (random.nextFloat() - random.nextFloat()) * 0.2;
        double z = warden.getZ() + (random.nextFloat() - random.nextFloat()) * 0.2;
        float shade = random.nextFloat() * 0.1F;
        Minecraft.getInstance().particleEngine.add(new GenericParticle(level, x, warden.getY()
                + warden.getBbHeight() * 0.25, z, 0.0, 0.02, 0.0, LegacySheet.COVER, 16)
                .lasts(20 + random.nextInt(10))
                .colour(shade, shade, shade)
                .opacity(0.5F, 0.0F)
                .cells(WISP_CELLS, WISP_FRAMES, 1)
                .loops()
                .size(1.0F, 0.3F));
    }

    /** The smoke the Warden rises out of the floor in: spirals, higher the further it has risen. */
    public static void wardenRise(me.moonscenty.alchemia.entity.boss.EldritchWarden warden) {
        if (!(warden.level() instanceof ClientLevel level)) {
            return;
        }
        RandomSource random = level.random;
        float risen = Math.max(1.0F, warden.getBbHeight()
                * ((me.moonscenty.alchemia.entity.boss.EldritchWarden.RISING - warden.spawnTimer())
                        / (float) me.moonscenty.alchemia.entity.boss.EldritchWarden.RISING));
        for (int i = 0; i < 33; i++) {
            double angle = Math.toRadians(random.nextInt(360));
            double radius = risen / 2.0;
            double x = warden.getX() + Math.cos(angle) * radius * random.nextFloat();
            double z = warden.getZ() + Math.sin(angle) * radius * random.nextFloat();
            double y = warden.getBoundingBox().minY + random.nextFloat() * risen;
            Minecraft.getInstance().particleEngine.add(new GenericParticle(level, x, y, z,
                    -Math.sin(angle) * 0.05, 0.01, Math.cos(angle) * 0.05, LegacySheet.COVER, 16)
                    .lasts(15 + random.nextInt(10))
                    .colour(0x22 / 255.0F, 0x11 / 255.0F, 0x2F / 255.0F)
                    .opacity(0.6F, 0.0F)
                    .cells(WISP_CELLS, WISP_FRAMES, 1)
                    .loops()
                    .size(1.5F, 0.5F));
        }
    }

    /** The Warden's screech: a ring going out from it. */
    public static void screech(me.moonscenty.alchemia.entity.boss.EldritchWarden warden) {
        if (!(warden.level() instanceof ClientLevel level)) {
            return;
        }
        for (int i = 0; i < 36; i++) {
            double angle = Math.toRadians(i * 10);
            Minecraft.getInstance().particleEngine.add(new GenericParticle(level, warden.getX(),
                    warden.getEyeY() - 0.5, warden.getZ(), Math.cos(angle) * 0.6, 0.0, Math.sin(angle) * 0.6,
                    LegacySheet.COVER, 16)
                    .lasts(10)
                    .colour(0.3F, 0.1F, 0.4F)
                    .opacity(0.6F, 0.0F)
                    .cells(WISP_CELLS, WISP_FRAMES, 1)
                    .loops()
                    .size(1.0F, 2.0F));
        }
    }

    /** The headless Construct: smoke out of its neck, now and then a spark, and its crackling while it charges. */
    public static void golemNeck(me.moonscenty.alchemia.entity.boss.EldritchGolem golem) {
        if (!(golem.level() instanceof ClientLevel level)) {
            return;
        }
        RandomSource random = level.random;
        float body = -golem.yBodyRot * Mth.DEG_TO_RAD - Mth.PI;
        double nx = Mth.sin(body) * -1.0;
        double nz = Mth.cos(body) * -1.0;
        double neckY = golem.getY() + 3.33 - 0.75;
        Minecraft.getInstance().particleEngine.add(new me.moonscenty.alchemia.client.particle.VentParticle(level,
                golem.getX() + nx * 0.66, neckY, golem.getZ() + nz * 0.66, 0.0, 0.001, 0.0, 0x555555).scaled(4.0F));
        if (random.nextInt(20) == 0) {
            float a = (random.nextFloat() - random.nextFloat()) / 2.0F;
            float b = (random.nextFloat() - random.nextFloat()) / 2.0F;
            spark(level, golem.getX() + nx + a, neckY + 0.5, golem.getZ() + nz + b, 0.65F + random.nextFloat() * 0.1F,
                    1.0F, 1.0F);
        }
        if (golem.arcing > 0) {
            arc(level, golem.getX(), golem.getY() + golem.getBbHeight() / 2.0, golem.getZ(),
                    golem.arcTo.getX() + 0.5, golem.arcTo.getY() + 1.0, golem.arcTo.getZ() + 0.5,
                    0.65F + random.nextFloat() * 0.1F, 1.0F, 1.0F);
            golem.arcing--;
        }
    }

    private static void spark(ClientLevel level, double x, double y, double z, float r, float g, float b) {
        Minecraft.getInstance().particleEngine.add(new GenericParticle(level, x, y, z, 0.0, 0.0, 0.0,
                LegacySheet.GLOW, 16)
                .lasts(6)
                .colour(r, g, b)
                .opacity(0.8F, 0.0F)
                .cells(WISP_CELLS, WISP_FRAMES, 1)
                .size(0.3F, 0.1F));
    }

    /** A block arc from the server: from the middle of the block's top to the middle of what it strikes. */
    public static void blockArc(me.moonscenty.alchemia.network.BlockArc arc) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        net.minecraft.world.entity.Entity to = level.getEntity(arc.to());
        if (to == null) {
            return;
        }
        arc(level, arc.from().getX() + 0.5, arc.from().getY() + 0.1, arc.from().getZ() + 0.5, to.getX(),
                to.getY() + to.getBbHeight() / 2.0, to.getZ(), arc.r(), arc.g(), arc.b());
    }

    /** A crooked line of light from one point to another, gone in a moment. */
    public static void arc(ClientLevel level, double x, double y, double z, double tx, double ty, double tz,
            float r, float g, float b) {
        RandomSource random = level.random;
        int steps = Math.max(4, (int) (Math.sqrt((tx - x) * (tx - x) + (ty - y) * (ty - y) + (tz - z) * (tz - z)) * 4));
        for (int i = 0; i <= steps; i++) {
            double t = i / (double) steps;
            double jitter = i == 0 || i == steps ? 0.0 : 0.2;
            spark(level, x + (tx - x) * t + (random.nextFloat() - 0.5) * jitter,
                    y + (ty - y) * t + (random.nextFloat() - 0.5) * jitter,
                    z + (tz - z) * t + (random.nextFloat() - 0.5) * jitter, r, g, b);
        }
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
