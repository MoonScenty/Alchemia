package me.moonscenty.alchemia.client;

import java.util.Map;
import java.util.WeakHashMap;

import me.moonscenty.alchemia.client.particle.GenericParticle;
import me.moonscenty.alchemia.client.particle.LegacySheet;
import me.moonscenty.alchemia.client.particle.VentParticle;
import me.moonscenty.alchemia.golem.Golem;
import me.moonscenty.alchemia.golem.GolemTrait;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

/**
 * What golems and their press put in the air, as the original drew it: the puffs a golem gives when told something,
 * the haze under a flyer, the grit a roller kicks up, and the steam off a press.
 */
public final class GolemEffects {
    /** How far round each roller's wheel has turned, in degrees. */
    private static final Map<Golem, float[]> WHEELS = new WeakHashMap<>();

    private GolemEffects() {
    }

    /** Every tick on the client: what its legs do to the air and the ground. */
    public static void tick(Golem golem) {
        if (!(golem.level() instanceof ClientLevel level)) {
            return;
        }
        if (golem.has(GolemTrait.FLYER) && (!golem.onGround() || golem.tickCount % 5 == 0)) {
            RandomSource random = level.random;
            Minecraft.getInstance().particleEngine.add(new GenericParticle(level, golem.getX(), golem.getY() + 0.1,
                    golem.getZ(), random.nextGaussian() / 100.0, -0.1, random.nextGaussian() / 100.0,
                    LegacySheet.GLOW_2, 8)
                    .lasts(40 + random.nextInt(20))
                    .opacity(0.2F, 0.0F)
                    .cells(24, 1, 1)
                    .size(2.0F, 8.0F)
                    .slows(1.0)
                    .collides()
                    .spins(random.nextFloat() * 360.0F, random.nextBoolean() ? -1.0F : 1.0F));
        }
        if (golem.has(GolemTrait.WHEELED)) {
            roll(golem, level);
        }
    }

    /**
     * Turns a roller's wheel by as far as it went, forwards or back by which way it is facing against which way
     * it went; and on the ground, going at a fair clip, it throws up bits of what it is rolling over.
     */
    private static void roll(Golem golem, ClientLevel level) {
        double dx = golem.getX() - golem.xo;
        double dy = golem.getY() - golem.yo;
        double dz = golem.getZ() - golem.zo;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        float travel = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
        double way = 360.0F - (golem.getYRot() - travel);
        float[] wheel = WHEELS.computeIfAbsent(golem, key -> new float[1]);
        wheel[0] = (float) (wheel[0] + distance / 1.571 * way);
        if (wheel[0] > 360.0F) {
            wheel[0] -= 360.0F;
        }
        if (golem.onGround() && !golem.isInWater() && distance > 0.25) {
            BlockPos under = BlockPos.containing(golem.getX(), golem.getY() - 0.2, golem.getZ());
            BlockState ground = level.getBlockState(under);
            if (ground.getRenderShape() != RenderShape.INVISIBLE) {
                RandomSource random = level.random;
                level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, ground),
                        golem.getX() + (random.nextFloat() - 0.5) * golem.getBbWidth(),
                        golem.getBoundingBox().minY + 0.1,
                        golem.getZ() + (random.nextFloat() - 0.5) * golem.getBbWidth(),
                        -golem.getDeltaMovement().x * 4.0, 1.5, -golem.getDeltaMovement().z * 4.0);
            }
        }
    }

    /** How far round a roller's wheel has turned. */
    public static float wheel(Golem golem) {
        float[] wheel = WHEELS.get(golem);
        return wheel == null ? 0.0F : wheel[0];
    }

    /** The puffs over its head: following, staying, or coming on a rank. */
    public static void emote(Golem golem, byte event) {
        if (!(golem.level() instanceof ClientLevel level)) {
            return;
        }
        RandomSource random = level.random;
        double x = golem.getX();
        double y = golem.getY() + golem.getBbHeight() + 0.1;
        double z = golem.getZ();
        switch (event) {
            case Golem.FOLLOWS -> add(new GenericParticle(level, x, y, z, 0.0, 0.0, 0.0, LegacySheet.COVER, 16)
                    .lasts(6).opacity(0.5F, 0.5F).cells(176 + (random.nextBoolean() ? 0 : 3), 3, 1)
                    .size(2.0F, 2.0F));
            case Golem.STAYS -> add(new GenericParticle(level, x, y, z, 0.0, 0.01, 0.0, LegacySheet.COVER, 16)
                    .lasts(20).colour(1.0F, 1.0F, 0.1F).opacity(0.5F, 0.5F).cells(14, 1, 1).size(2.0F, 2.0F));
            case Golem.RANKS_UP -> {
                for (int puff = 0; puff < 5; puff++) {
                    float size = 0.3F + random.nextFloat() * 0.4F;
                    add(new GenericParticle(level, x, y - 0.1, z, random.nextGaussian() * 0.01F,
                            random.nextFloat() * 0.02, random.nextGaussian() * 0.01F, LegacySheet.COVER, 16)
                            .lasts(20 + random.nextInt(20)).opacity(0.5F, 0.5F).cells(13, 1, 1).size(size, size));
                }
            }
            case 6 -> add(new GenericParticle(level, x, y, z, 0.0, 0.025, 0.0, LegacySheet.COVER, 16)
                    .lasts(10).colour(0.1F, 1.0F, 1.0F).opacity(0.5F, 0.5F).cells(15, 1, 1).size(2.0F, 2.0F));
            default -> add(new GenericParticle(level, x, y, z, 0.0, 0.05, 0.0, LegacySheet.COVER, 16)
                    .lasts(10).opacity(0.5F, 0.5F).cells(160, 10, 1).size(2.0F, 2.0F));
        }
    }

    /**
     * Steam off the top of a press, so many puffs at once, with a hiss as loud as asked; a hiss of nothing is no
     * hiss at all.
     */
    public static void pressHiss(Level level, BlockPos pos, int puffs, float loudness) {
        if (!(level instanceof ClientLevel client)) {
            return;
        }
        RandomSource random = level.random;
        if (loudness > 0.0F) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.FIRE_EXTINGUISH,
                    SoundSource.BLOCKS, loudness, 1.0F + random.nextFloat() * 0.1F, false);
        }
        for (int puff = 0; puff < puffs; puff++) {
            add(new VentParticle(client, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
                    random.nextGaussian() * 0.1, 0.0, random.nextGaussian() * 0.1, 0xAAAAAA));
        }
    }

    private static void add(net.minecraft.client.particle.Particle particle) {
        Minecraft.getInstance().particleEngine.add(particle);
    }
}
