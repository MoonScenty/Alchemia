package me.moonscenty.alchemia.block.entity;

import java.util.Optional;

import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.block.BellowsBlock;
import me.moonscenty.alchemia.block.InfernalFurnaceBlock;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;

/**
 * The molten heart of an infernal furnace: thirty-two squares of whatever has been dropped into it, smelted one at
 * a time with no fuel at all and spat out of the mouth.
 * <p>
 * It is slow on its own -- seven seconds an item -- and quick on fire drawn from the air, four seconds, five items to
 * every point of fire; every pair of bellows blowing into it takes a second off either. Ores and their clusters come
 * out with a nugget besides, now and then, and more often the more bellows there are. Anything that will not smelt
 * is burned to nothing. One smelt in twelve lets a point of flux out of the mouth.
 */
public class InfernalFurnaceBlockEntity extends BlockEntity implements WorldlyContainer {
    public static final int SIZE = 32;
    private static final int[] ALL = java.util.stream.IntStream.range(0, SIZE).toArray();
    private static final int SLOW = 140;
    private static final int FAST = 80;
    private static final int A_BELLOWS_SAVES = 20;
    private static final int DRAWS = 5;
    private static final int A_POINT_SMELTS = 5;
    private static final int FLUX_ONE_IN = 12;
    /** The block event the client's sparks and pops are sent as. */
    public static final int SMELTED = 3;

    private final NonNullList<ItemStack> contents = NonNullList.withSize(SIZE, ItemStack.EMPTY);
    private int cookTime;
    private int cookFor;
    private int speedy;

    public InfernalFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.INFERNAL_FURNACE.get(), pos, state);
    }

    /** The way out: the side the bars were on. */
    private Direction mouth() {
        return getBlockState().getValue(InfernalFurnaceBlock.FACING).getOpposite();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, InfernalFurnaceBlockEntity furnace) {
        boolean cooking = false;
        if (furnace.cookTime > 0) {
            furnace.cookTime--;
            cooking = true;
        }
        if (furnace.cookFor == 0) {
            furnace.cookFor = furnace.cookingTime(level);
        }
        if (furnace.cookTime > furnace.cookFor) {
            furnace.cookTime = furnace.cookFor;
        }
        if (furnace.cookTime == 0 && cooking) {
            for (int slot = 0; slot < SIZE; slot++) {
                ItemStack input = furnace.contents.get(slot);
                Optional<RecipeHolder<SmeltingRecipe>> recipe = smelting(level, input);
                if (input.isEmpty() || recipe.isEmpty()) {
                    continue;
                }
                if (furnace.speedy > 0) {
                    furnace.speedy--;
                }
                furnace.eject((ServerLevel) level, pos, recipe.get(), input);
                level.blockEvent(pos, state.getBlock(), SMELTED, 0);
                if (level.random.nextInt(FLUX_ONE_IN) == 0) {
                    AuraHandler.add(level, pos.relative(furnace.mouth()), ModAspects.FLUX, 1);
                }
                input.shrink(1);
                furnace.setChanged();
                break;
            }
        }
        if (furnace.speedy <= 0) {
            furnace.speedy = AuraHandler.drainAvailable(level, pos, ModAspects.FIRE, DRAWS) * A_POINT_SMELTS;
        }
        if (furnace.cookTime == 0 && !cooking) {
            for (int slot = 0; slot < SIZE; slot++) {
                ItemStack input = furnace.contents.get(slot);
                if (input.isEmpty()) {
                    continue;
                }
                if (smelting(level, input).isPresent()) {
                    furnace.cookFor = furnace.cookingTime(level);
                    furnace.cookTime = furnace.cookFor;
                } else {
                    furnace.burn(level, slot);
                }
                break;
            }
        }
    }

    private static Optional<RecipeHolder<SmeltingRecipe>> smelting(Level level, ItemStack input) {
        return input.isEmpty() ? Optional.empty()
                : level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(input), level);
    }

    private int cookingTime(Level level) {
        return (speedy > 0 ? FAST : SLOW) - A_BELLOWS_SAVES * bellows(level);
    }

    /** Bellows two blocks out on any side but the top, blowing in and not held still by redstone; three at most. */
    private int bellows(Level level) {
        int count = 0;
        for (Direction side : Direction.values()) {
            if (side == Direction.UP) {
                continue;
            }
            BlockState there = level.getBlockState(worldPosition.relative(side, 2));
            if (there.getBlock() instanceof BellowsBlock && there.getValue(BellowsBlock.FACING) == side.getOpposite()
                    && there.getValue(BellowsBlock.ENABLED)) {
                count++;
            }
        }
        return Math.min(3, count);
    }

    /** What was made, out of the mouth; a nugget besides now and then; and the experience a furnace would give. */
    private void eject(ServerLevel level, BlockPos pos, RecipeHolder<SmeltingRecipe> recipe, ItemStack input) {
        Direction out = mouth();
        RandomSource random = level.random;
        ItemStack made = recipe.value().assemble(new SingleRecipeInput(input), level.registryAccess());
        spit(level, pos, made, out, random);
        ItemStack bonus = bonus(input);
        if (!bonus.isEmpty()) {
            int extra = 0;
            int blowing = bellows(level);
            if (blowing == 0) {
                extra = random.nextInt(4) == 0 ? 1 : 0;
            } else {
                for (int i = 0; i < blowing; i++) {
                    if (random.nextFloat() < 0.44F) {
                        extra++;
                    }
                }
            }
            if (extra > 0) {
                spit(level, pos, bonus.copyWithCount(extra), out, random);
            }
        }
        float each = recipe.value().getExperience();
        int xp = made.getCount();
        if (each == 0.0F) {
            xp = 0;
        } else if (each < 1.0F) {
            int whole = Mth.floor(xp * each);
            if (whole < Mth.ceil(xp * each) && Math.random() < xp * each - whole) {
                whole++;
            }
            xp = whole;
        }
        if (xp > 0) {
            ExperienceOrb.award(level, new net.minecraft.world.phys.Vec3(pos.getX() + 0.5 + out.getStepX() * 1.2,
                    pos.getY() + 0.4, pos.getZ() + 0.5 + out.getStepZ() * 1.2), xp);
        }
    }

    private static void spit(ServerLevel level, BlockPos pos, ItemStack stack, Direction out, RandomSource random) {
        ItemEntity item = new ItemEntity(level, pos.getX() + 0.5 + out.getStepX() * 1.2, pos.getY() + 0.4,
                pos.getZ() + 0.5 + out.getStepZ() * 1.2, stack);
        item.setDeltaMovement(out.getStepX() == 0 ? (random.nextFloat() - random.nextFloat()) * 0.03 : out.getStepX() * 0.13,
                0.0, out.getStepZ() == 0 ? (random.nextFloat() - random.nextFloat()) * 0.03 : out.getStepZ() * 0.13);
        level.addFreshEntity(item);
    }

    /** The original's table of what comes out besides: a nugget off an ore or a cluster of it. */
    private static ItemStack bonus(ItemStack input) {
        Item nugget = null;
        if (input.is(Tags.Items.ORES_IRON) || input.is(ModItems.IRON_CLUSTER.get())) {
            nugget = Items.IRON_NUGGET;
        } else if (input.is(Tags.Items.ORES_GOLD) || input.is(ModItems.GOLD_CLUSTER.get())) {
            nugget = Items.GOLD_NUGGET;
        } else if (input.is(ModTags.Items.ORES_CINNABAR) || input.is(ModItems.CINNABAR_CLUSTER.get())) {
            nugget = ModItems.QUICKSILVER_DROP.get();
        }
        return nugget == null ? ItemStack.EMPTY : new ItemStack(nugget);
    }

    /** Something that will not smelt is burned to nothing, with a hiss. */
    private void burn(Level level, int slot) {
        contents.set(slot, ItemStack.EMPTY);
        level.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.3F,
                2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);
        if (level instanceof ServerLevel served) {
            served.sendParticles(ParticleTypes.LAVA, worldPosition.getX() + 0.5, worldPosition.getY() + 1.0,
                    worldPosition.getZ() + 0.5, 1, 0.3, 0.0, 0.3, 0.0);
        }
        setChanged();
    }

    /**
     * Takes a whole stack thrown in, or none of it: into a square with the same thing and room, or an empty one.
     * Something that will not smelt is burned the moment it lands.
     */
    public boolean take(ItemStack stack) {
        if (level == null) {
            return false;
        }
        for (int slot = 0; slot < SIZE; slot++) {
            ItemStack there = contents.get(slot);
            boolean fits = there.isEmpty()
                    || (ItemStack.isSameItemSameComponents(there, stack)
                            && there.getCount() + stack.getCount() <= stack.getMaxStackSize());
            if (!fits) {
                continue;
            }
            if (there.isEmpty()) {
                contents.set(slot, stack.copy());
            } else {
                there.grow(stack.getCount());
            }
            if (smelting(level, contents.get(slot)).isEmpty()) {
                burn(level, slot);
            }
            setChanged();
            return true;
        }
        return false;
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id != SMELTED || level == null) {
            return super.triggerEvent(id, param);
        }
        if (level.isClientSide) {
            Direction out = mouth();
            RandomSource random = level.random;
            for (int i = 0; i < 5; i++) {
                double x = worldPosition.getX() + 0.5 + (random.nextFloat() - random.nextFloat()) * 0.3 + out.getStepX();
                double z = worldPosition.getZ() + 0.5 + (random.nextFloat() - random.nextFloat()) * 0.3 + out.getStepZ();
                level.addParticle(ParticleTypes.LAVA, x, worldPosition.getY() + 0.3, z, 0.0, 0.0, 0.0);
                level.playLocalSound(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5,
                        worldPosition.getZ() + 0.5, SoundEvents.LAVA_POP, SoundSource.BLOCKS,
                        0.1F + random.nextFloat() * 0.1F, 0.9F + random.nextFloat() * 0.15F, false);
            }
        }
        return true;
    }

    // --- the thirty-two squares --------------------------------------------

    public NonNullList<ItemStack> contents() {
        return contents;
    }

    @Override
    public int getContainerSize() {
        return SIZE;
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
    public boolean stillValid(Player player) {
        return false;
    }

    @Override
    public void clearContent() {
        contents.clear();
    }

    /** Fed only from the top, through the hole over the molten heart; nothing is ever taken back out. */
    @Override
    public int[] getSlotsForFace(Direction side) {
        return side == Direction.UP ? ALL : new int[0];
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) {
        return side == Direction.UP;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return false;
    }

    // --- saving ------------------------------------------------------------

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        contents.clear();
        ContainerHelper.loadAllItems(tag, contents, registries);
        cookTime = tag.getShort("CookTime");
        speedy = tag.getShort("SpeedyTime");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, contents, registries);
        tag.putShort("CookTime", (short) cookTime);
        tag.putShort("SpeedyTime", (short) speedy);
    }

}
