package me.moonscenty.alchemia.wand;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;

// server-side only; return false when nothing happened so the cast isn't charged
@FunctionalInterface
public interface Spell {
    boolean cast(ServerLevel level, LivingEntity caster, ItemStack wand, HitResult aim, int heldFor);

    Spell NOTHING = (level, caster, wand, aim, heldFor) -> false;
}
