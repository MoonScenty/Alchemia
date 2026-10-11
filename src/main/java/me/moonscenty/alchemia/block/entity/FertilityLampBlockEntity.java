package me.moonscenty.alchemia.block.entity;

import java.util.List;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.block.LampBlock;
import me.moonscenty.alchemia.essentia.EssentiaHolder;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * A lamp of fertility at work: now and then bringing two animals of a kind together.
 *
 * <p>It burns victus, drawn through the face it hangs from, and holds up to four points. Every fifteen seconds,
 * if it has more than one, it looks round seven blocks for two grown animals of the same kind that are neither
 * young nor already courting, and sets them to it, at two points the pair.
 *
 * <p>It will not crowd a pen. Where there are already more than seven of a kind it leaves them be.
 */
public class FertilityLampBlockEntity extends BlockEntity implements EssentiaHolder {
    /** How much it holds, how often it works, how far it looks, and how many is a crowd. The original's numbers. */
    private static final int HOLDS = 4;
    private static final int EVERY = 300;
    private static final int REACHES = 7;
    private static final int CROWD = 7;
    private static final int A_PAIR = 2;

    private int charges;
    private int count;

    public FertilityLampBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FERTILITY_LAMP.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FertilityLampBlockEntity lamp) {
        boolean powered = level.hasNeighborSignal(pos);
        boolean lit = lamp.charges > 1 && !powered;
        if (lit != state.getValue(LampBlock.ENABLED)) {
            level.setBlock(pos, state.setValue(LampBlock.ENABLED, lit), Block.UPDATE_ALL);
        }
        if (lit && lamp.count++ % EVERY == 0) {
            lamp.matchmake(level);
        }
    }

    /** Two of a kind, grown and willing, set to courting. */
    public void matchmake(Level level) {
        List<Animal> near = level.getEntitiesOfClass(Animal.class,
                new AABB(worldPosition).inflate(REACHES));
        for (Animal one : near) {
            if (!free(one)) {
                continue;
            }
            List<Animal> kind = near.stream().filter(other -> other.getType() == one.getType()).toList();
            if (kind.size() > CROWD) {
                continue;
            }
            Animal partner = null;
            for (Animal other : kind) {
                if (!free(other)) {
                    continue;
                }
                if (partner != null) {
                    charges -= A_PAIR;
                    setChanged();
                    other.setInLove(null);
                    partner.setInLove(null);
                    return;
                }
                partner = other;
            }
        }
    }

    private static boolean free(Animal animal) {
        return animal.getAge() == 0 && !animal.isInLove() && animal.canFallInLove();
    }

    // --- being fed ----------------------------------------------------------------------------------------------

    @Override
    public AspectList held() {
        return charges > 0 ? AspectList.of(ModAspects.LIFE, charges) : AspectList.EMPTY;
    }

    @Override
    public boolean wants(Holder<Aspect> aspect) {
        return charges < HOLDS && aspect.value() == ModAspects.LIFE.value();
    }

    @Override
    public boolean accept(Holder<Aspect> aspect) {
        if (!wants(aspect)) {
            return false;
        }
        charges++;
        setChanged();
        return true;
    }

    /** Fed only through the face it hangs from. */
    @Override
    public boolean reachableFrom(Direction side) {
        return side == getBlockState().getValue(LampBlock.FACING);
    }

    public int charges() {
        return charges;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        charges = tag.getInt("charges");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("charges", charges);
    }
}
