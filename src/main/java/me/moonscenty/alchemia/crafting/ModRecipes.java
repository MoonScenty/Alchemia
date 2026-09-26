package me.moonscenty.alchemia.crafting;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The one recipe type an arcane workbench looks up, and the two ways of writing one down.
 * <p>
 * Shaped and shapeless share a type so the bench asks the manager once and gets both back.
 */
public class ModRecipes {
    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, Alchemia.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, Alchemia.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<ArcaneRecipe>> ARCANE =
            TYPES.register("arcane", () -> RecipeType.<ArcaneRecipe>simple(Alchemia.id("arcane")));

    public static final DeferredHolder<RecipeSerializer<?>, ArcaneShapedRecipe.Serializer> ARCANE_SHAPED =
            SERIALIZERS.register("arcane_shaped", ArcaneShapedRecipe.Serializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, ArcaneShapelessRecipe.Serializer> ARCANE_SHAPELESS =
            SERIALIZERS.register("arcane_shapeless", ArcaneShapelessRecipe.Serializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, ArcaneWandRecipe.Serializer> ARCANE_WAND =
            SERIALIZERS.register("arcane_wand", ArcaneWandRecipe.Serializer::new);

    private ModRecipes() {
    }
}
