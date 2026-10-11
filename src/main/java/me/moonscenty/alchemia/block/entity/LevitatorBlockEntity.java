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
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * A levitator at work: finding how far it can reach, and pushing what is in that reach.
 *
 * <p>It feels along the way it faces a block a tick, and stops short at the first solid block, so that what it
 * reaches settles over a few ticks after anything is built or broken in front of it. What it pushes it pushes a
 * tenth of a block a tick faster, to no more than a third of a block a tick; pushing sideways it holds things up
 * as well, and facing up it lets down slowly anyone crouching. Nothing it holds takes a fall.
 *
 * <p>Every thing it pushes for a tick costs it twice its range in hundredths of a point of aer, a point of aer
 * buying twelve hundred of them. It draws a point from the aura whenever it is down to its last ten.
 *
 * <p>Both sides run it: the server pushes what the server moves, and each player's own game pushes that player,
 * since a player's game is the one that says where the player goes.
 */
public class LevitatorBlockEntity extends BlockEntity {
    /** The ranges the toggle steps through, and how many hundredths a point of aer buys. The original's. */
    private static final int[] RANGES = {4, 8, 16, 32};
    private static final int PER_POINT = 1200;
    /** How hard it pushes, the most it lets anything go, and how it holds up what it pushes sideways. */
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

    /** One block further along, or back to the first when it meets something solid. */
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

    /** A mote of aer drifting off its face, the way it pushes. */
    private void drift(Level level, Direction facing) {
        BlockPos front = worldPosition.relative(facing);
        double x = front.getX() + (facing.getStepX() != 0 ? 0.5 - facing.getStepX() * 0.5 : 0.25 + level.random.nextFloat() * 0.5);
        double y = front.getY() + (facing.getStepY() != 0 ? 0.5 - facing.getStepY() * 0.5 : 0.25 + level.random.nextFloat() * 0.5);
        double z = front.getZ() + (facing.getStepZ() != 0 ? 0.5 - facing.getStepZ() * 0.5 : 0.25 + level.random.nextFloat() * 0.5);
        Vector3f to = new Vector3f((float) (x + facing.getStepX() * 2), (float) (y + facing.getStepY() * 2),
                (float) (z + facing.getStepZ() * 2));
        level.addParticle(new SparkleOptions(ModAspects.AIR.value().color(), to), x, y, z, 0, 0, 0);
    }

    /** The next range along, starting again from the shortest after the longest. */
    public void stepRange() {
        range = (range + 1) % RANGES.length;
        reach = 0;
        changed();
    }

    /** How far it reaches at most, as set. */
    public int range() {
        return RANGES[range];
    }

    /** What lifting one thing for a tick costs, in hundredths of a point of aer; also points of aer a minute. */
    public int cost() {
        return RANGES[range] * 2;
    }

    /** How far it reaches now, short of anything solid. */
    public int reach() {
        return reach;
    }

    public int vis() {
        return vis;
    }

    /** Hundredths of a point of aer, as though drawn from the air. */
    public void given(int hundredths) {
        boolean was = vis > 0;
        vis += hundredths;
        if (was) {
            setChanged();
        } else {
            changed();
        }
    }

    /** Saved, and told to anyone watching, since whether it has anything to push with is theirs to know too. */
    private void changed() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
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

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
