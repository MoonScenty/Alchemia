package me.moonscenty.alchemia.golem.seal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import me.moonscenty.alchemia.golem.Golem;
import me.moonscenty.alchemia.golem.GolemTrait;
import me.moonscenty.alchemia.golem.task.Task;
import me.moonscenty.alchemia.golem.task.Tasks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;

/**
 * Harvest: walks its area a block every quarter second, and sends deft, smart golems to whatever has grown ripe.
 * Crops picked by hand are picked; the rest are cut, and, if it is set to replant, the golem comes back with
 * whatever it was grown from and plants it again, tilling the ground first if it has gone back to dirt. Set to,
 * it asks for seed from providing seals when the golem has none.
 */
public class HarvestSeal extends Seal {
    private static final int REPLANT = 0;
    private static final int PROVISION = 1;

    /** A crop cut down that is to be planted again: where, against which face, what with, and on tilled earth. */
    private static final class Replant {
        final BlockPos pos;
        final Direction face;
        final ItemStack seed;
        final boolean farmland;
        int task;

        Replant(BlockPos pos, Direction face, ItemStack seed, boolean farmland, int task) {
            this.pos = pos;
            this.face = face;
            this.seed = seed;
            this.farmland = farmland;
            this.task = task;
        }
    }

    private int delay;
    private int count;
    private final Map<Long, Replant> replants = new HashMap<>();

    public HarvestSeal() {
        super("harvest", 0, new Toggle("replant", true), new Toggle("provision", false));
        this.delay = java.util.concurrent.ThreadLocalRandom.current().nextInt(33);
    }

    @Override
    public void tick(ServerLevel level, SealEntity seal) {
        if (delay % 100 == 0) {
            Iterator<Replant> it = replants.values().iterator();
            while (it.hasNext()) {
                Replant replant = it.next();
                if (!seal.bounds().contains(Vec3.atCenterOf(replant.pos))) {
                    Task old = Tasks.get(level, replant.task);
                    if (old != null) {
                        old.setSuspended(true);
                    }
                    it.remove();
                }
            }
        }
        if (delay++ % 5 != 0) {
            return;
        }
        BlockPos at = seal.posInArea(count++);
        Replant replant = replants.get(at.asLong());
        if (Fieldwork.grown(level, at)) {
            Task task = new Task(seal.pos(), at);
            task.setPriority(seal.priority());
            Tasks.add(level, task);
        } else if (toggle(REPLANT) && replant != null && level.isEmptyBlock(at)
                && Tasks.get(level, replant.task) == null) {
            Task task = new Task(seal.pos(), replant.pos);
            task.setPriority(seal.priority());
            Tasks.add(level, task);
            replant.task = task.id();
        }
    }

    @Override
    public boolean onTaskCompletion(ServerLevel level, Golem golem, Task task) {
        BlockPos at = task.pos();
        Replant replant = replants.get(at.asLong());
        if (Fieldwork.grown(level, at)) {
            FakePlayer hands = Fieldwork.hands(level, golem);
            BlockState state = level.getBlockState(at);
            if (Fieldwork.picked(state)) {
                state.useWithoutItem(level, hands, new BlockHitResult(Vec3.atCenterOf(at), Direction.UP, at, false));
            } else {
                Fieldwork.harvest(level, at, golem);
                if (toggle(REPLANT)) {
                    plan(level, task, at, state);
                }
            }
            golem.addRankXp(1);
            golem.swing(InteractionHand.MAIN_HAND);
        } else if (replant != null && replant.task == task.id() && level.isEmptyBlock(at)
                && golem.isCarrying(replant.seed)) {
            plant(level, golem, replant);
        }
        task.setSuspended(true);
        return true;
    }

    /** Works out how a crop just cut would be planted again, and asks for it to be. */
    private void plan(ServerLevel level, Task task, BlockPos at, BlockState was) {
        ItemStack seed = Fieldwork.seed(was);
        if (seed.isEmpty()) {
            return;
        }
        BlockState below = level.getBlockState(at.below());
        Direction face = null;
        if (seed.getItem() instanceof BlockItem item && !(was.getBlock() instanceof HorizontalDirectionalBlock)
                && item.getBlock().defaultBlockState().canSurvive(level, at)) {
            face = Direction.DOWN;
        } else if (was.hasProperty(HorizontalDirectionalBlock.FACING)) {
            face = was.getValue(HorizontalDirectionalBlock.FACING);
        }
        if (face != null) {
            Task again = new Task(task.seal(), at);
            again.setPriority(task.priority());
            again.setLifespan(300);
            replants.put(at.asLong(), new Replant(at, face, seed, below.is(Blocks.FARMLAND), again.id()));
            Tasks.add(level, again);
        }
    }

    /** Plants a seed back where a crop was cut, tilling the ground first if it needs it. */
    private static void plant(ServerLevel level, Golem golem, Replant replant) {
        FakePlayer hands = Fieldwork.hands(level, golem);
        BlockPos ground = replant.pos.below();
        if (replant.farmland && level.getBlockState(ground).is(BlockTags.DIRT)) {
            hands.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_HOE));
            Items.DIAMOND_HOE.useOn(new UseOnContext(hands, InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(ground), Direction.UP, ground, false)));
        }
        ItemStack seed = replant.seed.copyWithCount(1);
        hands.setItemInHand(InteractionHand.MAIN_HAND, seed);
        BlockPos against = replant.pos.relative(replant.face);
        InteractionResult planted = seed.useOn(new UseOnContext(hands, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(against), replant.face.getOpposite(), against, false)));
        hands.getInventory().clearContent();
        if (planted.consumesAction()) {
            level.levelEvent(2001, replant.pos, Block.getId(level.getBlockState(replant.pos)));
            golem.drop(replant.seed.copyWithCount(1));
            golem.addRankXp(1);
            golem.swing(InteractionHand.MAIN_HAND);
        }
    }

    /** A replanting golem must have the seed; set to, the seal asks providers for some when it has none. */
    @Override
    public boolean canGolemPerform(Golem golem, Task task) {
        Replant replant = replants.get(task.pos().asLong());
        if (replant == null || replant.task != task.id()) {
            return true;
        }
        boolean carrying = golem.isCarrying(replant.seed);
        if (!carrying && toggle(PROVISION)) {
            SealEntity seal = Seals.get(golem.level(), task.seal());
            if (seal != null) {
                Provisioning.request(golem.level(), seal, replant.seed);
            }
        }
        return carrying;
    }

    @Override
    public boolean canPlaceAt(Level level, BlockPos pos, Direction face) {
        return !level.isEmptyBlock(pos);
    }

    @Override
    public int[] categories() {
        return new int[] {AREA, OPTIONS, PRIORITY, TRAITS};
    }

    @Override
    public List<GolemTrait> required() {
        return List.of(GolemTrait.DEFT, GolemTrait.SMART);
    }

    @Override
    public boolean usesArea() {
        return true;
    }

    @Override
    public void save(CompoundTag tag, HolderLookup.Provider registries) {
        super.save(tag, registries);
        ListTag list = new ListTag();
        if (toggle(REPLANT)) {
            for (Replant replant : replants.values()) {
                CompoundTag entry = new CompoundTag();
                entry.put("pos", NbtUtils.writeBlockPos(replant.pos));
                entry.putByte("face", (byte) replant.face.get3DDataValue());
                entry.putBoolean("farmland", replant.farmland);
                entry.put("seed", replant.seed.save(registries));
                list.add(entry);
            }
        }
        tag.put("replant", list);
    }

    @Override
    public void load(CompoundTag tag, HolderLookup.Provider registries) {
        super.load(tag, registries);
        replants.clear();
        for (Tag each : tag.getList("replant", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) each;
            BlockPos pos = NbtUtils.readBlockPos(entry, "pos").orElse(null);
            ItemStack seed = ItemStack.parseOptional(registries, entry.getCompound("seed"));
            if (pos != null && !seed.isEmpty()) {
                replants.put(pos.asLong(), new Replant(pos, Direction.from3DDataValue(entry.getByte("face")), seed,
                        entry.getBoolean("farmland"), 0));
            }
        }
    }
}
