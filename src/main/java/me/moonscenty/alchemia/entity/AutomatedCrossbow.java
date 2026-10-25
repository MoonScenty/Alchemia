package me.moonscenty.alchemia.entity;

import me.moonscenty.alchemia.block.CrystalType;
import me.moonscenty.alchemia.item.PrimalArrowItem;
import me.moonscenty.alchemia.menu.CrossbowMenu;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModEntities;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * A crossbow on a tripod with a clockwork mind to aim it, the original's {@code EntityTurretCrossbow}: it shoots at
 * any monster it can see within twenty-four blocks, a bolt every one to three seconds, for as long as it has arrows.
 * <p>
 * Its owner opens it to load it, with arrows or primal arrows. Stood on a dispenser that faces up, it reloads itself
 * from it whenever it runs dry, taking a whole stack at a time.
 */
public class AutomatedCrossbow extends Construct implements RangedAttackMob, Container, MenuProvider {
    /** What it is worth hitting, and how hard that is. The original's numbers. */
    private static final double LIVES = 30.0;
    private static final double SEES = 24.0;
    private static final int PLATING = 2;
    private static final int MENDS_EVERY = 80;
    /** How long the arm takes to wind back after a reload, in ticks, and the event that sets it going. */
    private static final int WINDS_FOR = 10;
    private static final byte RELOADED = 17;
    /** How far back along its aim a bolt starts, so it does not start inside the thing that shot it. */
    private static final double SET_BACK = 0.9;
    private static final float BOLT_SPEED = 1.6F;

    /** The client's count of the reload's winding, and what it was a tick ago. */
    private int winding = -1;
    private float wound;
    private float woundBefore;

    public AutomatedCrossbow(EntityType<? extends AutomatedCrossbow> type, Level level) {
        super(type, level);
        xpReward = 0;
        setDropChance(EquipmentSlot.MAINHAND, 0.0F);
    }

    /** Set down on the block at a position, facing nowhere in particular. */
    public static <T extends AutomatedCrossbow> T at(EntityType<T> type, Level level, BlockPos at) {
        T turret = type.create(level);
        turret.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 0.0F, 0.0F);
        return turret;
    }

    public static AttributeSupplier.Builder attributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, LIVES)
                .add(Attributes.FOLLOW_RANGE, SEES);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new RangedAttackGoal(this, 0.0, 20, reloadsWithin(), (float) SEES));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Mob.class, 5, true, false,
                other -> other instanceof Enemy));
    }

    /** The longest it may wait between bolts, in ticks. */
    protected int reloadsWithin() {
        return 60;
    }

    @Override
    protected ItemStack folded() {
        return new ItemStack(ModItems.AUTOMATED_CROSSBOW.get());
    }

    @Override
    protected int mendsEvery() {
        return MENDS_EVERY;
    }

    @Override
    public int getArmorValue() {
        return PLATING;
    }

    /** The tripod slides only a little, and does not hop. */
    @Override
    public void knockback(double strength, double x, double z) {
        super.knockback(strength, x, z);
        Vec3 moving = getDeltaMovement();
        if (moving.y > 0.1) {
            setDeltaMovement(moving.x, 0.1, moving.z);
        }
    }

    // --- shooting ----------------------------------------------------------

    /**
     * One bolt at whatever it is aiming at, lifted by how far off that is, and a little rougher for a plain arrow
     * than a primal one, as the original's were.
     */
    @Override
    public void performRangedAttack(LivingEntity target, float range) {
        ItemStack ammo = getMainHandItem();
        if (ammo.isEmpty()) {
            return;
        }
        AbstractArrow bolt;
        float spread;
        if (ammo.getItem() instanceof PrimalArrowItem primal) {
            CrystalType kind = primal.kind();
            bolt = new PrimalArrow(level(), this, ammo.copyWithCount(1), null, kind);
            bolt.setBaseDamage((3.0 + range * 2.0F + random.nextGaussian() * 0.25) * PrimalArrow.multiplier(kind));
            spread = 1.0F;
        } else {
            bolt = new Arrow(level(), this, ammo.copyWithCount(1), null);
            bolt.setBaseDamage(2.5 + range * 2.0F + random.nextGaussian() * 0.25);
            bolt.pickup = AbstractArrow.Pickup.ALLOWED;
            spread = 2.0F;
        }
        Vec3 back = getViewVector(1.0F).scale(SET_BACK);
        bolt.setPos(bolt.getX() - back.x, bolt.getY() - back.y, bolt.getZ() - back.z);
        double dx = target.getX() - getX();
        double dy = target.getY() + target.getEyeHeight() + range * range * 4.0F - bolt.getY();
        double dz = target.getZ() - getZ();
        bolt.shoot(dx, dy, dz, BOLT_SPEED, spread);
        level().addFreshEntity(bolt);

        swing(InteractionHand.MAIN_HAND);
        playSound(SoundEvents.ARROW_SHOOT, 1.0F, 1.0F / (random.nextFloat() * 0.4F + 0.8F));
        ammo.shrink(1);
    }

    // --- reloading ---------------------------------------------------------

    @Override
    public void aiStep() {
        woundBefore = wound;
        if (!level().isClientSide && getMainHandItem().isEmpty()) {
            reloadFromBelow();
        }
        super.aiStep();
        if (level().isClientSide) {
            wind();
        }
    }

    /** Takes the first stack of arrows out of a dispenser it is standing on, if that dispenser faces up. */
    private void reloadFromBelow() {
        BlockPos below = blockPosition().below();
        BlockState state = level().getBlockState(below);
        if (!(level().getBlockEntity(below) instanceof DispenserBlockEntity dispenser)
                || !state.hasProperty(DispenserBlock.FACING) || state.getValue(DispenserBlock.FACING) != Direction.UP) {
            return;
        }
        for (int slot = 0; slot < dispenser.getContainerSize(); slot++) {
            ItemStack held = dispenser.getItem(slot);
            if (takes(held) && !held.isEmpty()) {
                setItemSlot(EquipmentSlot.MAINHAND, dispenser.removeItem(slot, held.getCount()));
                playSound(ModSounds.CAMERA_TICKS.get(), 1.0F, 1.0F);
                level().broadcastEntityEvent(this, RELOADED);
                return;
            }
        }
    }

    @Override
    public void handleEntityEvent(byte event) {
        if (event == RELOADED) {
            winding = 0;
        } else {
            super.handleEntityEvent(event);
        }
    }

    private void wind() {
        if (winding >= 0 && ++winding >= WINDS_FOR) {
            winding = -1;
        }
        wound = winding < 0 ? 0.0F : winding / (float) WINDS_FOR;
    }

    /** How far through winding back its arm it is, for drawing. */
    public float winding(float partial) {
        float step = wound - woundBefore;
        if (step < 0.0F) {
            step++;
        }
        return woundBefore + step * partial;
    }

    /** What it shoots: plain arrows and primal ones, as the original took. */
    public static boolean takes(ItemStack stack) {
        return stack.is(Items.ARROW) || stack.getItem() instanceof PrimalArrowItem;
    }

    // --- opening it --------------------------------------------------------

    @Override
    protected InteractionResult opened(Player player) {
        player.openMenu(this, buffer -> buffer.writeVarInt(getId()));
        return InteractionResult.CONSUME;
    }

    @Override
    protected void emptyOut() {
        if (!getMainHandItem().isEmpty()) {
            spawnAtLocation(getMainHandItem(), 0.5F);
            setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
    }

    @Override
    protected void salvage() {
        salvage(ModItems.CLOCKWORK_MIND.get(), 0.2F);
        salvage(ModItems.BRASS_GEAR.get(), 0.5F);
        salvage(ModBlocks.GREATWOOD.planks().get(), 0.5F);
        salvage(ModBlocks.GREATWOOD.planks().get(), 0.5F);
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new CrossbowMenu(id, inventory, this);
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
        return getItem(slot).split(count);
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
        }
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return isAlive() && owns(player) && player.distanceToSqr(this) < 64.0;
    }

    @Override
    public void clearContent() {
        setItem(0, ItemStack.EMPTY);
    }

    /** The two kinds are told apart by their registry, and the basic one comes from here. */
    public static AutomatedCrossbow basic(Level level, BlockPos at) {
        return at(ModEntities.AUTOMATED_CROSSBOW.get(), level, at);
    }
}
