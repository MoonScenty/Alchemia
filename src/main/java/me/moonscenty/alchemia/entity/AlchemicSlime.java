package me.moonscenty.alchemia.entity;

import me.moonscenty.alchemia.registry.ModEntities;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

/**
 * An alchemic slime: a slime of flux, purple and wet.
 *
 * <p>It is what congeals out of a puddle of flux goo, what the taint makes of something it kills that is not an
 * animal, and it grows by wading through more goo. Bigger than two, it spits pieces of itself at a player more than
 * four blocks off, every five seconds, shrinking by one for each; those pieces land as little slimes of their own.
 * Killed, it bursts into as many little slimes as it was big.
 */
public class AlchemicSlime extends Slime implements TaintedMob {
    /** How long between spits, and how long a spat piece reaches further than its size. The original's. */
    private static final int SPITS_EVERY = 100;
    private static final int FLUNG_FOR = 10;
    private static final double LOOKS = 16.0;

    private int spitIn = SPITS_EVERY;
    private int flung;

    public AlchemicSlime(EntityType<? extends AlchemicSlime> type, Level level) {
        super(type, level);
    }

    public AlchemicSlime(Level level) {
        this(ModEntities.ALCHEMIC_SLIME.get(), level);
    }

    /** A piece of a slime, spat from it at what it is after. */
    public AlchemicSlime(Level level, LivingEntity from, LivingEntity at) {
        this(level);
        setSize(1, true);
        flung = FLUNG_FOR;
        double y = (from.getBoundingBox().minY + from.getBoundingBox().maxY) / 2.0;
        double dx = at.getX() - from.getX();
        double dy = at.getBoundingBox().minY + at.getBbHeight() / 3.0F - y;
        double dz = at.getZ() - from.getZ();
        double across = Math.sqrt(dx * dx + dz * dz);
        if (across < 1.0E-7) {
            moveTo(from.getX(), y, from.getZ(), 0.0F, 0.0F);
            return;
        }
        moveTo(from.getX() + dx / across, y, from.getZ() + dz / across,
                (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F,
                (float) -(Mth.atan2(dy, across) * Mth.RAD_TO_DEG));
        double up = across * 0.2;
        double length = Math.sqrt(dx * dx + (dy + up) * (dy + up) + dz * dz);
        setDeltaMovement((dx / length + random.nextGaussian() * 0.0075) * 1.5,
                ((dy + up) / length + random.nextGaussian() * 0.0075) * 1.5,
                (dz / length + random.nextGaussian() * 0.0075) * 1.5);
    }

    /** A slime's size as a slime's, and worth two more in experience than it is big. */
    @Override
    public void setSize(int size, boolean resetHealth) {
        super.setSize(size, resetHealth);
        xpReward = size + 2;
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !TaintedMob.allied(target) && super.canAttack(target);
    }

    @Override
    public boolean isAlliedTo(Entity other) {
        return TaintedMob.allied(other) || super.isAlliedTo(other);
    }

    @Override
    public void tick() {
        super.tick();
        if (flung > 0) {
            flung--;
            if (level().isClientSide) {
                me.moonscenty.alchemia.client.TaintEffects.slimeSplash(this, getSize(), getSize() * (flung + 1));
            }
        }
        if (level() instanceof ServerLevel served && isAlive()) {
            spit(served);
        }
    }

    /** At a player more than four blocks off, every five seconds, while it is bigger than two. */
    private void spit(ServerLevel level) {
        Player player = level.getNearestPlayer(this, LOOKS);
        if (player == null) {
            return;
        }
        if (spitIn > 0) {
            spitIn--;
        }
        lookAt(player, 10.0F, 20.0F);
        if (distanceTo(player) > 4.0F && spitIn <= 0 && getSize() > 2) {
            spitIn = SPITS_EVERY + 1;
            level.addFreshEntity(new AlchemicSlime(level, this, player));
            playSound(ModSounds.GORE.get(), 1.0F, ((random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F) * 0.8F);
            setSize(getSize() - 1, true);
        }
    }

    /** Even the smallest bites, a point harder than it is big. */
    @Override
    protected boolean isDealsDamage() {
        return isEffectiveAi();
    }

    @Override
    protected float getAttackDamage() {
        return getSize() + 1;
    }

    /** As a slime bites, reaching further while it is still flying from a spit. */
    @Override
    protected void dealDamage(LivingEntity target) {
        if (!isAlive()) {
            return;
        }
        int reach = getSize() + (flung > 0 ? 2 : 0);
        DamageSource source = damageSources().mobAttack(this);
        if (distanceToSqr(target) < 0.6 * reach * 0.6 * reach && hasLineOfSight(target)
                && target.hurt(source, getAttackDamage())) {
            playSound(SoundEvents.SLIME_ATTACK, 1.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
            if (level() instanceof ServerLevel served) {
                EnchantmentHelper.doPostAttackEffects(served, target, source);
            }
        }
    }

    /** What it splashes as it lands: its own goo. */
    @Override
    protected ParticleOptions getParticleType() {
        return new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(ModItems.TAINTED_GOO.get()));
    }

    /**
     * Killed, it bursts into as many little slimes as it was big, laid out in pairs, rather than splitting in halves
     * as a slime does.
     */
    @Override
    public void remove(RemovalReason reason) {
        int size = getSize();
        if (!level().isClientSide && size > 1 && isDeadOrDying()) {
            for (int piece = 0; piece < size; piece++) {
                float across = (piece % 2 - 0.5F) * size / 4.0F;
                float along = (piece / 2 - 0.5F) * size / 4.0F;
                AlchemicSlime little = new AlchemicSlime(level());
                little.setSize(1, true);
                little.moveTo(getX() + across, getY() + 0.5, getZ() + along, random.nextFloat() * 360.0F, 0.0F);
                level().addFreshEntity(little);
            }
            // a slime splits itself on the way out; this one has already burst
            setSize(1, false);
        }
        super.remove(reason);
    }

    /** Bigger than one, a little goo; more with looting. */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        if (getSize() <= 1) {
            return;
        }
        int looting = source.getEntity() instanceof LivingEntity killer
                ? EnchantmentHelper.getEnchantmentLevel(level.registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                        .getHolderOrThrow(Enchantments.LOOTING), killer)
                : 0;
        int goo = random.nextInt(3) + (looting > 0 ? random.nextInt(looting + 1) : 0);
        for (int bit = 0; bit < goo; bit++) {
            spawnAtLocation(new ItemStack(ModItems.TAINTED_GOO.get()));
        }
    }

    /** It is never left in the world of itself; it is made. */
    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level,
            net.minecraft.world.entity.MobSpawnType reason) {
        return reason != net.minecraft.world.entity.MobSpawnType.NATURAL && super.checkSpawnRules(level, reason);
    }
}
