package me.moonscenty.alchemia.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Mob;

/** Saving where a mob keeps to, which the game itself does not save. */
public final class Home {
    private Home() {
    }

    public static void save(Mob mob, CompoundTag tag) {
        if (mob.hasRestriction()) {
            BlockPos home = mob.getRestrictCenter();
            tag.putInt("HomeD", (int) mob.getRestrictRadius());
            tag.putInt("HomeX", home.getX());
            tag.putInt("HomeY", home.getY());
            tag.putInt("HomeZ", home.getZ());
        }
    }

    public static void load(Mob mob, CompoundTag tag) {
        if (tag.contains("HomeD")) {
            mob.restrictTo(new BlockPos(tag.getInt("HomeX"), tag.getInt("HomeY"), tag.getInt("HomeZ")),
                    tag.getInt("HomeD"));
        }
    }
}
