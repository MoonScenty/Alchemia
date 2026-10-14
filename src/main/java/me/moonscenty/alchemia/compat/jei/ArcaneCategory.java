package me.moonscenty.alchemia.compat.jei;

import java.util.ArrayList;
import java.util.List;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.crafting.ArcaneRecipe;
import me.moonscenty.alchemia.crafting.ArcaneShapedRecipe;
import me.moonscenty.alchemia.crafting.ArcaneShapelessRecipe;
import me.moonscenty.alchemia.crafting.ArcaneWandRecipe;
import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.wand.WandParts;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

/** The arcane workbench: three by three, what it makes, and what it asks of the wand along the foot. */
public class ArcaneCategory implements IRecipeCategory<RecipeHolder<ArcaneRecipe>> {
    public static final RecipeType<RecipeHolder<ArcaneRecipe>> TYPE =
            RecipeType.createRecipeHolderType(Alchemia.id("arcane"));
    private static final int WIDTH = 120;
    private static final int HEIGHT = 84;
    private static final int COST_Y = 62;

    private final IDrawable icon;
    private final IDrawable slot;

    public ArcaneCategory(IGuiHelper gui) {
        this.icon = gui.createDrawableItemStack(new ItemStack(ModBlocks.ARCANE_WORKBENCH.get()));
        this.slot = gui.getSlotDrawable();
    }

    @Override
    public RecipeType<RecipeHolder<ArcaneRecipe>> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("research.alchemia.arcane_crafting");
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<ArcaneRecipe> holder, IFocusGroup focuses) {
        ArcaneRecipe recipe = holder.value();
        if (recipe instanceof ArcaneWandRecipe) {
            wand(builder);
            return;
        }
        List<Ingredient> ingredients = recipe.getIngredients();
        int width = recipe instanceof ArcaneShapedRecipe shaped ? shaped.pattern().width() : 3;
        if (recipe instanceof ArcaneShapelessRecipe) {
            builder.setShapeless();
        }
        for (int i = 0; i < ingredients.size(); i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, 1 + (i % width) * 18, 1 + (i / width) * 18)
                    .setStandardSlotBackground().addIngredients(ingredients.get(i));
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 95, 19).setStandardSlotBackground()
                .addItemStack(recipe.getResultItem(Minecraft.getInstance().level.registryAccess()));
    }

    /** Every rod with every cap, cycling together. */
    private static void wand(IRecipeLayoutBuilder builder) {
        List<ItemStack> caps = new ArrayList<>();
        List<ItemStack> rods = new ArrayList<>();
        List<ItemStack> wands = new ArrayList<>();
        for (var rodItem : ModItems.WAND_RODS.values()) {
            ItemStack rod = new ItemStack(rodItem.get());
            var rodPart = WandParts.rodOf(rod);
            for (var capItem : ModItems.WAND_CAPS.values()) {
                ItemStack cap = new ItemStack(capItem.get());
                var capPart = WandParts.capOf(cap);
                if (rodPart == null || capPart == null) {
                    continue;
                }
                caps.add(cap);
                rods.add(rod);
                wands.add(WandItem.of(rodPart, capPart));
            }
        }
        IRecipeSlotBuilder upper = builder.addSlot(RecipeIngredientRole.INPUT, 37, 1).setStandardSlotBackground()
                .addItemStacks(caps);
        IRecipeSlotBuilder rod = builder.addSlot(RecipeIngredientRole.INPUT, 19, 19).setStandardSlotBackground()
                .addItemStacks(rods);
        IRecipeSlotBuilder lower = builder.addSlot(RecipeIngredientRole.INPUT, 1, 37).setStandardSlotBackground()
                .addItemStacks(caps);
        IRecipeSlotBuilder made = builder.addSlot(RecipeIngredientRole.OUTPUT, 95, 19).setStandardSlotBackground()
                .addItemStacks(wands);
        builder.createFocusLink(upper, rod, lower, made);
    }

    @Override
    public void draw(RecipeHolder<ArcaneRecipe> holder, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX,
            double mouseY) {
        AspectRow.draw(graphics, holder.value().cost(), 1, COST_Y, 6);
        graphics.drawString(Minecraft.getInstance().font, "→", 72, 23, 0xFF404040, false);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, RecipeHolder<ArcaneRecipe> holder, IRecipeSlotsView slots,
            double mouseX, double mouseY) {
        Component name = AspectRow.under(holder.value().cost(), 1, COST_Y, 6, mouseX, mouseY);
        if (name != null) {
            tooltip.add(name);
        }
    }
}
