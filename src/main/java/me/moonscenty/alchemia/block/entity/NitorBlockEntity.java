package me.moonscenty.alchemia.block.entity;

import me.moonscenty.alchemia.block.NitorBlock;
import me.moonscenty.alchemia.client.NitorEffects;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;

// client-only ticker for the flame particles; animateTick fires too rarely
public class NitorBlockEntity extends BlockEntity {
    public NitorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NITOR.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, NitorBlockEntity nitor) {
        if (state.getBlock() instanceof NitorBlock flame) {
            NitorEffects.burn(level, pos, flame.colour());
        }
    }
}
