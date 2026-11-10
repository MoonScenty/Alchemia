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
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

public class ThaumatoriumMenu extends AlchemiaMenu {
    private static final int CATALYST_X = 48;
    private static final int CATALYST_Y = 16;
    private static final int PACK_X = 8;
    private static final int PACK_Y = 84;

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
        addPlayerInventory(inventory, PACK_X, PACK_Y);
    }

    @Nullable
    public ThaumatoriumBlockEntity thaumatorium() {
        return thaumatorium;
    }

    // both sides build this list the same way, sorted by name, so button ids match
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
            boolean known = me.moonscenty.alchemia.research.RecipeGates.known(knowledge, recipe.id(),
                    recipe.value().research());
            if ((known && !catalyst.isEmpty() && recipe.value().catalyst().test(catalyst))
                    || chosen.contains(recipe.id())) {
                offered.add(recipe);
            }
        }
        offered.sort(Comparator.comparing(recipe -> recipe.id().toString()));
        return offered;
    }

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
        return quickMoveBetween(index, 1);
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
