package me.moonscenty.alchemia.block.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import me.moonscenty.alchemia.block.PatternCrafterBlock;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

public class PatternCrafterBlockEntity extends SyncedBlockEntity {
    // grid slots per pattern, in dial order
    public static final int[][] PATTERNS = {
            {0, 1, 2, 3, 4, 5, 6, 7, 8},
            {0},
            {0, 1},
            {0, 3},
            {0, 1, 3, 4},
            {0, 1, 2},
            {0, 3, 6},
            {0, 1, 2, 3, 4, 5},
            {0, 1, 3, 4, 6, 7},
            {0, 1, 2, 3, 5, 6, 7, 8}};
    public static final int EVERY = 20;
    private static final int MADE = 1;
    private static final int SPIN = 10;

    private int mode;
    private int count;
    private int driven;
    private float speed;
    private float angle;
    private float lastAngle;

    public PatternCrafterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PATTERN_CRAFTER.get(), pos, state);
        this.count = (int) (pos.asLong() & 0x7FFFFFFF) % EVERY;
    }

    public int mode() {
        return mode;
    }

    public void cycle() {
        mode = (mode + 1) % PATTERNS.length;
        changed();
    }

    public float angle(float partial) {
        return lastAngle + (angle - lastAngle) * partial;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PatternCrafterBlockEntity crafter) {
        if (crafter.count++ % EVERY == 0 && state.getValue(PatternCrafterBlock.ENABLED)
                && level instanceof ServerLevel server) {
            crafter.craft(server);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, PatternCrafterBlockEntity crafter) {
        if (crafter.driven > 0) {
            crafter.driven--;
            crafter.speed++;
        } else {
            crafter.speed *= 0.8F;
        }
        crafter.lastAngle = crafter.angle;
        crafter.angle += crafter.speed;
    }

    public boolean craft(ServerLevel level) {
        IItemHandler above = level.getCapability(Capabilities.ItemHandler.BLOCK, worldPosition.above(),
                Direction.DOWN);
        IItemHandler below = level.getCapability(Capabilities.ItemHandler.BLOCK, worldPosition.below(),
                Direction.UP);
        if (above == null || below == null) {
            return false;
        }
        int[] pattern = PATTERNS[mode];
        for (int slot = 0; slot < above.getSlots(); slot++) {
            ItemStack wanted = above.getStackInSlot(slot);
            if (wanted.isEmpty() || available(above, wanted) < pattern.length) {
                continue;
            }
            List<ItemStack> grid = new ArrayList<>(NonNullList.withSize(9, ItemStack.EMPTY));
            for (int square : pattern) {
                grid.set(square, wanted.copyWithCount(1));
            }
            CraftingInput input = CraftingInput.of(3, 3, grid);
            Optional<RecipeHolder<CraftingRecipe>> found = level.getRecipeManager()
                    .getRecipeFor(RecipeType.CRAFTING, input, level);
            if (found.isEmpty()) {
                continue;
            }
            ItemStack made = found.get().value().assemble(input, level.registryAccess());
            if (made.isEmpty()) {
                continue;
            }
            NonNullList<ItemStack> left = found.get().value().getRemainingItems(input);
            if (!fits(below, made, left)) {
                continue;
            }
            ItemHandlerHelper.insertItemStacked(below, made, false);
            for (ItemStack back : left) {
                if (!back.isEmpty()) {
                    ItemHandlerHelper.insertItemStacked(below, back, false);
                }
            }
            take(above, wanted.copy(), pattern.length);
            level.blockEvent(worldPosition, getBlockState().getBlock(), MADE, 0);
            return true;
        }
        return false;
    }

    private static boolean fits(IItemHandler below, ItemStack made, List<ItemStack> left) {
        if (!ItemHandlerHelper.insertItemStacked(below, made.copy(), true).isEmpty()) {
            return false;
        }
        for (ItemStack back : left) {
            if (!back.isEmpty() && !ItemHandlerHelper.insertItemStacked(below, back.copy(), true).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static int available(IItemHandler from, ItemStack like) {
        int total = 0;
        for (int slot = 0; slot < from.getSlots(); slot++) {
            ItemStack there = from.getStackInSlot(slot);
            if (alike(like, there)) {
                total += there.getCount();
            }
        }
        return total;
    }

    private static void take(IItemHandler from, ItemStack like, int amount) {
        for (int slot = 0; slot < from.getSlots() && amount > 0; slot++) {
            if (alike(like, from.getStackInSlot(slot))) {
                amount -= from.extractItem(slot, amount, false).getCount();
            }
        }
    }

    // same item, or sharing a material tag (stands in for the original's ore dictionary)
    public static boolean alike(ItemStack a, ItemStack b) {
        if (a.isEmpty() || b.isEmpty()) {
            return false;
        }
        if (ItemStack.isSameItemSameComponents(a, b)) {
            return true;
        }
        return a.getTags().anyMatch(tag -> named(tag) && b.is(tag));
    }

    // c:ingots/iron counts, c:ingots does not
    private static boolean named(TagKey<Item> tag) {
        return tag.location().getNamespace().equals("c") && tag.location().getPath().contains("/");
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id != MADE) {
            return super.triggerEvent(id, param);
        }
        if (level != null && level.isClientSide) {
            driven = SPIN;
            level.playLocalSound(worldPosition, ModSounds.PUMP.get(), SoundSource.BLOCKS, 0.2F, 1.7F, false);
        }
        return true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putByte("type", (byte) mode);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        mode = Math.floorMod(tag.getByte("type"), PATTERNS.length);
    }
}
