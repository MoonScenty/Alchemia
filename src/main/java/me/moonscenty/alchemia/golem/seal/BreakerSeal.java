package me.moonscenty.alchemia.golem.seal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.moonscenty.alchemia.golem.Golem;
import me.moonscenty.alchemia.golem.GolemTrait;
import me.moonscenty.alchemia.golem.task.Task;
import me.moonscenty.alchemia.golem.task.Tasks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Block Breaker: walks its area a block a tick, and sends golems with grinders to break whatever is there that its
 * one filter entry lets by. A golem grinds away at it for half a second for every three points of the block's
 * hardness, the cracks showing, before it gives way; it drops what it would have dropped to the right tool.
 */
public class BreakerSeal extends Seal {
    private int delay;
    /** Which block each of its tasks is for. */
    private final Map<Integer, Long> blocks = new HashMap<>();

    public BreakerSeal() {
        super("breaker", 1, new Toggle("meta", true));
        this.delay = java.util.concurrent.ThreadLocalRandom.current().nextInt(42);
    }

    @Override
    public void tick(ServerLevel level, SealEntity seal) {
        if (delay % 100 == 0) {
            blocks.keySet().removeIf(id -> Tasks.get(level, id) == null);
        }
        delay++;
        BlockPos at = seal.posInArea(delay);
        if (!blocks.containsValue(at.asLong()) && breakable(level, at)) {
            Task task = new Task(seal.pos(), at);
            task.setPriority(seal.priority());
            task.setData((int) (level.getBlockState(at).getDestroySpeed(level, at) / 3.0F));
            Tasks.add(level, task);
            blocks.put(task.id(), at.asLong());
        }
    }

    /** Anything that can be broken at all, that its filter lets by. */
    private boolean breakable(Level level, BlockPos at) {
        BlockState state = level.getBlockState(at);
        if (state.isAir() || state.getDestroySpeed(level, at) < 0) {
            return false;
        }
        ItemStack entry = filter().get(0);
        if (entry.isEmpty()) {
            return true;
        }
        boolean alike = new Matching.Rule(toggle(0), false, false, false).alike(entry,
                new ItemStack(state.getBlock().asItem()));
        return blacklist() != alike;
    }

    @Override
    public boolean onTaskCompletion(ServerLevel level, Golem golem, Task task) {
        BlockPos at = task.pos();
        if (blocks.containsKey(task.id()) && breakable(level, at)) {
            golem.swing(InteractionHand.MAIN_HAND);
            BlockState state = level.getBlockState(at);
            if (task.data() > 0) {
                float hardness = state.getDestroySpeed(level, at) / 3.0F;
                SoundType sound = state.getSoundType(level, at, golem);
                level.playSound(null, at, sound.getBreakSound(), SoundSource.BLOCKS, (sound.getVolume() + 0.7F) / 8.0F,
                        sound.getPitch() * 0.5F);
                level.destroyBlockProgress(golem.getId(), at, (int) (9.0F * (1.0F - task.data() / hardness)));
                task.setLifespan(Math.max(task.lifespan(), 10));
                task.setData(task.data() - 1);
                return false;
            }
            level.destroyBlockProgress(golem.getId(), at, -1);
            Fieldwork.harvest(level, at, golem);
            golem.addRankXp(1);
            blocks.remove(task.id());
        }
        task.setSuspended(true);
        return true;
    }

    @Override
    public boolean canGolemPerform(Golem golem, Task task) {
        if (blocks.containsKey(task.id()) && breakable(golem.level(), task.pos())) {
            return true;
        }
        task.setSuspended(true);
        return false;
    }

    @Override
    public void onTaskSuspension(ServerLevel level, Task task) {
        blocks.remove(task.id());
    }

    @Override
    public boolean canPlaceAt(Level level, BlockPos pos, Direction face) {
        return !level.isEmptyBlock(pos);
    }

    @Override
    public int[] categories() {
        return new int[] {AREA, FILTER, OPTIONS, PRIORITY, TRAITS};
    }

    @Override
    public List<GolemTrait> required() {
        return List.of(GolemTrait.BREAKER);
    }

    @Override
    public boolean usesArea() {
        return true;
    }
}
