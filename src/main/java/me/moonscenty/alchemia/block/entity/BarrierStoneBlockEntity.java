package me.moonscenty.alchemia.block.entity;

import me.moonscenty.alchemia.block.BarrierStoneBlock;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import me.moonscenty.alchemia.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class BarrierStoneBlockEntity extends BlockEntity {
    public static final int SHOVES = 5;
    public static final int RAISES = 100;
    private static final float SHOVE = 0.2F;

    private int count;

    public BarrierStoneBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BARRIER_STONE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BarrierStoneBlockEntity stone) {
        if (stone.count == 0) {
            stone.count = level.random.nextInt(RAISES);
        }
        if (stone.count % SHOVES == 0 && !state.getValue(BarrierStoneBlock.POWERED)) {
            shove(level, pos);
        }
        if (++stone.count % RAISES == 0) {
            raise(level, pos);
        }
    }

    private static void shove(Level level, BlockPos pos) {
        AABB over = new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 3, pos.getZ() + 1)
                .inflate(0.1);
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, over,
                living -> !living.onGround() && !(living instanceof Player))) {
            float back = (living.getYRot() + 180.0F) * Mth.DEG_TO_RAD;
            living.push(-Mth.sin(back) * SHOVE, -0.1, Mth.cos(back) * SHOVE);
            living.hurtMarked = true;
        }
    }

    public static void raise(Level level, BlockPos pos) {
        BlockState ward = ModBlocks.WARDING_AURA.get().defaultBlockState();
        for (int up = 1; up <= 2; up++) {
            BlockPos at = pos.above(up);
            if (level.isEmptyBlock(at)) {
                level.setBlock(at, ward, Block.UPDATE_ALL);
            }
        }
    }
}
