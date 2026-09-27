package me.moonscenty.alchemia.block.entity;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.aspect.Aspects;
import me.moonscenty.alchemia.block.EssentiaSmelterBlock;
import me.moonscenty.alchemia.essentia.EssentiaHolder;
import me.moonscenty.alchemia.menu.EssentiaSmelterMenu;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Burns things down into what they are made of, and lets what comes off rise into whatever is stacked above.
 * <p>
 * It is a furnace in every way that matters: it eats fuel, it takes a while, and what it makes is not an item. The
 * difference is where the work goes — nothing comes out of the front, and a smelter with nothing over it simply
 * fills up and stops.
 */
public class EssentiaSmelterBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider, EssentiaHolder {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_FUEL = 1;

    public static final int READING_BURNING = 0;
    public static final int READING_BURNS_FOR = 1;
    public static final int READING_COOKED = 2;
    public static final int READING_COOKS_FOR = 3;
    public static final int READING_HELD = 4;
    public static final int READINGS = 5;

    /** How much essentia it can hold before it has to wait for the vessels above to take some. */
    public static final int CAPACITY = 50;
    /** How long a point of essentia takes to boil out of something, in ticks. */
    private static final int PER_ASPECT = 10;
    /** How often it lets one point go upwards. */
    private static final int POURS_EVERY = 40;

    private final NonNullList<ItemStack> contents = NonNullList.withSize(2, ItemStack.EMPTY);
    private AspectList held = AspectList.EMPTY;
    private int burning;
    private int burnsFor;
    private int cooked;
    private int cooksFor;
    private int counter;

    /**
     * What the screen needs that is not an item: how far the fuel and the work have got.
     * <p>
     * What is dissolved is not in here. A list of aspects will not fit through four integers, and the screen reads
     * it off the block entity itself, which is synced for the sake of the gauges anyway.
     */
    private final ContainerData readings = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case READING_BURNING -> burning;
                case READING_BURNS_FOR -> burnsFor;
                case READING_COOKED -> cooked;
                case READING_COOKS_FOR -> cooksFor;
                case READING_HELD -> held.total();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return READINGS;
        }
    };

    public ContainerData readings() {
        return readings;
    }

    public EssentiaSmelterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ESSENTIA_SMELTER.get(), pos, state);
    }

    @Override
    public AspectList held() {
        return held;
    }

    /**
     * Lets a tube take one. It gives but never takes: a smelter makes essentia, and anything piped back into one
     * would only sit there, since nothing in it ever reads what it is holding.
     */
    @Override
    public boolean release(Holder<Aspect> aspect) {
        if (held.get(aspect) <= 0) {
            return false;
        }
        held = held.reduce(aspect, 1);
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
        setChanged();
        return true;
    }

    public boolean lit() {
        return burning > 0;
    }

    // --- ticking -----------------------------------------------------------

    public static void tick(Level level, BlockPos pos, BlockState state, EssentiaSmelterBlockEntity smelter) {
        boolean wasLit = smelter.burning > 0;
        AspectList was = smelter.held;
        if (smelter.burning > 0) {
            smelter.burning--;
        }

        smelter.pour(level, pos);
        smelter.burn(level);
        smelter.cook();

        if (wasLit != smelter.burning > 0) {
            level.setBlock(pos, state.setValue(EssentiaSmelterBlock.LIT, smelter.burning > 0), Block.UPDATE_ALL);
        }
        // what is dissolved is read off the block entity by the screen, so a change has to reach the client
        if (!was.equals(smelter.held)) {
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
        smelter.setChanged();
    }

    /** Lets one point go up into the vessels above, now and then. A full smelter is one nobody emptied. */
    private void pour(Level level, BlockPos pos) {
        if (counter++ < POURS_EVERY || held.isEmpty()) {
            return;
        }
        counter = 0;
        for (Holder<Aspect> aspect : held.sortedByAmount()) {
            if (held.get(aspect) > 0 && AlembicBlockEntity.pourInto(level, pos, aspect)) {
                held = held.reduce(aspect, 1);
                return;
            }
        }
    }

    /** Lights itself from the fuel slot when there is work to do and nothing already burning. */
    private void burn(Level level) {
        if (burning > 0 || !canCook()) {
            return;
        }
        ItemStack fuel = contents.get(SLOT_FUEL);
        int lasts = fuel.isEmpty() ? 0 : fuel.getBurnTime(RecipeType.SMELTING);
        if (lasts <= 0) {
            return;
        }
        burning = lasts;
        burnsFor = lasts;
        ItemStack left = fuel.getItem().hasCraftingRemainingItem()
                ? new ItemStack(fuel.getItem().getCraftingRemainingItem())
                : ItemStack.EMPTY;
        fuel.shrink(1);
        if (fuel.isEmpty()) {
            contents.set(SLOT_FUEL, left);
        }
    }

    private void cook() {
        if (burning <= 0 || !canCook()) {
            cooked = 0;
            return;
        }
        ItemStack input = contents.get(SLOT_INPUT);
        AspectList made = Aspects.of(input);
        cooksFor = made.total() * PER_ASPECT;
        if (++cooked < cooksFor) {
            return;
        }
        cooked = 0;
        held = held.add(made);
        input.shrink(1);
    }

    /** Whether there is something worth burning, and room for what would come off it. */
    private boolean canCook() {
        ItemStack input = contents.get(SLOT_INPUT);
        if (input.isEmpty()) {
            return false;
        }
        AspectList made = Aspects.of(input);
        return !made.isEmpty() && made.total() <= CAPACITY - held.total();
    }

    /** What a broken smelter lets go. The essentia in it has nowhere to go but out. */
    public void spill() {
        held = AspectList.EMPTY;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.alchemia.essentia_smelter");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new EssentiaSmelterMenu(id, inventory, this, readings,
                ContainerLevelAccess.create(level, worldPosition), worldPosition);
    }

    public NonNullList<ItemStack> contents() {
        return contents;
    }

    // --- Container, so a hopper can work one ------------------------------

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
        return ContainerHelper.removeItem(contents, slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(contents, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        contents.set(slot, stack);
        setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == SLOT_FUEL
                ? stack.getBurnTime(RecipeType.SMELTING) > 0
                : !Aspects.of(stack).isEmpty();
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getCenter()) <= 64.0D;
    }

    @Override
    public void clearContent() {
        contents.clear();
    }

    /** Fed from above like a furnace, fuelled from the side, and nothing ever comes back out of the bottom. */
    @Override
    public int[] getSlotsForFace(Direction side) {
        return switch (side) {
            case UP -> new int[] {SLOT_INPUT};
            case DOWN -> new int[0];
            default -> new int[] {SLOT_FUEL};
        };
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) {
        return canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return false;
    }

    // --- saving ------------------------------------------------------------

    /** The gauges and the tooltip are drawn from this, so it has to reach the client without anyone asking. */
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        contents.clear();
        ContainerHelper.loadAllItems(tag, contents, registries);
        burning = tag.getInt("burning");
        burnsFor = tag.getInt("burnsFor");
        cooked = tag.getInt("cooked");
        held = AspectList.CODEC
                .parse(registries.createSerializationContext(NbtOps.INSTANCE), tag.get("held"))
                .result().orElse(AspectList.EMPTY);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, contents, registries);
        tag.putInt("burning", burning);
        tag.putInt("burnsFor", burnsFor);
        tag.putInt("cooked", cooked);
        AspectList.CODEC
                .encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), held)
                .result().ifPresent(written -> tag.put("held", written));
    }
}
