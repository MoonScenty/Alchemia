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
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

public class ArcaneWorkbenchMenu extends AlchemiaMenu {
    public static final int SLOT_RESULT = 0;
    public static final int FIRST_GRID = 1;
    public static final int SLOT_WAND = FIRST_GRID + ArcaneWorkbenchBlockEntity.GRID;
    private static final int BENCH_SLOTS = SLOT_WAND + 1;
    private static final int PACK_ROWS = 27;

    private static final int GRID_X = 40;
    private static final int GRID_Y = 40;
    private static final int GRID_STEP = 24;
    private static final int WAND_X = 160;
    private static final int WAND_Y = 24;
    public static final int RESULT_X = 160;
    public static final int RESULT_Y = 64;
    private static final int PACK_X = 16;
    private static final int PACK_Y = 151;

    private final Container bench;
    private final ResultContainer result = new ResultContainer();
    private final ContainerLevelAccess access;
    private final Player player;

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
                return VisHolder.of(stack) != null && !me.moonscenty.alchemia.item.WandItem.isStaff(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        addPlayerInventory(inventory, PACK_X, PACK_Y);
        lookUp();
    }

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

    // plain crafting is looked for first and costs no vis [ContainerArcaneWorkbench.onCraftMatrixChanged]
    public Optional<RecipeHolder<CraftingRecipe>> plain() {
        Level level = player.level();
        return level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, grid(), level);
    }

    public Optional<RecipeHolder<ArcaneRecipe>> matching() {
        if (plain().isPresent()) {
            return Optional.empty();
        }
        Level level = player.level();
        return level.getRecipeManager().getRecipeFor(ModRecipes.ARCANE.get(), grid(), level);
    }

    public AspectList cost() {
        return matching().map(found -> found.value().cost(grid())).orElse(AspectList.EMPTY);
    }

    public boolean known(RecipeHolder<ArcaneRecipe> recipe) {
        return me.moonscenty.alchemia.research.RecipeGates.known(player, recipe.id(), recipe.value().research());
    }

    public boolean affordable() {
        return VisHolder.canPay(wand(), cost(), player, VisHolder.Spending.CRAFTING);
    }

    // research and vis are checked here, not in the slot, so an unaffordable result never shows
    private void lookUp() {
        if (player.level().isClientSide) {
            return;
        }
        ItemStack made = plain()
                .map(found -> found.value().assemble(grid(), player.level().registryAccess()))
                .orElseGet(() -> matching()
                        .filter(this::known)
                        .filter(found -> VisHolder.canPay(wand(), found.value().cost(grid()), player,
                                VisHolder.Spending.CRAFTING))
                        .map(found -> found.value().assemble(grid(), player.level().registryAccess()))
                        .orElse(ItemStack.EMPTY));
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

    private void craft(Player taker, ItemStack made) {
        matching().ifPresent(found -> {
            VisHolder holder = VisHolder.of(wand());
            AspectList price = found.value().cost(grid());
            if (holder != null && !price.isEmpty()) {
                holder.take(wand(), price, taker, VisHolder.Spending.CRAFTING);
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
        } else if (held.getItem() instanceof me.moonscenty.alchemia.item.WandItem
                && !me.moonscenty.alchemia.item.WandItem.isStaff(held)) {
            if (!moveItemStackTo(held, SLOT_WAND, BENCH_SLOTS, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!(index < BENCH_SLOTS + PACK_ROWS
                ? moveItemStackTo(held, BENCH_SLOTS + PACK_ROWS, slots.size(), false)
                : moveItemStackTo(held, BENCH_SLOTS, BENCH_SLOTS + PACK_ROWS, false))) {
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

    @Override
    public void removed(Player taker) {
        super.removed(taker);
        result.clearContent();
    }

    @Override
    public boolean stillValid(Player taker) {
        return stillValid(access, taker, ModBlocks.ARCANE_WORKBENCH.get());
    }

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
            me.moonscenty.alchemia.player.CraftWarp.made(taker, stack);
            craft(taker, stack);
        }
    }
}
