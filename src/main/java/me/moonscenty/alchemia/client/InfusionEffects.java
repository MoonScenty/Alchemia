package me.moonscenty.alchemia.client;

import me.moonscenty.alchemia.block.entity.ArcanePedestalBlockEntity;
import me.moonscenty.alchemia.block.entity.InfusionMatrixBlockEntity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

public final class InfusionEffects {
    private InfusionEffects() {
    }

    public static void stand(Level level, BlockPos stand, int id, int data) {
        if (!(level instanceof ClientLevel client)) {
            return;
        }
        double x = stand.getX() + 0.5;
        double y = stand.getY() + 1.5;
        double z = stand.getZ() + 0.5;
        if (id == ArcanePedestalBlockEntity.BURST) {
            CrucibleEffects.bamf(client, x, y, z, 0.75F, 0.0F, 0.5F, true, false);
            BlockPos matrix = stand.offset(ArcanePedestalBlockEntity.unpack(data));
            RandomSource random = client.random;
            EldritchEffects.arc(client, matrix.getX() + 0.5, matrix.getY() + 0.5, matrix.getZ() + 0.5, x, y, z,
                    0.3F - random.nextFloat() * 0.1F, 0.0F, 0.3F - random.nextFloat() * 0.1F);
        } else if (id == ArcanePedestalBlockEntity.DONE) {
            CrucibleEffects.bamf(client, x, y, z, 0.5F, 0.1F, 0.6F, true, false);
        } else if (id == ArcanePedestalBlockEntity.POLLUTED) {
            for (int i = 0; i < Math.min(me.moonscenty.alchemia.client.particle.ParticleBudget.scale(20), data); i++) {
                TaintEffects.pollution(client, stand);
            }
        }
    }

    public static void formed(Level level, BlockPos matrix) {
        if (!(level instanceof ClientLevel client)) {
            return;
        }
        CrucibleEffects.bamf(client, matrix.getX() + 0.5, matrix.getY() + 0.5, matrix.getZ() + 0.5,
                0.5F, 0.1F, 0.6F, true, false);
        for (Vec3i corner : InfusionMatrixBlockEntity.CORNERS) {
            for (int up = 0; up <= 1; up++) {
                BlockPos at = matrix.offset(corner).above(up);
                CrucibleEffects.bamf(client, at.getX() + 0.5, at.getY() + 0.5, at.getZ() + 0.5,
                        0.5F, 0.1F, 0.6F, true, false);
            }
        }
    }

    public static void crackle(Level level, BlockPos matrix, int instability) {
        RandomSource random = level.random;
        if (!(level instanceof ClientLevel client) || instability <= 0 || random.nextInt(200) > instability) {
            return;
        }
        double x = matrix.getX() + 0.5 + (random.nextFloat() - random.nextFloat()) * 4.0F;
        double z = matrix.getZ() + 0.5 + (random.nextFloat() - random.nextFloat()) * 4.0F;
        int y = matrix.getY() - 2;
        while (!client.isEmptyBlock(BlockPos.containing(x, y, z)) && y < matrix.getY() + 8) {
            y++;
        }
        EldritchEffects.arc(client, matrix.getX() + 0.5, matrix.getY() + 0.5, matrix.getZ() + 0.5, x, y, z,
                0.8F, 0.1F, 1.0F);
    }
}
