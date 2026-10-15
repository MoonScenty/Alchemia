package me.moonscenty.alchemia.entity;

import me.moonscenty.alchemia.block.CrystalType;
import me.moonscenty.alchemia.registry.ModEntities;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * A primal arrow in flight, the original's {@code EntityPrimalArrow}: an arrow with a primal bound into it.
 * <ul>
 * <li>air: an arrow's damage, but as magic that no armour turns
 * <li>fire: fire damage, and sets what it hits alight for five seconds more
 * <li>water: an arrow's damage, and slows what it hits to a crawl for ten seconds
 * <li>earth: half as much again, and knocks back harder
 * <li>order: a little less, through armour, and leaves what it hits weak for ten seconds
 * <li>entropy: a little less, and withers what it hits for five seconds
 * </ul>
 * Nobody picks one back up, and one stuck in the ground is gone in five seconds.
 */
public class PrimalArrow extends AbstractArrow {
    private static final EntityDataAccessor<Byte> KIND = SynchedEntityData.defineId(PrimalArrow.class,
            EntityDataSerializers.BYTE);
    /** The original's arrows hit a shade harder than the game's own. */
    public static final double BASE_DAMAGE = 2.1;
    /** How long one lies stuck before it goes. */
    private static final int LIES_FOR = 100;
    /** What the elements leave behind, in ticks. */
    private static final int BURNS = 5;
    private static final int SLOWS_FOR = 200;
    private static final int WEAKENS_FOR = 200;
    private static final int WITHERS_FOR = 100;

    private int lying;

    public PrimalArrow(EntityType<? extends PrimalArrow> type, Level level) {
        super(type, level);
    }

    public PrimalArrow(Level level, LivingEntity shooter, ItemStack ammo, ItemStack weapon, CrystalType kind) {
        super(ModEntities.PRIMAL_ARROW.get(), shooter, level, ammo, weapon);
        entityData.set(KIND, (byte) kind.ordinal());
        setBaseDamage(BASE_DAMAGE * multiplier(kind));
        pickup = Pickup.DISALLOWED;
    }

    /** How much of an arrow's damage each kind does: earth more, order and entropy less. */
    public static double multiplier(CrystalType kind) {
        return switch (kind) {
            case EARTH -> 1.5;
            case ORDER, ENTROPY -> 0.8;
            default -> 1.0;
        };
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(KIND, (byte) 0);
    }

    public CrystalType kind() {
        return CrystalType.values()[Mth.clamp(entityData.get(KIND), 0, CrystalType.ENTROPY.ordinal())];
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(ModItems.PRIMAL_ARROWS.get(kind()).get());
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            me.moonscenty.alchemia.client.EldritchEffects.arrowGlow(level(), getX(), getY(), getZ(),
                    kind().aspect().value().color());
        }
    }

    /** Gone a few seconds after it sticks, rather than a minute. */
    @Override
    protected void tickDespawn() {
        if (++lying >= LIES_FOR) {
            discard();
        }
    }

    /** Hits as the original did: the kind's own damage, then whatever the element leaves behind. */
    @Override
    protected void onHitEntity(EntityHitResult hit) {
        Entity target = hit.getEntity();
        Entity owner = getOwner();
        CrystalType kind = kind();
        int damage = Mth.ceil(Mth.clamp(getDeltaMovement().length() * getBaseDamage(), 0.0, Integer.MAX_VALUE));
        if (isCritArrow()) {
            damage += random.nextInt(damage / 2 + 2);
        }
        DamageSource source = me.moonscenty.alchemia.registry.ModDamageTypes.arrow(level(), kind, this,
                owner == null ? this : owner);
        int burn = isOnFire() && kind != CrystalType.WATER ? BURNS : 0;
        if (kind == CrystalType.FIRE) {
            burn += BURNS;
        }
        if (burn > 0 && !(target instanceof EnderMan)) {
            target.igniteForSeconds(burn);
        }
        if (!target.hurt(source, damage)) {
            // turned aside: it bounces back off
            setDeltaMovement(getDeltaMovement().scale(-0.1));
            setYRot(getYRot() + 180.0F);
            yRotO += 180.0F;
            return;
        }
        if (target instanceof LivingEntity living) {
            switch (kind) {
                case WATER -> living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLOWS_FOR, 4), this);
                case ORDER -> living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, WEAKENS_FOR, 4), this);
                case ENTROPY -> living.addEffect(new MobEffectInstance(MobEffects.WITHER, WITHERS_FOR), this);
                default -> {
                }
            }
            doKnockback(living, source);
            if (kind == CrystalType.EARTH) {
                // earth hits a step harder than the bow alone
                Vec3 push = getDeltaMovement().multiply(1.0, 0.0, 1.0).normalize().scale(0.6);
                if (push.lengthSqr() > 0.0) {
                    living.push(push.x, 0.1, push.z);
                }
            }
            if (level() instanceof ServerLevel server) {
                EnchantmentHelper.doPostAttackEffectsWithItemSource(server, living, source, getWeaponItem());
            }
            doPostHurtEffects(living);
        }
        playSound(SoundEvents.ARROW_HIT, 1.0F, 1.2F / (random.nextFloat() * 0.2F + 0.9F));
        if (!(target instanceof EnderMan)) {
            discard();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("type", entityData.get(KIND));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(KIND, tag.getByte("type"));
    }
}
