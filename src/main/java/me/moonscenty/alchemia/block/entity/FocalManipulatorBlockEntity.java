package me.moonscenty.alchemia.block.entity;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.item.FocusItem;
import me.moonscenty.alchemia.menu.FocalManipulatorMenu;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import me.moonscenty.alchemia.registry.ModSounds;
import me.moonscenty.alchemia.wand.FocusUpgrade;
import me.moonscenty.alchemia.wand.FocusUpgrades;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class FocalManipulatorBlockEntity extends SyncedBlockEntity implements Container, MenuProvider {
    private static final int DRAWS_EVERY = 5;
    private static final int DRAWS = 5;

    private final NonNullList<ItemStack> laid = NonNullList.withSize(1, ItemStack.EMPTY);

    private AspectList owed = AspectList.EMPTY;
    private int bill;
    private FocusUpgrade working;
    private int rank;
    private int ticks;

    public FocalManipulatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FOCAL_MANIPULATOR.get(), pos, state);
    }

    public boolean start(FocusUpgrade upgrade, Player asking) {
        if (bill > 0 || level == null || !(getItem(0).getItem() instanceof FocusItem)) {
            return false;
        }
        int next = FocusUpgrades.nextRank(getItem(0));
        if (next < 1 || !FocusUpgrades.offers(getItem(0), next, asking).contains(upgrade)) {
            return false;
        }
        int levels = FocusUpgrades.levels(next);
        // checked on the player, not abilities (see Swapping.takeOne)
        if (!asking.isCreative() && asking.experienceLevel < levels) {
            return false;
        }

        owed = FocusUpgrades.price(upgrade, next);
        bill = owed.total();
        working = upgrade;
        rank = next;
        if (!asking.isCreative()) {
            asking.giveExperienceLevels(-levels);
        }
        level.playSound(null, worldPosition, ModSounds.CRAFT_START.get(), SoundSource.BLOCKS, 0.25F, 1.0F);
        changed();
        return true;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
            FocalManipulatorBlockEntity bench) {
        bench.ticks++;
        if (bench.bill <= 0 || bench.ticks % DRAWS_EVERY != 0) {
            return;
        }
        if (!bench.stillGood()) {
            bench.giveUp();
            return;
        }

        for (Holder<Aspect> aspect : bench.owed.sortedByAmount()) {
            int want = Math.min(DRAWS, bench.owed.get(aspect));
            int drawn = AuraHandler.drainAvailable(level, pos, aspect, want);
            if (drawn > 0) {
                bench.owed = bench.owed.reduce(aspect, drawn);
            }
        }
        if (bench.owed.isEmpty()) {
            bench.finish();
        } else {
            bench.changed();
        }
    }

    private void finish() {
        FocusUpgrades.apply(getItem(0), working);
        if (level != null) {
            level.playSound(null, worldPosition, ModSounds.WAND.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        clear();
    }

    // removing or swapping the focus mid-work discards all progress, as the original did
    private void giveUp() {
        if (level != null) {
            level.playSound(null, worldPosition, ModSounds.CRAFT_FAIL.get(), SoundSource.BLOCKS, 0.33F, 1.0F);
        }
        clear();
    }

    private void clear() {
        owed = AspectList.EMPTY;
        bill = 0;
        working = null;
        rank = 0;
        changed();
    }

    public AspectList owed() {
        return owed;
    }

    public int bill() {
        return bill;
    }

    public FocusUpgrade working() {
        return working;
    }

    public int rank() {
        return rank;
    }

    @Override
    protected void changed() {
        changed(Block.UPDATE_ALL);
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return laid.get(0).isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return laid.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        ItemStack taken = ContainerHelper.removeItem(laid, slot, count);
        if (!taken.isEmpty()) {
            setChanged();
        }
        return taken;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack taken = ContainerHelper.takeItem(laid, slot);
        if (!taken.isEmpty()) {
            setChanged();
        }
        return taken;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        laid.set(slot, stack);
        setChanged();
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide && bill > 0 && !stillGood()) {
            giveUp();
        }
    }

    // compares focus kind and ranks, not the stack, so it survives a reload
    private boolean stillGood() {
        return working != null && getItem(0).getItem() instanceof FocusItem
                && FocusUpgrades.nextRank(getItem(0)) == rank
                && FocusUpgrades.offers(getItem(0), rank).contains(working);
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        laid.clear();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.alchemia.focal_manipulator");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new FocalManipulatorMenu(id, inventory, this);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        laid.clear();
        ContainerHelper.loadAllItems(tag, laid, registries);
        owed = AspectList.read(tag, "owed", registries);
        bill = tag.getInt("bill");
        rank = tag.getInt("rank");
        working = tag.contains("working")
                ? FocusUpgrade.CODEC.parse(NbtOps.INSTANCE, tag.get("working")).result().orElse(null)
                : null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, laid, registries);
        AspectList.write(tag, "owed", owed, registries);
        tag.putInt("bill", bill);
        tag.putInt("rank", rank);
        if (working != null) {
            FocusUpgrade.CODEC.encodeStart(NbtOps.INSTANCE, working)
                    .result().ifPresent(written -> tag.put("working", written));
        }
    }
}
