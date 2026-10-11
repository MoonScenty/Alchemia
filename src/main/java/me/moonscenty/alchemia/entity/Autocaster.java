package me.moonscenty.alchemia.entity;

import java.util.UUID;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.item.FocusItem;
import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.menu.AutocasterMenu;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModEntities;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModWandParts;
import me.moonscenty.alchemia.wand.Casting;
import me.moonscenty.alchemia.wand.Focus;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * A focus bolted to a wall, with just enough mind to point itself at something and let the focus off.
 *
 * <p>It is a machine, not a thaumaturge. It draws its own vis out of the air around it a drop at a time, and
 * nothing it wears or carries makes a working any cheaper. What it is good for is being somewhere a person is
 * not: seven of the twelve foci are worth aiming at a thing, and an autocaster will aim one at anything its owner
 * has told it to mind.
 *
 * <p>It cannot move and cannot be pushed. It sits on whichever face it was put against, turns its head, and is
 * taken down again by clicking it with a wand.
 */
public class Autocaster extends PathfinderMob implements OwnableEntity, Container, MenuProvider {
    /** Whose it is. Written as a string the way the game writes a tamed animal's. */
    private static final EntityDataAccessor<String> OWNER =
            SynchedEntityData.defineId(Autocaster.class, EntityDataSerializers.STRING);
    /** Which face it is stuck to, which is the only thing that settles how it is drawn. */
    private static final EntityDataAccessor<Direction> FACING =
            SynchedEntityData.defineId(Autocaster.class, EntityDataSerializers.DIRECTION);
    /** The four things it may be told to mind, one to a bit, so the screen can read them all at once. */
    private static final EntityDataAccessor<Byte> MINDS =
            SynchedEntityData.defineId(Autocaster.class, EntityDataSerializers.BYTE);

    /** The bits in {@link #MINDS}, in the order the original's four buttons stood in. */
    public static final int ANIMALS = 0;
    public static final int MONSTERS = 1;
    public static final int PLAYERS = 2;
    public static final int FRIENDS = 3;

    /** How much of any one primal it will hold, and how much the aura hands it at a time. */
    public static final int HOLDS = 100;
    private static final int DRAWS = 1;
    /** How often it draws a drop out of the air, and how often it knits itself back together, in ticks. */
    private static final int DRAWS_EVERY = 4;
    private static final int MENDS_EVERY = 40;
    private static final float MENDS = 1.0F;

    /** What it is worth hitting, and how hard that is. The original's numbers. */
    private static final double LIVES = 50.0;
    private static final int PLATING = 4;
    /** How much of a broken one is worth picking up again, and how often its mind survives. The original's. */
    private static final float SALVAGED = 0.5F;
    private static final float MIND_SALVAGED = 0.2F;
    /** How often it looks round for something to shoot, as a one-in-this chance a tick. */
    private static final int LOOKS_ROUND = 5;
    /** The wait after a shot, as a share of what the same focus makes a hand wait. */
    private static final double SLOWER_BY_HAND = 1.33;
    /** A millisecond wait as the original wrote it, in the ticks this counts in. */
    private static final int A_TICK = 50;
    /** Its own store, a reservoir for each primal. Nobody is shown it, as nobody was shown the original's. */
    private AspectList vis = AspectList.EMPTY;
    /** Ticks before it will shoot again. */
    private int waits = 10;
    /** How many shots this burst has been, which is what a focus held down counts by. */
    private int shots;
    private boolean shotLastTick;

    public Autocaster(EntityType<? extends Autocaster> type, Level level) {
        super(type, level);
        setPersistenceRequired();
        xpReward = 6;
        setDropChance(EquipmentSlot.MAINHAND, 0.0F);
    }

    /** Put against a face, which is where it stays. */
    public Autocaster(Level level, BlockPos at, Direction face) {
        this(ModEntities.AUTOCASTER.get(), level);
        moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 0.0F, 0.0F);
        setFacing(face);
    }

    public static AttributeSupplier.Builder attributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, LIVES)
                .add(Attributes.FOLLOW_RANGE, Focus.Turret.SEES);
    }

    /**
     * Two goals and no more.
     * <p>
     * One picks something to shoot at; the other makes it shoot back at whatever shot it. Everything else a mob
     * has goals for -- wandering, fleeing, looking about -- is of no use to a thing bolted to a wall.
     */
    @Override
    protected void registerGoals() {
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, LOOKS_ROUND,
                true, false, this::worthShooting));
        mind(MONSTERS, true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(OWNER, "");
        builder.define(FACING, Direction.UP);
        builder.define(MINDS, (byte) 0);
    }

    // --- what it does with its time ---------------------------------------------------------------------

    /**
     * Pointing itself at whatever it has settled on.
     * <p>
     * Done here rather than in a goal of its own because that is all a goal would do, and because this runs the
     * tick before the look control reads it, which is where a mob's aiming belongs.
     */
    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        LivingEntity mark = getTarget();
        if (mark != null) {
            getLookControl().setLookAt(mark, getHeadRotSpeed(), getMaxHeadXRot());
        }
    }

    /**
     * Drawing breath, mending, and shooting.
     * <p>
     * The body is set to the head, because a turret has no body to speak of and what draws it wants one angle
     * for the ball rather than two.
     */
    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            return;
        }
        setYRot(yHeadRot);
        if (tickCount % DRAWS_EVERY == 0) {
            drawBreath();
        }
        if (tickCount % MENDS_EVERY == 0) {
            heal(MENDS);
        }
        if (waits > 0) {
            waits--;
        }

        boolean shot = shotLastTick;
        shotLastTick = false;
        if (getTarget() != null && waits <= 0) {
            shoot();
        }
        // a focus held down counts its own ticks; letting go is what puts the count back
        if (shot && !shotLastTick) {
            shots = 0;
        }
    }

    /**
     * One drop of whichever primal it has least of, out of the air it is standing in.
     * <p>
     * Least first, so a turret left alone fills evenly rather than filling up on whatever the land happens to be
     * thick with. It takes nothing a chunk has not got, so a thin sky simply leaves it dry.
     */
    private void drawBreath() {
        Holder<Aspect> lowest = null;
        int least = Integer.MAX_VALUE;
        for (Holder<Aspect> primal : ModAspects.primals()) {
            int here = vis.get(primal);
            if (here < HOLDS && here < least && AuraHandler.get(level(), blockPosition(), primal) > 0) {
                lowest = primal;
                least = here;
            }
        }
        if (lowest != null) {
            vis = vis.add(lowest, AuraHandler.drainAvailable(level(), blockPosition(), lowest, DRAWS));
        }
    }

    /**
     * Lets the focus off at whatever it is pointed at.
     * <p>
     * The order is the order a hand casts in: it must be in range, it must be seen, the price must be in the
     * reservoir, and only when the working itself has come to something is the price actually taken. A turret
     * firing into a wall would otherwise drink itself dry in a few seconds.
     *
     * <p>Before it shoots it lifts its nose by however much the focus drops on the way, scaled by how far off the
     * mark is, and puts it straight back afterwards. The lift is part of the shot and not part of where it is
     * looking.
     */
    private void shoot() {
        LivingEntity mark = getTarget();
        Focus focus = loaded();
        if (mark == null || focus == null || !(level() instanceof ServerLevel served)) {
            return;
        }
        Focus.Turret turret = focus.turret();
        float far = distanceTo(mark);
        if (turret == null || far > turret.range() || !getSensing().hasLineOfSight(mark)) {
            return;
        }
        if (!holds(focus.cost())) {
            return;
        }

        float lift = turret.lift() * (far / turret.range());
        setXRot(getXRot() - lift);
        boolean came = focus.spell().cast(served, this, wand(), Casting.aim(this), shots);
        setXRot(getXRot() + lift);
        if (!came) {
            return;
        }

        pay(focus.cost());
        // no floor of a tick: a focus charged by the tick is meant to go off every tick, as a held one does
        waits = focus.cooldown() / A_TICK;
        if (!focus.perTick()) {
            waits = (int) (waits * SLOWER_BY_HAND);
        }
        shots++;
        shotLastTick = true;
    }

    /** The wand a hand would be holding, made up on the spot so a spell has one to read. */
    private ItemStack wand() {
        ItemStack wand = WandItem.of(ModWandParts.GREATWOOD, ModWandParts.GOLD);
        WandItem.setFocus(wand, getMainHandItem());
        return wand;
    }

    // --- its reservoir ----------------------------------------------------------------------------------

    public AspectList vis() {
        return vis;
    }

    /** Whether the whole price is in the reservoir. Nothing it wears makes a working any cheaper. */
    public boolean holds(AspectList price) {
        for (Holder<Aspect> aspect : price.sortedByName()) {
            if (vis.get(aspect) < price.get(aspect)) {
                return false;
            }
        }
        return true;
    }

    private void pay(AspectList price) {
        for (Holder<Aspect> aspect : price.sortedByName()) {
            vis = vis.reduce(aspect, price.get(aspect));
        }
    }

    /** Fills it up, which is a thing only a test and the creative hand have any business doing. */
    public void fill() {
        for (Holder<Aspect> primal : ModAspects.primals()) {
            vis = vis.withAmount(primal, HOLDS);
        }
    }

    // --- what it is pointed at --------------------------------------------------------------------------

    /**
     * Whether this is something it has been told to mind.
     * <p>
     * The three kinds are the game's own: a monster, something alive that is not one, and a person. What is
     * already its owner's is passed over unless it has been set against friends, which is how the original let
     * somebody build a trap for themselves on purpose.
     */
    private boolean worthShooting(LivingEntity other) {
        boolean friendly = minds(FRIENDS);
        if (other instanceof Player player) {
            if (!minds(PLAYERS) || player.isCreative() || player.isSpectator()) {
                return false;
            }
            return friendly || !player.getUUID().equals(getOwnerUUID());
        }
        if (other instanceof Autocaster theirs) {
            return friendly && !java.util.Objects.equals(theirs.getOwnerUUID(), getOwnerUUID());
        }
        boolean monster = other.getType().getCategory() == MobCategory.MONSTER;
        if (!minds(monster ? MONSTERS : ANIMALS)) {
            return false;
        }
        if (other instanceof OwnableEntity owned && java.util.Objects.equals(owned.getOwnerUUID(), getOwnerUUID())) {
            return friendly;
        }
        return true;
    }

    public boolean minds(int what) {
        return (entityData.get(MINDS) & 1 << what) != 0;
    }

    /** Says what to mind, and forgets whatever it was aiming at, since it may no longer be fair game. */
    public void mind(int what, boolean on) {
        byte minds = entityData.get(MINDS);
        entityData.set(MINDS, (byte) (on ? minds | 1 << what : minds & ~(1 << what)));
        setTarget(null);
    }

    // --- being handled ----------------------------------------------------------------------------------

    /**
     * A wand takes it down; an empty hand opens it.
     * <p>
     * Only for whoever put it there. Somebody else's turret is somebody else's, and saying so is friendlier than
     * letting them find out by taking it off the wall.
     */
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (level().isClientSide || isRemoved()) {
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        if (!owns(player)) {
            player.displayClientMessage(Component.translatable("entity.alchemia.autocaster.not_yours"), true);
            return InteractionResult.CONSUME;
        }
        if (player.getItemInHand(hand).getItem() instanceof WandItem) {
            takeDown();
            player.swing(hand);
            return InteractionResult.CONSUME;
        }
        player.openMenu(this, buffer -> buffer.writeVarInt(getId()));
        return InteractionResult.CONSUME;
    }

    /** Off the wall and back into a hand: the focus, and the thing itself. */
    private void takeDown() {
        dropFocus();
        spawnAtLocation(new ItemStack(ModItems.AUTOCASTER.get()), 0.5F);
        discard();
    }

    private void dropFocus() {
        if (!getMainHandItem().isEmpty()) {
            spawnAtLocation(getMainHandItem(), 0.0F);
            setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
    }

    /** Whatever was in it falls out, which is the only reason anybody would break one open. */
    @Override
    public void die(DamageSource cause) {
        super.die(cause);
        if (!level().isClientSide) {
            dropFocus();
        }
    }

    /**
     * What is left of a broken one.
     * <p>
     * Half of its brasswork and half of its boards, and now and then the made mind at the heart of it, as the
     * original gave back.
     */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource cause, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, cause, recentlyHit);
        salvage(ModItems.BIOTHAUMIC_MIND.get(), MIND_SALVAGED);
        salvage(ModItems.BRASS_GEAR.get(), SALVAGED);
        salvage(ModBlocks.GREATWOOD.planks().get(), SALVAGED);
        salvage(ModBlocks.GREATWOOD.planks().get(), SALVAGED);
    }

    private void salvage(net.minecraft.world.level.ItemLike part, float chance) {
        if (getRandom().nextFloat() < chance) {
            spawnAtLocation(new ItemStack(part), 0.5F);
        }
    }

    /** A blow knocks it off true, so a thing being shot at can at least put the aim out. */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        setYRot((float) (getYRot() + getRandom().nextGaussian() * 45.0));
        setXRot((float) (getXRot() + getRandom().nextGaussian() * 20.0));
        return super.hurt(source, amount);
    }

    // --- a thing bolted to a wall -----------------------------------------------------------------------

    @Override
    public void travel(Vec3 wanted) {
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(net.minecraft.world.entity.Entity other) {
    }

    /**
     * How steeply it may aim.
     * <p>
     * The original wrote twenty here and meant something else by it: its look helper stepped the pitch twenty
     * degrees a tick towards wherever it was pointing, so twenty was a speed. This game's look control throws
     * the pitch away every tick and builds it again from level, so the same number would be a ceiling, and a
     * turret that cannot look more than twenty degrees down is a turret anything can walk under.
     */
    @Override
    public int getMaxHeadXRot() {
        return 90;
    }

    @Override
    public int getArmorValue() {
        return PLATING;
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    public Direction getFacing() {
        return entityData.get(FACING);
    }

    public void setFacing(Direction face) {
        entityData.set(FACING, face);
    }

    // --- whose it is ------------------------------------------------------------------------------------

    @Override
    public UUID getOwnerUUID() {
        String written = entityData.get(OWNER);
        try {
            return written.isEmpty() ? null : UUID.fromString(written);
        } catch (IllegalArgumentException malformed) {
            return null;
        }
    }

    public void setOwner(UUID owner) {
        entityData.set(OWNER, owner == null ? "" : owner.toString());
    }

    public boolean owns(Player player) {
        return getOwnerUUID() == null || player.getUUID().equals(getOwnerUUID());
    }

    /** It fights on its owner's side, so its owner's team is its team. */
    @Override
    public net.minecraft.world.scores.PlayerTeam getTeam() {
        LivingEntity owner = getOwner();
        return owner != null ? owner.getTeam() : super.getTeam();
    }

    // --- the one slot it has ----------------------------------------------------------------------------

    /** The focus goes in the hand, which is where a mob's one held thing lives and where it syncs from. */
    public FocusItem fitted() {
        return getMainHandItem().getItem() instanceof FocusItem focus ? focus : null;
    }

    /** What that focus does and costs, with whatever is worked into it counted in. */
    public Focus loaded() {
        FocusItem item = fitted();
        return item == null ? null : item.focus(getMainHandItem());
    }

    /** Whether an autocaster will take this at all: a focus, and one worth aiming at something. */
    public static boolean takes(ItemStack stack) {
        return stack.isEmpty()
                || stack.getItem() instanceof FocusItem focus && focus.focus(stack).turret() != null;
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return getMainHandItem().isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot == 0 ? getMainHandItem() : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        ItemStack taken = getItem(slot).split(count);
        if (!taken.isEmpty()) {
            setChanged();
        }
        return taken;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack held = getItem(slot);
        setItem(slot, ItemStack.EMPTY);
        return held;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot == 0) {
            setItemSlot(EquipmentSlot.MAINHAND, stack);
            setChanged();
        }
    }

    /**
     * A new focus is a new reach, so the thing looks for a mark as far off as this one will carry.
     * <p>
     * Nothing else about it changes: the vis it has drawn is its own and stays through a change of focus, as the
     * original's did.
     */
    @Override
    public void setChanged() {
        Focus.Turret turret = loaded() == null ? null : loaded().turret();
        var range = getAttribute(Attributes.FOLLOW_RANGE);
        if (range != null) {
            range.setBaseValue(turret == null ? Focus.Turret.SEES : turret.range());
        }
        setTarget(null);
    }

    @Override
    public boolean stillValid(Player player) {
        return isAlive() && owns(player) && player.distanceToSqr(this) < 64.0;
    }

    @Override
    public void clearContent() {
        setItem(0, ItemStack.EMPTY);
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new AutocasterMenu(id, inventory, this);
    }

    // --- saving -----------------------------------------------------------------------------------------

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("face", (byte) getFacing().ordinal());
        tag.putByte("minds", entityData.get(MINDS));
        tag.putString("owner", entityData.get(OWNER));
        AspectList.CODEC.encodeStart(registryAccess().createSerializationContext(NbtOps.INSTANCE), vis)
                .result().ifPresent(written -> tag.put("vis", written));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setFacing(Direction.from3DDataValue(tag.getByte("face")));
        entityData.set(MINDS, tag.getByte("minds"));
        entityData.set(OWNER, tag.getString("owner"));
        vis = AspectList.CODEC
                .parse(registryAccess().createSerializationContext(NbtOps.INSTANCE), tag.get("vis"))
                .result().orElse(AspectList.EMPTY);
        setChanged();
    }

}
