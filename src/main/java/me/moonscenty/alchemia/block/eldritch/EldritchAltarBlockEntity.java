package me.moonscenty.alchemia.block.eldritch;

import me.moonscenty.alchemia.block.entity.SyncedBlockEntity;
import me.moonscenty.alchemia.entity.EldritchGuardian;
import me.moonscenty.alchemia.entity.cult.Cultist;
import me.moonscenty.alchemia.entity.cult.CultistCleric;
import me.moonscenty.alchemia.entity.cult.CultistKnight;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class EldritchAltarBlockEntity extends SyncedBlockEntity {
    public static final int MOST_EYES = 4;
    private static final int[][] CLERICS = {{-2, -2}, {-2, 2}, {2, -2}, {2, 2}};

    private boolean spawner;
    private boolean open;
    private boolean spawnedClerics;
    // 0 = cult, 1 = guardian
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

    public void setSpawner(boolean spawner, int type) {
        this.spawner = spawner;
        this.spawnType = (byte) type;
        setChanged();
    }

    public void addEye() {
        if (eyes >= 2) {
            setSpawner(true, 1);
        }
        eyes++;
        changed();
    }

    public void setOpen(boolean open) {
        this.open = open;
        changed();
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

    private BlockPos aside(ServerLevel level) {
        return worldPosition.offset(Mth.randomBetweenInclusive(level.random, 4, 10)
                        * Mth.randomBetweenInclusive(level.random, -1, 1),
                Mth.randomBetweenInclusive(level.random, 0, 3) * Mth.randomBetweenInclusive(level.random, -1, 1),
                Mth.randomBetweenInclusive(level.random, 4, 10) * Mth.randomBetweenInclusive(level.random, -1, 1));
    }

    private static boolean place(ServerLevel level, Mob mob, double x, double y, double z) {
        BlockPos under = BlockPos.containing(x, y - 1, z);
        if (!level.getBlockState(under).isFaceSturdy(level, under, Direction.UP)) {
            return false;
        }
        mob.moveTo(x, y, z, level.random.nextFloat() * 360.0F, 0.0F);
        return level.noCollision(mob) && level.isUnobstructed(mob) && !level.containsAnyLiquid(mob.getBoundingBox());
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
        CompoundTag tag = new CompoundTag();
        tag.putByte("eyes", eyes);
        tag.putBoolean("open", open);
        return tag;
    }
}
