package me.moonscenty.alchemia.block.eldritch;

import me.moonscenty.alchemia.entity.EldritchGuardian;
import me.moonscenty.alchemia.entity.cult.Cultist;
import me.moonscenty.alchemia.entity.cult.CultistCleric;
import me.moonscenty.alchemia.entity.cult.CultistKnight;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * An eldritch altar: the heart of a ring of standing stones, and sometimes of something living there.
 *
 * <h3>What may come of it</h3>
 * A cult altar first calls up four clerics, one at each diagonal two out, who stand about it chanting; then, every
 * two seconds while there are fewer than eight of the cult about, a knight. Kill the clerics -- every one within
 * twenty-four blocks -- and it falls silent for good. A guardian altar keeps a guardian somewhere near, in the dark,
 * and never falls silent.
 *
 * <h3>Its eyes</h3>
 * It takes up to four eldritch eyes. The third makes it a guardian altar whatever it was. With four, and the rite
 * known, a wand opens the way above it.
 */
public class EldritchAltarBlockEntity extends BlockEntity {
    public static final int MOST_EYES = 4;
    private static final int[][] CLERICS = {{-2, -2}, {-2, 2}, {2, -2}, {2, 2}};

    private boolean spawner;
    private boolean open;
    private boolean spawnedClerics;
    /** 0 the cult, 1 a guardian. */
    private byte spawnType;
    private byte eyes;
    private int counter;

    public EldritchAltarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELDRITCH_ALTAR.get(), pos, state);
    }

    public int eyes() {
        return eyes;
    }

    public boolean isOpen() {
        return open;
    }

    public boolean isSpawner() {
        return spawner;
    }

    /** Makes it call up the cult (0) or a guardian (1), or nothing. */
    public void setSpawner(boolean spawner, int type) {
        this.spawner = spawner;
        this.spawnType = (byte) type;
        setChanged();
    }

    /** One more eye in its socket. The third makes it a guardian altar, whatever it was. */
    public void addEye() {
        if (eyes >= 2) {
            setSpawner(true, 1);
        }
        eyes++;
        sync();
    }

    public void setOpen(boolean open) {
        this.open = open;
        sync();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EldritchAltarBlockEntity altar) {
        if (!(level instanceof ServerLevel served) || !altar.spawner) {
            return;
        }
        if (altar.counter++ >= 80 && altar.counter % 40 == 0) {
            if (altar.spawnType == 0) {
                if (!altar.spawnedClerics) {
                    altar.spawnClerics(served);
                } else {
                    altar.spawnGuards(served);
                }
            } else {
                altar.spawnGuardian(served);
            }
        }
    }

    private void spawnClerics(ServerLevel level) {
        int count = 0;
        for (int[] at : CLERICS) {
            CultistCleric cleric = new CultistCleric(level);
            if (place(level, cleric, worldPosition.getX() + 0.5 + at[0], worldPosition.getY(),
                    worldPosition.getZ() + 0.5 + at[1])) {
                cleric.restrictTo(worldPosition, 8);
                cleric.finalizeSpawn(level, level.getCurrentDifficultyAt(worldPosition), MobSpawnType.STRUCTURE, null);
                if (level.addFreshEntity(cleric)) {
                    count++;
                    cleric.attendAltar(worldPosition);
                    cleric.spawnAnim();
                }
            }
        }
        if (count > 2) {
            spawnedClerics = true;
            setChanged();
        }
    }

    /** Knights while the clerics live; silence once they are gone. */
    private void spawnGuards(ServerLevel level) {
        AABB about = new AABB(worldPosition).inflate(24.0, 16.0, 24.0);
        if (level.getEntitiesOfClass(CultistCleric.class, about).isEmpty()) {
            spawner = false;
            setChanged();
            return;
        }
        if (level.getEntitiesOfClass(Cultist.class, about).size() >= 8) {
            return;
        }
        CultistKnight knight = new CultistKnight(level);
        BlockPos spot = aside(level);
        if (place(level, knight, spot.getX(), spot.getY(), spot.getZ())) {
            knight.finalizeSpawn(level, level.getCurrentDifficultyAt(worldPosition), MobSpawnType.STRUCTURE, null);
            knight.restrictTo(worldPosition, 16);
            level.addFreshEntity(knight);
            knight.spawnAnim();
        }
    }

    private void spawnGuardian(ServerLevel level) {
        EldritchGuardian guardian = new EldritchGuardian(level);
        BlockPos spot = aside(level);
        if (place(level, guardian, spot.getX(), spot.getY(), spot.getZ())
                && EldritchGuardian.checkSpawnRules(me.moonscenty.alchemia.registry.ModEntities.ELDRITCH_GUARDIAN
                        .get(), level, MobSpawnType.STRUCTURE, guardian.blockPosition(), level.random)) {
            guardian.finalizeSpawn(level, level.getCurrentDifficultyAt(worldPosition), MobSpawnType.STRUCTURE, null);
            guardian.restrictTo(worldPosition, 16);
            level.addFreshEntity(guardian);
            guardian.spawnAnim();
        }
    }

    /** Four to ten blocks off along each way or none, up to three up or down. */
    private BlockPos aside(ServerLevel level) {
        return worldPosition.offset(Mth.randomBetweenInclusive(level.random, 4, 10)
                        * Mth.randomBetweenInclusive(level.random, -1, 1),
                Mth.randomBetweenInclusive(level.random, 0, 3) * Mth.randomBetweenInclusive(level.random, -1, 1),
                Mth.randomBetweenInclusive(level.random, 4, 10) * Mth.randomBetweenInclusive(level.random, -1, 1));
    }

    /** Puts it there if it stands on something and has room. */
    private static boolean place(ServerLevel level, Mob mob, double x, double y, double z) {
        BlockPos under = BlockPos.containing(x, y - 1, z);
        if (!level.getBlockState(under).isFaceSturdy(level, under, Direction.UP)) {
            return false;
        }
        mob.moveTo(x, y, z, level.random.nextFloat() * 360.0F, 0.0F);
        return level.noCollision(mob) && level.isUnobstructed(mob) && !level.containsAnyLiquid(mob.getBoundingBox());
    }

    // --- saving and telling -------------------------------------------------

    private void sync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putByte("eyes", eyes);
        tag.putBoolean("open", open);
        tag.putBoolean("spawnedClerics", spawnedClerics);
        tag.putBoolean("spawner", spawner);
        tag.putByte("spawntype", spawnType);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        eyes = tag.getByte("eyes");
        open = tag.getBoolean("open");
        spawnedClerics = tag.getBoolean("spawnedClerics");
        spawner = tag.getBoolean("spawner");
        spawnType = tag.getByte("spawntype");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putByte("eyes", eyes);
        tag.putBoolean("open", open);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
