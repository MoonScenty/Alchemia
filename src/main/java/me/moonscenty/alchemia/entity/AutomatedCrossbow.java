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
import net.minecraft.world.InteractionHand;
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

public class AutomatedCrossbow extends ConstructWithSlot implements RangedAttackMob {
    // original's values
    private static final double LIVES = 30.0;
    private static final double SEES = 24.0;
    private static final int PLATING = 2;
    private static final int MENDS_EVERY = 80;
    // ticks; RELOADED is an entity event id
    private static final int WINDS_FOR = 10;
    private static final byte RELOADED = 17;
    // so the bolt does not start inside the turret
    private static final double SET_BACK = 0.9;
    private static final float BOLT_SPEED = 1.6F;

    // client-side only
    private int winding = -1;
    private float wound;
    private float woundBefore;

    public AutomatedCrossbow(EntityType<? extends AutomatedCrossbow> type, Level level) {
        super(type, level);
        xpReward = 0;
    }

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

    @Override
    protected boolean hops() {
        return false;
    }

    // plain arrows spread more than primal ones, as the original's
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

    public float winding(float partial) {
        float step = wound - woundBefore;
        if (step < 0.0F) {
            step++;
        }
        return woundBefore + step * partial;
    }

    public static boolean takes(ItemStack stack) {
        return stack.is(Items.ARROW) || stack.getItem() instanceof PrimalArrowItem;
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

    public static AutomatedCrossbow basic(Level level, BlockPos at) {
        return at(ModEntities.AUTOMATED_CROSSBOW.get(), level, at);
    }
}
