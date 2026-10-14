package me.moonscenty.alchemia.block.eldritch;

import me.moonscenty.alchemia.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.TriState;

/**
 * What the Outer Lands are made of where there is no maze: rock that nothing breaks, with the stars behind it. Laid
 * bare to the air, it gives way to the void -- a deadly void that pours out into whatever was opened.
 */
public class AncientBedrockBlock extends Block {
    public AncientBedrockBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos from,
            boolean moving) {
        if (!level.isClientSide && exposed(level, pos)) {
            open(level, pos);
        }
    }

    /** Whether air touches it on any side. */
    public static boolean exposed(LevelAccessor level, BlockPos pos) {
        for (Direction side : Direction.values()) {
            if (level.isEmptyBlock(pos.relative(side))) {
                return true;
            }
        }
        return false;
    }

    /** Turns it into the source of a void, that starts spreading next tick. */
    public static void open(LevelAccessor level, BlockPos pos) {
        level.setBlock(pos, ModBlocks.VACUUM.get().defaultBlockState(), Block.UPDATE_CLIENTS);
        level.scheduleTick(pos, ModBlocks.VACUUM.get(), 1);
    }

    @Override
    public TriState canSustainPlant(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos,
            Direction facing, BlockState plant) {
        return TriState.FALSE;
    }
}
