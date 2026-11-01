package me.moonscenty.alchemia.client;

import me.moonscenty.alchemia.client.particle.BubbleParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public final class SoapEffects {
    private SoapEffects() {
    }

    public static void lather(Level world, LivingEntity user, int count, double spread, int colour) {
        if (!(world instanceof ClientLevel level)) {
            return;
        }
        AABB box = user.getBoundingBox();
        for (int i = 0; i < count; i++) {
            double x = user.getX() + (level.random.nextFloat() - 0.5) * spread;
            double y = box.minY + level.random.nextFloat() * (box.maxY - box.minY);
            double z = user.getZ() + (level.random.nextFloat() - 0.5) * spread;
            Minecraft.getInstance().particleEngine.add(new BubbleParticle(level, x, y, z, colour, 1, 0.002,
                    BubbleParticle.Kind.BUBBLE));
        }
    }
}
