package me.moonscenty.alchemia.golem.seal;

import java.util.List;

import me.moonscenty.alchemia.golem.Golem;
import me.moonscenty.alchemia.golem.GolemTrait;
import me.moonscenty.alchemia.golem.task.Task;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;

// fells from the far end, a log at a time, until nothing joined to the first log is left
public class LumberSeal extends AreaBlockSeal {
    public LumberSeal() {
        super("lumber", 0, 33);
    }

    @Override
    protected boolean wanted(Level level, BlockPos at) {
        return Fieldwork.log(level, at);
    }

    @Override
    public boolean onTaskCompletion(ServerLevel level, Golem golem, Task task) {
        if (stillWanted(level, task)) {
            golem.swing(InteractionHand.MAIN_HAND);
            if (Fieldwork.breakFurthest(level, task.pos(), golem)) {
                task.setLifespan(Math.max(task.lifespan(), 10));
                golem.addRankXp(1);
                return false;
            }
            forget(task);
        }
        task.setSuspended(true);
        return true;
    }

    @Override
    public int[] categories() {
        return new int[] {AREA, PRIORITY, TRAITS};
    }

    @Override
    public List<GolemTrait> required() {
        return List.of(GolemTrait.BREAKER, GolemTrait.SMART);
    }
}
