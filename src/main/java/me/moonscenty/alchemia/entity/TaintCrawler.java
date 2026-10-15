package me.moonscenty.alchemia.entity;

import me.moonscenty.alchemia.AlchemiaConfig;
import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.block.taint.TaintSpread;
import me.moonscenty.alchemia.block.taint.Tainted;
import me.moonscenty.alchemia.player.effect.ModEffects;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModEntities;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A taint crawler: a little grub of taint, a silverfish's size and less, which leaves fibres where it crawls.
 *
 * <p>It comes of a taint growth when one is broken, of a chunk thick with flux, of an animal the taint has killed, and
 * out of an infested champion. It goes for players. On any but easy, a bite now and then gives taint poison.
 */
public class TaintCrawler extends Monster implements TaintedMob {
    /** The original's numbers. */
    private static final double LIVES = 8.0;
    private static final double SPEED = 0.275;
    private static final double BITES = 2.0;

    private BlockPos lastAt = BlockPos.ZERO;

    public TaintCrawler(EntityType<? extends TaintCrawler> type, Level level) {
        super(type, level);
        xpReward = 3;
    }

    public TaintCrawler(Level level) {
        this(ModEntities.TAINT_CRAWLER.get(), level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, LIVES)
                .add(Attributes.MOVEMENT_SPEED, SPEED)
                .add(Attributes.ATTACK_DAMAGE, BITES);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        goalSelector.addGoal(3, new RandomStrollGoal(this, 1.0));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !TaintedMob.allied(target) && super.canAttack(target);
    }

    @Override
    public boolean isAlliedTo(Entity other) {
        return TaintedMob.allied(other) || super.isAlliedTo(other);
    }

    /** Wherever it has crawled to, every quarter second, it leaves fibres if there is something to cling to. */
    @Override
    public void tick() {
        if (level().isClientSide && random.nextFloat() < 0.05F) {
            me.moonscenty.alchemia.client.TaintEffects.pollution(this, blockPosition());
        }
        if (level() instanceof ServerLevel served && tickCount % 5 == 0 && !lastAt.equals(blockPosition())) {
            lastAt = blockPosition();
            if (takes(served, lastAt)) {
                if (random.nextFloat() < AlchemiaConfig.TAINT_SPREAD_COST.get() / 3.0F) {
                    AuraHandler.drainAvailable(served, lastAt, ModAspects.FLUX, 1);
                }
                served.setBlockAndUpdate(lastAt, TaintSpread.fibres(served, lastAt));
            }
        }
        super.tick();
    }

    /** Open ground or something soft growing there, not under water, and with something solid to cling to. */
    private static boolean takes(ServerLevel level, BlockPos at) {
        BlockState there = level.getBlockState(at);
        if (there.is(BlockTags.LEAVES) || !there.getFluidState().isEmpty() || there.getBlock() instanceof Tainted) {
            return false;
        }
        boolean soft = there.isAir() || there.canBeReplaced() || there.getBlock() instanceof BushBlock;
        return soft && TaintSpread.besideSolid(level, at) && !TaintSpread.onlyBesideTaint(level, at);
    }

    /** On normal and hard, a bite can carry taint poison: three seconds of it, or six. */
    @Override
    public boolean doHurtTarget(Entity victim) {
        if (!super.doHurtTarget(victim)) {
            return false;
        }
        if (victim instanceof LivingEntity living) {
            int strength = level().getDifficulty() == Difficulty.NORMAL ? 3
                    : level().getDifficulty() == Difficulty.HARD ? 6 : 0;
            if (strength > 0 && random.nextInt(strength + 1) > 2) {
                living.addEffect(new MobEffectInstance(ModEffects.TAINT_POISON, strength * 20, 0), this);
            }
        }
        return true;
    }

    /** One time in eight, a scrap of what it was made of. */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        if (random.nextInt(8) == 0) {
            spawnAtLocation(new ItemStack(random.nextBoolean() ? ModItems.TAINTED_GOO.get()
                    : ModItems.TAINT_TENDRIL.get()), getBbHeight() / 2.0F);
        }
    }

    @Override
    public float getVoicePitch() {
        return 0.7F;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SILVERFISH_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SILVERFISH_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SILVERFISH_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(SoundEvents.SILVERFISH_STEP, 0.15F, 1.0F);
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.EVENTS;
    }

    /**
     * Sets one down where something tainted burst.
     *
     * @return whether it came
     */
    public static boolean spawnAt(ServerLevel level, double x, double y, double z) {
        TaintCrawler crawler = new TaintCrawler(level);
        crawler.moveTo(x, y, z, level.random.nextInt(360), 0.0F);
        return level.addFreshEntity(crawler);
    }

    /** A flux trouble: a crawler on the ground. */
    public static boolean fromFlux(ServerLevel level, BlockPos ground, net.minecraft.util.RandomSource random) {
        return ground.getY() + 1 < level.getMaxBuildHeight()
                && spawnAt(level, ground.getX() + 0.5, ground.getY() + 1, ground.getZ() + 0.5);
    }
}
