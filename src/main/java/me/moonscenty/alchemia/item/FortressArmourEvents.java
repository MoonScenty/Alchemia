package me.moonscenty.alchemia.item;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.living.ArmorHurtEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

// each piece worn turns aside a further share of what vanilla armour let through; values are tuned
// so a full suit matches the original's, since the vanilla armour maths can't be replaced
@EventBusSubscriber(modid = Alchemia.MODID)
public final class FortressArmourEvents {
    // share turned per piece worn: fire and explosions, other physical damage, magic
    public static final float TURNS_BURNING = 0.125F;
    public static final float TURNS_BLOWS = 0.0625F;
    public static final float TURNS_WORKINGS = 0.03125F;

    // a mask counts as this share of a piece; original's value
    public static final float A_MASK = 0.125F;

    private FortressArmourEvents() {
    }

    public static int worn(LivingEntity wearer) {
        int pieces = 0;
        for (EquipmentSlot slot : Worn.ARMOUR) {
            if (wearer.getItemBySlot(slot).getItem() instanceof FortressArmorItem) {
                pieces++;
            }
        }
        return pieces;
    }

    public static float turnedAside(LivingEntity wearer, DamageSource source) {
        float pieces = worn(wearer);
        for (EquipmentSlot slot : Worn.ARMOUR) {
            if (FortressArmorItem.fitting(wearer.getItemBySlot(slot)).map(HelmFitting::isMask).orElse(false)) {
                pieces += A_MASK;
            }
        }
        return pieces * turns(source);
    }

    public static float turns(DamageSource source) {
        if (source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_EXPLOSION)) {
            return TURNS_BURNING;
        }
        return source.is(Tags.DamageTypes.IS_MAGIC) ? TURNS_WORKINGS : TURNS_BLOWS;
    }

    @SubscribeEvent
    public static void turnsBlows(LivingDamageEvent.Pre event) {
        if (event.getSource().is(DamageTypeTags.BYPASSES_ARMOR)) {
            return;
        }
        float turned = turnedAside(event.getEntity(), event.getSource());
        if (turned <= 0.0F) {
            return;
        }
        event.setNewDamage(event.getNewDamage() * (1.0F - turned));
    }

    @SubscribeEvent
    public static void fallingDoesNotWearIt(ArmorHurtEvent event) {
        if (!event.getDamageSource().is(DamageTypeTags.IS_FALL)) {
            return;
        }
        event.getArmorMap().forEach((slot, entry) -> {
            ItemStack piece = entry.armorItemStack;
            if (piece.getItem() instanceof FortressArmorItem) {
                event.setNewDamage(slot, 0.0F);
            }
        });
    }
}
