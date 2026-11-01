package me.moonscenty.alchemia.golem.seal;

import java.util.Arrays;
import java.util.List;

import me.moonscenty.alchemia.golem.Golem;
import me.moonscenty.alchemia.golem.GolemTrait;
import me.moonscenty.alchemia.golem.task.Task;
import me.moonscenty.alchemia.golem.task.Tasks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;

// The Store seal; a whitelisted entry's count caps how many of it are wanted there.
public class FillSeal extends Seal {
    private static final int EXIST = 4;

    private final boolean advanced;
    private int delay;
    private int watched = Integer.MIN_VALUE;

    public FillSeal(boolean advanced) {
        super(advanced ? "fill_advanced" : "fill", advanced ? 9 : 1, new Toggle("meta", true),
                new Toggle("nbt", true), new Toggle("ore", false), new Toggle("mod", false),
                new Toggle("exist", false));
        this.advanced = advanced;
        this.delay = java.util.concurrent.ThreadLocalRandom.current().nextInt(50);
    }

    @Override
    public void tick(ServerLevel level, SealEntity seal) {
        if (delay++ % 20 != 0) {
            return;
        }
        Task old = Tasks.get(level, watched);
        if (old == null || old.reserved() || old.suspended() || old.completed()) {
            open(level, seal);
        }
    }

    private void open(ServerLevel level, SealEntity seal) {
        Task task = new Task(seal.pos(), seal.pos().pos());
        task.setPriority(seal.priority());
        Tasks.add(level, task);
        watched = task.id();
    }

    // offer another task at once so several golems can fill together
    @Override
    public void onTaskStarted(ServerLevel level, Golem golem, Task task) {
        SealEntity seal = Seals.get(level, task.seal());
        if (seal != null && !seal.stoppedByRedstone(level)) {
            open(level, seal);
        }
    }

    private int limit(Matching.Match match, int there) {
        int limit = match.stack().getCount();
        if (hasStacksizeLimiters() && match.entry() >= 0 && limit(match.entry()) > 0) {
            limit = there < limit(match.entry()) ? limit(match.entry()) - there : 0;
        }
        return limit;
    }

    @Override
    public boolean onTaskCompletion(ServerLevel level, Golem golem, Task task) {
        SealPos at = task.seal();
        IItemHandler container = Matching.container(level, at.pos(), at.face());
        Matching.Match match = Matching.firstAllowed(filter(), blacklist(), Arrays.asList(golem.carrying()), rule());
        if (match.found()) {
            ItemStack wanted = match.entry() >= 0 ? filter().get(match.entry()) : match.stack();
            if (container != null) {
                int limit = limit(match, Matching.count(container, wanted.copyWithCount(Integer.MAX_VALUE), rule()));
                if (limit > 0) {
                    ItemStack given = golem.drop(match.stack().copyWithCount(limit));
                    golem.hold(Matching.insert(container, given));
                    handled(level, golem, 1.0F);
                    golem.addRankXp(1);
                }
            } else {
                int limit = limit(match, Matching.countLoose(level, at.pos(), wanted, 1.5, rule()));
                if (limit > 0) {
                    ItemStack given = golem.drop(match.stack().copyWithCount(limit));
                    Direction face = at.face();
                    ItemEntity dropped = new ItemEntity(level, at.pos().getX() + 0.5 + face.getStepX(),
                            at.pos().getY() + 0.5 + face.getStepY(), at.pos().getZ() + 0.5 + face.getStepZ(), given);
                    dropped.setDeltaMovement(dropped.getDeltaMovement().multiply(0.2, 0.5, 0.2));
                    level.addFreshEntity(dropped);
                    handled(level, golem, 1.0F);
                }
            }
        }
        task.setSuspended(true);
        return true;
    }

    @Override
    public boolean canGolemPerform(Golem golem, Task task) {
        SealPos at = task.seal();
        IItemHandler container = Matching.container(golem.level(), at.pos(), at.face());
        Matching.Match match = Matching.firstAllowed(filter(), blacklist(), Arrays.asList(golem.carrying()), rule());
        if (!match.found()) {
            return false;
        }
        ItemStack wanted = match.entry() >= 0 ? filter().get(match.entry()) : match.stack();
        if (container == null) {
            return !hasStacksizeLimiters() || match.entry() < 0 || limit(match.entry()) <= 0
                    || Matching.countLoose(golem.level(), at.pos(), wanted, 1.5, rule()) < limit(match.entry());
        }
        if (toggle(EXIST) && Matching.count(container, match.stack(), rule()) <= 0) {
            return false;
        }
        if (!Matching.hasRoomFor(container, match.stack())) {
            return false;
        }
        return !hasStacksizeLimiters() || match.entry() < 0 || limit(match.entry()) <= 0
                || Matching.count(container, wanted.copyWithCount(Integer.MAX_VALUE), rule()) < limit(match.entry());
    }

    @Override
    public boolean canPlaceAt(Level level, BlockPos pos, Direction face) {
        return !level.isEmptyBlock(pos);
    }

    @Override
    public int[] categories() {
        return advanced ? new int[] {FILTER, OPTIONS, PRIORITY, TRAITS} : new int[] {FILTER, PRIORITY, TRAITS};
    }

    @Override
    public List<GolemTrait> required() {
        return advanced ? List.of(GolemTrait.SMART) : List.of();
    }

    @Override
    public List<GolemTrait> forbidden() {
        return List.of(GolemTrait.CLUMSY);
    }

    @Override
    public boolean hasStacksizeLimiters() {
        return !blacklist();
    }
}
