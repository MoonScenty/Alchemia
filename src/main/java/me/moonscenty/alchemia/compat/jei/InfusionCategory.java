package me.moonscenty.alchemia.compat.jei;

import java.util.ArrayList;
import java.util.List;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.crafting.InfusionInput;
import me.moonscenty.alchemia.crafting.InfusionRecipe;
import me.moonscenty.alchemia.registry.ModBlocks;
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
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * The infusion altar: the thing on the middle pedestal, the ring round it, the essentia below and how unsteady the
 * working is, and what comes of it. A working that adds to what it is given shows what the first thing on its list
 * would come out as.
 */
public class InfusionCategory implements IRecipeCategory<RecipeHolder<InfusionRecipe>> {
    public static final RecipeType<RecipeHolder<InfusionRecipe>> TYPE =
            RecipeType.createRecipeHolderType(Alchemia.id("infusion"));
    private static final int WIDTH = 150;
    private static final int HEIGHT = 130;
    private static final int CENTRE_X = 48;
    private static final int CENTRE_Y = 44;
    private static final int RADIUS = 34;
    private static final int ESSENTIA_Y = 92;

    private final IDrawable icon;

    public InfusionCategory(IGuiHelper gui) {
        this.icon = gui.createDrawableItemStack(new ItemStack(ModBlocks.INFUSION_MATRIX.get()));
    }

    @Override
    public RecipeType<RecipeHolder<InfusionRecipe>> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("research.alchemia.in_matrix");
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
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<InfusionRecipe> holder, IFocusGroup focuses) {
        InfusionRecipe recipe = holder.value();
        IRecipeSlotBuilder central = builder.addSlot(RecipeIngredientRole.INPUT, CENTRE_X, CENTRE_Y)
                .setStandardSlotBackground().addIngredients(recipe.central());
        List<Ingredient> ring = recipe.ring();
        float step = ring.isEmpty() ? 0F : 360F / ring.size();
        float angle = -90F;
        List<ItemStack> sample = new ArrayList<>();
        for (Ingredient part : ring) {
            int x = CENTRE_X + (int) (Mth.cos(angle / 180F * Mth.PI) * RADIUS);
            int y = CENTRE_Y + (int) (Mth.sin(angle / 180F * Mth.PI) * RADIUS);
            builder.addSlot(RecipeIngredientRole.INPUT, x, y).setStandardSlotBackground().addIngredients(part);
            ItemStack[] options = part.getItems();
            sample.add(options.length > 0 ? options[0] : ItemStack.EMPTY);
            angle += step;
        }
        ItemStack result = recipe.getResultItem(Minecraft.getInstance().level.registryAccess());
        if (result.isEmpty()) {
            // a working that adds to what it is given: show each thing it can be given, worked
            List<ItemStack> givens = List.of(recipe.central().getItems());
            List<ItemStack> worked = new ArrayList<>();
            for (ItemStack given : givens) {
                worked.add(recipe.assemble(new InfusionInput(given, sample),
                        Minecraft.getInstance().level.registryAccess()));
            }
            IRecipeSlotBuilder made = builder.addSlot(RecipeIngredientRole.OUTPUT, 126, CENTRE_Y)
                    .setStandardSlotBackground().addItemStacks(worked);
            builder.createFocusLink(central, made);
            return;
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 126, CENTRE_Y).setStandardSlotBackground().addItemStack(result);
    }

    @Override
    public void draw(RecipeHolder<InfusionRecipe> holder, IRecipeSlotsView slots, GuiGraphics graphics,
            double mouseX, double mouseY) {
        InfusionRecipe recipe = holder.value();
        AspectRow.draw(graphics, recipe.essentia(), 1, ESSENTIA_Y, 8);
        graphics.drawString(Minecraft.getInstance().font, "→", 106, CENTRE_Y + 4, 0xFF404040, false);
        Component unsteady = Component.translatable("jei.alchemia.instability", recipe.instability());
        graphics.drawString(Minecraft.getInstance().font, unsteady, 1, HEIGHT - 9, 0xFF404040, false);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, RecipeHolder<InfusionRecipe> holder, IRecipeSlotsView slots,
            double mouseX, double mouseY) {
        Component name = AspectRow.under(holder.value().essentia(), 1, ESSENTIA_Y, 8, mouseX, mouseY);
        if (name != null) {
            tooltip.add(name);
        }
    }
}
