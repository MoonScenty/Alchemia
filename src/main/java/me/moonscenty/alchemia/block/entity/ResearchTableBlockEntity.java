package me.moonscenty.alchemia.block.entity;

import me.moonscenty.alchemia.block.ResearchTableBlock;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ResearchTableBlockEntity extends SyncedBlockEntity implements Container, MenuProvider {
    public static final int SLOT_TOOLS = 0;
    public static final int SLOT_NOTES = 1;

    private final NonNullList<ItemStack> contents = NonNullList.withSize(2, ItemStack.EMPTY);

    public ResearchTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RESEARCH_TABLE.get(), pos, state);
    }

    public ItemStack get(int slot) {
        return contents.get(slot);
    }

    public ItemStack put(int slot, ItemStack stack) {
        ItemStack previous = contents.get(slot);
        contents.set(slot, stack);
        setChanged();
        showWhatIsOnTheDesk();
        return previous;
    }

    public boolean accepts(int slot, ItemStack stack) {
        return switch (slot) {
            case SLOT_TOOLS -> stack.is(ModItems.SCRIBING_TOOLS.get());
            case SLOT_NOTES -> stack.is(ModItems.RESEARCH_NOTES.get());
            default -> false;
        };
    }

    public boolean useInk() {
        ItemStack tools = contents.get(SLOT_TOOLS);
        // used to the last point, and a dry set stays in its slot to be refilled [ResearchManager.consumeInkFromTable]
        if (tools.isEmpty() || tools.getDamageValue() >= tools.getMaxDamage()) {
            return false;
        }
        tools.setDamageValue(tools.getDamageValue() + 1);
        setChanged();
        showWhatIsOnTheDesk();
        return true;
    }

    private void showWhatIsOnTheDesk() {
        if (level == null || level.isClientSide) {
            return;
        }
        BlockState updated = getBlockState()
                .setValue(ResearchTableBlock.HAS_TOOLS, !contents.get(SLOT_TOOLS).isEmpty())
                .setValue(ResearchTableBlock.HAS_NOTES, !contents.get(SLOT_NOTES).isEmpty());
        if (updated != getBlockState()) {
            level.setBlock(worldPosition, updated, Block.UPDATE_ALL);
        }
    }

    @Override
    public int getContainerSize() {
        return contents.size();
    }

    @Override
    public boolean isEmpty() {
        return contents.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return contents.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack taken = ContainerHelper.removeItem(contents, slot, amount);
        if (!taken.isEmpty()) {
            setChanged();
            showWhatIsOnTheDesk();
        }
        return taken;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return put(slot, ItemStack.EMPTY);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        put(slot, stack);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return accepts(slot, stack);
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        contents.clear();
        setChanged();
        showWhatIsOnTheDesk();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.alchemia.research_table");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new me.moonscenty.alchemia.menu.ResearchTableMenu(id, inventory, this,
                ContainerLevelAccess.create(level, worldPosition));
    }

    public NonNullList<ItemStack> contents() {
        return contents;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        contents.clear();
        ContainerHelper.loadAllItems(tag, contents, registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, contents, registries);
    }

    public static void drop(Level level, BlockPos pos, ResearchTableBlockEntity table) {
        net.minecraft.world.Containers.dropContents(level, pos, table.contents());
    }
}
