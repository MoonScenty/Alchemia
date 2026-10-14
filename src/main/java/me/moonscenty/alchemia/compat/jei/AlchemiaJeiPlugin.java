package me.moonscenty.alchemia.compat.jei;

import java.util.List;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.crafting.ModRecipes;
import me.moonscenty.alchemia.menu.ArcaneWorkbenchMenu;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModMenus;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;

/**
 * What JEI is told: the arcane workbench, the crucible and the infusion altar, each with its recipes, the block it
 * is done at, and, for the workbench, filling its grid from the bag. JEI is not needed to play; this is only read
 * when it is there.
 */
@JeiPlugin
public class AlchemiaJeiPlugin implements IModPlugin {
    private static final ResourceLocation ID = Alchemia.id("jei");

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper gui = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new ArcaneCategory(gui), new CrucibleCategory(gui), new InfusionCategory(gui));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipes = Minecraft.getInstance().level.getRecipeManager();
        registration.addRecipes(ArcaneCategory.TYPE, List.copyOf(recipes.getAllRecipesFor(ModRecipes.ARCANE.get())));
        registration.addRecipes(CrucibleCategory.TYPE,
                List.copyOf(recipes.getAllRecipesFor(ModRecipes.CRUCIBLE.get())));
        registration.addRecipes(InfusionCategory.TYPE,
                List.copyOf(recipes.getAllRecipesFor(ModRecipes.INFUSION.get())));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ARCANE_WORKBENCH.get()), ArcaneCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.CRUCIBLE.get()), CrucibleCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.INFUSION_MATRIX.get()), InfusionCategory.TYPE);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(ArcaneWorkbenchMenu.class, ModMenus.ARCANE_WORKBENCH.get(),
                ArcaneCategory.TYPE, ArcaneWorkbenchMenu.FIRST_GRID, 9, ArcaneWorkbenchMenu.SLOT_WAND + 1, 36);
    }
}
