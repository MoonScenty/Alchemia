package me.moonscenty.alchemia.wand.spell;

import me.moonscenty.alchemia.entity.FireBat;
import me.moonscenty.alchemia.registry.ModSounds;
import me.moonscenty.alchemia.wand.Casting;
import me.moonscenty.alchemia.wand.FocusUpgrade;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class Hellbat {
    // original's value; further than a wand's reach
    private static final double SEES = 32.0;

    private Hellbat() {
    }

    public static boolean cast(ServerLevel level, LivingEntity caster, ItemStack wand, HitResult aim, int heldFor) {
        LivingEntity target = target(level, caster, aim);
        if (target == null) {
            return false;
        }
        if (target instanceof Player && !level.getServer().isPvpAllowed()) {
            return false;
        }

        // original's spawn point
        Vec3 look = caster.getViewVector(1.0F);
        float yaw = caster.getYRot() * Mth.DEG_TO_RAD;
        double x = caster.getX() - Mth.cos(yaw) * 0.16F + look.x * 0.5;
        double y = caster.getBoundingBox().minY + caster.getBbHeight() / 2.0F + 0.2 + look.y * 0.5;
        double z = caster.getZ() - Mth.sin(yaw) * 0.16F + look.z * 0.5;

        FireBat bat = new FireBat(level);
        bat.moveTo(x, y + bat.getBbHeight(), z, caster.getYRot(), 0.0F);
        bat.summon(target, caster, Casting.worked(wand, FocusUpgrade.POTENCY),
                Casting.has(wand, FocusUpgrade.BAT_BOMBS), Casting.has(wand, FocusUpgrade.DEVIL_BATS),
                Casting.has(wand, FocusUpgrade.VAMPIRE_BATS));
        if (!level.addFreshEntity(bat)) {
            level.playSound(null, caster.getX(), caster.getY(), caster.getZ(), ModSounds.WAND_FAIL.get(),
                    SoundSource.PLAYERS, 0.1F, 0.8F + level.random.nextFloat() * 0.1F);
            return false;
        }
        level.levelEvent(LevelEvent.PARTICLES_MOBBLOCK_SPAWN, bat.blockPosition(), 0);
        level.playSound(null, bat.getX(), bat.getY(), bat.getZ(), ModSounds.ICE.get(), SoundSource.PLAYERS,
                0.2F, 0.95F + level.random.nextFloat() * 0.1F);
        return true;
    }

    private static LivingEntity target(ServerLevel level, LivingEntity caster, HitResult aim) {
        if (aim instanceof EntityHitResult struck && struck.getEntity() instanceof LivingEntity living
                && !(living instanceof FireBat)) {
            return living;
        }
        HitResult seen = Casting.aim(caster, SEES, 1.1, Hellbat::settable);
        return seen instanceof EntityHitResult found && found.getEntity() instanceof LivingEntity living
                ? living : null;
    }

    private static boolean settable(Entity entity) {
        return entity instanceof LivingEntity && entity.isPickable() && !(entity instanceof FireBat)
                && !entity.isSpectator();
    }
}
