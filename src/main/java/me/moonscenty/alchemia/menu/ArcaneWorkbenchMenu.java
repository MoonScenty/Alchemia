package me.moonscenty.alchemia.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.block.entity.ArcaneWorkbenchBlockEntity;
import me.moonscenty.alchemia.crafting.ArcaneRecipe;
import me.moonscenty.alchemia.crafting.ModRecipes;
import me.moonscenty.alchemia.item.VisHolder;
import me.moonscenty.alchemia.player.PlayerKnowledge;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModMenus;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

/**
 * Working at an arcane workbench: the grid, the wand that pays for the work, and what comes of the two.
 * <p>
 * Which recipe is on the grid is worked out on both sides rather than sent across. Everything it depends on — what
 * is in the slots, the recipes, and what the player has researched — is already on the client, so the screen can
 * show the price without a packet of its own.
 */
public class ArcaneWorkbenchMenu extends AbstractContainerMenu {
    public static final int SLOT_RESULT = 0;
    public static final int FIRST_GRID = 1;
    public static final int SLOT_WAND = FIRST_GRID + ArcaneWorkbenchBlockEntity.GRID;
    private static final int BENCH_SLOTS = SLOT_WAND + 1;

    // Where each piece sits on the drawn panel.
    private static final int GRID_X = 35;
    private static final int GRID_Y = 45;
    private static final int GRID_STEP = 18;
    private static final int WAND_X = 134;
    private static final int WAND_Y = 36;
    private static final int RESULT_X = 134;
    private static final int RESULT_Y = 90;
    private static final int PACK_X = 8;
    private static final int PACK_Y = 140;
    private static final int BELT_Y = 198;
    private static final int PITCH = 18;

    private final Container bench;
    private final ResultContainer result = new ResultContainer();
    private final ContainerLevelAccess access;
    private final Player player;

    /** What the client builds: the bench is a stand-in, filled in by the slots syncing across. */
    public ArcaneWorkbenchMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(ArcaneWorkbenchBlockEntity.SIZE), ContainerLevelAccess.NULL);
    }

    public ArcaneWorkbenchMenu(int id, Inventory inventory, Container bench, ContainerLevelAccess access) {
        super(ModMenus.ARCANE_WORKBENCH.get(), id);
        this.bench = bench;
        this.access = access;
        this.player = inventory.player;
        checkContainerSize(bench, ArcaneWorkbenchBlockEntity.SIZE);

        addSlot(new ArcaneResultSlot(result, RESULT_X, RESULT_Y));
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                addSlot(new Slot(bench, column + row * 3,
                        GRID_X + column * GRID_STEP, GRID_Y + row * GRID_STEP));
            }
        }
        addSlot(new Slot(bench, ArcaneWorkbenchBlockEntity.SLOT_WAND, WAND_X, WAND_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return VisHolder.of(stack) != null;
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, PACK_X + column * PITCH, PACK_Y + row * PITCH));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, PACK_X + column * PITCH, BELT_Y));
        }
        lookUp();
    }

    /** What is on the grid, in the shape the recipe manager wants it. */
    public CraftingInput grid() {
        List<ItemStack> items = new ArrayList<>(ArcaneWorkbenchBlockEntity.GRID);
        for (int slot = 0; slot < ArcaneWorkbenchBlockEntity.GRID; slot++) {
            items.add(bench.getItem(slot));
        }
        return CraftingInput.of(3, 3, items);
    }

    public ItemStack wand() {
        return bench.getItem(ArcaneWorkbenchBlockEntity.SLOT_WAND);
    }

    /** The recipe the grid spells out, whether or not it can be paid for. */
    public Optional<RecipeHolder<ArcaneRecipe>> matching() {
        Level level = player.level();
        return level.getRecipeManager().getRecipeFor(ModRecipes.ARCANE.get(), grid(), level);
    }

    /** What the work on the grid would cost. */
    public AspectList cost() {
        return matching().map(found -> found.value().cost()).orElse(AspectList.EMPTY);
    }

    /** Whether the research behind a recipe has been done. A recipe with no research named is open to anyone. */
    public boolean known(ArcaneRecipe recipe) {
        return recipe.research()
                .map(research -> PlayerKnowledge.of(player).hasResearch(research))
                .orElse(true);
    }

    /** Whether the wand on the bench can pay for what is on the grid. */
    public boolean affordable() {
        return VisHolder.canPay(wand(), cost());
    }

    /**
     * Puts the result out, or takes it away again.
     * <p>
     * Research and vis are weighed here rather than in the slot, so that work the player cannot do leaves the
     * result square empty instead of showing something they are not allowed to pick up.
     */
    private void lookUp() {
        if (player.level().isClientSide) {
            return;
        }
        ItemStack made = matching()
                .filter(found -> known(found.value()))
                .filter(found -> VisHolder.canPay(wand(), found.value().cost()))
                .map(found -> found.value().assemble(grid(), player.level().registryAccess()))
                .orElse(ItemStack.EMPTY);
        result.setItem(0, made);
        if (player instanceof ServerPlayer server) {
            server.connection.send(new ClientboundContainerSetSlotPacket(
                    containerId, incrementStateId(), SLOT_RESULT, made));
        }
    }

    @Override
    public void slotsChanged(Container container) {
        access.execute((level, pos) -> lookUp());
    }

    /** Spends the ingredients and the vis once the result is carried off. */
    private void craft(Player taker, ItemStack made) {
        matching().ifPresent(found -> {
            VisHolder holder = VisHolder.of(wand());
            if (holder != null && !found.value().cost().isEmpty()) {
                holder.take(wand(), found.value().cost(), taker);
            }
        });
        for (int slot = 0; slot < ArcaneWorkbenchBlockEntity.GRID; slot++) {
            ItemStack used = bench.getItem(slot);
            if (used.isEmpty()) {
                continue;
            }
            ItemStack left = used.getItem().hasCraftingRemainingItem()
                    ? new ItemStack(used.getItem().getCraftingRemainingItem())
                    : ItemStack.EMPTY;
            used.shrink(1);
            if (used.isEmpty() && !left.isEmpty()) {
                bench.setItem(slot, left);
            } else if (!left.isEmpty()) {
                taker.getInventory().placeItemBackInInventory(left);
            }
        }
        bench.setChanged();
        lookUp();
    }

    @Override
    public ItemStack quickMoveStack(Player taker, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack held = slot.getItem();
        ItemStack copy = held.copy();

        if (index < BENCH_SLOTS) {
            if (!moveItemStackTo(held, BENCH_SLOTS, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(held, copy);
        } else if (!moveItemStackTo(held, FIRST_GRID, BENCH_SLOTS, false)) {
            return ItemStack.EMPTY;
        }

        if (held.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (held.getCount() == copy.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(taker, held);
        return copy;
    }

    /** The grid keeps what is on it, but the result square is no place to leave anything. */
    @Override
    public void removed(Player taker) {
        super.removed(taker);
        result.clearContent();
    }

    @Override
    public boolean stillValid(Player taker) {
        return stillValid(access, taker, ModBlocks.ARCANE_WORKBENCH.get());
    }

    /** The square the finished thing waits in. Nothing may be put into it, and taking from it spends the work. */
    private class ArcaneResultSlot extends Slot {
        ArcaneResultSlot(Container container, int x, int y) {
            super(container, 0, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public void onTake(Player taker, ItemStack stack) {
            stack.onCraftedBy(taker.level(), taker, stack.getCount());
            craft(taker, stack);
        }
    }
}
