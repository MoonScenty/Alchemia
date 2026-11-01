package me.moonscenty.alchemia.wand.spell;

import me.moonscenty.alchemia.registry.ModSounds;
import me.moonscenty.alchemia.wand.Casting;
import me.moonscenty.alchemia.wand.FocusUpgrade;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public final class Thrown {
    // speed and inaccuracy
    private static final float FLIES = 1.5F;
    private static final float TRUE = 0.0F;

    // embers per tick and spray angle in degrees
    private static final int MOTES = 2;
    private static final float SCATTERS = 15.0F;
    // firebeam: spread multiplier, damage bonus and multiplier, ember lifetime
    private static final float NARROW = 0.25F;
    private static final float SHARPER = 0.5F;
    private static final float HOTTER = 1.5F;
    private static final int REACHES = 30;
    private static final float EMBER = 2.0F;
    private static final int ROARS_EVERY = 10;

    // scattershot: base splinters (plus two per rank) and damage each
    private static final int SPLINTERS = 5;
    private static final float SPLINTER = 1.0F;
    // ice spike and boulder damage, and what each rank of potency adds
    private static final float SPIKE = 3.0F;
    private static final float SHARPENS_SPIKE = 1.5F;
    private static final float BOULDER = 4.0F;
    private static final float SHARPENS_BOULDER = 2.0F;
    private static final int WALLS = 6;

    // vis shard damage, and what each rank of potency adds
    private static final float SLIVER = 1.0F;
    private static final float SHARPENS_SLIVER = 0.5F;

    private Thrown() {
    }

    public static boolean fire(ServerLevel level, LivingEntity caster, ItemStack wand, HitResult aim, int heldFor) {
        int potency = Casting.worked(wand, FocusUpgrade.POTENCY);
        int alchemists = Casting.worked(wand, FocusUpgrade.ALCHEMISTS_FIRE);
        if (Casting.has(wand, FocusUpgrade.FIREBALL)) {
            FireOrb ball = new FireOrb(level, caster)
                    .bursting(1.0F + potency * FireOrb.HARDER, alchemists > 0);
            return let(level, caster, ball, SoundEvents.BLAZE_SHOOT, 2.0F,
                    1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F);
        }

        boolean beam = Casting.has(wand, FocusUpgrade.FIREBEAM);
        float hurts = EMBER + potency;
        if (beam) {
            hurts = (hurts + SHARPER) * HOTTER;
        }
        for (int mote = 0; mote < MOTES + potency; mote++) {
            Ember ember = new Ember(level, caster).burning(hurts, alchemists);
            if (beam) {
                ember.lasting(REACHES);
            }
            ember.shootFromRotation(caster, caster.getXRot(), caster.getYRot(), 0.0F, FLIES,
                    beam ? NARROW : SCATTERS);
            level.addFreshEntity(ember);
        }
        if (heldFor % ROARS_EVERY == 0) {
            level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                    ModSounds.FIRE_LOOP.get(), SoundSource.PLAYERS, 0.33F, 2.0F);
        }
        return true;
    }

    public static boolean frost(ServerLevel level, LivingEntity caster, ItemStack wand, HitResult aim, int heldFor) {
        int potency = Casting.worked(wand, FocusUpgrade.POTENCY);
        int chills = Casting.worked(wand, FocusUpgrade.ALCHEMISTS_FROST);

        if (Casting.has(wand, FocusUpgrade.SCATTERSHOT)) {
            for (int shard = 0; shard < SPLINTERS + potency * 2; shard++) {
                FrostShard splinter = new FrostShard(level, caster)
                        .biting(SPLINTER, chills).asSplinter();
                splinter.shootFromRotation(caster, caster.getXRot(), caster.getYRot(), 0.0F, FLIES, 8.0F);
                level.addFreshEntity(splinter);
            }
            level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                    ModSounds.ICE.get(), SoundSource.PLAYERS, 0.4F, 1.0F + level.random.nextFloat() * 0.1F);
            return true;
        }
        if (Casting.has(wand, FocusUpgrade.ICE_BOULDER)) {
            return let(level, caster, new FrostShard(level, caster)
                    .biting(BOULDER + potency * SHARPENS_BOULDER, chills).bouncing(WALLS),
                    ModSounds.ICE.get(), 0.4F, 1.0F + level.random.nextFloat() * 0.1F);
        }
        return let(level, caster, new FrostShard(level, caster)
                .biting(SPIKE + potency * SHARPENS_SPIKE, chills),
                ModSounds.ICE.get(), 0.4F, 1.0F + level.random.nextFloat() * 0.1F);
    }

    public static boolean visShard(ServerLevel level, LivingEntity caster, ItemStack wand, HitResult aim,
            int heldFor) {
        if (!(aim instanceof EntityHitResult struck) || !(struck.getEntity() instanceof LivingEntity mark)) {
            return false;
        }
        int potency = Casting.worked(wand, FocusUpgrade.POTENCY);
        VisShard shard = new VisShard(level, caster, mark)
                .chasing(SLIVER + potency * SHARPENS_SLIVER, Casting.has(wand, FocusUpgrade.PERSISTENT));
        return let(level, caster, shard, ModSounds.SCAN.get(), 0.3F, 1.1F + level.random.nextFloat() * 0.1F);
    }

    public static boolean primal(ServerLevel level, LivingEntity caster, ItemStack wand, HitResult aim,
            int heldFor) {
        PrimalOrb orb = new PrimalOrb(level, caster);
        if (Casting.has(wand, FocusUpgrade.SEEKER)) {
            orb.seeking();
        }
        return let(level, caster, orb, ModSounds.ICE.get(), 0.3F, 0.8F + level.random.nextFloat() * 0.1F);
    }

    public static boolean pech(ServerLevel level, LivingEntity caster, ItemStack wand, HitResult aim, int heldFor) {
        PechBlast blast = new PechBlast(level, caster).cursing(
                Casting.worked(wand, FocusUpgrade.POTENCY),
                Casting.worked(wand, FocusUpgrade.EXTEND),
                Casting.has(wand, FocusUpgrade.NIGHTSHADE));
        return let(level, caster, blast, ModSounds.ICE.get(), 0.4F, 1.0F + level.random.nextFloat() * 0.1F);
    }

    public static boolean grapple(ServerLevel level, LivingEntity caster, ItemStack wand, HitResult aim,
            int heldFor) {
        Grapple hook = new Grapple(level, caster).hauling(
                Casting.worked(wand, FocusUpgrade.POTENCY),
                Casting.has(wand, FocusUpgrade.STICKY));
        return let(level, caster, hook, ModSounds.ICE.get(), 0.3F, 0.8F + level.random.nextFloat() * 0.1F);
    }

    private static boolean let(ServerLevel level, LivingEntity caster, ThrowableProjectile thrown, SoundEvent sound,
            float volume, float pitch) {
        thrown.shootFromRotation(caster, caster.getXRot(), caster.getYRot(), 0.0F, FLIES, TRUE);
        level.addFreshEntity(thrown);
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                sound, SoundSource.PLAYERS, volume, pitch);
        return true;
    }
}
