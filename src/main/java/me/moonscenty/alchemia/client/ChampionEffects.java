package me.moonscenty.alchemia.client;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.client.particle.GenericParticle;
import me.moonscenty.alchemia.client.particle.LegacySheet;
import me.moonscenty.alchemia.entity.ChampionModifier;
import me.moonscenty.alchemia.entity.Champions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.monster.Monster;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * The sparks about a champion, in its modifier's colour, from the original's particle sheet.
 * <p>
 * The original drew each of the thirteen its own way; these share one way of drawing and differ in colour, how
 * often, and whether they rise or sink. An infested one drips slime, as the original's did.
 */
@EventBusSubscriber(modid = Alchemia.MODID, value = Dist.CLIENT)
public final class ChampionEffects {
    private ChampionEffects() {
    }

    @SubscribeEvent
    public static void ticked(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Monster mob) || !(mob.level() instanceof ClientLevel level)
                || mob.isInvisible()) {
            return;
        }
        ChampionModifier modifier = Champions.modifier(mob);
        if (modifier == null) {
            return;
        }
        RandomSource random = mob.getRandom();
        double x = mob.getRandomX(0.5);
        double y = mob.getY() + random.nextDouble() * mob.getBbHeight();
        double z = mob.getRandomZ(0.5);
        if (modifier == ChampionModifier.INFESTED) {
            if (random.nextInt(4) == 0) {
                level.addParticle(ParticleTypes.ITEM_SLIME, x, y, z, 0.0, 0.0, 0.0);
            }
            return;
        }
        if (random.nextFloat() > chance(modifier)) {
            return;
        }
        float r = (modifier.colour() >> 16 & 0xFF) / 255.0F;
        float g = (modifier.colour() >> 8 & 0xFF) / 255.0F;
        float b = (modifier.colour() & 0xFF) / 255.0F;
        float shade = 0.85F + random.nextFloat() * 0.15F;
        float size = 0.6F + random.nextFloat() * 0.4F;
        Minecraft.getInstance().particleEngine.add(new GenericParticle(level, x, y, z, 0.0, drift(modifier), 0.0,
                modifier == ChampionModifier.GRIM ? LegacySheet.COVER : LegacySheet.GLOW, 16)
                .lasts(4 + random.nextInt(4))
                .colour(r * shade, g * shade, b * shade)
                .opacity(0.7F, 0.0F)
                .cells(176 + random.nextInt(4) * 3, 3, 1)
                .size(size, size));
    }

    private static float chance(ChampionModifier modifier) {
        return switch (modifier) {
            case ARMORED -> 0.25F;
            case MIGHTY -> 0.3F;
            default -> 0.5F;
        };
    }

    private static double drift(ChampionModifier modifier) {
        return switch (modifier) {
            case GRIM, SICKLY -> -0.02;
            case UNDYING, FIERY, VENOMOUS -> 0.03;
            default -> 0.0;
        };
    }
}
