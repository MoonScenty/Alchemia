package me.moonscenty.alchemia.block.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.block.MnemonicMatrixBlock;
import me.moonscenty.alchemia.block.ThaumatoriumBlock;
import me.moonscenty.alchemia.block.TubeBlock;
import me.moonscenty.alchemia.crafting.CrucibleRecipe;
import me.moonscenty.alchemia.crafting.ModRecipes;
import me.moonscenty.alchemia.essentia.EssentiaHolder;
import me.moonscenty.alchemia.menu.ThaumatoriumMenu;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import me.moonscenty.alchemia.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

/**
 * The thaumatorium: a crucible that works on its own.
 * <p>
 * It is told which crucible recipes to make, one plus two for every mnemonic matrix fixed to it. Given a catalyst
 * in its slot, it takes the first of those that catalyst finishes, draws the essentia it needs up the pipes one
 * aspect at a time in order of name, and when everything is in, uses one of the catalyst and puts out what was
 * made, into a container in front of it if there is one with room and onto the ground if there is none. A full
 * container in front stops it.
 * <p>
 * It works on the heat under its crucible -- lava, fire or a nitor directly below -- and a redstone signal at
 * either half or at the crucible stops it. Nothing is lost and no flux is made: everything that goes in comes out.
 * <p>
 * Both halves have one of these, and the upper one hands everything to the lower, which is the one that works.
 */
public class ThaumatoriumBlockEntity extends BlockEntity implements EssentiaHolder, WorldlyContainer, MenuProvider {
    private static final int LOOKS_ROUND_EVERY = 40;
    private static final int WORKS_EVERY = 5;
    /** The block event that sets the client's steam going, and for how long. */
    public static final int VENTS = 0;
    private static final int VENTS_FOR = 7;
    /** How many recipes it holds with no matrices, and how many each matrix adds. */
    private static final int REMEMBERS = 1;
    private static final int A_MATRIX_ADDS = 2;

    private final NonNullList<ItemStack> catalyst = NonNullList.withSize(1, ItemStack.EMPTY);
    private AspectList essentia = AspectList.EMPTY;
    /** The recipes it has been told to make, in the order it was told. */
    private final List<ResourceLocation> recipes = new ArrayList<>();
    private int remembers = REMEMBERS;
    @Nullable
    private Holder<Aspect> drawing;
    private boolean heated;
    private int counter;
    private int venting;

    public ThaumatoriumBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.THAUMATORIUM.get(), pos, state);
    }

    // --- the two halves ----------------------------------------------------

    private boolean isTop() {
        return getBlockState().getBlock() instanceof ThaumatoriumBlock && getBlockState().getValue(ThaumatoriumBlock.TOP);
    }

    /** The half that works: this one, or the one below it. */
    @Nullable
    public ThaumatoriumBlockEntity main() {
        if (!isTop()) {
            return this;
        }
        return level != null && level.getBlockEntity(worldPosition.below()) instanceof ThaumatoriumBlockEntity below
                && !below.isTop() ? below : null;
    }

    private Direction front() {
        return getBlockState().getValue(ThaumatoriumBlock.FACING);
    }

    // --- ticking -----------------------------------------------------------

    public static void serverTick(Level level, BlockPos pos, BlockState state, ThaumatoriumBlockEntity it) {
        if (it.isTop()) {
            return;
        }
        if (it.counter % LOOKS_ROUND_EVERY == 0) {
            it.heated = heated(level, pos);
            it.countMatrices(level, pos);
        }
        it.counter++;
        if (!it.heated || it.powered(level, pos) || it.counter % WORKS_EVERY != 0 || it.recipes.isEmpty()) {
            return;
        }
        ItemStack catalyst = it.catalyst.get(0);
        RecipeHolder<CrucibleRecipe> recipe = catalyst.isEmpty() ? null : it.current(level);
        if (recipe == null) {
            it.drawing = null;
            return;
        }
        ItemStack made = recipe.value().result().copy();
        IItemHandler container = it.inFront(level, pos);
        if (container != null && !ItemHandlerHelper.insertItemStacked(container, made, true).isEmpty()) {
            return;
        }
        it.drawing = null;
        AspectList wants = recipe.value().aspects();
        for (Holder<Aspect> aspect : wants.sortedByName()) {
            if (it.essentia.get(aspect) < wants.get(aspect)) {
                it.drawing = aspect;
                break;
            }
        }
        if (it.drawing == null) {
            it.complete(level, pos, made, container);
        } else {
            it.drawFromBeside(level, pos);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, ThaumatoriumBlockEntity it) {
        if (it.venting <= 0) {
            return;
        }
        it.venting--;
        Direction front = it.front();
        var random = level.random;
        level.addParticle(net.minecraft.core.particles.ParticleTypes.CLOUD,
                pos.getX() + 0.5 + jitter(random) + front.getStepX() / 2.0, pos.getY() + 0.5 + jitter(random),
                pos.getZ() + 0.5 + jitter(random) + front.getStepZ() / 2.0,
                front.getStepX() / 16.0 + jitter(random) / 4.0, jitter(random) / 4.0,
                front.getStepZ() / 16.0 + jitter(random) / 4.0);
    }

    private static double jitter(net.minecraft.util.RandomSource random) {
        return 0.1 - random.nextFloat() * 0.2;
    }

    /** Heat directly under the crucible: lava, fire, or a nitor. */
    private static boolean heated(Level level, BlockPos pos) {
        BlockPos under = pos.below(2);
        BlockState state = level.getBlockState(under);
        return state.getFluidState().is(FluidTags.LAVA) || state.getBlock() instanceof BaseFireBlock
                || state.getBlock() instanceof me.moonscenty.alchemia.block.NitorBlock;
    }

    private boolean powered(Level level, BlockPos pos) {
        return level.hasNeighborSignal(pos) || level.hasNeighborSignal(pos.below())
                || level.hasNeighborSignal(pos.above());
    }

    /** One plus two for every matrix fixed to it facing in: on either half's sides, and on top of the upper one. */
    private void countMatrices(Level level, BlockPos pos) {
        int matrices = 0;
        for (int layer = 0; layer < 2; layer++) {
            for (Direction side : Direction.values()) {
                if (side == Direction.DOWN || side == front() || (layer == 0 && side == Direction.UP)) {
                    continue;
                }
                BlockState there = level.getBlockState(pos.above(layer).relative(side));
                if (there.getBlock() instanceof MnemonicMatrixBlock
                        && there.getValue(MnemonicMatrixBlock.FACING) == side.getOpposite()) {
                    matrices++;
                }
            }
        }
        int now = REMEMBERS + A_MATRIX_ADDS * matrices;
        if (now != remembers) {
            remembers = now;
            while (recipes.size() > remembers) {
                recipes.remove(recipes.size() - 1);
            }
            changed();
        }
    }

    /** The first recipe it was told to make that the catalyst in its slot finishes. */
    @Nullable
    private RecipeHolder<CrucibleRecipe> current(Level level) {
        for (ResourceLocation id : recipes) {
            Optional<RecipeHolder<CrucibleRecipe>> found = recipe(level, id);
            if (found.isPresent() && found.get().value().catalyst().test(catalyst.get(0))) {
                return found.get();
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static Optional<RecipeHolder<CrucibleRecipe>> recipe(Level level, ResourceLocation id) {
        return level.getRecipeManager().byKey(id)
                .filter(holder -> holder.value().getType() == ModRecipes.CRUCIBLE.get())
                .map(holder -> (RecipeHolder<CrucibleRecipe>) holder);
    }

    @Nullable
    private IItemHandler inFront(Level level, BlockPos pos) {
        return level.getCapability(Capabilities.ItemHandler.BLOCK, pos.relative(front()), front().getOpposite());
    }

    /** One point straight out of a vessel set against a side; a pipe there fills it on its own. */
    private void drawFromBeside(Level level, BlockPos pos) {
        for (int layer = 0; layer < 2; layer++) {
            for (Direction side : Direction.values()) {
                if (side == front() || side == Direction.DOWN || (layer == 0 && side == Direction.UP)) {
                    continue;
                }
                BlockPos beside = pos.above(layer).relative(side);
                if (level.getBlockState(beside).getBlock() instanceof TubeBlock
                        || !(level.getBlockEntity(beside) instanceof EssentiaHolder source)
                        || source instanceof ThaumatoriumBlockEntity
                        || !source.reachableFrom(side.getOpposite()) || !source.givesTo(side.getOpposite())) {
                    continue;
                }
                if (source.held().get(drawing) > 0 && source.release(drawing)) {
                    accept(drawing);
                    return;
                }
            }
        }
    }

    private void complete(Level level, BlockPos pos, ItemStack made, @Nullable IItemHandler container) {
        catalyst.get(0).shrink(1);
        essentia = AspectList.EMPTY;
        ItemStack left = container == null ? made : ItemHandlerHelper.insertItemStacked(container, made, false);
        if (!left.isEmpty()) {
            Direction front = front();
            ItemEntity dropped = new ItemEntity(level, pos.getX() + 0.5 + front.getStepX() * 0.66,
                    pos.getY() + 0.33, pos.getZ() + 0.5 + front.getStepZ() * 0.66, left);
            dropped.setDeltaMovement(0.075 * front.getStepX(), 0.025, 0.075 * front.getStepZ());
            level.addFreshEntity(dropped);
            level.blockEvent(pos, getBlockState().getBlock(), VENTS, 0);
        }
        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.25F,
                2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);
        changed();
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id == VENTS) {
            venting = VENTS_FOR;
            return true;
        }
        return super.triggerEvent(id, param);
    }

    // --- what it is told to make --------------------------------------------

    public List<ResourceLocation> recipes() {
        ThaumatoriumBlockEntity main = main();
        return main == null ? List.of() : List.copyOf(main.recipes);
    }

    public int remembers() {
        ThaumatoriumBlockEntity main = main();
        return main == null ? REMEMBERS : main.remembers;
    }

    /** Tells it to make this, or to stop making it if it already was. No more than it can remember. */
    public void toggle(ResourceLocation recipe) {
        ThaumatoriumBlockEntity main = main();
        if (main == null) {
            return;
        }
        if (!main.recipes.remove(recipe)) {
            if (main.recipes.size() >= main.remembers) {
                return;
            }
            main.recipes.add(recipe);
        }
        main.changed();
    }

    public AspectList essentia() {
        ThaumatoriumBlockEntity main = main();
        return main == null ? AspectList.EMPTY : main.essentia;
    }

    // --- essentia ----------------------------------------------------------

    @Override
    public AspectList held() {
        return AspectList.EMPTY;
    }

    /** Only the aspect it is drawing on now, and only as much of it as the recipe still wants. */
    @Override
    public boolean wants(Holder<Aspect> aspect) {
        ThaumatoriumBlockEntity main = main();
        if (main == null || main.drawing == null || main.drawing.value() != aspect.value() || main.level == null) {
            return false;
        }
        RecipeHolder<CrucibleRecipe> recipe = main.current(main.level);
        return recipe != null && main.essentia.get(aspect) < recipe.value().aspects().get(aspect);
    }

    @Override
    public boolean accept(Holder<Aspect> aspect) {
        ThaumatoriumBlockEntity main = main();
        if (main == null || !wants(aspect)) {
            return false;
        }
        main.essentia = main.essentia.add(aspect, 1);
        main.changed();
        return true;
    }

    /** Pipes meet it on every face but the front. */
    @Override
    public boolean reachableFrom(Direction side) {
        return side != front();
    }

    @Override
    public boolean givesTo(Direction side) {
        return false;
    }

    private void changed() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    // --- the catalyst slot -------------------------------------------------

    public NonNullList<ItemStack> contents() {
        ThaumatoriumBlockEntity main = main();
        return main == null ? NonNullList.withSize(1, ItemStack.EMPTY) : main.catalyst;
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return contents().get(0).isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return contents().get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack taken = ContainerHelper.removeItem(contents(), slot, amount);
        mainChanged();
        return taken;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(contents(), slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        contents().set(slot, stack);
        mainChanged();
    }

    private void mainChanged() {
        ThaumatoriumBlockEntity main = main();
        if (main != null) {
            main.setChanged();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getCenter()) <= 64.0;
    }

    @Override
    public void clearContent() {
        contents().clear();
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return new int[] {0};
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) {
        return true;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return true;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.alchemia.thaumatorium");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        ThaumatoriumBlockEntity main = main();
        return main == null ? null : new ThaumatoriumMenu(id, inventory, main);
    }

    // --- saving ------------------------------------------------------------

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        catalyst.clear();
        ContainerHelper.loadAllItems(tag, catalyst, registries);
        remembers = Math.max(REMEMBERS, tag.getInt("maxrec"));
        essentia = AspectList.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag.get("Aspects"))
                .result().orElse(AspectList.EMPTY);
        recipes.clear();
        for (Tag entry : tag.getList("recipes", Tag.TAG_STRING)) {
            recipes.add(ResourceLocation.parse(entry.getAsString()));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, catalyst, registries);
        tag.putInt("maxrec", remembers);
        AspectList.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), essentia).result()
                .ifPresent(written -> tag.put("Aspects", written));
        ListTag listed = new ListTag();
        recipes.forEach(id -> listed.add(StringTag.valueOf(id.toString())));
        tag.put("recipes", listed);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    /** For the blocks around it: is this the half that holds the catalyst. */
    public boolean holdsCatalyst() {
        return !isTop();
    }

    /** Whether this is a working thaumatorium's lower half, for the blocks that check on it. */
    public static boolean isThaumatorium(BlockState state) {
        return state.is(ModBlocks.THAUMATORIUM.get());
    }
}
