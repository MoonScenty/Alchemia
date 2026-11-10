package me.moonscenty.alchemia.block.entity;

import java.util.List;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.aspect.Aspects;
import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.block.CrucibleBlock;
import me.moonscenty.alchemia.client.CrucibleEffects;
import me.moonscenty.alchemia.crafting.CrucibleInput;
import me.moonscenty.alchemia.crafting.CrucibleRecipe;
import me.moonscenty.alchemia.crafting.ModRecipes;
import me.moonscenty.alchemia.player.PlayerKnowledge;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class CrucibleBlockEntity extends SyncedBlockEntity {
    private static final int WATER = 0x3F76E4;

    public static final int FULL = 1000;
    private static final int PER_CRAFT = 50;

    private static final int HOTTEST = 200;
    private static final int WORKS_AT = 150;

    public static final int CAPACITY = 100;
    private static final int BOILS_OFF = 100;

    // boil-off counter resets: further back after a craft than after melting (original's values)
    private static final int STARTS_AT = -100;
    private static final int AFTER_WORK = -250;
    private static final int AFTER_MELT = -150;
    private static final int SCALDS_EVERY = 10;
    // tags the pot's own output so it is not swallowed again
    public static final String MADE_HERE = "alchemia.crucible_made";

    private int water;
    private int heat;
    private AspectList dissolved = AspectList.EMPTY;
    private int counter = STARTS_AT;
    private int touches;

    private static final int BOIL_UP = 2;
    private static final int CRAFTED = 99;

    public CrucibleBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRUCIBLE.get(), pos, state);
    }

    public AspectList dissolved() {
        return dissolved;
    }

    public int water() {
        return water;
    }

    public boolean working() {
        return heat > WORKS_AT && water > 0;
    }

    public void fill() {
        water = FULL;
        changed();
    }

    public int pour(int amount, boolean really) {
        int room = Math.max(0, FULL - water);
        int taken = Math.min(room, amount);
        if (really && taken > 0) {
            water += taken;
            changed();
        }
        return taken;
    }

    public void drain() {
        if (level != null && !level.isClientSide && !dissolved.isEmpty()) {
            level.blockEvent(worldPosition, getBlockState().getBlock(), BOIL_UP, 5);
        }
        spillAll();
        water = 0;
        changed();
    }

    @Override
    public boolean triggerEvent(int id, int data) {
        if (id == CRAFTED) {
            if (level != null && level.isClientSide) {
                CrucibleEffects.crafted(level, worldPosition);
            }
            return true;
        }
        if (id == BOIL_UP) {
            if (level != null && level.isClientSide) {
                CrucibleEffects.boil(level, worldPosition, this, data);
            }
            return true;
        }
        return super.triggerEvent(id, data);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CrucibleBlockEntity crucible) {
        if (!(level instanceof ServerLevel served)) {
            return;
        }
        crucible.warm(served, pos);
        if (crucible.working()) {
            crucible.swallow(served, pos);
        }
        // overflow spills one point a tick (original's rate)
        if (crucible.dissolved.total() > CAPACITY) {
            crucible.spill(served, pos, 1);
        }
        crucible.counter = Math.min(crucible.counter + 1, BOILS_OFF);
        if (crucible.counter >= BOILS_OFF && crucible.working()) {
            crucible.counter = 0;
            crucible.spill(served, pos, 1);
        }
    }

    private void warm(ServerLevel level, BlockPos pos) {
        int before = heat;
        if (water > 0 && CrucibleBlock.heated(level, pos)) {
            heat = Math.min(HOTTEST, heat + 1);
        } else {
            heat = Math.max(0, heat - 1);
        }
        if ((before > WORKS_AT) != (heat > WORKS_AT)) {
            changed();
        }
    }

    private void swallow(ServerLevel level, BlockPos pos) {
        AABB inside = new AABB(pos).deflate(0.15, 0.0, 0.15).contract(0.0, 0.4, 0.0);
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, inside)) {
            if (item.isRemoved() || item.getItem().isEmpty() || item.getPersistentData().getBoolean(MADE_HERE)) {
                continue;
            }
            ItemStack stack = item.getItem();
            int left = put(level, pos, stack, item.getOwner() instanceof Player player ? player : null);
            if (left == stack.getCount()) {
                continue;
            }
            if (left <= 0) {
                item.discard();
            } else {
                stack.setCount(left);
                item.setItem(stack);
            }
        }
    }

    public boolean putByHand(ItemStack held, Player player) {
        return level instanceof ServerLevel served && working()
                && put(served, worldPosition, held.copyWithCount(1), player) == 0;
    }

    private int put(ServerLevel level, BlockPos pos, ItemStack stack, Player thrower) {
        int left = stack.getCount();
        boolean melted = false;
        boolean crafted = false;
        while (left > 0) {
            Taken taken = take(level, pos, stack.copyWithCount(1), thrower);
            if (taken == Taken.NOTHING) {
                break;
            }
            crafted |= taken == Taken.CRAFTED;
            melted |= taken == Taken.MELTED;
            left--;
        }
        if (melted) {
            level.playSound(null, pos, ModSounds.BUBBLE.get(), SoundSource.BLOCKS, 0.2F,
                    1.0F + level.random.nextFloat() * 0.4F);
            level.blockEvent(pos, getBlockState().getBlock(), BOIL_UP, 1);
        }
        if (crafted) {
            level.blockEvent(pos, getBlockState().getBlock(), CRAFTED, 0);
        }
        if (melted || crafted) {
            changed();
        }
        return left;
    }

    private enum Taken {
        CRAFTED, MELTED, NOTHING
    }

    private Taken take(ServerLevel level, BlockPos pos, ItemStack one, Player thrower) {
        CrucibleRecipe recipe = best(level, new CrucibleInput(one, dissolved), thrower);
        if (recipe != null && water > 0) {
            dissolved = recipe.taken(dissolved);
            water = Math.max(0, water - PER_CRAFT);
            ItemStack made = recipe.assemble(new CrucibleInput(one, dissolved), level.registryAccess());
            me.moonscenty.alchemia.player.CraftWarp.made(thrower, made);
            eject(level, pos, made);
            counter = AFTER_WORK;
            return Taken.CRAFTED;
        }

        AspectList made = Aspects.of(one);
        if (made.isEmpty()) {
            return Taken.NOTHING;
        }
        dissolved = dissolved.add(made);
        counter = AFTER_MELT;
        return Taken.MELTED;
    }

    // original: of the recipes the thrower knows, the one asking for the most kinds of aspect
    private CrucibleRecipe best(ServerLevel level, CrucibleInput input, Player thrower) {
        CrucibleRecipe best = null;
        int most = 0;
        for (RecipeHolder<CrucibleRecipe> holder : level.getRecipeManager().getAllRecipesFor(ModRecipes.CRUCIBLE.get())) {
            CrucibleRecipe recipe = holder.value();
            int kinds = recipe.aspects().sortedByName().size();
            if (kinds > most && me.moonscenty.alchemia.research.RecipeGates.known(thrower, holder.id(), recipe.research())
                    && recipe.matches(input, level)) {
                best = recipe;
                most = kinds;
            }
        }
        return best;
    }

    public void touched(net.minecraft.world.entity.LivingEntity living) {
        if (!(level instanceof ServerLevel served) || !working() || ++touches < SCALDS_EVERY) {
            return;
        }
        touches = 0;
        living.hurt(served.damageSources().inFire(), 1.0F);
        served.playSound(null, worldPosition, net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS,
                0.4F, 2.0F + served.random.nextFloat() * 0.4F);
    }


    private static void eject(ServerLevel level, BlockPos pos, ItemStack made) {
        ItemEntity out = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, made);
        out.setDeltaMovement(0.0, 0.15, 0.0);
        out.getPersistentData().putBoolean(MADE_HERE, true);
        level.addFreshEntity(out);
    }

    private void spill(ServerLevel level, BlockPos pos, int amount) {
        List<Holder<Aspect>> held = dissolved.sortedByAmount();
        if (held.isEmpty()) {
            return;
        }
        Holder<Aspect> aspect = held.get(level.random.nextInt(held.size()));
        int lost = Math.min(amount, dissolved.get(aspect));
        dissolved = dissolved.reduce(aspect, lost);
        AuraHandler.add(level, pos, ModAspects.FLUX, lost);
        changed();
    }

    public void spillAll() {
        if (level instanceof ServerLevel served && !dissolved.isEmpty()) {
            AuraHandler.add(served, worldPosition, ModAspects.FLUX, dissolved.total());
        }
        dissolved = AspectList.EMPTY;
    }

    public int colour() {
        float murk = (float) dissolved.total() / CAPACITY;
        if (murk > 0.0F) {
            murk = 0.5F + murk / 2.0F;
        }
        float red = ((WATER >> 16) & 0xFF) * (1.0F - murk / 3.0F);
        float green = ((WATER >> 8) & 0xFF) * (1.0F - murk);
        float blue = (WATER & 0xFF) * (1.0F - murk / 2.0F);
        return (int) Math.max(0.0F, red) << 16 | (int) Math.max(0.0F, green) << 8 | (int) Math.max(0.0F, blue);
    }

    public float surface() {
        float water = 0.3F + 0.5F * this.water / FULL;
        float raised = water + (float) dissolved.total() / CAPACITY * (1.0F - water);
        // stay off exactly 1.0, where the surface z-fights the rim
        return raised > 1.0F ? 1.001F : raised == 1.0F ? 0.9999F : raised;
    }

    @Override
    protected void changed() {
        setChanged();
        if (level == null || level.isClientSide) {
            return;
        }
        int shown = water <= 0 ? 0 : Math.max(1, water * CrucibleBlock.FULL / FULL);
        BlockState updated = getBlockState().setValue(CrucibleBlock.LEVEL, Math.min(CrucibleBlock.FULL, shown));
        if (updated != getBlockState()) {
            level.setBlock(worldPosition, updated, Block.UPDATE_ALL);
        } else {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        water = tag.getInt("water");
        heat = tag.getInt("heat");
        dissolved = AspectList.read(tag, "dissolved", registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("water", water);
        tag.putInt("heat", heat);
        AspectList.write(tag, "dissolved", dissolved, registries);
    }
}
