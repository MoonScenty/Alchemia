package me.moonscenty.alchemia.block.eldritch;

import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** A capstone of an eldritch ring. It does nothing; it is only there to be drawn. */
public class EldritchCapBlockEntity extends BlockEntity {
    public EldritchCapBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELDRITCH_CAP.get(), pos, state);
    }
}
