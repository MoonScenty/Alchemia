package me.moonscenty.alchemia.registry;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModArmorMaterials {
    public static final DeferredRegister<ArmorMaterial> MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, Alchemia.MODID);

    public static final int ALCHEMIUM_LASTS = 25;
    public static final int VOID_LASTS = 10;

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> ALCHEMIUM =
            MATERIALS.register("alchemium", () -> material("alchemium", stops(2, 6, 5, 2), 25,
                    () -> Ingredient.of(ModItems.ALCHEMIUM_INGOT)));

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> VOID =
            MATERIALS.register("void", () -> material("void", stops(3, 7, 6, 3), 10,
                    () -> Ingredient.of(ModItems.VOID_INGOT)));

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> TRAVELLER =
            MATERIALS.register("traveller", () -> new ArmorMaterial(stops(1, 3, 2, 1), 25,
                    SoundEvents.ARMOR_EQUIP_LEATHER, () -> Ingredient.of(net.minecraft.world.item.Items.LEATHER),
                    List.of(new ArmorMaterial.Layer(Alchemia.id("traveller_boots"))), 0.0F, 0.0F));

    // own material: the armour texture is named by the material, so sharing one shares the texture
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> GOGGLES =
            MATERIALS.register("goggles", () -> new ArmorMaterial(stops(1, 3, 2, 1), 25,
                    SoundEvents.ARMOR_EQUIP_IRON, () -> Ingredient.of(ModItems.BRASS_INGOT),
                    List.of(new ArmorMaterial.Layer(Alchemia.id("goggles"))), 0.0F, 0.0F));

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> HARNESS =
            MATERIALS.register("hover_harness", () -> new ArmorMaterial(stops(1, 3, 2, 1), 25,
                    SoundEvents.ARMOR_EQUIP_IRON, () -> Ingredient.of(ModItems.BRASS_INGOT),
                    List.of(new ArmorMaterial.Layer(Alchemia.id("hover_harness"))), 0.0F, 0.0F));

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> CLOTH =
            MATERIALS.register("cloth", () -> new ArmorMaterial(stops(1, 3, 2, 1), 25,
                    SoundEvents.ARMOR_EQUIP_LEATHER, () -> Ingredient.of(ModItems.ENCHANTED_FABRIC),
                    robeLayers("cloth"), 0.0F, 0.0F));

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> VOID_ROBE =
            MATERIALS.register("void_robe", () -> new ArmorMaterial(stops(3, 7, 6, 3), 10,
                    SoundEvents.ARMOR_EQUIP_LEATHER, () -> Ingredient.of(ModItems.VOID_INGOT),
                    robeLayers("void_robe"), 0.0F, 0.0F));

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> FORTRESS =
            MATERIALS.register("fortress", () -> new ArmorMaterial(stops(3, 7, 6, 3), 25,
                    SoundEvents.ARMOR_EQUIP_IRON, () -> Ingredient.of(ModItems.ALCHEMIUM_INGOT),
                    List.of(new ArmorMaterial.Layer(Alchemia.id("fortress"))), 0.0F, 0.0F));

    public static final int FORTRESS_LASTS = 40;

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> CRIMSON_ROBE =
            MATERIALS.register("crimson_robe", () -> material("crimson_robe", stops(2, 6, 5, 2), 9,
                    () -> Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT)));
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> CRIMSON_PLATE =
            MATERIALS.register("crimson_plate", () -> material("crimson_plate", stops(2, 6, 5, 2), 9,
                    () -> Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT)));
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> CRIMSON_PRAETOR =
            MATERIALS.register("crimson_praetor", () -> material("crimson_praetor", stops(3, 8, 6, 3), 20,
                    () -> Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT)));
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> CRIMSON_BOOTS =
            MATERIALS.register("crimson_boots", () -> material("crimson_boots", stops(2, 6, 5, 2), 9,
                    () -> Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT)));

    public static final int CRIMSON_LASTS = 15;
    public static final int PRAETOR_LASTS = 45;

    public static final int CLOTH_LASTS = 25;
    public static final int VOID_ROBE_LASTS = 10;

    private ModArmorMaterials() {
    }

    private static List<ArmorMaterial.Layer> robeLayers(String name) {
        return List.of(new ArmorMaterial.Layer(Alchemia.id(name), "", true),
                new ArmorMaterial.Layer(Alchemia.id(name), "_overlay", false));
    }

    private static Map<ArmorItem.Type, Integer> stops(int head, int chest, int legs, int feet) {
        Map<ArmorItem.Type, Integer> by = new EnumMap<>(ArmorItem.Type.class);
        by.put(ArmorItem.Type.HELMET, head);
        by.put(ArmorItem.Type.CHESTPLATE, chest);
        by.put(ArmorItem.Type.LEGGINGS, legs);
        by.put(ArmorItem.Type.BOOTS, feet);
        // the map must cover every ArmorItem.Type, BODY included
        by.put(ArmorItem.Type.BODY, chest);
        return by;
    }

    private static ArmorMaterial material(String name, Map<ArmorItem.Type, Integer> stops, int enchantable,
            java.util.function.Supplier<Ingredient> mendedWith) {
        return new ArmorMaterial(stops, enchantable, SoundEvents.ARMOR_EQUIP_IRON, mendedWith,
                List.of(new ArmorMaterial.Layer(Alchemia.id(name))), 0.0F, 0.0F);
    }

    public static Holder<ArmorMaterial> of(DeferredHolder<ArmorMaterial, ArmorMaterial> which) {
        return which;
    }
}
