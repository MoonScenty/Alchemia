package me.moonscenty.alchemia.registry;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.CrystalType;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/**
 * The mod's own ways of being hurt: the original's air, fire and order arrows, each its own kind of harm. The air
 * and order arrows go through armour and count as magic; the fire arrow is fire. The other three primal arrows hurt
 * as an arrow does.
 */
public final class ModDamageTypes {
    public static final ResourceKey<DamageType> AIR_ARROW = key("air_arrow");
    public static final ResourceKey<DamageType> FIRE_ARROW = key("fire_arrow");
    public static final ResourceKey<DamageType> ORDER_ARROW = key("order_arrow");

    private ModDamageTypes() {
    }

    private static ResourceKey<DamageType> key(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, Alchemia.id(name));
    }

    public static void bootstrap(BootstrapContext<DamageType> context) {
        context.register(AIR_ARROW, new DamageType("alchemia.air_arrow", 0.1F));
        context.register(FIRE_ARROW, new DamageType("alchemia.fire_arrow", 0.1F));
        context.register(ORDER_ARROW, new DamageType("alchemia.order_arrow", 0.1F));
    }

    /** What a primal arrow of this kind hurts with. */
    public static DamageSource arrow(Level level, CrystalType kind, Entity arrow, Entity shooter) {
        ResourceKey<DamageType> type = switch (kind) {
            case AIR -> AIR_ARROW;
            case FIRE -> FIRE_ARROW;
            case ORDER -> ORDER_ARROW;
            default -> null;
        };
        if (type == null) {
            return level.damageSources().arrow((net.minecraft.world.entity.projectile.AbstractArrow) arrow, shooter);
        }
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(type),
                arrow, shooter);
    }
}
