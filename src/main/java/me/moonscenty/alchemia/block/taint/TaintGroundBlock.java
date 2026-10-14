package me.moonscenty.alchemia.block.taint;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Land the taint has eaten: soil where there was dirt, rock where there was stone, and a crust where there was
 * wood or something softer -- and, now and then, a geyser, where a growth on a tainted trunk has broken through.
 * A geyser lets up a swarm when anybody comes within twenty-four blocks of it, and otherwise breathes flux into thin
 * air.
 * <p>
 * All three keep the spread going while the flux holds out and go back to something ordinary when it does not.
 * The crust is the odd one: it has no more strength than sand, so it falls, and when it is stacked it slumps
 * sideways too, which is what gives a tainted wood its sagging look.
 */
public class TaintGroundBlock extends Block implements Tainted {
    public enum Kind implements StringRepresentable {
        SOIL("soil"), CRUST("crust"), ROCK("rock"), GEYSER("geyser");

        private final String name;

        Kind(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    /** How rarely a starved block gives up, so a patch dies back over a while rather than all at once. */
    private static final int DIE_CHANCE = 10;
    /** How tall a stack of crust has to be before the top of it can slump off sideways. */
    private static final int SLUMP_FROM = 3;
    /** Below this much flux about it, a geyser breathes more out. The original's quarter of the usual aura. */
    private static final int GEYSER_BREATHES_BELOW = me.moonscenty.alchemia.aura.AuraGeneration.BASE / 4;

    private final Kind kind;

    public TaintGroundBlock(Kind kind, Properties properties) {
        super(properties);
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }

    @Override
    public void die(ServerLevel level, BlockPos pos, BlockState state) {
        level.setBlock(pos, switch (kind) {
            case SOIL -> Blocks.DIRT.defaultBlockState();
            case ROCK -> me.moonscenty.alchemia.registry.ModBlocks.POROUS_STONE.get().defaultBlockState();
            case CRUST -> Blocks.AIR.defaultBlockState();
            case GEYSER -> me.moonscenty.alchemia.registry.ModBlocks.FLUX_GOO.get().defaultBlockState();
        }, Block.UPDATE_ALL);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (TaintSpread.starved(level, pos) && random.nextInt(DIE_CHANCE) == 0) {
            die(level, pos, state);
            TaintSpread.heal(level, pos, random, 25);
            return;
        }
        if (kind != Kind.ROCK) {
            TaintSpread.spread(level, pos, random);
        }
        if (kind == Kind.CRUST) {
            slump(level, pos, random);
        }
        if (kind == Kind.GEYSER) {
            vent(level, pos, random);
        }
    }

    /**
     * A geyser's turn: a swarm, one time in five, if anybody is within twenty-four blocks to be gone after; otherwise,
     * if the air is thin of flux, a breath of it.
     */
    private static void vent(ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextFloat() < 0.2F
                && level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 24.0, false) != null) {
            var swarm = new me.moonscenty.alchemia.entity.TaintSwarm(level);
            swarm.moveTo(pos.getX() + 0.5, pos.getY() + 1.25, pos.getZ() + 0.5, random.nextInt(360), 0.0F);
            level.addFreshEntity(swarm);
        } else if (me.moonscenty.alchemia.aura.AuraHandler.get(level, pos,
                me.moonscenty.alchemia.registry.ModAspects.FLUX) < GEYSER_BREATHES_BELOW) {
            me.moonscenty.alchemia.aura.AuraHandler.add(level, pos, me.moonscenty.alchemia.registry.ModAspects.FLUX, 1);
        }
    }

    // --- crust falling and slumping -------------------------------------------

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (kind == Kind.CRUST) {
            level.scheduleTick(pos, this, 2);
        }
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
            LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (kind == Kind.CRUST) {
            level.scheduleTick(pos, this, 2);
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (kind == Kind.CRUST && FallingBlock.isFree(level.getBlockState(pos.below())) && pos.getY() >= level.getMinBuildHeight()) {
            FallingBlockEntity.fall(level, pos, state);
        }
    }

    /** The top of a tall enough stack, with air beside it all the way down, tips off the edge. */
    private void slump(ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.isEmptyBlock(pos.above())) {
            return;
        }
        Direction way = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        for (int down = 1; down < SLUMP_FROM + 1; down++) {
            if (!level.isEmptyBlock(pos.relative(way).below(down)) || !level.getBlockState(pos.below(down)).is(this)) {
                return;
            }
        }
        BlockPos over = pos.relative(way);
        if (level.isEmptyBlock(over)) {
            level.removeBlock(pos, false);
            FallingBlockEntity.fall(level, over, defaultBlockState());
        }
    }
}
