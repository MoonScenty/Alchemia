package me.moonscenty.alchemia.client.particle;

import java.util.ArrayDeque;
import java.util.Deque;

import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class SwarmParticle extends LegacyParticle {
    private static final int BUZZING = 3;
    private static final long BUZZ_NANOS = 1_500_000L;
    private static final Deque<Long> BUZZES = new ArrayDeque<>();

    private final Entity swarm;
    private final float speed;
    private final float turns;
    private final float lifts;
    private final float scale;
    private final float red;
    private final float green;
    private final float blue;
    private float yaw;
    private float pitch;
    private int dying;

    public SwarmParticle(ClientLevel level, double x, double y, double z, Entity swarm, float red, float green,
            float blue, float speed, float turns, float lifts) {
        super(level, x, y, z, LegacySheet.COVER);
        this.swarm = swarm;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.speed = speed;
        this.turns = turns;
        this.lifts = lifts;
        this.scale = random.nextFloat() * 0.5F + 1.0F;
        this.xd = (random.nextFloat() - random.nextFloat()) * 0.2F;
        this.yd = (random.nextFloat() - random.nextFloat()) * 0.2F;
        this.zd = (random.nextFloat() - random.nextFloat()) * 0.2F;
        this.hasPhysics = true;
        this.lifetime = Integer.MAX_VALUE;
        setColor(red, green, blue);
    }

    @Override
    public void tick() {
        xo = x;
        yo = y;
        zo = z;
        age++;
        boolean gone = !swarm.isAlive() || swarm instanceof LivingEntity living && living.deathTime > 0;
        if (gone) {
            dying++;
            xd *= 0.9;
            zd *= 0.9;
            yd -= lifts / 2.0F;
            if (dying > 50) {
                remove();
                return;
            }
        } else {
            yd += lifts;
        }
        move(xd, yd, zd);
        xd *= 0.985;
        yd *= 0.985;
        zd *= 0.985;
        if (!gone) {
            boolean hurt = swarm instanceof LivingEntity living && living.hurtTime > 0;
            int turn = (int) (turns / 2.0F);
            float yawTurn = turns / 2.0F + random.nextInt(turn);
            float pitchTurn = turns / 2.0F + random.nextInt(turn);
            boolean far = distanceToSqr(swarm) > swarm.getBbWidth();
            face(far && !hurt ? yawTurn : -yawTurn, far && !hurt ? pitchTurn : -pitchTurn);
            double hx = -Mth.sin(yaw * Mth.DEG_TO_RAD) * Mth.cos(pitch * Mth.DEG_TO_RAD);
            double hz = Mth.cos(yaw * Mth.DEG_TO_RAD) * Mth.cos(pitch * Mth.DEG_TO_RAD);
            double hy = -Mth.sin(pitch * Mth.DEG_TO_RAD);
            head(hx, hy, hz);
            float dim = hurt ? 2.0F : 1.0F;
            setColor(red, green / dim, blue / dim);
        }
        buzz();
        setAlpha((50.0F - dying) / 50.0F);
        int cell = 7 + age % 8;
        cell(cell, 4);
    }

    private double distanceToSqr(Entity entity) {
        double dx = entity.getX() - x;
        double dy = entity.getY() - y;
        double dz = entity.getZ() - z;
        return dx * dx + dy * dy + dz * dz;
    }

    private void face(float yawStep, float pitchStep) {
        double dx = swarm.getX() - x;
        double dz = swarm.getZ() - z;
        double dy = (swarm.getBoundingBox().minY + swarm.getBoundingBox().maxY) / 2.0 - y;
        double across = Math.sqrt(dx * dx + dz * dz);
        float wantYaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
        float wantPitch = (float) -(Mth.atan2(dy, across) * Mth.RAD_TO_DEG);
        pitch = turn(pitch, wantPitch, pitchStep);
        yaw = turn(yaw, wantYaw, yawStep);
    }

    // original's turn; a negative limit keeps turning one way, which makes hurt swarms circle
    private static float turn(float from, float to, float most) {
        float by = Mth.wrapDegrees(to - from);
        if (by > most) {
            by = most;
        }
        if (by < -most) {
            by = -most;
        }
        return from + by;
    }

    private void head(double hx, double hy, double hz) {
        double length = Math.sqrt(hx * hx + hy * hy + hz * hz);
        float wobble = 15.0F;
        hx = hx / length + random.nextGaussian() * (random.nextBoolean() ? -1 : 1) * 0.0075 * wobble;
        hy = hy / length + random.nextGaussian() * (random.nextBoolean() ? -1 : 1) * 0.0075 * wobble;
        hz = hz / length + random.nextGaussian() * (random.nextBoolean() ? -1 : 1) * 0.0075 * wobble;
        xd = hx * speed;
        yd = hy * speed;
        zd = hz * speed;
    }

    private void buzz() {
        long now = System.nanoTime();
        while (!BUZZES.isEmpty() && BUZZES.peekFirst() < now) {
            BUZZES.pollFirst();
        }
        var player = Minecraft.getInstance().player;
        if (BUZZES.size() < BUZZING && random.nextInt(50) == 0 && player != null
                && player.distanceToSqr(x, y, z) < 64.0) {
            level.playLocalSound(x, y, z, ModSounds.FLY.get(), SoundSource.HOSTILE, 0.03F,
                    0.5F + random.nextFloat() * 0.4F, false);
            BUZZES.addLast(now + BUZZ_NANOS);
        }
    }

    @Override
    public float getQuadSize(float partial) {
        float bob = Mth.sin(age / 3.0F) * 0.25F + 1.0F;
        return 0.1F * scale * bob;
    }

    @Override
    protected int getLightColor(float partial) {
        return FULL_BRIGHT;
    }
}
