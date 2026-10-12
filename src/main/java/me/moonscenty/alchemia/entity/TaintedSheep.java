package me.moonscenty.alchemia.entity;

import java.util.EnumSet;
import java.util.List;

import me.moonscenty.alchemia.block.taint.TaintSpread;
import me.moonscenty.alchemia.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.IShearable;

/**
 * A sheep the taint has made over, its fleece gone purple. It still grazes, now and then, but what it grazes turns to
 * taint where it stood -- grass it pulls up, or grass it stands on -- and its fleece grows back for it. Shorn, it
 * gives one to three purple wool.
 */
public class TaintedSheep extends TaintedBeast implements IShearable {
    private static final EntityDataAccessor<Boolean> SHEARED = SynchedEntityData.defineId(TaintedSheep.class,
            EntityDataSerializers.BOOLEAN);
    /** The entity event that starts it grazing, as a sheep's. */
    private static final byte GRAZES = 10;
    private static final int GRAZING_FOR = 40;

    private int grazing;
    private Graze graze;

    public TaintedSheep(EntityType<? extends TaintedSheep> type, Level level) {
        super(type, level);
    }

    public TaintedSheep(Level level) {
        this(ModEntities.TAINTED_SHEEP.get(), level);
    }

    /** The original's numbers. */
    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ARMOR, 1.0);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        graze = new Graze(this);
        goalSelector.addGoal(2, graze);
    }

    @Override
    protected boolean huntsAnimals() {
        return false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SHEARED, false);
    }

    public boolean isSheared() {
        return entityData.get(SHEARED);
    }

    public void setSheared(boolean sheared) {
        entityData.set(SHEARED, sheared);
    }

    @Override
    protected void customServerAiStep() {
        grazing = graze.left();
        super.customServerAiStep();
    }

    @Override
    public void aiStep() {
        if (level().isClientSide) {
            grazing = Math.max(0, grazing - 1);
        }
        super.aiStep();
    }

    @Override
    public void handleEntityEvent(byte event) {
        if (event == GRAZES) {
            grazing = GRAZING_FOR;
        } else {
            super.handleEntityEvent(event);
        }
    }

    /** How far its head is lowered to graze, for the renderer, as a sheep's is. */
    public float headEatPositionScale(float partial) {
        if (grazing <= 0) {
            return 0.0F;
        }
        if (grazing >= 4 && grazing <= 36) {
            return 1.0F;
        }
        return grazing < 4 ? (grazing - partial) / 4.0F : -(grazing - GRAZING_FOR - partial) / 4.0F;
    }

    /** How its head rocks as it grazes, for the renderer, as a sheep's does. */
    public float headEatAngleScale(float partial) {
        if (grazing > 4 && grazing <= 36) {
            float along = (grazing - 4 - partial) / 32.0F;
            return Mth.PI / 5.0F + 0.2199115F * Mth.sin(along * 28.7F);
        }
        return grazing > 0 ? Mth.PI / 5.0F : getXRot() * Mth.DEG_TO_RAD;
    }

    /** Having grazed, its fleece grows back. */
    public void ate() {
        setSheared(false);
    }

    @Override
    public boolean isShearable(Player player, ItemStack item, Level level, BlockPos pos) {
        return isAlive() && !isSheared();
    }

    @Override
    public List<ItemStack> onSheared(Player player, ItemStack item, Level level, BlockPos pos) {
        level.playSound(null, this, SoundEvents.SHEEP_SHEAR, player == null ? SoundSource.BLOCKS : SoundSource.PLAYERS,
                1.0F, 1.0F);
        setSheared(true);
        int wool = 1 + random.nextInt(3);
        return List.of(new ItemStack(Items.PURPLE_WOOL, wool));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Sheared", isSheared());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setSheared(tag.getBoolean("Sheared"));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SHEEP_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SHEEP_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SHEEP_AMBIENT;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(SoundEvents.SHEEP_STEP, 0.15F, 1.0F);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        if (random.nextInt(3) == 0) {
            spawnAtLocation(TaintedMob.gooOrTendril(random, 0.5F), getBbHeight() / 2.0F);
        }
    }

    /**
     * Grazing, which taints what it grazes: one time in two hundred and fifty, on short grass or standing on a grass
     * block, it lowers its head for two seconds, and at the end of it the grass goes and fibres are left.
     */
    private static final class Graze extends Goal {
        private final TaintedSheep sheep;
        private int left;

        Graze(TaintedSheep sheep) {
            this.sheep = sheep;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        int left() {
            return left;
        }

        @Override
        public boolean canUse() {
            if (sheep.getRandom().nextInt(250) != 0) {
                return false;
            }
            BlockPos at = sheep.blockPosition();
            return sheep.level().getBlockState(at).is(Blocks.SHORT_GRASS)
                    || sheep.level().getBlockState(at.below()).is(Blocks.GRASS_BLOCK);
        }

        @Override
        public void start() {
            left = GRAZING_FOR;
            sheep.level().broadcastEntityEvent(sheep, GRAZES);
            sheep.getNavigation().stop();
        }

        @Override
        public void stop() {
            left = 0;
        }

        @Override
        public boolean canContinueToUse() {
            return left > 0;
        }

        @Override
        public void tick() {
            left = Math.max(0, left - 1);
            if (left != 4 || !(sheep.level() instanceof ServerLevel level)) {
                return;
            }
            BlockPos at = sheep.blockPosition();
            if (level.getBlockState(at).is(Blocks.SHORT_GRASS)) {
                level.levelEvent(2001, at, Block.getId(Blocks.SHORT_GRASS.defaultBlockState()));
                level.setBlockAndUpdate(at, TaintSpread.fibres(level, at));
                sheep.ate();
            } else if (level.getBlockState(at.below()).is(Blocks.GRASS_BLOCK)) {
                level.levelEvent(2001, at.below(), Block.getId(Blocks.GRASS_BLOCK.defaultBlockState()));
                level.setBlockAndUpdate(at, TaintSpread.fibres(level, at));
                sheep.ate();
            }
        }
    }
}
