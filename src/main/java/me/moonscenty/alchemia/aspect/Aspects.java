package me.moonscenty.alchemia.aspect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.crafting.CrucibleRecipe;
import me.moonscenty.alchemia.crafting.InfusionRecipe;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

// Data-map aspects first; anything else is traced back through the recipes that make it.
public final class Aspects {
    private static final float CRAFTING_LOSS = 0.75F;
    private static final int MAX_PER_ASPECT = 64;
    private static final int MAX_DEPTH = 32;

    private static final Map<Item, AspectList> CACHE = new ConcurrentHashMap<>();
    private static Map<Item, List<RecipeHolder<?>>> byResult = Map.of();
    private static HolderLookup.Provider registries;

    private Aspects() {
    }

    public static void bind(RecipeManager recipes, HolderLookup.Provider lookup) {
        registries = lookup;
        byResult = indexByResult(recipes, lookup);
        CACHE.clear();
        Alchemia.LOGGER.debug("Aspect lookup bound to {} recipes with results", byResult.size());
    }

    public static AspectList of(ItemStack stack) {
        if (stack.isEmpty()) {
            return AspectList.EMPTY;
        }
        AspectList base = of(stack.getItem());
        if (stack.getItem() instanceof ContainedAspects holder) {
            base = base.add(holder.contained(stack));
        }
        return AspectBonuses.ofStack(stack, base).capAt(MAX_PER_ASPECT).cull();
    }

    public static AspectList of(Item item) {
        return lookup(item, new HashSet<>());
    }

    private static AspectList lookup(Item item, Set<Item> visiting) {
        AspectList known = CACHE.get(item);
        if (known != null) {
            return known;
        }

        AspectList resolved = AspectBonuses.ofItem(item, resolve(item, visiting)).capAt(MAX_PER_ASPECT).cull();
        CACHE.put(item, resolved);
        return resolved;
    }

    public static AspectList of(net.minecraft.world.entity.Entity entity) {
        AspectList kind = of(entity.getType());
        return entity instanceof OwnAspects own ? kind.add(own.ownAspects()) : kind;
    }

    public static AspectList of(EntityType<?> type) {
        AspectList listed = type.builtInRegistryHolder().getData(ModDataMaps.ENTITY_ASPECTS);
        return listed == null ? AspectList.EMPTY : listed;
    }

    private static AspectList resolve(Item item, Set<Item> visiting) {
        AspectList listed = item.builtInRegistryHolder().getData(ModDataMaps.ITEM_ASPECTS);
        if (listed != null) {
            return listed;
        }

        // recipe cycles, and trees too deep to bother with
        if (visiting.size() >= MAX_DEPTH || !visiting.add(item)) {
            return AspectList.EMPTY;
        }

        try {
            for (RecipeHolder<?> holder : byResult.getOrDefault(item, List.of())) {
                AspectList derived = fromRecipe(holder.value(), visiting);
                if (!derived.isEmpty()) {
                    return derived;
                }
            }
            return AspectList.EMPTY;
        } finally {
            visiting.remove(item);
        }
    }

    private static AspectList fromRecipe(Recipe<?> recipe, Set<Item> visiting) {
        ItemStack result = resultOf(recipe);
        if (result.isEmpty()) {
            return AspectList.EMPTY;
        }

        AspectList total = paid(recipe);
        for (Ingredient ingredient : recipe.getIngredients()) {
            if (ingredient.isEmpty()) {
                continue;
            }
            // the first choice of a tag ingredient that has aspects speaks for the rest
            for (ItemStack choice : ingredient.getItems()) {
                AspectList carried = lookup(choice.getItem(), visiting);
                if (!carried.isEmpty()) {
                    total = total.add(carried);
                    break;
                }
            }
        }

        return total.scale(CRAFTING_LOSS / result.getCount());
    }

    // crucible/infusion essentia counts towards the result; arcane vis does not (it goes back to the aura)
    private static AspectList paid(Recipe<?> recipe) {
        if (recipe instanceof CrucibleRecipe boiled) {
            return boiled.aspects();
        }
        if (recipe instanceof InfusionRecipe infused) {
            return infused.essentia();
        }
        return AspectList.EMPTY;
    }

    // essentia recipes first: reversible ones (nuggets -> ingot) would trace back to plain metal
    private static final java.util.Comparator<RecipeHolder<?>> BY_WHAT_IT_SAYS =
            java.util.Comparator.comparingInt(holder -> paid(holder.value()).isEmpty() ? 1 : 0);

    private static ItemStack resultOf(Recipe<?> recipe) {
        try {
            return recipe.getResultItem(registries);
        } catch (Exception failed) {
            // special recipes have no fixed result
            return ItemStack.EMPTY;
        }
    }

    private static Map<Item, List<RecipeHolder<?>>> indexByResult(RecipeManager recipes, HolderLookup.Provider lookup) {
        Map<Item, List<RecipeHolder<?>>> index = new HashMap<>();
        for (RecipeHolder<?> holder : recipes.getRecipes()) {
            ItemStack result;
            try {
                result = holder.value().getResultItem(lookup);
            } catch (Exception failed) {
                continue;
            }
            if (!result.isEmpty()) {
                index.computeIfAbsent(result.getItem(), item -> new ArrayList<>()).add(holder);
            }
        }
        index.values().forEach(made -> made.sort(BY_WHAT_IT_SAYS));
        return index;
    }
}
