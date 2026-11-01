package me.moonscenty.alchemia.client.particle;

import net.minecraft.client.Minecraft;

/** The original's measure of how many particles are wanted. */
public final class ParticleBudget {
    private ParticleBudget() {
    }

    /** Half of {@code n} at the least particle setting, {@code n} at decreased, double at all. */
    public static int scale(int n) {
        return switch (Minecraft.getInstance().options.particles().get()) {
            case MINIMAL -> n / 2;
            case DECREASED -> n;
            case ALL -> n * 2;
        };
    }
}
