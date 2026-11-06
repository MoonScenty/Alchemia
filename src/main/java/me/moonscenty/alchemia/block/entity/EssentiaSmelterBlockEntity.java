package me.moonscenty.alchemia.block.entity;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.aspect.Aspects;
import me.moonscenty.alchemia.block.EssentiaSmelterBlock;
import me.moonscenty.alchemia.essentia.EssentiaHolder;
import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.block.SmelterAuxBlock;
import me.moonscenty.alchemia.block.SmelterVentBlock;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.menu.EssentiaSmelterMenu;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
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
import net.minecraft.world.level.block.state.BlockState;

public class EssentiaSmelterBlockEntity extends SyncedBlockEntity implements WorldlyContainer, MenuProvider, EssentiaHolder {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_FUEL = 1;

    public static final int READING_BURNING = 0;
    public static final int READING_BURNS_FOR = 1;
    public static final int READING_COOKED = 2;
    public static final int READING_COOKS_FOR = 3;
    public static final int READING_HELD = 4;
    public static final int READINGS = 5;

    public static final int CAPACITY = 50;
    private static final int PER_ASPECT = 10;
    private static final float BELLOWS_HURRY = 0.125F;
    // alumentum scales the pour interval (original's value)
    private static final float HURRIED = 0.8F;
    // flux is kept two thirds as often as other aspects (original)
    private static final float FLUX_KEPT = 0.66F;
    // chance per vent, tried in turn
    private static final float VENT_CATCHES = 0.333F;
    public static final int VENTED = 1;

    private final NonNullList<ItemStack> contents = NonNullList.withSize(2, ItemStack.EMPTY);
    private AspectList held = AspectList.EMPTY;
    private int burning;
    private int burnsFor;
    private int cooked;
    private int cooksFor;
    private int counter;
    private boolean hurried;

    // dissolved aspects do not fit in ContainerData; the screen reads the synced block entity
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

    @Override
    public boolean release(Holder<Aspect> aspect) {
        if (held.get(aspect) <= 0) {
            return false;
        }
        held = held.reduce(aspect, 1);
        changed();
        return true;
    }

    public boolean lit() {
        return burning > 0;
    }

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
        if (!was.equals(smelter.held)) {
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
        smelter.setChanged();
    }

    private EssentiaSmelterBlock.Tier tier() {
        return getBlockState().getBlock() instanceof EssentiaSmelterBlock smelter
                ? smelter.tier() : EssentiaSmelterBlock.Tier.BASIC;
    }

    // keeps pouring after the fire is out
    private void pour(Level level, BlockPos pos) {
        int every = tier().poursEvery();
        if (hurried) {
            every = (int) (every * HURRIED);
        }
        if (++counter < every || held.isEmpty()) {
            return;
        }
        counter = 0;
        pourUp(level, pos);
        for (Direction side : SmelterAuxBlock.SIDES) {
            BlockState beside = level.getBlockState(pos.relative(side));
            if (beside.getBlock() instanceof SmelterAuxBlock
                    && beside.getValue(SmelterAuxBlock.FACING) == side.getOpposite()) {
                pourUp(level, pos.relative(side));
            }
        }
    }

    private void pourUp(Level level, BlockPos from) {
        for (Holder<Aspect> aspect : held.sortedByAmount()) {
            if (held.get(aspect) > 0 && AlembicBlockEntity.pourInto(level, from, aspect)) {
                held = held.reduce(aspect, 1);
                return;
            }
        }
    }

    private void burn(Level level) {
        if (burning > 0 || !canCook()) {
            return;
        }
        ItemStack fuel = contents.get(SLOT_FUEL);
        int lasts = fuel.isEmpty() ? 0 : fuel.getBurnTime(RecipeType.SMELTING);
        hurried = fuel.is(ModItems.ALUMENTUM.get());
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
        cooksFor = Math.max(1, (int) (made.total() * PER_ASPECT * (1.0F - BELLOWS_HURRY * bellows())));
        if (++cooked < cooksFor) {
            return;
        }
        cooked = 0;
        held = held.add(kept(made));
        input.shrink(1);
    }

    private AspectList kept(AspectList made) {
        if (level == null) {
            return made;
        }
        float keeps = tier().efficiency();
        AspectList kept = made;
        int lost = 0;
        for (Holder<Aspect> aspect : made.sortedByName()) {
            float threshold = aspect.value() == ModAspects.FLUX.value() ? keeps * FLUX_KEPT : keeps;
            for (int point = 0; point < made.get(aspect); point++) {
                if (level.random.nextFloat() > threshold) {
                    kept = kept.reduce(aspect, 1);
                    lost++;
                }
            }
        }
        int polluting = 0;
        for (int point = 0; point < lost; point++) {
            if (!vented()) {
                polluting++;
            }
        }
        if (polluting > 0) {
            AuraHandler.add(level, worldPosition, ModAspects.FLUX, polluting);
        }
        return kept;
    }

    private boolean vented() {
        for (Direction side : SmelterAuxBlock.SIDES) {
            BlockState beside = level.getBlockState(worldPosition.relative(side));
            if (beside.getBlock() instanceof SmelterVentBlock
                    && beside.getValue(SmelterVentBlock.FACING) == side.getOpposite()
                    && level.random.nextFloat() < VENT_CATCHES) {
                level.blockEvent(worldPosition, getBlockState().getBlock(), VENTED, side.get3DDataValue());
                return true;
            }
        }
        return false;
    }

    private int bellows() {
        if (level == null || !getBlockState().hasProperty(me.moonscenty.alchemia.block.EssentiaSmelterBlock.FACING)) {
            return 0;
        }
        net.minecraft.core.Direction front = getBlockState().getValue(me.moonscenty.alchemia.block.EssentiaSmelterBlock.FACING);
        return me.moonscenty.alchemia.block.BellowsBlock.blowingOn(level, worldPosition,
                front.getClockWise(), front.getCounterClockWise(), front.getOpposite());
    }

    private boolean canCook() {
        ItemStack input = contents.get(SLOT_INPUT);
        if (input.isEmpty()) {
            return false;
        }
        AspectList made = Aspects.of(input);
        return !made.isEmpty() && made.total() <= CAPACITY - held.total();
    }

    public void spill() {
        if (level != null && !level.isClientSide && !held.isEmpty()) {
            AuraHandler.add(level, worldPosition, ModAspects.FLUX, held.total());
        }
        held = AspectList.EMPTY;
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new EssentiaSmelterMenu(id, inventory, this, readings,
                ContainerLevelAccess.create(level, worldPosition), worldPosition);
    }

    public NonNullList<ItemStack> contents() {
        return contents;
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
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        contents.clear();
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        // sides take what is smelted, the bottom takes fuel, the top takes nothing [TileSmelter]
        return switch (side) {
            case UP -> new int[0];
            case DOWN -> new int[] {SLOT_FUEL};
            default -> new int[] {SLOT_INPUT};
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

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        contents.clear();
        ContainerHelper.loadAllItems(tag, contents, registries);
        burning = tag.getInt("burning");
        burnsFor = tag.getInt("burnsFor");
        cooked = tag.getInt("cooked");
        hurried = tag.getBoolean("hurried");
        held = AspectList.read(tag, "held", registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, contents, registries);
        tag.putInt("burning", burning);
        tag.putInt("burnsFor", burnsFor);
        tag.putInt("cooked", cooked);
        tag.putBoolean("hurried", hurried);
        AspectList.write(tag, "held", held, registries);
    }
}
