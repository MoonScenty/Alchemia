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
    /** What each pair of bellows blowing on it takes off the time anything takes to boil down. */
    private static final float BELLOWS_HURRY = 0.125F;
    /** What alumentum in the fire does to the wait between pours: four fifths of it, as the original had it. */
    private static final float HURRIED = 0.8F;
    /**
     * How much harder flux is to keep than anything else. The original held on to a point of flux only two thirds
     * as often as a point of anything else, so it is the first thing a smelter loses.
     */
    private static final float FLUX_KEPT = 0.66F;
    /** What a vent catches of each point lost, tried vent by vent until one catches it. */
    private static final float VENT_CATCHES = 0.333F;
    /** The block event a vent's puff is sent to the client as. */
    public static final int VENTED = 1;

    private final NonNullList<ItemStack> contents = NonNullList.withSize(2, ItemStack.EMPTY);
    private AspectList held = AspectList.EMPTY;
    private int burning;
    private int burnsFor;
    private int cooked;
    private int cooksFor;
    private int counter;
    /** Whether what is burning now was alumentum, remembered because the fuel is gone by the time it matters. */
    private boolean hurried;

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

    /** What kind of smelter it is, which is settled by the block it is in. */
    private EssentiaSmelterBlock.Tier tier() {
        return getBlockState().getBlock() instanceof EssentiaSmelterBlock smelter
                ? smelter.tier() : EssentiaSmelterBlock.Tier.BASIC;
    }

    /**
     * Lets one point go up into the vessels above, now and then, and one more up through every pump beside it.
     * A full smelter is one nobody emptied.
     * <p>
     * It pours whether it is burning or not: what has already boiled off goes on rising after the fire is out.
     */
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

    /** One point into the column of vessels standing on the given block, if any of them will take one. */
    private void pourUp(Level level, BlockPos from) {
        for (Holder<Aspect> aspect : held.sortedByAmount()) {
            if (held.get(aspect) > 0 && AlembicBlockEntity.pourInto(level, from, aspect)) {
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

    /**
     * What survives the boiling. Every point is a chance: a smelter keeps four in five of what goes into it, a
     * better one more. What is lost goes up as flux, unless a vent beside it catches it first.
     */
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

    /** Whether a vent beside it caught a point of flux, and if one did, its puff. */
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

    /**
     * How many bellows are blowing on it: from either side and from behind, never from the front, which is
     * where the fire is fed. Three at most, as the original counted.
     */
    private int bellows() {
        if (level == null || !getBlockState().hasProperty(me.moonscenty.alchemia.block.EssentiaSmelterBlock.FACING)) {
            return 0;
        }
        net.minecraft.core.Direction front = getBlockState().getValue(me.moonscenty.alchemia.block.EssentiaSmelterBlock.FACING);
        return me.moonscenty.alchemia.block.BellowsBlock.blowingOn(level, worldPosition,
                front.getClockWise(), front.getCounterClockWise(), front.getOpposite());
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

    /** What a broken smelter lets go. The essentia in it has nowhere to go but out, as flux, as the original's did. */
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
        hurried = tag.getBoolean("hurried");
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
        tag.putBoolean("hurried", hurried);
        AspectList.CODEC
                .encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), held)
                .result().ifPresent(written -> tag.put("held", written));
    }
}
