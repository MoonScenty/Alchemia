package me.moonscenty.alchemia.item;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.CrystalType;
import me.moonscenty.alchemia.entity.PrimalArrow;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingGetProjectileEvent;

@EventBusSubscriber(modid = Alchemia.MODID)
public class PrimalArrowItem extends ArrowItem {
    private static final float INFINITY_SAVES = 0.33F;
    private final CrystalType kind;

    public PrimalArrowItem(CrystalType kind, Properties properties) {
        super(properties);
        this.kind = kind;
    }

    public CrystalType kind() {
        return kind;
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack ammo, LivingEntity shooter, ItemStack weapon) {
        return new PrimalArrow(level, shooter, ammo.copyWithCount(1), weapon, kind);
    }

    @Override
    public boolean isInfinite(ItemStack ammo, ItemStack bow, LivingEntity shooter) {
        var infinity = shooter.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(Enchantments.INFINITY);
        return EnchantmentHelper.getItemEnchantmentLevel(infinity, bow) > 0
                && shooter.getRandom().nextFloat() < INFINITY_SAVES;
    }

    @SubscribeEvent
    public static void nock(LivingGetProjectileEvent event) {
        if (!(event.getProjectileWeaponItemStack().getItem() instanceof BowItem)
                || !(event.getEntity() instanceof Player player)) {
            return;
        }
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof PrimalArrowItem) {
                event.setProjectileItemStack(stack);
                return;
            }
        }
    }
}
