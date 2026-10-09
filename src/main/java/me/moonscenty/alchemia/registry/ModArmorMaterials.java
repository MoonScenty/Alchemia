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

/**
 * What our two metals are worth as armour.
 * <p>
 * The numbers are the original's. Alchemium stops what iron stops and lasts two-thirds again as long as diamond
 * does, which is what makes it the suit somebody actually wears while they work. Void stops nearly what diamond
 * stops and wears out faster than leather, which makes it the suit somebody wears once, on purpose.
 * <p>
 * Neither is given toughness. The version this was ported from had no such number, and inventing one for a suit
 * would have quietly made it better than the original ever meant it to be.
 */
public final class ModArmorMaterials {
    public static final DeferredRegister<ArmorMaterial> MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, Alchemia.MODID);

    /** How many times over the base a suit of each lasts. Vanilla's iron is fifteen and its diamond thirty-three. */
    public static final int ALCHEMIUM_LASTS = 25;
    public static final int VOID_LASTS = 10;

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> ALCHEMIUM =
            MATERIALS.register("alchemium", () -> material("alchemium", stops(2, 6, 5, 2), 25,
                    () -> Ingredient.of(ModItems.ALCHEMIUM_INGOT)));

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> VOID =
            MATERIALS.register("void", () -> material("void", stops(3, 7, 6, 3), 10,
                    () -> Ingredient.of(ModItems.VOID_INGOT)));

    /**
     * The traveller's boots, which are leather with a band round them and not a metal at all.
     * <p>
     * Leather's own numbers, because that is what they are made of. What is worth having about them is not what
     * they stop. How long they last is set on the item, not here, since the original set it outright rather
     * than as so many times a base.
     */
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> TRAVELLER =
            MATERIALS.register("traveller", () -> new ArmorMaterial(stops(1, 3, 2, 1), 15,
                    SoundEvents.ARMOR_EQUIP_LEATHER, () -> Ingredient.of(net.minecraft.world.item.Items.LEATHER),
                    List.of(new ArmorMaterial.Layer(Alchemia.id("traveller_boots"))), 0.0F, 0.0F));

    /**
     * The goggles, which stop about what a hat stops.
     * <p>
     * A material of their own rather than the boots', because the sheet a piece is drawn from is named here and
     * nowhere else. Two pieces sharing a material would be two pieces sharing a picture, however different the
     * models hung on them are.
     */
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> GOGGLES =
            MATERIALS.register("goggles", () -> new ArmorMaterial(stops(1, 3, 2, 1), 15,
                    SoundEvents.ARMOR_EQUIP_IRON, () -> Ingredient.of(ModItems.BRASS_INGOT),
                    List.of(new ArmorMaterial.Layer(Alchemia.id("goggles"))), 0.0F, 0.0F));

    /**
     * The two robes, which are worn meshes rather than sheets stretched over a body.
     * <p>
     * Both are given **no layers at all**. The layer that draws armour walks a material's layers and draws a
     * sheet for each; with none, it draws nothing, and the robe is left to the layer of our own that knows how
     * to hang a mesh off a limb. Anything less and every robe would be drawn twice.
     * <p>
     * Cloth takes leather's numbers because that is what it is. The void robe takes void metal's, which is what
     * the original gave it -- a robe that stops as much as a breastplate and falls apart as fast.
     */
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> CLOTH =
            MATERIALS.register("cloth", () -> new ArmorMaterial(stops(1, 3, 2, 1), 25,
                    SoundEvents.ARMOR_EQUIP_LEATHER, () -> Ingredient.of(net.minecraft.world.item.Items.LEATHER),
                    List.of(), 0.0F, 0.0F));

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> VOID_ROBE =
            MATERIALS.register("void_robe", () -> new ArmorMaterial(stops(3, 7, 6, 3), 10,
                    SoundEvents.ARMOR_EQUIP_LEATHER, () -> Ingredient.of(ModItems.VOID_INGOT),
                    List.of(), 0.0F, 0.0F));

    /**
     * Fortress armour: as much stopped as void metal, as long-lasting as alchemium, and better at taking an
     * enchantment than anything else in the mod. Nothing in a single metal manages all three, which is the point
     * of building it on an altar out of a suit that was already finished.
     * <p>
     * One layer, drawn as usual. With the original's jar its sheet is the original's and the model it is drawn on
     * is read out of the original's code; without, both are plain armour.
     */
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> FORTRESS =
            MATERIALS.register("fortress", () -> new ArmorMaterial(stops(3, 7, 6, 3), 40,
                    SoundEvents.ARMOR_EQUIP_IRON, () -> Ingredient.of(ModItems.ALCHEMIUM_INGOT),
                    List.of(new ArmorMaterial.Layer(Alchemia.id("fortress"))), 0.0F, 0.0F));

    public static final int FORTRESS_LASTS = 25;

    /** How many times over the base each robe lasts. */
    public static final int CLOTH_LASTS = 25;
    public static final int VOID_ROBE_LASTS = 10;

    private ModArmorMaterials() {
    }

    /** What each piece takes off a blow, given head first. */
    private static Map<ArmorItem.Type, Integer> stops(int head, int chest, int legs, int feet) {
        Map<ArmorItem.Type, Integer> by = new EnumMap<>(ArmorItem.Type.class);
        by.put(ArmorItem.Type.HELMET, head);
        by.put(ArmorItem.Type.CHESTPLATE, chest);
        by.put(ArmorItem.Type.LEGGINGS, legs);
        by.put(ArmorItem.Type.BOOTS, feet);
        // a horse cannot wear either of these, but the map has to answer for every kind of piece there is
        by.put(ArmorItem.Type.BODY, chest);
        return by;
    }

    private static ArmorMaterial material(String name, Map<ArmorItem.Type, Integer> stops, int enchantable,
            java.util.function.Supplier<Ingredient> mendedWith) {
        return new ArmorMaterial(stops, enchantable, SoundEvents.ARMOR_EQUIP_IRON, mendedWith,
                List.of(new ArmorMaterial.Layer(Alchemia.id(name))), 0.0F, 0.0F);
    }

    /** The holder an armour piece is built from. */
    public static Holder<ArmorMaterial> of(DeferredHolder<ArmorMaterial, ArmorMaterial> which) {
        return which;
    }
}
