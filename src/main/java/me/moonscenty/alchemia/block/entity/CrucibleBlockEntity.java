package me.moonscenty.alchemia.block.entity;

import java.util.List;
import java.util.Optional;

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
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
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

    /**
     * How long since the pot last boiled something off. It starts behind, and the original set it further back
     * whenever something went in -- a long way after a piece of work, less after something melted -- so a pot being
     * worked boils off less than one left standing.
     */
    private static final int STARTS_AT = -100;
    private static final int AFTER_WORK = -250;
    private static final int AFTER_MELT = -150;
    /** One touch in this many scalds whatever is standing in a hot pot, for a point of fire. */
    private static final int SCALDS_EVERY = 10;
    /** What the pot marks its own work with, so it does not take back in what it has just made. */
    public static final String MADE_HERE = "alchemia.crucible_made";

    private int water;
    private int heat;
    private AspectList dissolved = AspectList.EMPTY;
    private int counter = STARTS_AT;
    private int touches;

    /** The event that tells the client the pot boiled up, with how hard as its data. */
    private static final int BOIL_UP = 2;
    /** The event that tells the client a recipe came out, for the puff it goes up in. */
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
        dissolved = AspectList.EMPTY;
        changed();
    }

    /**
     * Water poured in a little at a time, by a jug or a pipe of somebody else's.
     * <p>
     * Unlike a bucket emptied into it, this does not throw out what is dissolved: it only tops the pot up.
     *
     * @return how much it took, which is never more than there was room for
     */
    public int pour(int amount, boolean really) {
        int room = Math.max(0, FULL - water);
        int taken = Math.min(room, amount);
        if (really && taken > 0) {
            water += taken;
            changed();
        }
        return taken;
    }

    /** Empties the pot into a bucket. Whatever was dissolved goes into the air rather than into the bucket. */
    public void drain() {
        if (level != null && !level.isClientSide && !dissolved.isEmpty()) {
            // the original boiled up five times as hard when what was in the pot was thrown out
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

    // --- ticking -----------------------------------------------------------

    public static void tick(Level level, BlockPos pos, BlockState state, CrucibleBlockEntity crucible) {
        if (!(level instanceof ServerLevel served)) {
            return;
        }
        crucible.warm(served, pos);
        if (crucible.working()) {
            crucible.swallow(served, pos);
        }
        // running over loses a point a tick, as the original's did, rather than everything over the brim at once
        if (crucible.dissolved.total() > CAPACITY) {
            crucible.spill(served, pos, 1);
        }
        crucible.counter = Math.min(crucible.counter + 1, BOILS_OFF);
        if (crucible.counter >= BOILS_OFF && crucible.working()) {
            crucible.counter = 0;
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
     * Takes in whatever has fallen into the pot, a whole stack at once as the original did: each piece in turn
     * either finishes a recipe or comes apart into what it is made of, until the stack is gone or the rest of it is
     * something the pot cannot take. What the pot made itself is left be.
     */
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

    /**
     * One thing put in by hand, the original's right click on the pot with something other than a wand.
     *
     * @return whether the pot took it
     */
    public boolean putByHand(ItemStack held, Player player) {
        return level instanceof ServerLevel served && working()
                && put(served, worldPosition, held.copyWithCount(1), player) == 0;
    }

    /**
     * Puts in as much of a stack as the pot will take, a piece at a time, and says how many are left. The pot
     * bubbles once for whatever melted and puffs once for whatever was made, however many pieces it was.
     */
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

    /**
     * One thing into the pot. A recipe wants only that there be some water left, and drinks its fifty or whatever
     * there is, as the original's did.
     */
    private Taken take(ServerLevel level, BlockPos pos, ItemStack one, Player thrower) {
        Optional<RecipeHolder<CrucibleRecipe>> found = level.getRecipeManager()
                .getRecipeFor(ModRecipes.CRUCIBLE.get(), new CrucibleInput(one, dissolved), level);

        if (found.isPresent() && known(found.get().value(), thrower) && water > 0) {
            CrucibleRecipe recipe = found.get().value();
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

    /**
     * Something alive standing in a hot pot: one touch in ten scalds it for a point of fire damage, with a hiss.
     */
    public void touched(net.minecraft.world.entity.LivingEntity living) {
        if (!(level instanceof ServerLevel served) || !working() || ++touches < SCALDS_EVERY) {
            return;
        }
        touches = 0;
        living.hurt(served.damageSources().inFire(), 1.0F);
        served.playSound(null, worldPosition, net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS,
                0.4F, 2.0F + served.random.nextFloat() * 0.4F);
    }

    private boolean known(CrucibleRecipe recipe, Player thrower) {
        return recipe.research()
                .map(research -> thrower != null && PlayerKnowledge.of(thrower).hasResearch(research))
                .orElse(true);
    }

    private static void eject(ServerLevel level, BlockPos pos, ItemStack made) {
        ItemEntity out = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, made);
        out.setDeltaMovement(0.0, 0.15, 0.0);
        out.getPersistentData().putBoolean(MADE_HERE, true);
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
     * What colour the water reads as, as the original coloured it: plain water, darkening towards a murky purple as
     * the pot fills with what is dissolved in it. Anything at all in it takes it halfway there at once.
     */
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

    /**
     * How high the surface stands, as the original worked it out: from 0.3 dry to 0.8 brim-full of water, and what is
     * dissolved raises it the rest of the way towards the rim.
     */
    public float surface() {
        float water = 0.3F + 0.5F * this.water / FULL;
        float raised = water + (float) dissolved.total() / CAPACITY * (1.0F - water);
        // the original kept it off exactly the rim, where it would fight the top of the walls
        return raised > 1.0F ? 1.001F : raised == 1.0F ? 0.9999F : raised;
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
