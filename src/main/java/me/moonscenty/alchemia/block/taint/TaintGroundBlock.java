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
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;

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

    private static final int DIE_CHANCE = 10;
    private static final int SLUMP_FROM = 3;
    // original's quarter of the base aura
    private static final int GEYSER_BREATHES_BELOW = me.moonscenty.alchemia.aura.AuraGeneration.BASE / 4;

    private final Kind kind;

    public TaintGroundBlock(Kind kind, Properties properties) {
        super(properties);
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }

    // original's values
    public static final int POISONS_ONE_IN = 750;
    public static final int POISONS_FOR = 200;

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, net.minecraft.world.entity.Entity entity) {
        if (!level.isClientSide && entity instanceof net.minecraft.world.entity.LivingEntity living
                && !living.isInvertedHealAndHarm() && level.random.nextInt(POISONS_ONE_IN) == 0) {
            poison(living);
        }
        super.stepOn(level, pos, state, entity);
    }

    public static void poison(net.minecraft.world.entity.LivingEntity living) {
        living.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                me.moonscenty.alchemia.player.effect.ModEffects.TAINT_POISON, POISONS_FOR, 0, false, true));
    }

    @Override
    public void die(ServerLevel level, BlockPos pos, BlockState state) {
        level.setBlock(pos, switch (kind) {
            case SOIL, CRUST -> TaintDustBlock.of(me.moonscenty.alchemia.registry.ModBlocks.TAINT_DUST.get(),
                    TaintDustBlock.FULL);
            case ROCK -> me.moonscenty.alchemia.registry.ModBlocks.POROUS_STONE.get().defaultBlockState();
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
        if (kind == Kind.CRUST && FallingBlock.isFree(level.getBlockState(pos.below())) && !besideWood(level, pos.below())
                && pos.getY() >= level.getMinBuildHeight()) {
            FallingBlockEntity.fall(level, pos, state);
        }
    }

    // the original's crust hangs on anywhere within a block of a log
    private static boolean besideWood(LevelAccessor level, BlockPos pos) {
        for (BlockPos near : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
            if (level.getBlockState(near).is(net.minecraft.tags.BlockTags.LOGS)) {
                return true;
            }
        }
        return false;
    }

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
        if (level.isEmptyBlock(over) && !besideWood(level, over.below())) {
            level.removeBlock(pos, false);
            FallingBlockEntity.fall(level, over, defaultBlockState());
        }
    }
}
