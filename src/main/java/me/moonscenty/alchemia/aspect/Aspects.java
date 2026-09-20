package me.moonscenty.alchemia.aspect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

/**
 * Works out what aspects a thing is made of.
 * <p>
 * Anything listed in the aspect data map answers for itself. Everything else is traced back through the recipes that
 * make it: the aspects of the ingredients are added up, then thinned out, since crafting always loses a little. That
 * way a modded item nobody has written down still ends up with sensible aspects.
 */
public final class Aspects {
    /** Crafting is lossy, so a result never quite carries the whole of what went into it. */
    private static final float CRAFTING_LOSS = 0.75F;
    private static final int MAX_PER_ASPECT = 64;
    /** How far back through recipes to trace before giving up. */
    private static final int MAX_DEPTH = 32;

    private static final Map<Item, AspectList> CACHE = new ConcurrentHashMap<>();
    private static Map<Item, List<RecipeHolder<?>>> byResult = Map.of();
    private static HolderLookup.Provider registries;

    private Aspects() {
    }

    /** Points the lookup at a fresh set of recipes and throws away everything worked out from the old ones. */
    public static void bind(RecipeManager recipes, HolderLookup.Provider lookup) {
        registries = lookup;
        byResult = indexByResult(recipes, lookup);
        CACHE.clear();
        Alchemia.LOGGER.debug("Aspect lookup bound to {} recipes with results", byResult.size());
    }

    /** What this particular stack is made of, enchantments and all. */
    public static AspectList of(ItemStack stack) {
        if (stack.isEmpty()) {
            return AspectList.EMPTY;
        }
        return AspectBonuses.ofStack(stack, of(stack.getItem())).capAt(MAX_PER_ASPECT).cull();
    }

    /** What a plain, unenchanted one of these is made of. */
    public static AspectList of(Item item) {
        AspectList known = CACHE.get(item);
        if (known != null) {
            return known;
        }

        AspectList resolved = lookup(item, new HashSet<>());
        CACHE.put(item, resolved);
        return resolved;
    }

    /** Resolves an item and everything it earns for being the kind of thing it is. */
    private static AspectList lookup(Item item, Set<Item> visiting) {
        AspectList known = CACHE.get(item);
        if (known != null) {
            return known;
        }

        AspectList resolved = AspectBonuses.ofItem(item, resolve(item, visiting)).capAt(MAX_PER_ASPECT).cull();
        CACHE.put(item, resolved);
        return resolved;
    }

    /** Mobs are always written down; there is no recipe to trace one back through. */
    public static AspectList of(EntityType<?> type) {
        AspectList listed = type.builtInRegistryHolder().getData(ModDataMaps.ENTITY_ASPECTS);
        return listed == null ? AspectList.EMPTY : listed;
    }

    private static AspectList resolve(Item item, Set<Item> visiting) {
        AspectList listed = item.builtInRegistryHolder().getData(ModDataMaps.ITEM_ASPECTS);
        if (listed != null) {
            return listed;
        }

        // an item cannot be part of its own recipe tree, and a very deep tree is not worth following
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

        AspectList total = AspectList.EMPTY;
        for (Ingredient ingredient : recipe.getIngredients()) {
            if (ingredient.isEmpty()) {
                continue;
            }
            // a tag ingredient stands for many items; the first one that carries aspects speaks for the rest
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

    private static ItemStack resultOf(Recipe<?> recipe) {
        try {
            return recipe.getResultItem(registries);
        } catch (Exception failed) {
            // special recipes build their result from the grid and have nothing to offer here
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
        return index;
    }
}
