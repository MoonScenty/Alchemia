package me.moonscenty.alchemia.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

public final class Looting {
    private Looting() {
    }

    public static int of(DamageSource source) {
        return source.getEntity() instanceof LivingEntity killer ? of(killer) : 0;
    }

    public static int of(LivingEntity killer) {
        return EnchantmentHelper.getEnchantmentLevel(killer.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING), killer);
    }
}
