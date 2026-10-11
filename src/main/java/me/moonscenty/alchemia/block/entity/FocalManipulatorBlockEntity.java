package me.moonscenty.alchemia.block.entity;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.item.FocusItem;
import me.moonscenty.alchemia.menu.FocalManipulatorMenu;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import me.moonscenty.alchemia.wand.FocusUpgrade;
import me.moonscenty.alchemia.wand.FocusUpgrades;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A focus laid on the bench, and the working of one more thing into it.
 *
 * <p>Work is a bill of primals that has to be drawn out of the air a little at a time. Nothing is held in
 * reserve: five of each aspect every five ticks, out of the chunk the bench is standing in, until the bill is
 * clear. That is what makes a manipulator want a thick sky and makes it take a while.
 *
 * <p>Taking the focus off the bench part-way through throws the work away, vis and levels and all. The original
 * did the same, and it is the only answer that does not leave half an upgrade written on a focus somebody has
 * walked off with.
 */
public class FocalManipulatorBlockEntity extends BlockEntity implements Container, MenuProvider {
    /** How often it draws, and how much of any one aspect it can pull at a time. */
    private static final int DRAWS_EVERY = 5;
    private static final int DRAWS = 5;

    private final NonNullList<ItemStack> laid = NonNullList.withSize(1, ItemStack.EMPTY);

    /** What is still owed, what the whole bill was, and what is being worked in where. */
    private AspectList owed = AspectList.EMPTY;
    private int bill;
    private FocusUpgrade working;
    private int rank;
    private int ticks;

    public FocalManipulatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FOCAL_MANIPULATOR.get(), pos, state);
    }

    // --- the work ---------------------------------------------------------------------------------------

    /**
     * Takes on a piece of work, if the focus will have it and the one asking can pay the levels.
     *
     * @return whether anything was started, which is also whether the levels were taken
     */
    public boolean start(FocusUpgrade upgrade, Player asking) {
        if (bill > 0 || level == null || !(getItem(0).getItem() instanceof FocusItem)) {
            return false;
        }
        int next = FocusUpgrades.nextRank(getItem(0));
        if (next < 1 || !FocusUpgrades.offers(getItem(0), next).contains(upgrade)) {
            return false;
        }
        int levels = FocusUpgrades.levels(next);
        // asked of the player rather than of their abilities, for the reason written in Swapping.takeOne
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
        level.playSound(null, worldPosition, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 0.5F, 1.2F);
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
            level.playSound(null, worldPosition, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 0.8F);
        }
        clear();
    }

    private void giveUp() {
        if (level != null) {
            level.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.33F, 1.0F);
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

    /** What is still owed, which is what the screen draws its bar from. */
    public AspectList owed() {
        return owed;
    }

    /** What the whole bill was, so a part-paid one can be drawn as a share of it. */
    public int bill() {
        return bill;
    }

    /** What is being worked in, or nothing when the bench is idle. */
    public FocusUpgrade working() {
        return working;
    }

    /** Which rank is being worked on now, counting from one. */
    public int rank() {
        return rank;
    }

    private void changed() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    // --- the one square it has --------------------------------------------------------------------------

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

    /** A focus taken off, or swapped for one this piece of work means nothing to, is work abandoned. */
    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide && bill > 0 && !stillGood()) {
            giveUp();
        }
    }

    /**
     * Whether what is on the bench now is still something this piece of work can be finished on.
     * <p>
     * Asked of the focus rather than of the stack it came in: a focus of the same kind with the same ranks
     * filled is the same piece of work by any measure that matters, and asking it this way survives the world
     * being closed and opened again, which holding on to the stack itself would not.
     */
    private boolean stillGood() {
        return working != null && getItem(0).getItem() instanceof FocusItem
                && FocusUpgrades.nextRank(getItem(0)) == rank
                && FocusUpgrades.offers(getItem(0), rank).contains(working);
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getCenter()) < 64.0;
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

    // --- saving -----------------------------------------------------------------------------------------

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        laid.clear();
        ContainerHelper.loadAllItems(tag, laid, registries);
        owed = AspectList.CODEC
                .parse(registries.createSerializationContext(NbtOps.INSTANCE), tag.get("owed"))
                .result().orElse(AspectList.EMPTY);
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
        AspectList.CODEC
                .encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), owed)
                .result().ifPresent(written -> tag.put("owed", written));
        tag.putInt("bill", bill);
        tag.putInt("rank", rank);
        if (working != null) {
            FocusUpgrade.CODEC.encodeStart(NbtOps.INSTANCE, working)
                    .result().ifPresent(written -> tag.put("working", written));
        }
    }

    /** The screen draws the bill and what is left of it, so all of that has to reach the client on its own. */
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
