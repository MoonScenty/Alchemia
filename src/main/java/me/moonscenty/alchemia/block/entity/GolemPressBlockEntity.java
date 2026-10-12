package me.moonscenty.alchemia.block.entity;

import java.util.List;

import javax.annotation.Nullable;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.block.TubeBlock;
import me.moonscenty.alchemia.client.GolemEffects;
import me.moonscenty.alchemia.essentia.EssentiaHolder;
import me.moonscenty.alchemia.golem.GolemDesign;
import me.moonscenty.alchemia.item.GolemItem;
import me.moonscenty.alchemia.menu.GolemPressMenu;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.NonNullList;

/**
 * The working heart of a golem press.
 *
 * <p>Somebody picks a golem at the press and it takes what that golem is made of out of their pack there and then.
 * Then it presses, a measure of machina at a time every quarter of a second, until it has had as many measures as
 * the golem cost; and then the golem item drops into the press's one slot. The machina comes up a tube, which may
 * meet it on any side but the top, or straight out of a jar set against it. A press with its slot full of something
 * else waits.
 */
public class GolemPressBlockEntity extends BlockEntity implements EssentiaHolder, Container, MenuProvider {
    /** What the menu reads: how many measures are left, and how many there were. */
    public static final int READINGS = 2;
    public static final int READING_COST = 0;
    public static final int READING_MAX = 1;
    /** How far down the press comes, in degrees of its swing, when it is working. */
    public static final int PRESS_DOWN = 90;

    private final NonNullList<ItemStack> slot = NonNullList.withSize(1, ItemStack.EMPTY);
    @Nullable
    private GolemDesign pressing;
    private int cost;
    private int maxCost;
    /** A measure that came up a tube and has not been pressed in yet. */
    private boolean buffered;
    private int ticks;
    /** How far the press has come down, for drawing it. Only the client moves it. */
    public int press;

    private final ContainerData readings = new ContainerData() {
        @Override
        public int get(int index) {
            return index == READING_COST ? cost : maxCost;
        }

        @Override
        public void set(int index, int value) {
            if (index == READING_COST) {
                cost = value;
            } else {
                maxCost = value;
            }
        }

        @Override
        public int getCount() {
            return READINGS;
        }
    };

    public GolemPressBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GOLEM_PRESS.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, GolemPressBlockEntity it) {
        it.ticks++;
        if (it.ticks % 5 != 0 || it.pressing == null) {
            return;
        }
        if (it.cost > 0 && (it.buffered || it.draw(level, pos))) {
            it.buffered = false;
            it.cost--;
            it.setChanged();
        }
        if (it.cost <= 0) {
            ItemStack made = GolemItem.of(it.pressing);
            ItemStack there = it.slot.get(0);
            if (there.isEmpty() || ItemStack.isSameItemSameComponents(there, made)
                    && there.getCount() < there.getMaxStackSize()) {
                if (there.isEmpty()) {
                    it.slot.set(0, made);
                } else {
                    there.grow(1);
                }
                level.playSound(null, pos, ModSounds.WAND.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                it.pressing = null;
                it.cost = 0;
                it.changed();
            }
        }
    }

    /**
     * The press coming down while it works and going back up when it is done, hissing steam all the while, as the
     * original drew it.
     */
    public static void clientTick(Level level, BlockPos pos, BlockState state, GolemPressBlockEntity it) {
        boolean working = it.cost > 0 && it.pressing != null;
        if (it.press < PRESS_DOWN && working) {
            it.press += 6;
            if (it.press >= 60) {
                GolemEffects.pressHiss(level, pos, 8, 0.66F);
            }
        }
        if (it.press >= PRESS_DOWN && level.random.nextInt(8) == 0) {
            GolemEffects.pressHiss(level, pos, 1, 0.1F);
        }
        if (it.press > 0 && !working) {
            if (it.press >= PRESS_DOWN) {
                GolemEffects.pressHiss(level, pos, 5, 0.0F);
            }
            it.press -= 3;
        }
    }

    /**
     * Starts pressing a golem, if the slot can take one and the player has everything it is made of, which is taken
     * from them now.
     */
    public boolean start(GolemDesign design, Player player) {
        ItemStack made = GolemItem.of(design);
        ItemStack there = slot.get(0);
        if (pressing != null || !there.isEmpty() && !(ItemStack.isSameItemSameComponents(there, made)
                && there.getCount() < there.getMaxStackSize())) {
            return false;
        }
        List<ItemStack> needs = design.components();
        for (ItemStack need : needs) {
            if (carried(player, need) < need.getCount()) {
                return false;
            }
        }
        for (ItemStack need : needs) {
            take(player, need);
        }
        pressing = design;
        cost = design.cost();
        maxCost = cost;
        if (level != null) {
            level.playSound(null, worldPosition, ModSounds.CRAFT_START.get(), SoundSource.BLOCKS, 0.25F, 1.0F);
        }
        changed();
        return true;
    }

    /** How many of a thing a player has about them, whatever else is on it. */
    public static int carried(Player player, ItemStack wanted) {
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (ItemStack.isSameItem(stack, wanted)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static void take(Player player, ItemStack wanted) {
        int left = wanted.getCount();
        for (ItemStack stack : player.getInventory().items) {
            if (left <= 0) {
                break;
            }
            if (ItemStack.isSameItem(stack, wanted)) {
                int taken = Math.min(left, stack.getCount());
                stack.shrink(taken);
                left -= taken;
            }
        }
        player.getInventory().setChanged();
    }

    public boolean working() {
        return pressing != null && cost > 0;
    }

    /** One measure of machina straight out of a vessel against a side; a tube there brings it on its own. */
    private boolean draw(Level level, BlockPos pos) {
        Holder<Aspect> machina = ModAspects.MECHANISM;
        for (Direction side : Direction.values()) {
            if (side == Direction.UP) {
                continue;
            }
            BlockPos beside = pos.relative(side);
            if (level.getBlockState(beside).getBlock() instanceof TubeBlock
                    || !(level.getBlockEntity(beside) instanceof EssentiaHolder source)
                    || !source.reachableFrom(side.getOpposite()) || !source.givesTo(side.getOpposite())) {
                continue;
            }
            if (source.held().get(machina) > 0 && source.release(machina)) {
                return true;
            }
        }
        return false;
    }

    /** What it has in it, for when it is broken. */
    public void spill() {
        if (level != null) {
            Containers.dropContents(level, worldPosition, this);
        }
    }

    private void changed() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    // --- essentia -----------------------------------------------------------------------------------------

    @Override
    public AspectList held() {
        return AspectList.EMPTY;
    }

    /** One measure of machina at a time, while it is pressing. */
    @Override
    public boolean wants(Holder<Aspect> aspect) {
        return working() && !buffered && aspect.value() == ModAspects.MECHANISM.value();
    }

    @Override
    public boolean accept(Holder<Aspect> aspect) {
        if (!wants(aspect)) {
            return false;
        }
        buffered = true;
        return true;
    }

    /** Tubes meet it on its sides and from below; the top is the press. */
    @Override
    public boolean reachableFrom(Direction side) {
        return side != Direction.UP;
    }

    @Override
    public boolean givesTo(Direction side) {
        return false;
    }

    // --- the slot -----------------------------------------------------------------------------------------

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return slot.get(0).isEmpty();
    }

    @Override
    public ItemStack getItem(int index) {
        return slot.get(index);
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        ItemStack out = ContainerHelper.removeItem(slot, index, count);
        if (!out.isEmpty()) {
            setChanged();
        }
        return out;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        return ContainerHelper.takeItem(slot, index);
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        slot.set(index, stack);
        setChanged();
    }

    /** Only golems belong in it, and only the press puts them there. */
    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return stack.getItem() instanceof GolemItem;
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        slot.clear();
    }

    // --- the menu -----------------------------------------------------------------------------------------

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.alchemia.golem_press");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new GolemPressMenu(id, inventory, this, readings,
                ContainerLevelAccess.create(player.level(), worldPosition));
    }

    // --- saving -------------------------------------------------------------------------------------------

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        slot.clear();
        ContainerHelper.loadAllItems(tag, slot, registries);
        pressing = tag.contains("pressing") ? GolemDesign.fromLong(tag.getLong("pressing")) : null;
        cost = tag.getInt("cost");
        maxCost = tag.getInt("max_cost");
        buffered = tag.getBoolean("buffered");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, slot, registries);
        if (pressing != null) {
            tag.putLong("pressing", pressing.toLong());
        }
        tag.putInt("cost", cost);
        tag.putInt("max_cost", maxCost);
        tag.putBoolean("buffered", buffered);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
