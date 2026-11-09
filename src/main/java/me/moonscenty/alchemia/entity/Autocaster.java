package me.moonscenty.alchemia.entity;

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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class Autocaster extends ConstructWithSlot {
    private static final EntityDataAccessor<Direction> FACING =
            SynchedEntityData.defineId(Autocaster.class, EntityDataSerializers.DIRECTION);

    // per primal
    public static final int HOLDS = 100;
    private static final int DRAWS = 1;
    private static final int DRAWS_EVERY = 4;
    private static final int MENDS_EVERY = 40;

    // original's values
    private static final double LIVES = 50.0;
    private static final int PLATING = 4;
    // original's values
    private static final float SALVAGED = 0.5F;
    private static final float MIND_SALVAGED = 0.2F;
    // one-in-N chance per tick
    private static final int LOOKS_ROUND = 5;
    // cooldown multiplier over the same focus cast by hand
    private static final double SLOWER_BY_HAND = 1.33;
    // focus cooldowns are in milliseconds
    private static final int A_TICK = 50;
    // original's values: radians across, and degrees a tick
    private static final double AIM_CONE = 0.3;
    private static final float TURNS = 10.0F;
    private static final float TILTS = 20.0F;
    private AspectList vis = AspectList.EMPTY;
    private int waits = 10;
    // counts shots in a burst, which per-tick foci scale by
    private int shots;
    private boolean shotLastTick;

    public Autocaster(EntityType<? extends Autocaster> type, Level level) {
        super(type, level);
        xpReward = 6;
    }

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

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 12.0F));
        goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, LOOKS_ROUND,
                true, false, this::worthShooting));
        mind(MONSTERS, true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FACING, Direction.UP);
    }

    @Override
    protected ItemStack folded() {
        return new ItemStack(ModItems.AUTOCASTER.get());
    }

    @Override
    protected int mendsEvery() {
        return MENDS_EVERY;
    }

    // aims here rather than in a goal so it runs before the look control reads it
    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        LivingEntity mark = getTarget();
        if (mark != null) {
            getLookControl().setLookAt(mark, TURNS, TILTS);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            return;
        }
        forgetStrangers();
        if (tickCount % DRAWS_EVERY == 0) {
            drawBreath();
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

    // least-stocked primal first so it fills evenly; takes only what the chunk has
    private void drawBreath() {
        Holder<Aspect> lowest = null;
        int least = Integer.MAX_VALUE;
        for (Holder<Aspect> primal : ModAspects.primals()) {
            int here = vis.get(primal);
            if (here < holdsAtMost() && here < least && AuraHandler.get(level(), blockPosition(), primal) > 0) {
                lowest = primal;
                least = here;
            }
        }
        if (lowest != null) {
            vis = vis.add(lowest, AuraHandler.drainAvailable(level(), blockPosition(), lowest, DRAWS));
        }
    }

    // vis is only taken when the spell reports it went
    // the pitch is raised for the focus's drop during the shot only, then restored
    private void shoot() {
        LivingEntity mark = getTarget();
        Focus focus = loaded();
        if (mark == null || focus == null || !(level() instanceof ServerLevel served)) {
            return;
        }
        Focus.Turret turret = focus.turret();
        float far = distanceTo(mark);
        if (turret == null || far > turret.range() || !aimedAt(mark, turret.range())) {
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

    public boolean aimedAt(LivingEntity mark, float range) {
        Vec3 toMark = new Vec3(mark.getX(), mark.getBoundingBox().minY + mark.getBbHeight() / 2.0, mark.getZ())
                .subtract(getEyePosition());
        double along = toMark.dot(getViewVector(1.0F));
        return along > 0.0 && along / toMark.length() > Math.cos(AIM_CONE / 2.0) && along < range;
    }

    // spells read the caster's wand, so make one up
    private ItemStack wand() {
        ItemStack wand = WandItem.of(ModWandParts.GREATWOOD, ModWandParts.GOLD);
        WandItem.setFocus(wand, getMainHandItem());
        return wand;
    }

    public AspectList vis() {
        return vis;
    }

    // no wand or gear discounts apply
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

    protected int holdsAtMost() {
        return HOLDS;
    }

    protected void setVis(Holder<Aspect> aspect, int amount) {
        vis = vis.withAmount(aspect, amount);
    }

    public void fill() {
        for (Holder<Aspect> primal : ModAspects.primals()) {
            vis = vis.withAmount(primal, holdsAtMost());
        }
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return mindsKind(target) && super.canAttack(target);
    }

    @Override
    protected boolean letsHandle(Player player, InteractionHand hand) {
        return false;
    }

    @Override
    protected void emptyOut() {
        if (!getMainHandItem().isEmpty()) {
            spawnAtLocation(getMainHandItem(), 0.0F);
            setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
    }

    // original's salvage
    @Override
    protected void salvage() {
        salvage(ModItems.BIOALCHEMIC_MIND.get(), MIND_SALVAGED);
        salvage(ModItems.BRASS_GEAR.get(), SALVAGED);
        salvage(ModBlocks.GREATWOOD.planks().get(), SALVAGED);
        salvage(ModBlocks.GREATWOOD.planks().get(), SALVAGED);
    }

    @Override
    public void travel(Vec3 wanted) {
    }

    @Override
    protected double slides() {
        return 1.0;
    }

    @Override
    protected boolean breathes() {
        return true;
    }

    @Override
    public int getArmorValue() {
        return PLATING;
    }

    public Direction getFacing() {
        return entityData.get(FACING);
    }

    public void setFacing(Direction face) {
        entityData.set(FACING, face);
    }

    public FocusItem fitted() {
        return getMainHandItem().getItem() instanceof FocusItem focus ? focus : null;
    }

    public Focus loaded() {
        FocusItem item = fitted();
        return item == null ? null : item.focus(getMainHandItem());
    }

    public static boolean takes(ItemStack stack) {
        return stack.isEmpty()
                || stack.getItem() instanceof FocusItem focus && focus.focus(stack).turret() != null;
    }

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
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new AutocasterMenu(id, inventory, this);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("face", (byte) getFacing().ordinal());
        AspectList.CODEC.encodeStart(registryAccess().createSerializationContext(NbtOps.INSTANCE), vis)
                .result().ifPresent(written -> tag.put("vis", written));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setFacing(Direction.from3DDataValue(tag.getByte("face")));
        vis = AspectList.CODEC
                .parse(registryAccess().createSerializationContext(NbtOps.INSTANCE), tag.get("vis"))
                .result().orElse(AspectList.EMPTY);
        setChanged();
    }

}
