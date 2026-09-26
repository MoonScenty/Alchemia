package me.moonscenty.alchemia.block.entity;

import me.moonscenty.alchemia.menu.ArcaneWorkbenchMenu;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * What is laid out on an arcane workbench: the nine squares of the grid, and the wand that pays for the work.
 * <p>
 * Unlike a vanilla bench this keeps hold of what is on it when the screen is closed, because the wand is meant to
 * be left there — the charger sitting above the bench tops it up while nobody is using it.
 */
public class ArcaneWorkbenchBlockEntity extends BlockEntity implements Container, MenuProvider {
    public static final int GRID = 9;
    /** Where the wand lies. The last slot, so the nine before it are the grid in reading order. */
    public static final int SLOT_WAND = GRID;
    public static final int SIZE = GRID + 1;

    private final NonNullList<ItemStack> contents = NonNullList.withSize(SIZE, ItemStack.EMPTY);

    public ArcaneWorkbenchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ARCANE_WORKBENCH.get(), pos, state);
    }

    public ItemStack wand() {
        return contents.get(SLOT_WAND);
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
        }
        return taken;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(contents, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        contents.set(slot, stack);
        stack.limitSize(getMaxStackSize(stack));
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null
                && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getCenter()) <= 64.0D;
    }

    @Override
    public void clearContent() {
        contents.clear();
        setChanged();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.alchemia.arcane_workbench");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ArcaneWorkbenchMenu(id, inventory, this, ContainerLevelAccess.create(level, worldPosition));
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

    /** The wand is drawn lying on the bench, so what is on it has to reach the client without the screen open. */
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public static void drop(Level level, BlockPos pos, ArcaneWorkbenchBlockEntity bench) {
        Containers.dropContents(level, pos, bench.contents);
    }
}
