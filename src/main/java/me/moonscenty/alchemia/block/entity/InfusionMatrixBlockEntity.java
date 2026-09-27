package me.moonscenty.alchemia.block.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.crafting.InfusionInput;
import me.moonscenty.alchemia.crafting.InfusionRecipe;
import me.moonscenty.alchemia.crafting.ModRecipes;
import me.moonscenty.alchemia.essentia.EssentiaReach;
import me.moonscenty.alchemia.player.PlayerKnowledge;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The working of an infusion: what is being made, what is still owed for it, and how badly it wants to go wrong.
 * <p>
 * A turn of the work is twenty ticks. On each turn it drinks a point of every essentia still owed out of the jars
 * standing round it, and once nothing is owed it eats the ring a thing at a time. When the ring is bare the thing
 * on the pedestal beneath is replaced by what was being made.
 * <p>
 * It is slow on purpose. An infusion is meant to be something you stand and watch, partly because watching is the
 * only warning you get that it is about to go wrong.
 */
public class InfusionMatrixBlockEntity extends BlockEntity {
    /** How long a turn of the work takes. */
    public static final int CYCLE = 20;
    /** How far out the ring of pedestals may stand, and how far below the matrix they may be. */
    private static final int RING = 8;
    private static final int DEEP = 10;
    /** How far the matrix reaches for essentia. No pipe is needed: a shelf of jars nearby is the plumbing. */
    private static final int JARS = 12;
    /** How far under the matrix the thing being worked on stands. */
    private static final int UNDER = 2;
    /** One turn in this many goes wrong, per point of instability. */
    private static final int MISHAP = 500;
    /** No work is ever worse than this, however it is laid out. */
    public static final int WORST = 25;

    private ResourceLocation working;
    private AspectList owed = AspectList.EMPTY;
    private List<BlockPos> ring = List.of();
    private int instability;
    private int counter;
    /** How long it has been running, which is what the drawing uses to wind itself up. */
    private int turning;

    public InfusionMatrixBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.INFUSION_MATRIX.get(), pos, state);
    }

    public boolean busy() {
        return working != null;
    }

    public int instability() {
        return instability;
    }

    public int turning() {
        return turning;
    }

    /** What is still to be drunk, for anything that wants to say so. */
    public AspectList owed() {
        return owed;
    }

    // --- starting ----------------------------------------------------------

    /**
     * Looks at what is laid out and starts on it, if it makes anything.
     *
     * @return whether the work started
     */
    public boolean start(Player player) {
        if (level == null || level.isClientSide || busy()) {
            return false;
        }
        ArcanePedestalBlockEntity under = stand(level, worldPosition.below(UNDER));
        if (under == null || under.held().isEmpty()) {
            return false;
        }

        List<BlockPos> stands = around(level, worldPosition);
        List<ItemStack> standing = new ArrayList<>();
        List<BlockPos> holding = new ArrayList<>();
        for (BlockPos at : stands) {
            ArcanePedestalBlockEntity stand = stand(level, at);
            if (stand != null && !stand.held().isEmpty()) {
                standing.add(stand.held());
                holding.add(at);
            }
        }

        InfusionInput laid = new InfusionInput(under.held(), standing);
        Optional<RecipeHolder<InfusionRecipe>> found = level.getRecipeManager()
                .getRecipeFor(ModRecipes.INFUSION.get(), laid, level);
        if (found.isEmpty() || !known(found.get().value(), player)) {
            return false;
        }

        InfusionRecipe recipe = found.get().value();
        working = found.get().id();
        owed = recipe.essentia();
        ring = List.copyOf(holding);
        instability = Math.min(WORST, recipe.instability());
        counter = 0;
        turning = 0;
        level.playSound(null, worldPosition, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 0.7F, 1.6F);
        changed();
        return true;
    }

    private static boolean known(InfusionRecipe recipe, Player player) {
        return recipe.research()
                .map(research -> player != null && PlayerKnowledge.of(player).hasResearch(research))
                .orElse(true);
    }

    // --- working -----------------------------------------------------------

    public static void tick(Level level, BlockPos pos, BlockState state, InfusionMatrixBlockEntity matrix) {
        if (!matrix.busy()) {
            return;
        }
        matrix.turning++;
        if (++matrix.counter < CYCLE) {
            return;
        }
        matrix.counter = 0;
        matrix.turn((ServerLevel) level, pos);
    }

    /** One turn of the work. */
    private void turn(ServerLevel level, BlockPos pos) {
        ArcanePedestalBlockEntity under = stand(level, pos.below(UNDER));
        InfusionRecipe recipe = recipe(level);
        if (recipe == null || under == null || under.held().isEmpty()) {
            fail(level, pos);
            return;
        }
        if (instability > 0 && level.random.nextInt(MISHAP) <= instability) {
            InfusionMishaps.strike(level, pos, this, ring);
        }

        if (!owed.isEmpty()) {
            drink(level, pos);
            return;
        }
        if (eat(level)) {
            return;
        }
        finish(level, pos, under, recipe);
    }

    /** Takes a point of everything still owed out of the jars within reach. */
    private void drink(ServerLevel level, BlockPos pos) {
        AspectList left = owed;
        for (Holder<Aspect> aspect : owed.sortedByAmount()) {
            BlockPos from = EssentiaReach.drain(level, pos, aspect, JARS);
            if (from != null) {
                left = left.reduce(aspect, 1);
                EssentiaReach.thread(level, from, pos, aspect.value().color());
            }
        }
        if (left != owed) {
            owed = left;
            changed();
        }
    }

    /**
     * Eats one thing off the ring.
     *
     * @return whether there was anything left to eat
     */
    private boolean eat(ServerLevel level) {
        for (BlockPos at : ring) {
            ArcanePedestalBlockEntity stand = stand(level, at);
            if (stand == null || stand.held().isEmpty()) {
                continue;
            }
            ItemStack eaten = stand.held();
            stand.hold(ItemStack.EMPTY);
            EssentiaReach.thread(level, at, worldPosition, 0xC8B4FF);
            level.playSound(null, at, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 0.6F,
                    1.2F + level.random.nextFloat() * 0.3F);
            return !eaten.isEmpty();
        }
        return false;
    }

    /** The work is done: what was on the pedestal becomes what was being made. */
    private void finish(ServerLevel level, BlockPos pos, ArcanePedestalBlockEntity under, InfusionRecipe recipe) {
        under.hold(recipe.result().copy());
        level.playSound(null, pos, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 1.0F, 1.0F);
        stop();
    }

    /** The work has come apart: whatever was owed is simply lost. */
    private void fail(ServerLevel level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1.0F, 0.6F);
        stop();
    }

    private void stop() {
        working = null;
        owed = AspectList.EMPTY;
        ring = List.of();
        instability = 0;
        turning = 0;
        changed();
    }

    /** Made worse by something going wrong, which makes the next thing going wrong likelier. */
    public void worsen() {
        instability = Math.min(WORST, instability + 1);
        changed();
    }

    // --- what is laid out --------------------------------------------------

    private InfusionRecipe recipe(Level level) {
        if (working == null) {
            return null;
        }
        return level.getRecipeManager().byKey(working)
                .map(RecipeHolder::value)
                .filter(InfusionRecipe.class::isInstance)
                .map(InfusionRecipe.class::cast)
                .orElse(null);
    }

    private static ArcanePedestalBlockEntity stand(Level level, BlockPos at) {
        return level.getBlockEntity(at) instanceof ArcanePedestalBlockEntity stand ? stand : null;
    }

    /**
     * The pedestals a matrix can see: one to a column, out to the ring and down as far as the work reaches.
     * <p>
     * One to a column so that a stack of pedestals is one place rather than ten, which is also how a works built
     * on a hillside stays readable.
     */
    public static List<BlockPos> around(Level level, BlockPos pos) {
        List<BlockPos> found = new ArrayList<>();
        for (int east = -RING; east <= RING; east++) {
            for (int south = -RING; south <= RING; south++) {
                if (east == 0 && south == 0) {
                    continue;
                }
                for (int down = 1; down <= DEEP; down++) {
                    BlockPos at = pos.offset(east, -down, south);
                    if (level.getBlockEntity(at) instanceof ArcanePedestalBlockEntity) {
                        found.add(at);
                        break;
                    }
                }
            }
        }
        return found;
    }

    private void changed() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    // --- writing it down ---------------------------------------------------

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        working = tag.contains("working") ? ResourceLocation.parse(tag.getString("working")) : null;
        owed = AspectList.CODEC
                .parse(registries.createSerializationContext(NbtOps.INSTANCE), tag.get("owed"))
                .result().orElse(AspectList.EMPTY);
        instability = tag.getInt("instability");
        turning = tag.getInt("turning");
        List<BlockPos> stands = new ArrayList<>();
        for (long packed : tag.getLongArray("ring")) {
            stands.add(BlockPos.of(packed));
        }
        ring = List.copyOf(stands);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (working != null) {
            tag.putString("working", working.toString());
        }
        AspectList.CODEC
                .encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), owed)
                .result().ifPresent(written -> tag.put("owed", written));
        tag.putInt("instability", instability);
        tag.putInt("turning", turning);
        tag.putLongArray("ring", ring.stream().mapToLong(BlockPos::asLong).toArray());
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
