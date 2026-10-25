package me.moonscenty.alchemia.golem;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.client.GolemEffects;
import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.player.PlayerKnowledge;
import me.moonscenty.alchemia.registry.ModDataComponents;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

/**
 * A golem: a made thing pressed from a material and four parts, that belongs to whoever set it down.
 *
 * <p>Everything it is comes from its {@link GolemDesign}, which it carries packed into one number and which every
 * client sees. How much it takes and gives, how fast it goes, whether it climbs, flies, rolls or fights, what it
 * looks like: all of it is read off the design whenever the design changes, as the original did.
 *
 * <p>Left to itself it keeps near the place it was set down, or follows its owner if told to with a bell. A wand
 * takes it back up into a golem item, rank and all; a dye colours the little flag on its back. What it carries it
 * carries in its hands, one stack, or two if it has a hauler's basket.
 */
public class Golem extends PathfinderMob implements OwnableEntity, net.minecraft.world.entity.monster.RangedAttackMob {
    private static final EntityDataAccessor<Long> DESIGN =
            SynchedEntityData.defineId(Golem.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Byte> FLAGS =
            SynchedEntityData.defineId(Golem.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> COLOUR =
            SynchedEntityData.defineId(Golem.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Optional<UUID>> OWNER =
            SynchedEntityData.defineId(Golem.class, EntityDataSerializers.OPTIONAL_UUID);

    private static final int CLIMBING = 1;
    private static final int FOLLOWING = 2;
    private static final int FIGHTING = 8;

    /** The little puffs it gives when told something, and when it comes on a rank. The original's events. */
    public static final byte FOLLOWS = 5;
    public static final byte STAYS = 8;
    public static final byte RANKS_UP = 9;

    /** What a kill teaches a smart golem. */
    private static final int KILL_XP = 8;

    private GolemDesign design = GolemDesign.BASIC;
    private Set<GolemTrait> traits = design.traits();
    private int rankXp;
    @Nullable
    private BlockPos home;
    /** Which way it is set to go about, so that the AI is only rebuilt when that changes. */
    private String built = "";
    /** What it is doing for a seal just now. */
    @Nullable
    private me.moonscenty.alchemia.golem.task.Task task;

    public Golem(EntityType<? extends Golem> type, Level level) {
        super(type, level);
        xpReward = 5;
        setPersistenceRequired();
        setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        setDropChance(EquipmentSlot.OFFHAND, 0.0F);
    }

    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FLYING_SPEED, 0.3)
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.ATTACK_DAMAGE, 0.0)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DESIGN, 0L);
        builder.define(FLAGS, (byte) 0);
        builder.define(COLOUR, (byte) 0);
        builder.define(OWNER, Optional.empty());
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DESIGN.equals(key)) {
            design = GolemDesign.fromLong(entityData.get(DESIGN));
            traits = design.traits();
        }
    }

    // --- what it is ---------------------------------------------------------------------------------------

    public GolemDesign design() {
        return design;
    }

    public boolean has(GolemTrait trait) {
        return traits.contains(trait);
    }

    /** Gives it a new design and makes it over to match. */
    public void setDesign(GolemDesign design) {
        entityData.set(DESIGN, design.toLong());
        this.design = design;
        this.traits = design.traits();
        refresh();
    }

    public int rankXp() {
        return rankXp;
    }

    /**
     * Sets it down for the first time, as the golem item does: whose it is, what it is, and where home is.
     */
    public void placed(UUID owner, GolemDesign design, int rankXp, BlockPos at) {
        setOwner(owner);
        this.rankXp = rankXp;
        this.home = at;
        setDesign(design);
        setHealth(getMaxHealth());
    }

    /**
     * Reads its whole make-up off its design: how much it takes, how high it steps, how far from home it goes,
     * how it finds its way, how hard it hits, and what it sets about doing.
     */
    private void refresh() {
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(design.maxHealth());
        if (getHealth() > getMaxHealth()) {
            setHealth(getMaxHealth());
        }
        getAttribute(Attributes.STEP_HEIGHT).setBaseValue(has(GolemTrait.WHEELED) ? 0.5 : 0.6);
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(design.damage());
        restrictTo(home(), design.reach());
        if (!level().isClientSide) {
            String way = (has(GolemTrait.FLYER) ? "f" : has(GolemTrait.CLIMBER) ? "c" : "g")
                    + (following() ? "1" : "0") + (has(GolemTrait.FIGHTER) ? "1" : "0");
            if (!way.equals(built)) {
                built = way;
                navigation.stop();
                navigation = makeNavigation();
                moveControl = has(GolemTrait.FLYER) ? new FlyingMoveControl(this, 20, true) : new MoveControl(this);
                setNoGravity(has(GolemTrait.FLYER));
                setGoals();
            }
        }
    }

    private PathNavigation makeNavigation() {
        if (has(GolemTrait.FLYER)) {
            FlyingPathNavigation flying = new FlyingPathNavigation(this, level());
            flying.setCanOpenDoors(false);
            flying.setCanFloat(true);
            flying.setCanPassDoors(true);
            return flying;
        }
        GroundPathNavigation ground = has(GolemTrait.CLIMBER) ? new WallClimberNavigation(this, level())
                : new GroundPathNavigation(this, level());
        ground.setCanPassDoors(true);
        return ground;
    }

    /** What it gets up to, which depends on whether it follows, and whether it fights. */
    private void setGoals() {
        goalSelector.removeAllGoals(goal -> true);
        targetSelector.removeAllGoals(goal -> true);
        // golems keep out of water when they can, as the original's did
        setPathfindingMalus(PathType.WATER, has(GolemTrait.FLYER) ? 0.0F : -1.0F);
        if (following()) {
            goalSelector.addGoal(4, new GolemGoals.FollowOwner(this, 1.0, 10.0F, 2.0F));
        } else {
            goalSelector.addGoal(3, new GolemGoals.GotoEntity(this));
            goalSelector.addGoal(4, new GolemGoals.GotoBlock(this));
            goalSelector.addGoal(5, new GolemGoals.GoHome(this));
        }
        goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        if (has(GolemTrait.FIGHTER)) {
            if (!has(GolemTrait.FLYER)) {
                goalSelector.addGoal(0, new FloatGoal(this));
            }
            if (has(GolemTrait.RANGED) && design.arms() == GolemParts.Arms.DARTS) {
                goalSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.RangedAttackGoal(this, 1.0, 20, 25,
                        16.0F));
            }
            goalSelector.addGoal(2, new me.moonscenty.alchemia.entity.QuickMeleeGoal(this, 1.15, false));
            if (following()) {
                targetSelector.addGoal(1, new GolemGoals.DefendOwner(this));
                targetSelector.addGoal(2, new GolemGoals.AidOwner(this));
            }
            targetSelector.addGoal(3, new HurtByTargetGoal(this));
        }
    }

    /** Where it was set down, or told to stay. */
    public BlockPos home() {
        return home == null ? blockPosition() : home;
    }

    // --- flags --------------------------------------------------------------------------------------------

    private boolean flag(int bit) {
        return (entityData.get(FLAGS) & bit) != 0;
    }

    private void setFlag(int bit, boolean on) {
        byte flags = entityData.get(FLAGS);
        entityData.set(FLAGS, (byte) (on ? flags | bit : flags & ~bit));
    }

    public boolean following() {
        return flag(FOLLOWING);
    }

    public void setFollowing(boolean following) {
        setFlag(FOLLOWING, following);
    }

    /** Whether it has something to fight just now, which darts and claws are drawn by. */
    public boolean fighting() {
        return flag(FIGHTING);
    }

    public boolean climbing() {
        return flag(CLIMBING);
    }

    @Override
    public boolean onClimbable() {
        return climbing();
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target);
        setFlag(FIGHTING, getTarget() != null);
    }

    /** The colour of its flag, one more than the dye's number, or nought for none. */
    public int colour() {
        return entityData.get(COLOUR);
    }

    public void setColour(int colour) {
        entityData.set(COLOUR, (byte) colour);
    }

    // --- whose it is --------------------------------------------------------------------------------------

    @Override
    @Nullable
    public UUID getOwnerUUID() {
        return entityData.get(OWNER).orElse(null);
    }

    public void setOwner(@Nullable UUID owner) {
        entityData.set(OWNER, Optional.ofNullable(owner));
    }

    public boolean owns(Player player) {
        return player.getUUID().equals(getOwnerUUID());
    }

    /** It is on its owner's side, so its owner's team is its team. */
    @Override
    public net.minecraft.world.scores.PlayerTeam getTeam() {
        LivingEntity owner = getOwner();
        return owner != null ? owner.getTeam() : super.getTeam();
    }

    /** Its owner, its owner's other golems, and whoever its owner counts a friend. */
    @Override
    public boolean isAlliedTo(Entity other) {
        UUID owner = getOwnerUUID();
        if (owner != null) {
            if (other.getUUID().equals(owner)) {
                return true;
            }
            if (other instanceof OwnableEntity owned && owner.equals(owned.getOwnerUUID())) {
                return true;
            }
            LivingEntity master = getOwner();
            if (master != null && master != this && master.isAlliedTo(other)) {
                return true;
            }
        }
        return super.isAlliedTo(other);
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !isAlliedTo(target) && super.canAttack(target);
    }

    // --- living -------------------------------------------------------------------------------------------

    @Override
    public void aiStep() {
        updateSwingTime();
        super.aiStep();
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            if (built.isEmpty()) {
                // one summoned rather than set down has had no design read into it yet
                refresh();
            }
            if (task != null && task.suspended()) {
                task = null;
            }
            LivingEntity target = getTarget();
            if (target != null && (!target.isAlive() || isAlliedTo(target)
                    || has(GolemTrait.RANGED) && distanceToSqr(target) > 1024.0
                    || target instanceof Player && !((ServerLevel) level()).getServer().isPvpAllowed())) {
                setTarget(null);
            }
            if (tickCount % (has(GolemTrait.REPAIR) ? 40 : 100) == 0) {
                heal(1.0F);
            }
            if (has(GolemTrait.CLIMBER)) {
                setFlag(CLIMBING, !swinging && horizontalCollision);
            }
        } else {
            GolemEffects.tick(this);
        }
    }

    /**
     * A flyer drifts where it is steered and slows by a tenth a tick, as the original's did, and settles a little
     * when it has nothing to chase.
     */
    @Override
    public void travel(Vec3 input) {
        if (has(GolemTrait.FLYER) && isControlledByLocalInstance()) {
            moveRelative(0.1F, input);
            move(net.minecraft.world.entity.MoverType.SELF, getDeltaMovement());
            Vec3 drift = getDeltaMovement().scale(0.9);
            if (getTarget() == null) {
                drift = drift.add(0.0, -0.005, 0.0);
            }
            setDeltaMovement(drift);
            calculateEntityAnimation(false);
            return;
        }
        super.travel(input);
    }

    /** How fast it goes about, for whatever is moving it. */
    public float moveSpeed() {
        return design.speed();
    }

    @Override
    public int getArmorValue() {
        return design.armour();
    }

    /** It does not breathe. */
    @Override
    protected int decreaseAirSupply(int air) {
        return air;
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        if (has(GolemTrait.FLYER) || has(GolemTrait.CLIMBER)) {
            return false;
        }
        return super.causeFallDamage(distance, multiplier, source);
    }

    /** Only a heavy golem on legs is heavy enough to be heard walking. */
    @Override
    protected MovementEmission getMovementEmission() {
        return has(GolemTrait.HEAVY) && !has(GolemTrait.FLYER) ? MovementEmission.ALL : MovementEmission.NONE;
    }

    /**
     * Fire does nothing to a fireproof golem and an explosion a third as much to a blastproof one, never more than
     * half of it at once. Cactus does nothing at all. Stuck in a wall or fallen out of the world, it is put back
     * home before the hurt is counted.
     */
    @Override
    protected void actuallyHurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_FIRE) && has(GolemTrait.FIREPROOF) || source.is(DamageTypes.CACTUS)) {
            return;
        }
        if (source.is(DamageTypeTags.IS_EXPLOSION) && has(GolemTrait.BLASTPROOF)) {
            amount = Math.min(getMaxHealth() / 2.0F, amount * 0.3F);
        }
        if (source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.FELL_OUT_OF_WORLD)) {
            BlockPos back = home();
            moveTo(back.getX() + 0.5, back.getY() + 0.5, back.getZ() + 0.5, 0.0F, 0.0F);
        }
        super.actuallyHurt(source, amount);
    }

    /** Its blows teach a smart golem when they kill, and a deft one leaves what it kills as if a player had. */
    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit) {
            if (target instanceof LivingEntity living && has(GolemTrait.DEFT) && getOwner() instanceof Player owner) {
                living.setLastHurtByPlayer(owner);
            }
            if (target instanceof Mob mob && !mob.isAlive()) {
                addRankXp(KILL_XP);
            }
        }
        return hit;
    }

    /**
     * A dart from its launchers: a third as hard as it hits by hand, more the further off the target, aimed a little
     * high the further it has to go.
     */
    @Override
    public void performRangedAttack(LivingEntity target, float range) {
        GolemDart dart = new GolemDart(level(), this);
        dart.setBaseDamage(getAttributeValue(Attributes.ATTACK_DAMAGE) / 3.0 + range + random.nextGaussian() * 0.25);
        double dx = target.getX() - getX();
        double dy = target.getEyeY() + range * range - dart.getY();
        double dz = target.getZ() - getZ();
        dart.shoot(dx, dy, dz, 1.6F, 3.0F);
        level().addFreshEntity(dart);
        playSound(SoundEvents.ARROW_SHOOT, 1.0F, 1.0F / (random.nextFloat() * 0.4F + 0.8F));
    }

    /** A smart golem learns from what it does, and comes on a rank at a time, up to ten. */
    public void addRankXp(int xp) {
        if (!has(GolemTrait.SMART) || level().isClientSide || design.rank() >= GolemDesign.TOP_RANK) {
            return;
        }
        rankXp += xp;
        int needed = design.xpToNextRank();
        if (rankXp >= needed) {
            rankXp -= needed;
            setDesign(design.withRank(design.rank() + 1));
            level().broadcastEntityEvent(this, RANKS_UP);
            level().playSound(null, this, SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 0.25F, 1.0F);
        }
    }

    @Override
    public void handleEntityEvent(byte event) {
        if (event >= FOLLOWS && event <= RANKS_UP) {
            GolemEffects.emote(this, event);
        } else {
            super.handleEntityEvent(event);
        }
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.CAMERA_CLACK.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.CAMERA_CLACK.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.TOOL.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 240;
    }

    // --- being handled ------------------------------------------------------------------------------------

    /**
     * Only its owner can do anything with it. A wand takes it back up; a bell, once its owner knows how, tells it
     * to follow or to stay where it is; a dye colours its flag.
     */
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (player.isShiftKeyDown() || held.is(Items.NAME_TAG)) {
            return InteractionResult.PASS;
        }
        if (level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (isRemoved()) {
            return InteractionResult.CONSUME;
        }
        if (!owns(player)) {
            player.displayClientMessage(Component.translatable("entity.alchemia.golem.not_yours"), true);
            return InteractionResult.CONSUME;
        }
        if (held.getItem() instanceof WandItem) {
            playSound(ModSounds.ZAP.get(), 1.0F, 1.0F);
            takeUp();
            player.swing(hand, true);
        } else if (held.is(ModItems.GOLEM_BELL.get())
                && PlayerKnowledge.of(player).hasResearch(Alchemia.id("golem_direct"))) {
            playSound(ModSounds.SCAN.get(), 1.0F, 1.0F);
            giveUpTask();
            setFollowing(!following());
            if (following()) {
                player.displayClientMessage(Component.translatable("entity.alchemia.golem.follow"), true);
                level().broadcastEntityEvent(this, FOLLOWS);
            } else {
                player.displayClientMessage(Component.translatable("entity.alchemia.golem.stay"), true);
                level().broadcastEntityEvent(this, STAYS);
                home = blockPosition();
            }
            refresh();
            player.swing(hand, true);
        } else if (held.getItem() instanceof DyeItem dye) {
            playSound(ModSounds.ZAP.get(), 1.0F, 1.0F);
            setColour(dye.getDyeColor().getId() + 1);
            held.consume(1, player);
            player.swing(hand, true);
        }
        return InteractionResult.CONSUME;
    }

    /** Back into a golem item, with all it has learned, dropping whatever it was carrying. */
    private void takeUp() {
        giveUpTask();
        dropCarried();
        spawnAtLocation(item(), 0.5F);
        discard();
    }

    /** It as a golem item. */
    public ItemStack item() {
        ItemStack stack = new ItemStack(ModItems.GOLEM.get());
        stack.set(ModDataComponents.GOLEM_DESIGN.get(), design);
        if (rankXp > 0) {
            stack.set(ModDataComponents.GOLEM_XP.get(), rankXp);
        }
        return stack;
    }

    @Override
    public void die(DamageSource cause) {
        giveUpTask();
        me.moonscenty.alchemia.entity.OwnedDeaths.tell(this);
        super.die(cause);
        if (!level().isClientSide) {
            dropCarried();
        }
    }

    /** Some of what it was pressed from, and some of each, the more for a looting blade. */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        int looting = source.getEntity() instanceof LivingEntity killer
                ? EnchantmentHelper.getEnchantmentLevel(level.registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                        .getHolderOrThrow(Enchantments.LOOTING), killer)
                : 0;
        float chance = 0.3F + looting * 0.15F;
        for (ItemStack part : design.components()) {
            if (random.nextFloat() < chance) {
                ItemStack left = part.copy();
                left.shrink(random.nextInt(left.getCount()));
                spawnAtLocation(left, 0.25F);
            }
        }
    }

    // --- work ---------------------------------------------------------------------------------------------

    @Nullable
    public me.moonscenty.alchemia.golem.task.Task task() {
        return task;
    }

    public void setTask(@Nullable me.moonscenty.alchemia.golem.task.Task task) {
        this.task = task;
    }

    /** Lets go of whatever it had taken on, for another golem to take. */
    private void giveUpTask() {
        if (task != null) {
            task.setReserved(false);
        }
    }

    /**
     * Whether it may work for a seal: not a locked one of somebody else's, and only if it has every trait the seal
     * wants and none it will not have.
     */
    public boolean mayWork(@Nullable me.moonscenty.alchemia.golem.seal.SealEntity seal) {
        if (seal == null) {
            return true;
        }
        if (seal.locked() && !java.util.Objects.equals(getOwnerUUID(), seal.owner())) {
            return false;
        }
        return traits.containsAll(seal.seal().required()) && seal.seal().forbidden().stream().noneMatch(this::has);
    }

    // --- carrying -----------------------------------------------------------------------------------------

    /** How many stacks it can carry. */
    public int hands() {
        return has(GolemTrait.HAULER) ? 2 : 1;
    }

    private static EquipmentSlot hand(int which) {
        return which == 0 ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
    }

    /** What it is carrying, a stack to a hand, empty where it carries nothing. */
    public ItemStack[] carrying() {
        ItemStack[] held = new ItemStack[hands()];
        for (int which = 0; which < held.length; which++) {
            held[which] = getItemBySlot(hand(which));
        }
        return held;
    }

    /** Takes as much of a stack as it can hold, and gives back what it could not. */
    public ItemStack hold(ItemStack stack) {
        ItemStack left = stack.copy();
        for (int which = 0; which < hands() && !left.isEmpty(); which++) {
            ItemStack there = getItemBySlot(hand(which));
            if (there.isEmpty()) {
                setItemSlot(hand(which), left);
                return ItemStack.EMPTY;
            }
            if (ItemStack.isSameItemSameComponents(there, left) && there.getCount() < there.getMaxStackSize()) {
                int moved = Math.min(left.getCount(), there.getMaxStackSize() - there.getCount());
                there.grow(moved);
                left.shrink(moved);
            }
        }
        return left;
    }

    /**
     * Puts down some of what it is carrying: all of the first stack it holds when asked for nothing in particular,
     * or as much as is asked of a thing like the one given. What it still holds in the second hand moves to the
     * first.
     */
    public ItemStack drop(ItemStack wanted) {
        ItemStack out = ItemStack.EMPTY;
        for (int which = 0; which < hands() && out.isEmpty(); which++) {
            ItemStack there = getItemBySlot(hand(which));
            if (there.isEmpty()) {
                continue;
            }
            if (wanted.isEmpty()) {
                out = there.copy();
                setItemSlot(hand(which), ItemStack.EMPTY);
            } else if (ItemStack.isSameItemSameComponents(there, wanted)) {
                out = there.split(Math.min(wanted.getCount(), there.getCount()));
                if (there.isEmpty()) {
                    setItemSlot(hand(which), ItemStack.EMPTY);
                }
            }
        }
        if (hands() > 1 && getItemBySlot(hand(0)).isEmpty() && !getItemBySlot(hand(1)).isEmpty()) {
            setItemSlot(hand(0), getItemBySlot(hand(1)).copy());
            setItemSlot(hand(1), ItemStack.EMPTY);
        }
        return out;
    }

    /** Whether it has room for some of this. */
    public boolean canCarry(ItemStack stack) {
        for (int which = 0; which < hands(); which++) {
            ItemStack there = getItemBySlot(hand(which));
            if (there.isEmpty() || there.getCount() < there.getMaxStackSize()
                    && ItemStack.isSameItemSameComponents(there, stack)) {
                return true;
            }
        }
        return false;
    }

    public boolean isCarrying(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        for (int which = 0; which < hands(); which++) {
            if (ItemStack.isSameItemSameComponents(getItemBySlot(hand(which)), stack)) {
                return true;
            }
        }
        return false;
    }

    private void dropCarried() {
        for (EquipmentSlot slot : new EquipmentSlot[] {EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND}) {
            ItemStack there = getItemBySlot(slot);
            if (!there.isEmpty()) {
                spawnAtLocation(there, 0.25F);
                setItemSlot(slot, ItemStack.EMPTY);
            }
        }
    }

    // --- saving -------------------------------------------------------------------------------------------

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putLong("design", design.toLong());
        tag.put("home", NbtUtils.writeBlockPos(home()));
        tag.putByte("flags", (byte) (entityData.get(FLAGS) & FOLLOWING));
        tag.putInt("rank_xp", rankXp);
        tag.putByte("colour", (byte) colour());
        if (getOwnerUUID() != null) {
            tag.putUUID("owner", getOwnerUUID());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        home = NbtUtils.readBlockPos(tag, "home").orElse(null);
        entityData.set(FLAGS, tag.getByte("flags"));
        rankXp = tag.getInt("rank_xp");
        setColour(tag.getByte("colour"));
        setOwner(tag.hasUUID("owner") ? tag.getUUID("owner") : null);
        setDesign(GolemDesign.fromLong(tag.getLong("design")));
    }
}
