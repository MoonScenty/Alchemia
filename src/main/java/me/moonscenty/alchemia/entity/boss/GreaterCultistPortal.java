package me.moonscenty.alchemia.entity.boss;

import me.moonscenty.alchemia.block.BannerBlockEntity;
import me.moonscenty.alchemia.block.LootBlock;
import me.moonscenty.alchemia.entity.cult.Cultist;
import me.moonscenty.alchemia.entity.cult.CultistCleric;
import me.moonscenty.alchemia.entity.cult.CultistKnight;
import me.moonscenty.alchemia.entity.cult.CultistPortal;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModEntities;
import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;

/**
 * The greater crimson portal, that opens in a great hall: a portal three blocks high that the Crimson Cult pours
 * through, wave after wave, while anyone is within forty-eight blocks.
 * <p>
 * Opening, it sets the cult's four banners about itself, six blocks out, and crates of loot appear round it. Its
 * first waves come fast; after that they come the slower the more cultists are already about. The thirteenth is the
 * Crimson Praetor. Until then it mends a point a tick; after him every wave it lets through costs it five to nine,
 * and it wears itself out. Touching it burns for eight. It dies in a blast that breaks nothing, and leaves one or two
 * primordial pearls.
 */
public class GreaterCultistPortal extends CultistPortal {
    private static final double WAKES_WITHIN = 48.0;
    private static final int PRAETOR = 12;

    private int stage;
    private int stageCounter = 200;
    private final ServerBossEvent bar = new ServerBossEvent(net.minecraft.network.chat.Component.empty(),
            BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);

    public GreaterCultistPortal(EntityType<? extends GreaterCultistPortal> type, Level level) {
        super(type, level);
        xpReward = 30;
        setPersistenceRequired();
        activate();
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 500.0)
                .add(Attributes.ARMOR, 5.0)
                .add(Attributes.ATTACK_DAMAGE, 0.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    protected void think(ServerLevel level) {
        if (stageCounter > 0) {
            stageCounter--;
            if (stageCounter == 160 && stage == 0) {
                level.broadcastEntityEvent(this, (byte) 16);
                for (Direction side : Direction.Plane.HORIZONTAL) {
                    BlockPos at = new BlockPos((int) getX() - side.getStepX() * 6, (int) getY(),
                            (int) getZ() + side.getStepZ() * 6);
                    level.setBlock(at, ModBlocks.BANNER.get().defaultBlockState(), Block.UPDATE_ALL);
                    if (level.getBlockEntity(at) instanceof BannerBlockEntity banner) {
                        banner.hang(switch (side) {
                            case NORTH -> 8;
                            case WEST -> 12;
                            case EAST -> 4;
                            default -> 0;
                        }, false);
                        playSound(ModSounds.WAND_FAIL.get(), 1.0F, 1.0F);
                    }
                }
            }
            if (stageCounter > 20 && stageCounter < 150 && stage == 0 && stageCounter % 13 == 0) {
                int i = (int) getX() + random.nextInt(5) - random.nextInt(5);
                int k = (int) getZ() + random.nextInt(5) - random.nextInt(5);
                BlockPos at = new BlockPos(i, (int) getY(), k);
                if (i != (int) getX() && k != (int) getZ() && level.isEmptyBlock(at)) {
                    level.broadcastEntityEvent(this, (byte) 16);
                    float roll = level.random.nextFloat();
                    int rarity = roll < 0.05F ? 2 : roll < 0.2F ? 1 : 0;
                    level.setBlock(at, ModBlocks.LOOT_CRATE.get().defaultBlockState()
                            .setValue(LootBlock.RARITY, rarity), Block.UPDATE_ALL);
                    playSound(ModSounds.WAND_FAIL.get(), 1.0F, 1.0F);
                }
            }
        } else if (level.getNearestPlayer(this, WAKES_WITHIN) != null) {
            level.broadcastEntityEvent(this, (byte) 16);
            if (stage < 5) {
                stageCounter = 15 + random.nextInt(10 - stage) - stage;
                letThrough(level);
            } else if (stage == PRAETOR) {
                stageCounter = 50 + timing() * 2 + random.nextInt(50);
                praetor(level);
            } else {
                int timing = timing();
                stageCounter = timing + random.nextInt(5 + timing / 3);
                letThrough(level);
            }
            stage++;
        } else {
            stageCounter = 30 + random.nextInt(30);
        }
        if (stage < PRAETOR) {
            heal(1.0F);
        }
        bar.setProgress(getHealth() / getMaxHealth());
    }

    /** The more of the cult already about, the longer the wait. */
    private int timing() {
        return level().getEntitiesOfClass(Cultist.class, new AABB(blockPosition()).inflate(32.0)).size() * 20;
    }

    @Override
    public void letThrough(ServerLevel level) {
        Cultist cultist = random.nextFloat() > 0.33F ? new CultistKnight(level) : new CultistCleric(level);
        cultist.moveTo(getX() + random.nextFloat() - random.nextFloat(), getY() + 0.25,
                getZ() + random.nextFloat() - random.nextFloat(), random.nextFloat() * 360.0F, 0.0F);
        cultist.finalizeSpawn(level, level.getCurrentDifficultyAt(cultist.blockPosition()), MobSpawnType.MOB_SUMMONED,
                null);
        cultist.restrictTo(blockPosition(), 32);
        level.addFreshEntity(cultist);
        cultist.spawnAnim();
        cultist.playSound(ModSounds.WAND_FAIL.get(), 1.0F, 1.0F);
        if (stage > PRAETOR) {
            hurt(damageSources().fellOutOfWorld(), 5 + random.nextInt(5));
        }
    }

    private void praetor(ServerLevel level) {
        CultistLeader leader = ModEntities.CULTIST_LEADER.get().create(level);
        if (leader == null) {
            return;
        }
        leader.moveTo(getX() + random.nextFloat() - random.nextFloat(), getY() + 0.25,
                getZ() + random.nextFloat() - random.nextFloat(), random.nextFloat() * 360.0F, 0.0F);
        leader.finalizeSpawn(level, level.getCurrentDifficultyAt(leader.blockPosition()), MobSpawnType.MOB_SUMMONED,
                null);
        leader.restrictTo(blockPosition(), 32);
        level.addFreshEntity(leader);
        leader.spawnAnim();
        leader.playSound(ModSounds.WAND_FAIL.get(), 1.0F, 1.0F);
    }

    @Override
    public void playerTouch(Player player) {
        if (distanceToSqr(player) < 3.0 && player.hurt(damageSources().indirectMagic(this, this), 8.0F)) {
            playSound(ModSounds.ZAP.get(), 1.0F, (random.nextFloat() - random.nextFloat()) * 0.1F + 1.0F);
        }
    }

    @Override
    public void die(DamageSource source) {
        if (!level().isClientSide) {
            level().explode(this, getX(), getY(), getZ(), 2.0F, false, Level.ExplosionInteraction.NONE);
        }
        dieQuietly(source);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        ThaumcraftBoss.dropPearls(this);
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bar.setName(getDisplayName());
        bar.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bar.removePlayer(player);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("stage", stage);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        stage = tag.getInt("stage");
        activate();
    }
}
