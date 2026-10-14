package me.moonscenty.alchemia.client;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;

/**
 * How the Outer Lands look: no sky, no clouds to speak of, and a thick fog everywhere of the original's colour --
 * a dark purple, the same at every hour, because there are no hours there.
 */
public class OuterLandsEffects extends DimensionSpecialEffects {
    /** The original's 0xA080A0, at fifteen parts in a hundred. */
    private static final Vec3 FOG = new Vec3(160 / 255.0 * 0.15, 128 / 255.0 * 0.15, 160 / 255.0 * 0.15);

    public OuterLandsEffects() {
        super(1.0F, false, SkyType.NONE, false, false);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 colour, float brightness) {
        return FOG;
    }

    @Override
    public boolean isFoggyAt(int x, int z) {
        return true;
    }
}
