package me.moonscenty.alchemia.block.entity;

import me.moonscenty.alchemia.block.BellowsBlock;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BellowsBlockEntity extends BlockEntity {
    // one extra cook tick every 2 ticks = 50% faster
    private static final int PUSHES_EVERY = 2;

    public static final float EMPTIEST = 0.35F;
    private static final float EMPTIES = 0.075F;
    private static final float FILLS = 0.025F;

    private int since;
    private float inflation = 1.0F;
    private float wasInflated = 1.0F;
    private boolean filling;
    private boolean begun;

    public BellowsBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BELLOWS.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BellowsBlockEntity bellows) {
        if (!state.getValue(BellowsBlock.ENABLED) || ++bellows.since < PUSHES_EVERY) {
            return;
        }
        bellows.since = 0;
        if (level.getBlockEntity(pos.relative(state.getValue(BellowsBlock.FACING)))
                instanceof AbstractFurnaceBlockEntity furnace
                // never push the final tick, so the furnace completes the item itself
                && furnace.cookingProgress > 0 && furnace.cookingProgress < furnace.cookingTotalTime - 1) {
            furnace.cookingProgress++;
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BellowsBlockEntity bellows) {
        bellows.wasInflated = bellows.inflation;
        if (!state.getValue(BellowsBlock.ENABLED)) {
            return;
        }
        if (!bellows.begun) {
            bellows.inflation = EMPTIEST + level.random.nextFloat() * 0.55F;
            bellows.wasInflated = bellows.inflation;
            bellows.begun = true;
        }
        if (!bellows.filling) {
            bellows.inflation -= EMPTIES;
            if (bellows.inflation <= EMPTIEST) {
                bellows.filling = true;
            }
        } else {
            bellows.inflation += FILLS;
            if (bellows.inflation >= 1.0F) {
                bellows.filling = false;
                level.playLocalSound(pos, SoundEvents.GHAST_SHOOT, SoundSource.BLOCKS, 0.01F,
                        0.5F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F, false);
            }
        }
    }

    public float inflation(float partial) {
        return wasInflated + (inflation - wasInflated) * partial;
    }
}
