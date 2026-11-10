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
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModSounds;
import me.moonscenty.alchemia.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
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
import net.minecraft.world.level.block.state.BlockState;

public class InfusionMatrixBlockEntity extends SyncedBlockEntity {
    // ticks per cycle with no altar upgrades; one step is half a cycle
    public static final int CYCLE = 20;
    // costs are percentages of the recipe price
    public static final int FULL_COST = 100;
    public static final int CHEAPEST = 50;

    // original's values
    private static final int SPEED_STONE_CYCLE = -2;
    private static final int SPEED_STONE_COST = 1;
    private static final int COST_STONE_CYCLE = 1;
    private static final int COST_STONE_COST = -2;
    // original's values
    private static final int ANCIENT_CORNERS_CYCLE = -2;
    private static final int ANCIENT_CORNERS_COST = -10;
    private static final int ANCIENT_CORNERS_SHAKE = 2;
    private static final int ELDRITCH_CORNERS_CYCLE = -4;
    private static final int ELDRITCH_CORNERS_COST = 5;
    private static final int ELDRITCH_CORNERS_SHAKE = -4;
    private static final int RING = 8;
    private static final int DEEP = 10;
    private static final int STEADIED = 12;
    private static final int STEADIED_UP = 5;
    private static final int JARS = 12;
    private static final int UNDER = 2;
    // original's value
    private static final int EATS_FOR = 5;
    // mishap chance per step: instability in MISHAP
    private static final int MISHAP = 500;
    // instability cap
    public static final int WORST = 25;
    private static final int HUMS_EVERY = 65;

    // corner stones are not neighbours, so nothing notifies the matrix when one breaks; poll
    private static final int LOOKS_ROUND = 20;

    private boolean awake;
    private ResourceLocation working;
    // receives the warp; not saved
    private java.util.UUID worker;
    private AspectList owed = AspectList.EMPTY;
    private List<BlockPos> ring = List.of();
    private int instability;
    private ItemStack central = ItemStack.EMPTY;
    private int recipeShake;
    private List<ItemStack> remaining = List.of();
    private int eating;
    private List<ResourceLocation> asked = List.of();
    private int cycle = CYCLE;
    private int cost = FULL_COST;
    private int counter;
    // a timestamp, not a counter, so the client can animate without per-tick sync
    private long woken;
    private int watch;
    private int humming = -1;

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

    public int cycle() {
        return cycle;
    }

    public int cost() {
        return cost;
    }

    public long wokenAt() {
        return woken;
    }

    public AspectList owed() {
        return owed;
    }

    public static final List<Vec3i> CORNERS = List.of(
            new Vec3i(1, -2, -1),
            new Vec3i(1, -2, 1),
            new Vec3i(-1, -2, 1),
            new Vec3i(-1, -2, -1));

    public boolean built(Level level) {
        return corners(level) != null;
    }

    public me.moonscenty.alchemia.block.PillarKind corners(Level level) {
        for (me.moonscenty.alchemia.block.PillarKind kind : me.moonscenty.alchemia.block.PillarKind.values()) {
            if (standing(level, kind.stone()) || standingPillars(level, kind)) {
                return kind;
            }
        }
        return null;
    }

    private boolean standingPillars(Level level, me.moonscenty.alchemia.block.PillarKind kind) {
        if (!standing(level, ModBlocks.ARCANE_PILLAR.get())) {
            return false;
        }
        for (BlockPos at : columns()) {
            if (level.getBlockState(at).getValue(me.moonscenty.alchemia.block.ArcanePillarBlock.KIND) != kind) {
                return false;
            }
        }
        return true;
    }

    private boolean standing(Level level, Block corner) {
        if (!(level.getBlockEntity(worldPosition.below(UNDER)) instanceof ArcanePedestalBlockEntity)) {
            return false;
        }
        for (BlockPos at : columns()) {
            if (!level.getBlockState(at).is(corner)) {
                return false;
            }
        }
        return true;
    }

    // each pedestal adds instability; an identical pedestal mirrored across the matrix cancels it
    public int symmetry(Level level) {
        int against = 0;
        for (BlockPos at : around(level, worldPosition)) {
            ArcanePedestalBlockEntity stand = stand(level, at);
            if (stand == null) {
                continue;
            }
            boolean holding = !stand.held().isEmpty();
            against += holding ? 3 : 2;
            if (holding && kindOf(level, at) == me.moonscenty.alchemia.block.PillarKind.ELDRITCH) {
                against += 1;
            }

            ArcanePedestalBlockEntity opposite = stand(level, mirror(at));
            if (opposite != null && level.getBlockState(mirror(at)).is(level.getBlockState(at).getBlock())) {
                against -= 2;
                if (holding && !opposite.held().isEmpty()) {
                    against -= 1;
                    if (kindOf(level, mirror(at)) == me.moonscenty.alchemia.block.PillarKind.ELDRITCH) {
                        against -= 2;
                    }
                }
            }
        }
        me.moonscenty.alchemia.block.PillarKind kind = corners(level);
        if (kind == me.moonscenty.alchemia.block.PillarKind.ANCIENT) {
            against += ANCIENT_CORNERS_SHAKE;
        } else if (kind == me.moonscenty.alchemia.block.PillarKind.ELDRITCH) {
            against += ELDRITCH_CORNERS_SHAKE;
        }
        return against + ancientStands / 4 + steadying(level);
    }

    private int ancientStands;

    // read once when a working starts; rebuilding mid-work does not change its terms
    private void reckon(Level level) {
        cycle = CYCLE;
        cost = FULL_COST;
        me.moonscenty.alchemia.block.PillarKind kind = corners(level);
        if (kind == me.moonscenty.alchemia.block.PillarKind.ANCIENT) {
            cycle += ANCIENT_CORNERS_CYCLE;
            cost += ANCIENT_CORNERS_COST;
        } else if (kind == me.moonscenty.alchemia.block.PillarKind.ELDRITCH) {
            cycle += ELDRITCH_CORNERS_CYCLE;
            cost += ELDRITCH_CORNERS_COST;
        }
        // each ancient pedestal -1%, each eldritch one +0.25%
        int quarters = 0;
        ancientStands = 0;
        for (BlockPos at : around(level, worldPosition)) {
            me.moonscenty.alchemia.block.PillarKind stand = kindOf(level, at);
            if (stand == me.moonscenty.alchemia.block.PillarKind.ANCIENT) {
                cost -= 1;
                ancientStands++;
            } else if (stand == me.moonscenty.alchemia.block.PillarKind.ELDRITCH) {
                quarters++;
            }
        }
        cost += quarters / 4;
        for (Vec3i corner : CORNERS) {
            BlockState under = level.getBlockState(worldPosition.offset(corner).below());
            if (under.is(ModBlocks.INFUSION_SPEED_STONE.get())) {
                cycle += SPEED_STONE_CYCLE;
                cost += SPEED_STONE_COST;
            } else if (under.is(ModBlocks.INFUSION_COST_STONE.get())) {
                cycle += COST_STONE_CYCLE;
                cost += COST_STONE_COST;
            }
        }
        cycle = Math.max(1, cycle);
        cost = Math.max(CHEAPEST, cost);
    }

    // discounts round down; an aspect discounted to zero is dropped
    private AspectList priced(AspectList asked) {
        if (cost == FULL_COST) {
            return asked;
        }
        AspectList paying = AspectList.EMPTY;
        for (Holder<Aspect> aspect : asked.sortedByAmount()) {
            int points = asked.get(aspect) * cost / FULL_COST;
            if (points > 0) {
                paying = paying.add(aspect, points);
            }
        }
        return paying;
    }

    private BlockPos mirror(BlockPos at) {
        return new BlockPos(2 * worldPosition.getX() - at.getX(), at.getY(),
                2 * worldPosition.getZ() - at.getZ());
    }

    // in tenths, truncated: mirrored pairs steady, unpaired ones destabilise
    private int steadying(Level level) {
        int tenths = 0;
        for (int east = -STEADIED; east <= STEADIED; east++) {
            for (int south = -STEADIED; south <= STEADIED; south++) {
                if (east == 0 && south == 0) {
                    continue;
                }
                for (int up = -DEEP; up <= STEADIED_UP; up++) {
                    BlockPos at = worldPosition.offset(east, up, south);
                    if (!level.getBlockState(at).is(ModTags.Blocks.STABILISES_INFUSION)) {
                        continue;
                    }
                    tenths += level.getBlockState(mirror(at)).is(ModTags.Blocks.STABILISES_INFUSION) ? -1 : 1;
                }
            }
        }
        return tenths / 10;
    }

    private List<BlockPos> columns() {
        List<BlockPos> all = new ArrayList<>();
        for (Vec3i corner : CORNERS) {
            BlockPos foot = worldPosition.offset(corner);
            all.add(foot);
            all.add(foot.above());
        }
        return all;
    }

    private void raise(Level level) {
        me.moonscenty.alchemia.block.PillarKind kind = corners(level);
        if (kind == null) {
            return;
        }
        for (BlockPos at : columns()) {
            level.setBlock(at, ModBlocks.ARCANE_PILLAR.get().defaultBlockState()
                    .setValue(me.moonscenty.alchemia.block.ArcanePillarBlock.KIND, kind), Block.UPDATE_ALL);
        }
    }

    private void lower(Level level) {
        for (BlockPos at : columns()) {
            BlockState there = level.getBlockState(at);
            if (there.is(ModBlocks.ARCANE_PILLAR.get())) {
                level.setBlock(at, there.getValue(me.moonscenty.alchemia.block.ArcanePillarBlock.KIND).stone()
                        .defaultBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    private static me.moonscenty.alchemia.block.PillarKind kindOf(Level level, BlockPos at) {
        return level.getBlockState(at).getBlock() instanceof me.moonscenty.alchemia.block.ArcanePedestalBlock stand
                ? stand.kind() : me.moonscenty.alchemia.block.PillarKind.NORMAL;
    }

    public Woken wake(Player player) {
        if (level == null || level.isClientSide) {
            return Woken.NOTHING;
        }
        if (!awake) {
            if (!built(level)) {
                return Woken.UNBUILT;
            }
            raise(level);
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

    public enum Woken {
        UNBUILT,
        WOKEN,
        STARTED,
        BUSY,
        NOTHING
    }

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
        if (found.isEmpty() || !me.moonscenty.alchemia.research.RecipeGates.known(player, found.get().id(),
                found.get().value().research())) {
            return false;
        }

        InfusionRecipe recipe = found.get().value();
        working = found.get().id();
        worker = player != null ? player.getUUID() : null;
        reckon(level);
        owed = priced(recipe.essentiaFor(under.held()));
        asked = owed.sortedByName().stream().map(aspect -> aspect.value().id()).toList();
        ring = List.copyOf(holding);
        central = under.held().copy();
        remaining = standing.stream().map(ItemStack::copy).toList();
        eating = 0;
        // not capped at WORST here; only shake() caps it (original)
        recipeShake = recipe.instabilityFor(under.held());
        instability = recipeShake + symmetry(level);
        counter = 0;
        level.playSound(null, worldPosition, ModSounds.CRAFT_START.get(), SoundSource.BLOCKS, 0.5F, 1.0F);
        changed();
        return true;
    }


    public static void tick(Level level, BlockPos pos, BlockState state, InfusionMatrixBlockEntity matrix) {
        if (!matrix.awake) {
            return;
        }
        if (++matrix.watch % (matrix.busy() ? matrix.cycle : LOOKS_ROUND) == 0 && !matrix.built(level)) {
            matrix.sleep(level);
            return;
        }
        if (!matrix.busy()) {
            return;
        }
        if (++matrix.counter < Math.max(1, matrix.cycle / 2)) {
            return;
        }
        matrix.counter = 0;
        matrix.turn((ServerLevel) level, pos);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, InfusionMatrixBlockEntity matrix) {
        if (!matrix.busy()) {
            matrix.humming = -1;
            return;
        }
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;
        if (++matrix.humming == 0) {
            level.playLocalSound(x, y, z, ModSounds.INFUSER_START.get(), SoundSource.BLOCKS, 0.5F, 1.0F, false);
        } else if (matrix.humming % HUMS_EVERY == 0) {
            level.playLocalSound(x, y, z, ModSounds.INFUSER.get(), SoundSource.BLOCKS, 0.5F, 1.0F, false);
        }
    }

    private void turn(ServerLevel level, BlockPos pos) {
        ArcanePedestalBlockEntity under = stand(level, pos.below(UNDER));
        InfusionRecipe recipe = recipe(level);
        boolean valid = recipe != null && under != null && !under.held().isEmpty()
                && ItemStack.isSameItemSameComponents(under.held(), central);
        if (!valid || instability > 0 && level.random.nextInt(MISHAP) <= instability) {
            InfusionMishaps.strike(level, pos);
            // a mishap uses up the whole step
            if (valid) {
                return;
            }
        }
        if (!valid) {
            fail(level, pos);
            return;
        }

        if (!owed.isEmpty()) {
            drink(level, pos);
            return;
        }
        if (remaining.isEmpty()) {
            finish(level, pos, under, recipe);
            return;
        }
        eat(level);
    }

    private void drink(ServerLevel level, BlockPos pos) {
        for (Holder<Aspect> aspect : owed.sortedByAmount()) {
            BlockPos from = EssentiaReach.drainForInfusion(level, pos, aspect, JARS);
            if (from != null) {
                owed = owed.reduce(aspect, 1);
                EssentiaReach.thread(level, from, pos, aspect.value().color());
                changed();
                return;
            }
            if (level.random.nextInt(Math.max(1, 100 - recipeShake * 3)) == 0) {
                shake();
            }
        }
        changed();
    }

    // a missing ring item stalls the work: it asks for extra essentia and may shake (original)
    private void eat(ServerLevel level) {
        List<BlockPos> stands = around(level, worldPosition);
        for (int index = 0; index < remaining.size(); index++) {
            ItemStack wanted = remaining.get(index);
            for (BlockPos at : stands) {
                ArcanePedestalBlockEntity stand = stand(level, at);
                if (stand == null || !ItemStack.isSameItemSameComponents(stand.held(), wanted)) {
                    continue;
                }
                if (eating == 0) {
                    eating = EATS_FOR;
                    EssentiaReach.thread(level, at, worldPosition, 0xC8B4FF);
                } else if (eating-- <= 1) {
                    ItemStack left = stand.held().getCraftingRemainingItem();
                    stand.hold(left.isEmpty() ? ItemStack.EMPTY : left.copy());
                    List<ItemStack> rest = new ArrayList<>(remaining);
                    rest.remove(index);
                    remaining = List.copyOf(rest);
                    level.playSound(null, at, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 0.6F,
                            1.2F + level.random.nextFloat() * 0.3F);
                }
                changed();
                return;
            }
            if (!asked.isEmpty() && level.random.nextInt(1 + index) == 0) {
                ResourceLocation more = asked.get(level.random.nextInt(asked.size()));
                me.moonscenty.alchemia.registry.ModAspects.REGISTRY.getHolder(more)
                        .ifPresent(aspect -> owed = owed.add(aspect, 1));
                if (level.random.nextInt(Math.max(1, 50 - recipeShake * 2)) == 0) {
                    shake();
                }
                changed();
            }
        }
    }

    private void shake() {
        instability = Math.min(WORST, instability + 1);
    }

    private void finish(ServerLevel level, BlockPos pos, ArcanePedestalBlockEntity under, InfusionRecipe recipe) {
        // assembled rather than read off the recipe: enchanting recipes return the input itself
        ItemStack before = under.held();
        ItemStack made = recipe.assemble(new InfusionInput(before, List.of()), level.registryAccess());
        // each prior infusion enchantment gives a 1 in 10 chance of warp
        if (recipe.enchants().isPresent()) {
            int already = me.moonscenty.alchemia.enchantment.InfusionEnchantments.on(before).size();
            if (level.random.nextInt(10) < already) {
                made.set(me.moonscenty.alchemia.registry.ModDataComponents.WARP.get(),
                        made.getOrDefault(me.moonscenty.alchemia.registry.ModDataComponents.WARP.get(), 0) + 1);
            }
        }
        under.hold(made);
        if (worker != null) {
            me.moonscenty.alchemia.player.CraftWarp.made(level.getPlayerByUUID(worker), made);
        }
        level.playSound(null, pos, ModSounds.WAND.get(), SoundSource.BLOCKS, 0.5F, 1.0F);
        level.playSound(null, pos.below(UNDER), ModSounds.POOF.get(), SoundSource.BLOCKS, 0.4F,
                1.0F + (float) level.random.nextGaussian() * 0.05F);
        stop();
    }

    private void fail(ServerLevel level, BlockPos pos) {
        level.playSound(null, pos, ModSounds.CRAFT_FAIL.get(), SoundSource.BLOCKS, 1.0F, 0.6F);
        stop();
    }

    private void stop() {
        working = null;
        owed = AspectList.EMPTY;
        ring = List.of();
        central = ItemStack.EMPTY;
        remaining = List.of();
        asked = List.of();
        eating = 0;
        recipeShake = 0;
        instability = 0;
        counter = 0;
        cycle = CYCLE;
        cost = FULL_COST;
        changed();
    }

    public void sleep(Level level) {
        if (busy() && level instanceof ServerLevel served) {
            fail(served, worldPosition);
        }
        awake = false;
        watch = 0;
        woken = 0;
        lower(level);
        changed();
    }

    public void steady() {
        instability = 0;
        changed();
    }

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

    // only the topmost pedestal in each column counts
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

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        awake = tag.getBoolean("awake");
        working = tag.contains("working") ? ResourceLocation.parse(tag.getString("working")) : null;
        owed = AspectList.read(tag, "owed", registries);
        instability = tag.getInt("instability");
        recipeShake = tag.getInt("recipe_shake");
        eating = tag.getInt("eating");
        var context = registries.createSerializationContext(NbtOps.INSTANCE);
        central = tag.contains("central")
                ? ItemStack.CODEC.parse(context, tag.get("central")).result().orElse(ItemStack.EMPTY)
                : ItemStack.EMPTY;
        remaining = tag.contains("remaining")
                ? ItemStack.CODEC.listOf().parse(context, tag.get("remaining")).result().map(List::copyOf).orElse(List.of())
                : List.of();
        List<ResourceLocation> wanted = new ArrayList<>();
        for (var name : tag.getList("asked", net.minecraft.nbt.Tag.TAG_STRING)) {
            wanted.add(ResourceLocation.parse(name.getAsString()));
        }
        asked = List.copyOf(wanted);
        cycle = tag.contains("cycle") ? tag.getInt("cycle") : CYCLE;
        cost = tag.contains("cost") ? tag.getInt("cost") : FULL_COST;
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
        AspectList.write(tag, "owed", owed, registries);
        tag.putInt("instability", instability);
        tag.putInt("recipe_shake", recipeShake);
        tag.putInt("eating", eating);
        var context = registries.createSerializationContext(NbtOps.INSTANCE);
        if (!central.isEmpty()) {
            ItemStack.CODEC.encodeStart(context, central).result().ifPresent(written -> tag.put("central", written));
        }
        if (!remaining.isEmpty()) {
            ItemStack.CODEC.listOf().encodeStart(context, remaining).result()
                    .ifPresent(written -> tag.put("remaining", written));
        }
        net.minecraft.nbt.ListTag names = new net.minecraft.nbt.ListTag();
        asked.forEach(name -> names.add(net.minecraft.nbt.StringTag.valueOf(name.toString())));
        tag.put("asked", names);
        tag.putInt("cycle", cycle);
        tag.putInt("cost", cost);
        tag.putLong("woken", woken);
        tag.putLongArray("ring", ring.stream().mapToLong(BlockPos::asLong).toArray());
    }
}
