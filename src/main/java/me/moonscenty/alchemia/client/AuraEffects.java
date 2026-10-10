package me.moonscenty.alchemia.client;

import me.moonscenty.alchemia.aura.TaintCloud;
import me.moonscenty.alchemia.client.particle.CrumbParticle;
import me.moonscenty.alchemia.client.particle.GenericParticle;
import me.moonscenty.alchemia.client.particle.GlowWispParticle;
import me.moonscenty.alchemia.client.particle.LegacySheet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * What the aura's plants and the taint cloud show, as the original showed them.
 */
public final class AuraEffects {
    /** How far across the cloud's puffs and its rain spread. */
    private static final int PUFFS_WITHIN = 18;
    private static final int RAIN_WITHIN = 16;

    private static int rainSound;

    private AuraEffects() {
    }

    /** A shimmerleaf gives off a pale blue-green glow now and then, which hangs where it is and fades. */
    public static void shimmerleaf(Level level, BlockPos pos, RandomSource random) {
        if (!(level instanceof ClientLevel client) || random.nextInt(3) != 0) {
            return;
        }
        float r = 0.3F + level.random.nextFloat() * 0.3F;
        float g = 0.7F + level.random.nextFloat() * 0.3F;
        float b = 0.7F + level.random.nextFloat() * 0.3F;
        add(new GlowWispParticle(client, pos.getX() + 0.5 + (random.nextFloat() - random.nextFloat()) * 0.1F,
                pos.getY() + 0.5 + (random.nextFloat() - random.nextFloat()) * 0.15F,
                pos.getZ() + 0.5 + (random.nextFloat() - random.nextFloat()) * 0.1F, 0.2F, r, g, b, false, 0.0F));
    }

    /** A vishroom lets go a little purple glow now and then, which dwindles and sinks. */
    public static void vishroom(Level level, BlockPos pos, RandomSource random) {
        if (!(level instanceof ClientLevel client) || random.nextInt(3) != 0) {
            return;
        }
        add(new GlowWispParticle(client, pos.getX() + 0.5 + (random.nextFloat() - random.nextFloat()) * 0.4F,
                pos.getY() + 0.3, pos.getZ() + 0.5 + (random.nextFloat() - random.nextFloat()) * 0.4F,
                0.1F, 0.5F, 0.3F, 0.8F, true, 0.015F));
    }

    /**
     * A taint cloud, every tick: its rain, falling as the game's own rain falls but splashing up crumbs of slime, and
     * great slow puffs of its cloud.
     */
    public static void taintCloud(TaintCloud cloud) {
        if (!(cloud.level() instanceof ClientLevel level)) {
            return;
        }
        RandomSource random = level.random;
        rain(level, cloud, random);
        BlockPos at = cloud.blockPosition();
        for (int i = 0; i < CrucibleEffects.count(1); i++) {
            BlockPos puff = at.offset(random.nextInt(PUFFS_WITHIN) - random.nextInt(PUFFS_WITHIN), 1,
                    random.nextInt(PUFFS_WITHIN) - random.nextInt(PUFFS_WITHIN));
            float size = 25.0F + 15.0F * random.nextFloat();
            add(new GenericParticle(level, puff.getX() + random.nextFloat(), puff.getY() + random.nextFloat(),
                    puff.getZ() + random.nextFloat(), 0.0, -random.nextFloat() * 0.15, 0.0, LegacySheet.COVER_2, 16)
                    .lasts(32 + random.nextInt(4))
                    .colour(1.0F, 0.5F, 1.0F)
                    .opacity(0.3F, 0.3F)
                    .cells(176, 32, 1)
                    .size(size, size)
                    .spins(random.nextFloat() * 360.0F, random.nextBoolean()
                            ? -1.0F - random.nextFloat() : 1.0F + random.nextFloat()));
        }
    }

    /** The cloud's rain, as the game's rain lands: on lava it smokes, on anything else it splashes. */
    private static void rain(ClientLevel level, TaintCloud cloud, RandomSource random) {
        int drops = switch (Minecraft.getInstance().options.particles().get()) {
            case ALL -> 5;
            case DECREASED -> 2;
            case MINIMAL -> 0;
        };
        BlockPos at = cloud.blockPosition();
        double soundX = 0;
        double soundY = 0;
        double soundZ = 0;
        int hits = 0;
        for (int i = 0; i < drops; i++) {
            BlockPos column = at.offset(random.nextInt(RAIN_WITHIN) - random.nextInt(RAIN_WITHIN), 0,
                    random.nextInt(RAIN_WITHIN) - random.nextInt(RAIN_WITHIN));
            BlockPos top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, column);
            if (Mth.square(top.getX() - at.getX()) + Mth.square(top.getZ() - at.getZ()) > RAIN_WITHIN * RAIN_WITHIN
                    || top.getY() > at.getY() + RAIN_WITHIN || top.getY() < at.getY() - RAIN_WITHIN) {
                continue;
            }
            BlockPos below = top.below();
            float fx = random.nextFloat();
            float fz = random.nextFloat();
            FluidState fluid = level.getFluidState(below);
            if (fluid.is(FluidTags.LAVA)) {
                level.addParticle(ParticleTypes.SMOKE, top.getX() + fx, top.getY() + 0.1, top.getZ() + fz,
                        0.0, 0.0, 0.0);
            } else if (!level.getBlockState(below).isAir()) {
                hits++;
                if (random.nextInt(hits) == 0) {
                    VoxelShape shape = level.getBlockState(below).getCollisionShape(level, below);
                    soundX = below.getX() + fx;
                    soundY = below.getY() + 0.1 + (shape.isEmpty() ? 1.0 : shape.max(Direction.Axis.Y)) - 1.0;
                    soundZ = below.getZ() + fz;
                }
                splash(level, top, random);
            }
        }
        if (hits > 0 && random.nextInt(3) < rainSound++) {
            rainSound = 0;
            boolean under = soundY > at.getY() + 1
                    && level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, at).getY() > Mth.floor(at.getY());
            level.playLocalSound(soundX, soundY, soundZ, SoundEvents.WEATHER_RAIN, SoundSource.WEATHER,
                    under ? 0.1F : 0.2F, under ? 0.5F : 1.0F, false);
        }
    }

    /** A drop of flux rain landing: a crumb of slime, flung up a little off the ground, that fades as it goes. */
    private static void splash(ClientLevel level, BlockPos top, RandomSource random) {
        float angle = random.nextFloat() * Mth.TWO_PI;
        float out = random.nextFloat() * 0.5F + 0.5F;
        add(new CrumbParticle(level, top.getX() + 0.5 + Mth.sin(angle) * out, top.getY(),
                top.getZ() + 0.5 + Mth.cos(angle) * out, new ItemStack(Items.SLIME_BALL), 1.0F, 0.2F, 1.0F, 0.6F,
                (int) (44.0F / (random.nextFloat() * 0.9F + 0.1F))));
    }

    private static void add(Particle particle) {
        Minecraft.getInstance().particleEngine.add(particle);
    }
}
