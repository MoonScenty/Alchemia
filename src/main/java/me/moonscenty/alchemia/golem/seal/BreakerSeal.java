package me.moonscenty.alchemia.golem.seal;

import java.util.List;

import me.moonscenty.alchemia.golem.Golem;
import me.moonscenty.alchemia.golem.GolemTrait;
import me.moonscenty.alchemia.golem.task.Task;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

// grinds half a second per three points of hardness, then drops what the right tool would
public class BreakerSeal extends AreaBlockSeal {
    public BreakerSeal() {
        super("breaker", 1, 42, new Toggle("meta", true));
    }

    @Override
    protected boolean wanted(Level level, BlockPos at) {
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
    protected void prepare(ServerLevel level, BlockPos at, Task task) {
        task.setData((int) (level.getBlockState(at).getDestroySpeed(level, at) / 3.0F));
    }

    @Override
    public boolean onTaskCompletion(ServerLevel level, Golem golem, Task task) {
        BlockPos at = task.pos();
        if (stillWanted(level, task)) {
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
            forget(task);
        }
        task.setSuspended(true);
        return true;
    }

    @Override
    public int[] categories() {
        return new int[] {AREA, FILTER, OPTIONS, PRIORITY, TRAITS};
    }

    @Override
    public List<GolemTrait> required() {
        return List.of(GolemTrait.BREAKER);
    }
}
