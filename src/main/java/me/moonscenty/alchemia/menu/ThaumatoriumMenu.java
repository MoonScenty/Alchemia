package me.moonscenty.alchemia.menu;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

import me.moonscenty.alchemia.block.entity.ThaumatoriumBlockEntity;
import me.moonscenty.alchemia.crafting.CrucibleRecipe;
import me.moonscenty.alchemia.crafting.ModRecipes;
import me.moonscenty.alchemia.player.PlayerKnowledge;
import me.moonscenty.alchemia.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * Standing at a thaumatorium: the catalyst slot, and the crucible recipes it could be told to make.
 * <p>
 * The recipes on offer are the ones the player has worked out whose catalyst is the thing in the slot, and with
 * them every one the thaumatorium is already making, so that those can be taken off again. Both sides work the list
 * out the same way, sorted by name, so that a button pressed on one means the same recipe on the other.
 */
public class ThaumatoriumMenu extends AbstractContainerMenu {
    private static final int CATALYST_X = 48;
    private static final int CATALYST_Y = 16;
    private static final int PACK_X = 8;
    private static final int PACK_Y = 84;
    private static final int BELT_Y = 142;
    private static final int PITCH = 18;

    private final Player player;
    @Nullable
    private final ThaumatoriumBlockEntity thaumatorium;
    private final Container slot;

    public ThaumatoriumMenu(int id, Inventory inventory, RegistryFriendlyByteBuf extra) {
        this(id, inventory, inventory.player.level().getBlockEntity(extra.readBlockPos())
                instanceof ThaumatoriumBlockEntity found ? found.main() : null);
    }

    public ThaumatoriumMenu(int id, Inventory inventory, @Nullable ThaumatoriumBlockEntity thaumatorium) {
        super(ModMenus.THAUMATORIUM.get(), id);
        this.player = inventory.player;
        this.thaumatorium = thaumatorium;
        this.slot = thaumatorium == null ? new SimpleContainer(1) : thaumatorium;
        addSlot(new Slot(slot, 0, CATALYST_X, CATALYST_Y));
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, PACK_X + column * PITCH, PACK_Y + row * PITCH));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, PACK_X + column * PITCH, BELT_Y));
        }
    }

    @Nullable
    public ThaumatoriumBlockEntity thaumatorium() {
        return thaumatorium;
    }

    /** What could be chosen now: worked-out recipes the catalyst finishes, and whatever is already chosen. */
    public List<RecipeHolder<CrucibleRecipe>> offered() {
        if (thaumatorium == null) {
            return List.of();
        }
        ItemStack catalyst = slot.getItem(0);
        List<?> chosen = thaumatorium.recipes();
        PlayerKnowledge knowledge = PlayerKnowledge.of(player);
        List<RecipeHolder<CrucibleRecipe>> offered = new ArrayList<>();
        for (RecipeHolder<CrucibleRecipe> recipe
                : player.level().getRecipeManager().getAllRecipesFor(ModRecipes.CRUCIBLE.get())) {
            boolean known = recipe.value().research().map(knowledge::hasResearch).orElse(true);
            if ((known && !catalyst.isEmpty() && recipe.value().catalyst().test(catalyst))
                    || chosen.contains(recipe.id())) {
                offered.add(recipe);
            }
        }
        offered.sort(Comparator.comparing(recipe -> recipe.id().toString()));
        return offered;
    }

    /** A button is a recipe on offer, by its place in the list: choosing it, or unchoosing it. */
    @Override
    public boolean clickMenuButton(Player clicker, int id) {
        List<RecipeHolder<CrucibleRecipe>> offered = offered();
        if (thaumatorium == null || id < 0 || id >= offered.size()) {
            return false;
        }
        thaumatorium.toggle(offered.get(id).id());
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player taker, int index) {
        Slot from = slots.get(index);
        if (!from.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack held = from.getItem();
        ItemStack copy = held.copy();
        if (index == 0) {
            if (!moveItemStackTo(held, 1, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(held, 0, 1, false)) {
            return ItemStack.EMPTY;
        }
        if (held.isEmpty()) {
            from.set(ItemStack.EMPTY);
        } else {
            from.setChanged();
        }
        return copy;
    }

    @Override
    public boolean stillValid(Player viewer) {
        return thaumatorium != null && !thaumatorium.isRemoved()
                && viewer.distanceToSqr(thaumatorium.getBlockPos().getCenter()) <= 64.0;
    }

    public BlockPos at() {
        return thaumatorium == null ? BlockPos.ZERO : thaumatorium.getBlockPos();
    }
}
