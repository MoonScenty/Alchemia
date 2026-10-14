package me.moonscenty.alchemia.outer;

import me.moonscenty.alchemia.block.LootBlock;
import me.moonscenty.alchemia.block.taint.TaintFibreBlock;
import me.moonscenty.alchemia.entity.Champions;
import me.moonscenty.alchemia.entity.Taintacle;
import me.moonscenty.alchemia.entity.boss.EldritchGolem;
import me.moonscenty.alchemia.entity.boss.EldritchWarden;
import me.moonscenty.alchemia.entity.boss.GreaterCultistPortal;
import me.moonscenty.alchemia.entity.boss.TaintacleGiant;
import me.moonscenty.alchemia.aura.AuraGeneration;
import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;

/**
 * What wakes in a great hall when its lock is turned.
 * <p>
 * The world keeps count of how many halls have been woken, and what wakes goes round by it -- the Construct, the
 * Warden, the Crimson Cult, the taint -- skipping one now and then. Each is staged as the original staged it: the
 * Warden on a throne at the far corner with an obelisk and urns on either side of the hall and four eldritch
 * pedestals about its middle; the Construct on a throne in the middle between three obelisks, crates and urns heaped
 * in the corners; the cult's greater portal over a ring of sixteen pillars on a ragged floor of doorway stone; and
 * five taintacles, some of them giants, in a hall gone over to taint. Everyone near is told what has come, and then
 * the door melts away, and the lock with it.
 */
public final class BossRooms {
    /** The throne: stairs round a glowing crust, by the original's numbers. */
    private static final int[][] THRONE = {{2, 2, 2}, {0, -1, 1}, {3, 3, 3}};

    private BossRooms() {
    }

    public static void wake(ServerLevel level, BlockPos lock) {
        int cx = lock.getX() >> 4;
        int cz = lock.getZ() >> 4;
        int bx = cx;
        int bz = cz;
        int exit = 0;
        Labyrinth labyrinth = Labyrinth.of(level.getServer());
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                Cell cell = labyrinth.get(cx + dx, cz + dz);
                if (cell == null) {
                    continue;
                }
                if (cell.feature == Cell.BOSS_UPPER_LEFT) {
                    bx = cx + dx;
                    bz = cz + dz;
                }
                if (cell.isBossRoom() && cell.exits() > 0) {
                    exit = cell.feature;
                }
            }
        }
        BlockPos middle = new BlockPos(bx * 16 + 16, MazeBuilder.Y, bz * 16 + 16);
        String which;
        switch (labyrinth.nextBoss(level.random)) {
            case 0 -> {
                golem(level, middle, exit, lock);
                which = "golem";
            }
            case 1 -> {
                warden(level, middle, exit, lock);
                which = "warden";
            }
            case 2 -> {
                cult(level, middle);
                which = "crimson";
            }
            default -> {
                taint(level, middle);
                which = "taint";
            }
        }
        for (Player player : level.players()) {
            if (player.distanceToSqr(lock.getCenter()) < 300.0) {
                player.sendSystemMessage(Component.translatable("boss.alchemia." + which));
            }
        }
        for (BlockPos at : BlockPos.betweenClosed(lock.offset(-2, -2, -2), lock.offset(2, 2, 2))) {
            if (level.getBlockState(at).is(ModBlocks.ELDRITCH_DOOR.get())) {
                level.sendParticles(new net.minecraft.core.particles.DustParticleOptions(
                        new org.joml.Vector3f(0.25F, 0.0F, 0.25F), 1.5F), at.getX() + 0.5, at.getY() + 0.5,
                        at.getZ() + 0.5, 12, 0.3, 0.3, 0.3, 0.0);
                level.removeBlock(at, false);
            }
        }
        level.removeBlock(lock, false);
    }

    /** Where the far side of the hall is from its middle, by which quarter has the door. */
    private static int[] far(int exit) {
        return switch (exit) {
            case Cell.BOSS_UPPER_LEFT -> new int[] {8, 8};
            case Cell.BOSS_UPPER_RIGHT -> new int[] {-8, 8};
            case Cell.BOSS_LOWER_LEFT -> new int[] {8, -8};
            case Cell.BOSS_LOWER_RIGHT -> new int[] {-8, -8};
            default -> new int[] {0, 0};
        };
    }

    private static void throne(ServerLevel level, int x, int y, int z) {
        for (int a = 0; a <= 2; a++) {
            for (int b = 0; b <= 2; b++) {
                int meta = THRONE[a][b];
                BlockState state = meta < 0 ? ModBlocks.GLOWING_CRUSTED_STONE.get().defaultBlockState() : stairs(meta);
                level.setBlock(new BlockPos(x - 1 + b, y, z - 1 + a), state, Block.UPDATE_ALL);
            }
        }
    }

    private static BlockState stairs(int meta) {
        Direction facing = switch (meta & 3) {
            case 0 -> Direction.EAST;
            case 1 -> Direction.WEST;
            case 2 -> Direction.SOUTH;
            default -> Direction.NORTH;
        };
        return ModBlocks.ANCIENT_STONE.stairs().get().defaultBlockState().setValue(StairBlock.FACING, facing)
                .setValue(StairBlock.HALF, (meta & 4) != 0 ? Half.TOP : Half.BOTTOM);
    }

    private static void obelisk(ServerLevel level, BlockPos at) {
        level.setBlock(at, ModBlocks.ELDRITCH_OBELISK.get().defaultBlockState(), Block.UPDATE_ALL);
        for (int up = 1; up <= 4; up++) {
            level.setBlock(at.above(up), ModBlocks.ELDRITCH_OBELISK_BODY.get().defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    private static BlockState loot(boolean crate, int rarity) {
        return (crate ? ModBlocks.LOOT_CRATE : ModBlocks.LOOT_URN).get().defaultBlockState()
                .setValue(LootBlock.RARITY, rarity);
    }

    /** Turns it to face the lock, as the original stood its bosses. */
    private static void faceLock(Mob mob, double x, double y, double z, BlockPos lock) {
        double dx = lock.getX() - x;
        double dy = lock.getY() - (y + mob.getEyeHeight());
        double dz = lock.getZ() - z;
        double across = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Mth.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
        float pitch = (float) -(Mth.atan2(dy, across) * 180.0 / Math.PI);
        mob.moveTo(x, y, z, yaw, pitch);
        mob.setYHeadRot(yaw);
    }

    private static void warden(ServerLevel level, BlockPos middle, int exit, BlockPos lock) {
        RandomSource random = level.random;
        int x = middle.getX();
        int y = middle.getY();
        int z = middle.getZ();
        int[] far = far(exit);
        int ox = x + far[0];
        int oz = z + far[1];
        obelisk(level, new BlockPos(ox, y + 4, z));
        obelisk(level, new BlockPos(x, y + 4, oz));
        level.setBlock(new BlockPos(ox, y + 2, z), ModBlocks.ELDRITCH_CAP.get().defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(new BlockPos(x, y + 2, oz), ModBlocks.ELDRITCH_CAP.get().defaultBlockState(), Block.UPDATE_ALL);
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i != 0 && j != 0 && random.nextFloat() < 0.9F) {
                    float roll = random.nextFloat();
                    level.setBlock(new BlockPos(ox + i, y + 2, z + j),
                            loot(false, roll < 0.1F ? 2 : roll < 0.3F ? 1 : 0), Block.UPDATE_ALL);
                }
                if (i != 0 && j != 0 && random.nextFloat() < 0.9F) {
                    float roll = random.nextFloat();
                    level.setBlock(new BlockPos(x + i, y + 2, oz + j),
                            loot(false, roll < 0.1F ? 2 : roll < 0.3F ? 1 : 0), Block.UPDATE_ALL);
                }
            }
        }
        for (int[] at : new int[][] {{-2, -2}, {-2, 2}, {2, 2}, {2, -2}}) {
            level.setBlock(new BlockPos(x + at[0], y + 2, z + at[1]),
                    ModBlocks.ELDRITCH_PEDESTAL.get().defaultBlockState(), Block.UPDATE_ALL);
        }
        throne(level, ox, y + 2, oz);
        EldritchWarden warden = ModEntities.ELDRITCH_WARDEN.get().create(level);
        if (warden != null) {
            faceLock(warden, ox + 0.5, y + 3, oz + 0.5, lock);
            warden.finalizeSpawn(level, level.getCurrentDifficultyAt(lock), MobSpawnType.EVENT, null);
            warden.restrictTo(new BlockPos(x, y + 2, z), 32);
            level.addFreshEntity(warden);
        }
    }

    private static void golem(ServerLevel level, BlockPos middle, int exit, BlockPos lock) {
        RandomSource random = level.random;
        int x = middle.getX();
        int y = middle.getY();
        int z = middle.getZ();
        int[] far = far(exit);
        int ox = far[0];
        int oz = far[1];
        obelisk(level, new BlockPos(x + ox, y + 4, z + oz));
        obelisk(level, new BlockPos(x - ox, y + 4, z + oz));
        obelisk(level, new BlockPos(x + ox, y + 4, z - oz));
        for (int[] at : new int[][] {{ox, oz}, {-ox, oz}, {ox, -oz}}) {
            level.setBlock(new BlockPos(x + at[0], y + 2, z + at[1]), ModBlocks.ELDRITCH_CAP.get().defaultBlockState(),
                    Block.UPDATE_ALL);
        }
        throne(level, x, y + 2, z);
        for (int a = -10; a <= 10; a++) {
            for (int b = -10; b <= 10; b++) {
                boolean corner = a < -2 && b < -2 || a > 2 && b > 2 || a < -2 && b > 2 || a > 2 && b < -2;
                BlockPos at = new BlockPos(x + a, y + 2, z + b);
                if (corner && random.nextFloat() < 0.15F && level.isEmptyBlock(at)) {
                    float roll = random.nextFloat();
                    level.setBlock(at, loot(random.nextFloat() < 0.3F, roll < 0.05F ? 2 : roll < 0.2F ? 1 : 0),
                            Block.UPDATE_ALL);
                }
            }
        }
        EldritchGolem golem = ModEntities.ELDRITCH_GOLEM.get().create(level);
        if (golem != null) {
            faceLock(golem, x + 0.5, y + 3, z + 0.5, lock);
            golem.finalizeSpawn(level, level.getCurrentDifficultyAt(lock), MobSpawnType.EVENT, null);
            level.addFreshEntity(golem);
        }
    }

    private static void cult(ServerLevel level, BlockPos middle) {
        RandomSource random = level.random;
        int x = middle.getX();
        int y = middle.getY();
        int z = middle.getZ();
        for (int a = -4; a <= 4; a++) {
            for (int b = -4; b <= 4; b++) {
                if ((Math.abs(a) == 2 || Math.abs(b) == 2) && random.nextBoolean()) {
                    continue;
                }
                if ((Math.abs(a) == 3 || Math.abs(b) == 3) && random.nextFloat() > 0.33F) {
                    continue;
                }
                if ((Math.abs(a) == 4 || Math.abs(b) == 4) && random.nextFloat() > 0.25F) {
                    continue;
                }
                level.setBlock(new BlockPos(x + b, y + 1, z + a), ModBlocks.ANCIENT_DOORWAY.get().defaultBlockState(),
                        Block.UPDATE_ALL);
            }
        }
        BlockState stone = ModBlocks.ANCIENT_STONE.block().get().defaultBlockState();
        BlockState glyphed = ModBlocks.GLYPHED_STONE.get().defaultBlockState();
        BlockState low = ModBlocks.ANCIENT_STONE.slab().get().defaultBlockState();
        BlockState high = low.setValue(SlabBlock.TYPE, SlabType.TOP);
        for (int a = 0; a <= 4; a++) {
            for (int b = 0; b <= 4; b++) {
                if (a == 0 || a == 4 || b == 0 || b == 4) {
                    int px = x - 8 + b * 4;
                    int pz = z - 8 + a * 4;
                    level.setBlock(new BlockPos(px, y + 2, pz), stone, Block.UPDATE_ALL);
                    level.setBlock(new BlockPos(px, y + 3, pz), glyphed, Block.UPDATE_ALL);
                    level.setBlock(new BlockPos(px, y + 4, pz), low, Block.UPDATE_ALL);
                    level.setBlock(new BlockPos(px, y + 10, pz), stone, Block.UPDATE_ALL);
                    level.setBlock(new BlockPos(px, y + 9, pz), glyphed, Block.UPDATE_ALL);
                    level.setBlock(new BlockPos(px, y + 8, pz), high, Block.UPDATE_ALL);
                }
            }
        }
        GreaterCultistPortal portal = ModEntities.GREATER_CULTIST_PORTAL.get().create(level);
        if (portal != null) {
            portal.moveTo(x + 0.5, y + 2, z + 0.5, 0.0F, 0.0F);
            level.addFreshEntity(portal);
        }
    }

    private static void taint(ServerLevel level, BlockPos middle) {
        RandomSource random = level.random;
        int x = middle.getX();
        int y = middle.getY();
        int z = middle.getZ();
        AuraHandler.add(level, middle, ModAspects.FLUX, AuraGeneration.BASE * 4);
        for (int a = -12; a <= 12; a++) {
            for (int b = -12; b <= 12; b++) {
                for (int k = 0; k < 9; k++) {
                    BlockPos at = new BlockPos(x + b, y + 2 + k, z + a);
                    if (level.isEmptyBlock(at) && nextToSolid(level, at) && random.nextInt(3) != 0) {
                        level.setBlock(at, TaintFibreBlock.fitted(level, at,
                                ModBlocks.TAINT_FIBRE.get().defaultBlockState()), Block.UPDATE_ALL);
                    }
                }
                if (random.nextFloat() < 0.15F) {
                    level.setBlock(new BlockPos(x + b, y + 2, z + a), ModBlocks.TAINT_SOIL.get().defaultBlockState(),
                            Block.UPDATE_ALL);
                    if (random.nextFloat() < 0.2F) {
                        level.setBlock(new BlockPos(x + b, y + 3, z + a),
                                ModBlocks.TAINT_SOIL.get().defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
                if ((Math.abs(a) == 4 || Math.abs(b) == 4) && random.nextBoolean()) {
                    continue;
                }
                if ((Math.abs(a) >= 5 || Math.abs(b) >= 5) && random.nextFloat() > 0.33F) {
                    continue;
                }
                if ((Math.abs(a) >= 7 || Math.abs(b) >= 7) && random.nextFloat() > 0.25F) {
                    continue;
                }
                level.setBlock(new BlockPos(x + b, y + 1, z + a), ModBlocks.TAINT_CRUST.get().defaultBlockState(),
                        Block.UPDATE_ALL);
            }
        }
        boolean hard = level.getDifficulty() == Difficulty.HARD;
        boolean first = random.nextBoolean();
        boolean third = random.nextBoolean();
        tentacle(level, hard, x + 0.5, y + 3, z + 0.5);
        tentacle(level, first, x + 3.5, y + 3, z + 3.5);
        tentacle(level, !first, x - 2.5, y + 3, z + 3.5);
        tentacle(level, third, x + 3.5, y + 3, z - 2.5);
        tentacle(level, !third, x - 2.5, y + 3, z - 2.5);
    }

    private static void tentacle(ServerLevel level, boolean giant, double x, double y, double z) {
        Monster tentacle = giant ? ModEntities.TAINTACLE_GIANT.get().create(level) : new Taintacle(level);
        if (tentacle == null) {
            return;
        }
        tentacle.moveTo(x, y, z, 0.0F, 0.0F);
        if (tentacle instanceof TaintacleGiant) {
            tentacle.finalizeSpawn(level, level.getCurrentDifficultyAt(tentacle.blockPosition()), MobSpawnType.EVENT,
                    null);
        } else {
            Champions.makeRandom(tentacle);
            tentacle.setPersistenceRequired();
        }
        level.addFreshEntity(tentacle);
    }

    private static boolean nextToSolid(ServerLevel level, BlockPos pos) {
        for (Direction side : Direction.values()) {
            BlockPos next = pos.relative(side);
            if (level.getBlockState(next).isFaceSturdy(level, next, side.getOpposite())) {
                return true;
            }
        }
        return false;
    }

}
