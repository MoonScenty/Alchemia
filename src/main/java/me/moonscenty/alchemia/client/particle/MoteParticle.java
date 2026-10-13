package me.moonscenty.alchemia.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;

/**
 * A mote of light rising off the magical forest's grass, as the original's {@code FXWispyOrb}: a random pale colour,
 * drifting and bobbing upward, twinkling through a row of the sheet and fading out over its life. It glows.
 */
public class MoteParticle extends LegacyParticle {
    private static final int ROW = 8;
    private static final double RISE = 0.002;

    private final float fade;
    private int cell;

    public MoteParticle(ClientLevel level, double x, double y, double z, int age) {
        super(level, x, y, z, LegacySheet.GLOW);
        rCol = 0.25F + random.nextFloat() * 0.75F;
        gCol = 0.25F + random.nextFloat() * 0.75F;
        bCol = 0.25F + random.nextFloat() * 0.75F;
        hasPhysics = false;
        gravity = 0.0F;
        quadSize = 0.1F * (random.nextFloat() * 0.5F + 0.5F) * 2.0F * (random.nextFloat() * 0.5F + 0.3F);
        xd = (random.nextFloat() * 2.0F - 1.0F) * 0.02F;
        yd = random.nextFloat() * 0.02F;
        zd = (random.nextFloat() * 2.0F - 1.0F) * 0.02F;
        lifetime = (int) (age + age / 2 * random.nextFloat());
        alpha = 0.5F;
        fade = alpha / lifetime;
        cell = random.nextInt(16);
        cell(cell, ROW);
    }

    @Override
    public void tick() {
        xo = x;
        yo = y;
        zo = z;
        yd += RISE;
        xd += (random.nextFloat() - random.nextFloat()) * 0.01F;
        zd += (random.nextFloat() - random.nextFloat()) * 0.01F;
        x += xd;
        y += yd;
        z += zd;
        xd *= 0.85F;
        yd *= 0.85F;
        zd *= 0.85F;
        if (age++ >= lifetime) {
            remove();
            return;
        }
        alpha = Math.max(0.0F, alpha - fade);
        cell = (cell + 1) % 16;
        cell(cell, ROW);
    }

    @Override
    protected int getLightColor(float partialTick) {
        return FULL_BRIGHT;
    }
}
