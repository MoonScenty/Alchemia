package me.moonscenty.alchemia.block.entity;

import me.moonscenty.alchemia.menu.SpaMenu;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import me.moonscenty.alchemia.registry.ModFluids;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
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
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

/**
 * A spa at work: a tank, a pocket of bath salts, and which of the two it is pouring.
 *
 * <p>Every two seconds, unless a redstone signal holds it, it pours a bucket's worth into the world above it. Mixing,
 * it pours purifying fluid, made of a bucket of water and a dose of salts. Not mixing, it pours whatever is in its
 * tank, so long as that is something that can stand in the world. The first bucket goes on top of it; after that,
 * each goes into a place beside the pool already there, within two blocks of the middle either way, so that the pool
 * spreads out to five by five, a block at a time, wherever there is floor to hold it.
 */
public class SpaBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider {
    /** What the tank holds, how much a pour takes, and how often it pours. The original's. */
    public static final int CAPACITY = 5000;
    private static final int POUR = 1000;
    private static final int EVERY = 40;
    /** How far from the middle the pool spreads, either way. */
    private static final int SPREADS = 2;

    /** What the screen is told: how much is in the tank, of what, and whether it is mixing. */
    public static final int READING_AMOUNT = 0;
    public static final int READING_FLUID = 1;
    public static final int READING_MIXING = 2;
    public static final int READINGS = 3;

    private final NonNullList<ItemStack> salts = NonNullList.withSize(1, ItemStack.EMPTY);
    private final FluidTank tank = new FluidTank(CAPACITY) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };
    private boolean mixing = true;
    private int ticks;

    private final ContainerData readings = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case READING_AMOUNT -> tank.getFluidAmount();
                case READING_FLUID -> BuiltInRegistries.FLUID.getId(tank.getFluid().getFluid());
                case READING_MIXING -> mixing ? 1 : 0;
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

    public SpaBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SPA.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SpaBlockEntity spa) {
        if (spa.ticks++ % EVERY == 0 && !level.hasNeighborSignal(pos) && spa.ready()) {
            spa.pour(level);
        }
    }

    /** Whether it has what it needs for a pour. */
    private boolean ready() {
        if (tank.getFluidAmount() < POUR) {
            return false;
        }
        if (mixing) {
            return tank.getFluid().is(Fluids.WATER) && salts.getFirst().is(ModItems.BATH_SALTS.get());
        }
        return !pouring().defaultFluidState().createLegacyBlock().isAir();
    }

    /** What it pours: purifying fluid, if mixing, or else what is in the tank. */
    private Fluid pouring() {
        return mixing ? ModFluids.PURIFYING.get() : tank.getFluid().getFluid();
    }

    private void pour(Level level) {
        Fluid fluid = pouring();
        BlockState pool = fluid.defaultFluidState().createLegacyBlock();
        BlockPos above = worldPosition.above();
        if (level.getBlockState(above) == pool) {
            for (int x = -SPREADS; x <= SPREADS; x++) {
                for (int z = -SPREADS; z <= SPREADS; z++) {
                    BlockPos into = above.offset(x, 0, z);
                    if (fits(level, into, pool) && touches(level, into, pool)) {
                        pourAt(level, into, pool);
                        return;
                    }
                }
            }
        } else if (fits(level, above, pool)) {
            pourAt(level, above, pool);
        }
    }

    /** Whether a pool can be poured here: floor under it, room in it, and not already a still pool of it. */
    private static boolean fits(Level level, BlockPos at, BlockState pool) {
        if (pool.getFluidState().is(Fluids.WATER) && level.dimensionType().ultraWarm()) {
            return false;
        }
        BlockState there = level.getBlockState(at);
        BlockPos below = at.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP) && there.canBeReplaced()
                && there != pool;
    }

    private static boolean touches(Level level, BlockPos at, BlockState pool) {
        for (Direction side : Direction.values()) {
            if (level.getBlockState(at.relative(side)) == pool) {
                return true;
            }
        }
        return false;
    }

    private void pourAt(Level level, BlockPos at, BlockState pool) {
        if (mixing) {
            salts.getFirst().shrink(1);
        }
        tank.drain(POUR, IFluidHandler.FluidAction.EXECUTE);
        level.setBlock(at, pool, Block.UPDATE_ALL);
        setChanged();
    }

    /** Mixing, or pouring what is in the tank as it is. */
    public void toggleMixing() {
        mixing = !mixing;
        setChanged();
    }

    public boolean mixing() {
        return mixing;
    }

    public FluidTank tank() {
        return tank;
    }

    /** The tank as pipes and buckets see it: filled from any side, drawn from any but the top. */
    public IFluidHandler tank(Direction side) {
        return side == Direction.UP ? new IFluidHandler() {
            @Override
            public int getTanks() {
                return tank.getTanks();
            }

            @Override
            public FluidStack getFluidInTank(int which) {
                return tank.getFluidInTank(which);
            }

            @Override
            public int getTankCapacity(int which) {
                return tank.getTankCapacity(which);
            }

            @Override
            public boolean isFluidValid(int which, FluidStack stack) {
                return tank.isFluidValid(which, stack);
            }

            @Override
            public int fill(FluidStack resource, FluidAction action) {
                return tank.fill(resource, action);
            }

            @Override
            public FluidStack drain(FluidStack resource, FluidAction action) {
                return FluidStack.EMPTY;
            }

            @Override
            public FluidStack drain(int maxDrain, FluidAction action) {
                return FluidStack.EMPTY;
            }
        } : tank;
    }

    // --- the pocket of salts ---------------------------------------------------------------------------------

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return salts.getFirst().isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return salts.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack taken = ContainerHelper.removeItem(salts, slot, amount);
        setChanged();
        return taken;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack taken = ContainerHelper.takeItem(salts, slot);
        setChanged();
        return taken;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        salts.set(slot, stack);
        stack.limitSize(getMaxStackSize(stack));
        setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return stack.is(ModItems.BATH_SALTS.get());
    }

    @Override
    public boolean stillValid(Player player) {
        return net.minecraft.world.Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        salts.clear();
    }

    /** Salts go in from any side but the top, and come out the same way. */
    @Override
    public int[] getSlotsForFace(Direction side) {
        return side == Direction.UP ? new int[0] : new int[] {0};
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) {
        return side != Direction.UP && canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return side != Direction.UP;
    }

    // --- the screen ------------------------------------------------------------------------------------------

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new SpaMenu(id, inventory, this, readings, ContainerLevelAccess.create(level, worldPosition));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        salts.clear();
        ContainerHelper.loadAllItems(tag, salts, registries);
        tank.readFromNBT(registries, tag.getCompound("tank"));
        mixing = !tag.contains("mixing") || tag.getBoolean("mixing");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, salts, registries);
        tag.put("tank", tank.writeToNBT(registries, new CompoundTag()));
        tag.putBoolean("mixing", mixing);
    }
}
