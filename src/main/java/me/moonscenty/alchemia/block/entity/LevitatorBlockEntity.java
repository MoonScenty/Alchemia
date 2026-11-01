package me.moonscenty.alchemia.block.entity;

import java.util.List;

import org.joml.Vector3f;

import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.block.LevitatorBlock;
import me.moonscenty.alchemia.particle.SparkleOptions;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class LevitatorBlockEntity extends SyncedBlockEntity {
    // original's values; PER_POINT is hundredths of upkeep per point of aer
    private static final int[] RANGES = {4, 8, 16, 32};
    private static final int PER_POINT = 1200;
    private static final double PUSH = 0.1;
    private static final double MOST = 0.35;
    private static final double HOLD_UP = 0.08;
    private static final double SLOWED = 0.9;

    private int range = 1;
    private int reach;
    private int feeling;
    private int vis;

    public LevitatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LEVITATOR.get(), pos, state);
    }

    // ticks on both sides: each client moves its own player
    public static void tick(Level level, BlockPos pos, BlockState state, LevitatorBlockEntity levitator) {
        Direction facing = state.getValue(LevitatorBlock.FACING);
        levitator.feel(level, facing);
        if (!level.isClientSide && levitator.vis < 10) {
            int drawn = AuraHandler.drainAvailable(level, pos, ModAspects.AIR, 1) * PER_POINT;
            if (drawn > 0) {
                levitator.given(drawn);
            }
        }
        if (levitator.reach > 0 && levitator.vis > 0 && state.getValue(LevitatorBlock.ENABLED)) {
            levitator.lift(level, facing);
        }
    }

    private void feel(Level level, Direction facing) {
        int most = RANGES[range];
        if (reach > most) {
            reach = 0;
        }
        int along = 1 + feeling % most;
        BlockPos at = worldPosition.relative(facing, along);
        if (level.getBlockState(at).isSolidRender(level, at)) {
            if (along < reach) {
                reach = along;
            }
            feeling = 0;
        } else {
            if (along > reach) {
                reach = along;
            }
            feeling++;
        }
    }

    private void lift(Level level, Direction facing) {
        AABB within = new AABB(worldPosition).expandTowards(
                facing.getStepX() * reach, facing.getStepY() * reach, facing.getStepZ() * reach);
        List<Entity> found = level.getEntitiesOfClass(Entity.class, within,
                entity -> entity instanceof ItemEntity || entity.isPushable());
        int before = vis;
        for (Entity entity : found) {
            if (level.isClientSide) {
                if (entity.isControlledByLocalInstance()) {
                    push(entity, facing);
                }
                continue;
            }
            if (!(entity instanceof Player) && !(entity.getControllingPassenger() instanceof Player)) {
                push(entity, facing);
                entity.hasImpulse = true;
            }
            entity.resetFallDistance();
            vis -= cost();
            if (vis <= 0) {
                vis = 0;
                break;
            }
        }
        if (!level.isClientSide) {
            if (before > 0 && vis == 0) {
                changed();
            } else if (vis != before) {
                setChanged();
            }
        } else if (level.random.nextFloat() < 0.2F) {
            drift(level, facing);
        }
    }

    private static void push(Entity entity, Direction facing) {
        Vec3 moving = entity.getDeltaMovement();
        if (entity.isShiftKeyDown() && facing == Direction.UP) {
            if (moving.y < 0) {
                entity.setDeltaMovement(moving.x, moving.y * SLOWED, moving.z);
            }
            return;
        }
        double x = moving.x + PUSH * facing.getStepX();
        double y = moving.y + PUSH * facing.getStepY();
        double z = moving.z + PUSH * facing.getStepZ();
        if (facing.getAxis() != Direction.Axis.Y && !entity.onGround()) {
            if (y < 0) {
                y *= SLOWED;
            }
            y += HOLD_UP;
        }
        entity.setDeltaMovement(clamp(x), clamp(y), clamp(z));
    }

    private static double clamp(double speed) {
        return Math.max(-MOST, Math.min(MOST, speed));
    }

    private void drift(Level level, Direction facing) {
        BlockPos front = worldPosition.relative(facing);
        double x = front.getX() + (facing.getStepX() != 0 ? 0.5 - facing.getStepX() * 0.5 : 0.25 + level.random.nextFloat() * 0.5);
        double y = front.getY() + (facing.getStepY() != 0 ? 0.5 - facing.getStepY() * 0.5 : 0.25 + level.random.nextFloat() * 0.5);
        double z = front.getZ() + (facing.getStepZ() != 0 ? 0.5 - facing.getStepZ() * 0.5 : 0.25 + level.random.nextFloat() * 0.5);
        Vector3f to = new Vector3f((float) (x + facing.getStepX() * 2), (float) (y + facing.getStepY() * 2),
                (float) (z + facing.getStepZ() * 2));
        level.addParticle(new SparkleOptions(ModAspects.AIR.value().color(), to), x, y, z, 0, 0, 0);
    }

    public void stepRange() {
        range = (range + 1) % RANGES.length;
        reach = 0;
        changed();
    }

    public int range() {
        return RANGES[range];
    }

    public int cost() {
        return RANGES[range] * 2;
    }

    public int reach() {
        return reach;
    }

    public int vis() {
        return vis;
    }

    public void given(int hundredths) {
        boolean was = vis > 0;
        vis += hundredths;
        if (was) {
            setChanged();
        } else {
            changed();
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        range = Math.floorMod(tag.getByte("range"), RANGES.length);
        vis = tag.getInt("vis");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putByte("range", (byte) range);
        tag.putInt("vis", vis);
    }
}
