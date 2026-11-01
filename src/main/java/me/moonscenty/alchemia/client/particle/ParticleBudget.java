package me.moonscenty.alchemia.client.particle;

import net.minecraft.client.Minecraft;

public final class ParticleBudget {
    private ParticleBudget() {
    }

    public static int scale(int n) {
        return switch (Minecraft.getInstance().options.particles().get()) {
            case MINIMAL -> n / 2;
            case DECREASED -> n;
            case ALL -> n * 2;
        };
    }
}
