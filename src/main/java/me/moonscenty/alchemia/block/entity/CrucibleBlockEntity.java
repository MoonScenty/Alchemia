package me.moonscenty.alchemia.block.entity;

import java.util.List;
import java.util.Optional;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.aspect.Aspects;
import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.block.CrucibleBlock;
import me.moonscenty.alchemia.crafting.CrucibleInput;
import me.moonscenty.alchemia.crafting.CrucibleRecipe;
import me.moonscenty.alchemia.crafting.ModRecipes;
import me.moonscenty.alchemia.player.PlayerKnowledge;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * What is in a crucible: how much water, how hot it is, and what has been dissolved into it so far.
 * <p>
 * Dissolving is what the pot is for. Anything that falls in comes apart into what it is made of, and once the right
 * things are floating in the water, one last thing thrown in boils the lot into something new.
 * <p>
 * The pot holds only so much. Past that it spills, and what spills does not simply vanish: it goes into the air as
 * flux, which is the price of working carelessly and the reason a crucible is worth watching.
 */
public class CrucibleBlockEntity extends BlockEntity {
    /** Plain water, for a pot that has had nothing thrown in it yet. */
    private static final int WATER = 0x3F76E4;

    /** How much water a full pot holds, and how much a piece of work drinks. */
    public static final int FULL = 1000;
    private static final int PER_CRAFT = 50;

    /** How hot it gets, how fast, and how hot it has to be to do anything. */
    private static final int HOTTEST = 200;
    private static final int WORKS_AT = 150;

    /** How much can be dissolved before the pot runs over. */
    public static final int CAPACITY = 100;
    /** A hot pot boils away what is in it this often, in ticks, whether or not it is full. */
    private static final int BOILS_OFF = 100;

    private int water;
    private int heat;
    private AspectList dissolved = AspectList.EMPTY;
    private int counter;

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
        dissolved = AspectList.EMPTY;
        changed();
    }

    /** Empties the pot into a bucket. Whatever was dissolved goes into the air rather than into the bucket. */
    public void drain() {
        spillAll();
        water = 0;
        changed();
    }

    // --- ticking -----------------------------------------------------------

    public static void tick(Level level, BlockPos pos, BlockState state, CrucibleBlockEntity crucible) {
        if (!(level instanceof ServerLevel served)) {
            return;
        }
        crucible.warm(served, pos);
        if (crucible.working()) {
            crucible.swallow(served, pos);
            if (crucible.counter++ >= BOILS_OFF) {
                crucible.counter = 0;
                crucible.spill(served, pos, 1);
            }
        }
        while (crucible.dissolved.total() > CAPACITY) {
            crucible.spill(served, pos, 1);
        }
    }

    /** Comes up to heat over a fire and falls away from one. A dry pot cools however big the fire under it is. */
    private void warm(ServerLevel level, BlockPos pos) {
        int before = heat;
        if (water > 0 && CrucibleBlock.heated(level, pos)) {
            heat = Math.min(HOTTEST, heat + 1);
        } else {
            heat = Math.max(0, heat - 1);
        }
        // the froth starts and stops at the working mark, so the client is told when it is crossed
        if ((before > WORKS_AT) != (heat > WORKS_AT)) {
            changed();
        }
    }

    /**
     * Takes in whatever has fallen into the pot, one piece at a time.
     * <p>
     * A piece that finishes a recipe is spent on it; anything else comes apart into what it is made of. Either way
     * it is used up, so dropping a stack in feeds it in one by one rather than all at once.
     */
    private void swallow(ServerLevel level, BlockPos pos) {
        AABB inside = new AABB(pos).deflate(0.15, 0.0, 0.15).contract(0.0, 0.4, 0.0);
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, inside)) {
            if (item.isRemoved() || item.getItem().isEmpty()) {
                continue;
            }
            ItemStack stack = item.getItem();
            if (!take(level, pos, stack.copyWithCount(1), item.getOwner() instanceof Player player ? player : null)) {
                continue;
            }
            stack.shrink(1);
            if (stack.isEmpty()) {
                item.discard();
            } else {
                item.setItem(stack);
            }
            return;
        }
    }

    /** One thing into the pot. False if the pot could not take it at all. */
    private boolean take(ServerLevel level, BlockPos pos, ItemStack one, Player thrower) {
        Optional<RecipeHolder<CrucibleRecipe>> found = level.getRecipeManager()
                .getRecipeFor(ModRecipes.CRUCIBLE.get(), new CrucibleInput(one, dissolved), level);

        if (found.isPresent() && known(found.get().value(), thrower) && water >= PER_CRAFT) {
            CrucibleRecipe recipe = found.get().value();
            dissolved = recipe.taken(dissolved);
            water -= PER_CRAFT;
            eject(level, pos, recipe.assemble(new CrucibleInput(one, dissolved), level.registryAccess()));
            level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.6F, 1.2F);
            changed();
            return true;
        }

        AspectList made = Aspects.of(one);
        if (made.isEmpty()) {
            return false;
        }
        dissolved = dissolved.add(made);
        level.playSound(null, pos, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 0.3F, 1.4F);
        changed();
        return true;
    }

    private boolean known(CrucibleRecipe recipe, Player thrower) {
        return recipe.research()
                .map(research -> thrower != null && PlayerKnowledge.of(thrower).hasResearch(research))
                .orElse(true);
    }

    private static void eject(ServerLevel level, BlockPos pos, ItemStack made) {
        ItemEntity out = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, made);
        out.setDeltaMovement(0.0, 0.15, 0.0);
        level.addFreshEntity(out);
    }

    // --- spilling ----------------------------------------------------------

    /** Loses some of what is dissolved. It goes into the air as flux rather than nowhere. */
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

    /** Everything at once, for a pot tipped over or broken. */
    public void spillAll() {
        if (level instanceof ServerLevel served && !dissolved.isEmpty()) {
            AuraHandler.add(served, worldPosition, ModAspects.FLUX, dissolved.total());
        }
        dissolved = AspectList.EMPTY;
    }

    // --- what it looks like ------------------------------------------------

    /**
     * What colour the water reads as: whatever is dissolved in it, weighted by how much of each there is.
     * <p>
     * Mixed by amount rather than by picking the largest, so a pot slowly filling with one thing drifts towards its
     * colour instead of snapping to it once that aspect wins.
     */
    public int colour() {
        if (dissolved.isEmpty()) {
            return WATER;
        }
        long red = 0;
        long green = 0;
        long blue = 0;
        long total = 0;
        for (Holder<Aspect> aspect : dissolved.sortedByName()) {
            int amount = dissolved.get(aspect);
            int colour = aspect.value().color();
            red += (long) ((colour >> 16) & 0xFF) * amount;
            green += (long) ((colour >> 8) & 0xFF) * amount;
            blue += (long) (colour & 0xFF) * amount;
            total += amount;
        }
        return (int) (red / total) << 16 | (int) (green / total) << 8 | (int) (blue / total);
    }

    /**
     * Keeps the block state in step with what is in the pot, and tells the client.
     * <p>
     * The state carries only how full it looks, since that is all the model needs; everything else lives here.
     */
    private void changed() {
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

    // --- saving ------------------------------------------------------------

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        water = tag.getInt("water");
        heat = tag.getInt("heat");
        dissolved = AspectList.CODEC
                .parse(registries.createSerializationContext(NbtOps.INSTANCE), tag.get("dissolved"))
                .result().orElse(AspectList.EMPTY);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("water", water);
        tag.putInt("heat", heat);
        AspectList.CODEC
                .encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), dissolved)
                .result().ifPresent(written -> tag.put("dissolved", written));
    }

    /** The colour of the water and the froth are drawn from this, so it has to reach the client on its own. */
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
