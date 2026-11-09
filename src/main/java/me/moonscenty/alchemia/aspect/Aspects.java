package me.moonscenty.alchemia.aspect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.crafting.ArcaneRecipe;
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
import net.minecraft.world.item.crafting.RecipeType;

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
            return fromRecipes(byResult.getOrDefault(item, List.of()), visiting);
        } finally {
            visiting.remove(item);
        }
    }

    // original's order: the crucible's word, then the arcane table, the altar, and last the cheapest plain recipe
    private static AspectList fromRecipes(List<RecipeHolder<?>> recipes, Set<Item> visiting) {
        for (RecipeHolder<?> holder : recipes) {
            if (holder.value() instanceof CrucibleRecipe boiled && boiled.catalyst().getItems().length > 0) {
                return lookup(boiled.catalyst().getItems()[0].getItem(), visiting)
                        .add(rooted(boiled.aspects(), boiled.result().getCount())).cull();
            }
        }
        AspectList arcane = null;
        for (RecipeHolder<?> holder : recipes) {
            if (holder.value() instanceof ArcaneRecipe recipe) {
                int makes = resultOf(recipe).getCount();
                arcane = fromIngredients(recipe, makes, false, visiting).add(rooted(recipe.cost(), makes));
            }
        }
        if (arcane != null) {
            return arcane.cull();
        }
        for (RecipeHolder<?> holder : recipes) {
            if (holder.value() instanceof InfusionRecipe infused) {
                int makes = infused.result().getCount();
                return fromIngredients(infused, makes, true, visiting).add(rooted(infused.essentia(), makes)).cull();
            }
        }
        AspectList cheapest = AspectList.EMPTY;
        for (RecipeHolder<?> holder : recipes) {
            if (holder.value().getType() == RecipeType.CRAFTING) {
                AspectList made = fromIngredients(holder.value(), resultOf(holder.value()).getCount(), false, visiting);
                if (made.total() > 0 && (cheapest.isEmpty() || made.total() < cheapest.total())) {
                    cheapest = made;
                }
            }
        }
        return cheapest;
    }

    private static AspectList rooted(AspectList paid, int makes) {
        AspectList out = AspectList.EMPTY;
        for (var aspect : paid.sortedByName()) {
            out = out.add(aspect, (int) (Math.sqrt(paid.get(aspect)) / makes));
        }
        return out;
    }

    private static AspectList fromIngredients(Recipe<?> recipe, int makes, boolean cheapestChoice, Set<Item> visiting) {
        AspectList total = AspectList.EMPTY;
        for (Ingredient ingredient : recipe.getIngredients()) {
            if (!ingredient.isEmpty()) {
                total = total.add(carried(ingredient, cheapestChoice, visiting));
            }
        }
        return total.scale(CRAFTING_LOSS / makes);
    }

    // a tag ingredient speaks through its first choice with aspects, or at the altar its cheapest
    private static AspectList carried(Ingredient ingredient, boolean cheapestChoice, Set<Item> visiting) {
        AspectList picked = AspectList.EMPTY;
        for (ItemStack choice : ingredient.getItems()) {
            AspectList carried = lookup(choice.getItem(), visiting);
            if (!carried.isEmpty() && (picked.isEmpty() || carried.total() < picked.total())) {
                picked = carried;
                if (!cheapestChoice) {
                    break;
                }
            }
        }
        return picked;
    }

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
        index.values().forEach(made -> made.sort(java.util.Comparator.comparing(holder -> holder.id().toString())));
        return index;
    }
}
