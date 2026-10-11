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

/**
 * The bellows working: pushing a furnace along on the server, and breathing in and out on the client.
 *
 * <p>The breathing is only for looking at. A pair of bellows that is pumping is a pair that is blowing, whatever
 * the bag happens to be doing at that moment, so nothing the server does waits on it.
 */
public class BellowsBlockEntity extends BlockEntity {
    /** How often they give a furnace an extra tick of cooking: every second tick, so half again as fast. */
    private static final int PUSHES_EVERY = 2;

    /** How full the bag is, from the emptiest it gets to quite full, and which way it is going. */
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

    /**
     * One extra tick of cooking for the furnace in front, every other tick, while there is something in it to
     * hurry. Never the very last tick: the furnace finishes its own work, so what comes out of it comes out the
     * way a furnace's work always does.
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, BellowsBlockEntity bellows) {
        if (!state.getValue(BellowsBlock.ENABLED) || ++bellows.since < PUSHES_EVERY) {
            return;
        }
        bellows.since = 0;
        if (level.getBlockEntity(pos.relative(state.getValue(BellowsBlock.FACING)))
                instanceof AbstractFurnaceBlockEntity furnace
                && furnace.cookingProgress > 0 && furnace.cookingProgress < furnace.cookingTotalTime - 1) {
            furnace.cookingProgress++;
        }
    }

    /** The bag emptying quickly and filling slowly, with a puff at the top of every breath. */
    public static void clientTick(Level level, BlockPos pos, BlockState state, BellowsBlockEntity bellows) {
        bellows.wasInflated = bellows.inflation;
        if (!state.getValue(BellowsBlock.ENABLED)) {
            return;
        }
        if (!bellows.begun) {
            // each pair starts somewhere of its own, so a row of them does not breathe in step
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

    /** How full the bag is this frame. */
    public float inflation(float partial) {
        return wasInflated + (inflation - wasInflated) * partial;
    }
}
