package me.moonscenty.alchemia.block.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.crafting.InfusionInput;
import me.moonscenty.alchemia.crafting.InfusionRecipe;
import me.moonscenty.alchemia.crafting.ModRecipes;
import me.moonscenty.alchemia.block.ArcanePillarBlock;
import me.moonscenty.alchemia.essentia.EssentiaReach;
import me.moonscenty.alchemia.player.PlayerKnowledge;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import me.moonscenty.alchemia.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
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
import net.minecraft.world.level.block.Blocks;
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

    /** How often a matrix that is awake looks around to see whether its altar is still standing. */
    private static final int LOOKS_ROUND = 100;

    /** Whether the altar under it is built and the stones have been woken. */
    private boolean awake;
    private ResourceLocation working;
    private AspectList owed = AspectList.EMPTY;
    private List<BlockPos> ring = List.of();
    private int instability;
    private int counter;
    /**
     * When it woke, by the world clock.
     * <p>
     * The drawing needs to know how long the stones have been turning, and a counter ticked up on the server is
     * no use for that: a client only hears about it when something else happens to be synced, so the number it
     * draws with jumps about and the stones shiver in place instead of turning. A moment in time is sent once and
     * stays true.
     */
    private long woken;
    /** Ticks since it woke, for looking round at the altar now and then. */
    private int watch;

    public InfusionMatrixBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.INFUSION_MATRIX.get(), pos, state);
    }

    public boolean busy() {
        return working != null;
    }

    public boolean awake() {
        return awake;
    }

    public int instability() {
        return instability;
    }

    /** When the stones began to turn, by the world clock. */
    public long wokenAt() {
        return woken;
    }

    /** What is still to be drunk, for anything that wants to say so. */
    public AspectList owed() {
        return owed;
    }

    // --- building it -------------------------------------------------------

    /**
     * Where the four pillars stand, and which way each is turned.
     * <p>
     * The pillar is drawn leaning towards the north-east corner of its own block, so the four of them have to be
     * turned a quarter apart to lean away from the middle together. The turn a pillar is given is read off its
     * facing, which is why these four look arbitrary: south is no turn at all, and each quarter after it goes
     * round the corners in order.
     */
    private static final Map<Vec3i, Direction> CORNERS = Map.of(
            new Vec3i(1, -2, -1), Direction.NORTH,
            new Vec3i(1, -2, 1), Direction.EAST,
            new Vec3i(-1, -2, 1), Direction.SOUTH,
            new Vec3i(-1, -2, -1), Direction.WEST);

    /**
     * Raises the altar: the four stones at the corners become pillars.
     * <p>
     * This is what the first touch of a wand does. Nothing is placed and nothing is spent -- the stone is already
     * there, and what the wand does is tell it what it is for. A works is built by hand and then woken, which is a
     * better moment than setting down four pillars one at a time and wondering whether they count.
     *
     * A pillar stands two blocks tall, so it is built from two, and the upper stone is swallowed by the one below
     * it. Raising a pillar out of a single stone would leave the picture standing in a block of somebody else's
     * air, which is how you end up with a wall built through your altar.
     *
     * @return whether anything was raised
     */
    public static boolean raise(Level level, BlockPos pos) {
        boolean raised = false;
        for (Map.Entry<Vec3i, Direction> corner : CORNERS.entrySet()) {
            BlockPos at = pos.offset(corner.getKey());
            if (!stone(level, at) || !stone(level, at.above())) {
                continue;
            }
            level.setBlock(at, ModBlocks.ARCANE_PILLAR.get().defaultBlockState()
                    .setValue(ArcanePillarBlock.FACING, corner.getValue()), Block.UPDATE_ALL);
            level.setBlock(at.above(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            raised = true;
        }
        return raised;
    }

    /** Every pillar still standing becomes the two stones it was built from. */
    public static void lower(Level level, BlockPos pos) {
        for (Vec3i corner : CORNERS.keySet()) {
            BlockPos at = pos.offset(corner);
            if (!(level.getBlockState(at).getBlock() instanceof ArcanePillarBlock)) {
                continue;
            }
            BlockState was = ModBlocks.ARCANE_STONE.block().get().defaultBlockState();
            level.setBlock(at, was, Block.UPDATE_ALL);
            if (level.getBlockState(at.above()).canBeReplaced()) {
                level.setBlock(at.above(), was, Block.UPDATE_ALL);
            }
        }
    }

    private static boolean stone(Level level, BlockPos at) {
        return level.getBlockState(at).is(ModBlocks.ARCANE_STONE.block().get());
    }

    /**
     * Whether there is an altar under this at all: a pedestal to work on and four pillars at the corners.
     * <p>
     * A matrix with nothing under it is eight stones hanging in a room. It will not wake and, if the altar is
     * pulled apart under a working, it will not go on with it.
     */
    public boolean built(Level level) {
        if (!(level.getBlockEntity(worldPosition.below(UNDER)) instanceof ArcanePedestalBlockEntity)) {
            return false;
        }
        for (Vec3i corner : CORNERS.keySet()) {
            if (!(level.getBlockState(worldPosition.offset(corner)).getBlock() instanceof ArcanePillarBlock)) {
                return false;
            }
        }
        return true;
    }

    /**
     * What a wand does when it is pointed at the matrix.
     * <p>
     * The first touch wakes the altar: the corner stones become pillars and the stones begin to turn. Every touch
     * after that sets a working going. Two touches rather than one because waking is a thing you do once to a
     * building and starting is a thing you do every time, and it would be a poor altar that could not tell you
     * which of the two it had just done.
     *
     * @return what happened, for the wand to say out loud
     */
    public Woken wake(Player player) {
        if (level == null || level.isClientSide) {
            return Woken.NOTHING;
        }
        if (!awake) {
            raise(level, worldPosition);
            if (!built(level)) {
                return Woken.UNBUILT;
            }
            awake = true;
            woken = level.getGameTime();
            watch = 0;
            changed();
            return Woken.WOKEN;
        }
        if (busy()) {
            return Woken.BUSY;
        }
        return start(player) ? Woken.STARTED : Woken.NOTHING;
    }

    /** What came of pointing a wand at it. */
    public enum Woken {
        /** The altar is not finished, so nothing happened. */
        UNBUILT,
        /** The stones have begun to turn. */
        WOKEN,
        /** A working has started. */
        STARTED,
        /** It is already at work. */
        BUSY,
        /** What is laid out makes nothing. */
        NOTHING
    }

    // --- starting ----------------------------------------------------------

    /**
     * Looks at what is laid out and starts on it, if it makes anything.
     *
     * @return whether the work started
     */
    public boolean start(Player player) {
        if (level == null || level.isClientSide || busy() || !awake) {
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
        if (!matrix.awake) {
            return;
        }
        // an altar taken apart under a working stops being an altar, and what is left of it goes back to stone
        if (++matrix.watch % (matrix.busy() ? CYCLE : LOOKS_ROUND) == 0 && !matrix.built(level)) {
            if (matrix.busy()) {
                matrix.fail((ServerLevel) level, pos);
            }
            matrix.sleep(level, pos);
            return;
        }
        if (!matrix.busy()) {
            return;
        }
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

    /** The working is over, one way or another. The altar stays awake: it is still an altar. */
    private void stop() {
        working = null;
        owed = AspectList.EMPTY;
        ring = List.of();
        instability = 0;
        counter = 0;
        changed();
    }

    /**
     * Back to sleep, and back to stone.
     * <p>
     * An altar is only an altar while all of it is standing. Take one pillar out and the rest are four-fifths of
     * nothing, so they are given back as the stone they were built from rather than left standing as ruins nobody
     * can use and everybody has to break by hand.
     */
    public void sleep(Level level, BlockPos pos) {
        awake = false;
        watch = 0;
        woken = 0;
        lower(level, pos);
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
        awake = tag.getBoolean("awake");
        working = tag.contains("working") ? ResourceLocation.parse(tag.getString("working")) : null;
        owed = AspectList.CODEC
                .parse(registries.createSerializationContext(NbtOps.INSTANCE), tag.get("owed"))
                .result().orElse(AspectList.EMPTY);
        instability = tag.getInt("instability");
        woken = tag.getLong("woken");
        List<BlockPos> stands = new ArrayList<>();
        for (long packed : tag.getLongArray("ring")) {
            stands.add(BlockPos.of(packed));
        }
        ring = List.copyOf(stands);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (awake) {
            tag.putBoolean("awake", true);
        }
        if (working != null) {
            tag.putString("working", working.toString());
        }
        AspectList.CODEC
                .encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), owed)
                .result().ifPresent(written -> tag.put("owed", written));
        tag.putInt("instability", instability);
        tag.putLong("woken", woken);
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
