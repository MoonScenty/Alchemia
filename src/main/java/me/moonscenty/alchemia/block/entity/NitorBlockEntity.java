package me.moonscenty.alchemia.block.entity;

import me.moonscenty.alchemia.block.NitorBlock;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * A nitor's ticking, which is the whole of what makes one visible.
 * <p>
 * It holds nothing and saves nothing. It exists because the block has no model at all -- what is there to see is
 * motes coming off the spot, and something has to let them off every tick. The hook the game offers for idle
 * scenery is called a few times a minute for any one block, which is fine for a torch that is already drawn and
 * useless for a thing that is nothing but its own sparks.
 * <p>
 * Client side only. Nothing about a nitor needs the server to think about it.
 */
public class NitorBlockEntity extends BlockEntity {
    public NitorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NITOR.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, NitorBlockEntity nitor) {
        if (state.getBlock() instanceof NitorBlock flame) {
            flame.burn(level, pos);
        }
    }
}
