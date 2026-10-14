package me.moonscenty.alchemia.compat.jei;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.crafting.CrucibleRecipe;
import me.moonscenty.alchemia.registry.ModBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
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
import net.minecraft.world.item.crafting.RecipeHolder;

/** The crucible: the thing dropped in, what must be dissolved there first, and what comes of it. */
public class CrucibleCategory implements IRecipeCategory<RecipeHolder<CrucibleRecipe>> {
    public static final RecipeType<RecipeHolder<CrucibleRecipe>> TYPE =
            RecipeType.createRecipeHolderType(Alchemia.id("crucible"));
    private static final int WIDTH = 120;
    private static final int HEIGHT = 64;
    private static final int ASPECTS_Y = 24;

    private final IDrawable icon;

    public CrucibleCategory(IGuiHelper gui) {
        this.icon = gui.createDrawableItemStack(new ItemStack(ModBlocks.CRUCIBLE.get()));
    }

    @Override
    public RecipeType<RecipeHolder<CrucibleRecipe>> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("research.alchemia.in_crucible");
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
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<CrucibleRecipe> holder, IFocusGroup focuses) {
        CrucibleRecipe recipe = holder.value();
        builder.addSlot(RecipeIngredientRole.CATALYST, 1, 1).setStandardSlotBackground()
                .addIngredients(recipe.catalyst());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 95, 1).setStandardSlotBackground()
                .addItemStack(recipe.getResultItem(Minecraft.getInstance().level.registryAccess()));
    }

    @Override
    public void draw(RecipeHolder<CrucibleRecipe> holder, IRecipeSlotsView slots, GuiGraphics graphics,
            double mouseX, double mouseY) {
        AspectRow.draw(graphics, holder.value().aspects(), 1, ASPECTS_Y, 6);
        graphics.drawString(Minecraft.getInstance().font, "+", 28, 5, 0xFF404040, false);
        graphics.drawString(Minecraft.getInstance().font, "→", 72, 5, 0xFF404040, false);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, RecipeHolder<CrucibleRecipe> holder, IRecipeSlotsView slots,
            double mouseX, double mouseY) {
        Component name = AspectRow.under(holder.value().aspects(), 1, ASPECTS_Y, 6, mouseX, mouseY);
        if (name != null) {
            tooltip.add(name);
        }
    }
}
