package me.moonscenty.alchemia.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import me.moonscenty.alchemia.client.particle.CrumbParticle;
import me.moonscenty.alchemia.client.particle.GenericParticle;
import me.moonscenty.alchemia.client.particle.LegacySheet;
import me.moonscenty.alchemia.client.particle.SwarmParticle;
import me.moonscenty.alchemia.entity.TaintSwarm;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** What the creatures of the taint leave in the air: their own particles, as the original drew them. */
public final class TaintEffects {
    /** How many flies make up a swarm. */
    private static final int FLIES = 25;
    private static final Map<TaintSwarm, List<SwarmParticle>> SWARMS = new WeakHashMap<>();

    private TaintEffects() {
    }

    /** A slow purple wisp of pollution rising off where a crawler is. */
    public static void pollution(Entity crawler, BlockPos at) {
        if (!(crawler.level() instanceof ClientLevel level)) {
            return;
        }
        RandomSource random = crawler.getRandom();
        Minecraft.getInstance().particleEngine.add(new GenericParticle(level,
                at.getX() + 0.2F + random.nextFloat() * 0.6F, at.getY() + 0.2F + random.nextFloat() * 0.6F,
                at.getZ() + 0.2F + random.nextFloat() * 0.6F, (random.nextFloat() - random.nextFloat()) * 0.005,
                0.02, (random.nextFloat() - random.nextFloat()) * 0.005, LegacySheet.COVER_2, 8)
                .lasts(400 + random.nextInt(100))
                .colour(1.0F, 0.3F, 0.9F)
                .opacity(0.5F, 0.0F)
                .cells(24, 1, 1)
                .size(2.0F, 5.0F)
                .slows(1.0)
                .spins(random.nextFloat() * 360.0F, random.nextBoolean() ? -1.0F : 1.0F));
    }

    /** Keeps a swarm in flies: one more each tick until it has its full count, each following it about. */
    public static void swarm(TaintSwarm swarm) {
        if (!(swarm.level() instanceof ClientLevel level)) {
            return;
        }
        List<SwarmParticle> flies = SWARMS.computeIfAbsent(swarm, key -> new ArrayList<>());
        flies.removeIf(fly -> !fly.isAlive());
        if (flies.size() < FLIES) {
            RandomSource random = swarm.getRandom();
            SwarmParticle fly = new SwarmParticle(level,
                    swarm.getX() + (random.nextFloat() - random.nextFloat()) * 2.0F,
                    swarm.getY() + (random.nextFloat() - random.nextFloat()) * 2.0F,
                    swarm.getZ() + (random.nextFloat() - random.nextFloat()) * 2.0F, swarm,
                    0.8F + random.nextFloat() * 0.2F, random.nextFloat() * 0.4F, 1.0F - random.nextFloat() * 0.2F,
                    0.22F, 15.0F, 0.08F);
            Minecraft.getInstance().particleEngine.add(fly);
            flies.add(fly);
        }
    }

    /** Slime and broken ground thrown up in a ring as a tentacle rises out of the earth. */
    public static void arise(Entity tentacle) {
        if (!(tentacle.level() instanceof ClientLevel level)) {
            return;
        }
        RandomSource random = tentacle.getRandom();
        float height = tentacle.getBbHeight();
        BlockPos under = tentacle.blockPosition().below();
        var ground = level.getBlockState(under);
        for (int bit = 0; bit < 2.0F * height; bit++) {
            float angle = random.nextFloat() * Mth.PI * height;
            float out = random.nextFloat() * 0.5F + 0.5F;
            Minecraft.getInstance().particleEngine.add(new CrumbParticle(level,
                    tentacle.getX() + Mth.sin(angle) * height * 0.25F * out, tentacle.getY(),
                    tentacle.getZ() + Mth.cos(angle) * height * 0.25F * out, new ItemStack(Items.SLIME_BALL),
                    0.4F, 0.0F, 0.4F, 0.5F, (int) (66.0F / (random.nextFloat() * 0.9F + 0.1F))));
            if (!ground.isAir()) {
                angle = random.nextFloat() * Mth.PI * height;
                out = random.nextFloat() * 0.5F + 0.5F;
                level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, ground),
                        tentacle.getX() + Mth.sin(angle) * height * 0.25F * out, tentacle.getY(),
                        tentacle.getZ() + Mth.cos(angle) * height * 0.25F * out, 0.0, 0.0, 0.0);
            }
        }
    }
}
