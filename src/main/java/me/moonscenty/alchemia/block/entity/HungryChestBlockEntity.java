package me.moonscenty.alchemia.block.entity;

import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The inside of a hungry chest, which is the inside of any chest, and the snap of its lid when it eats.
 *
 * <p>The snap is a little lift of the lid on its own, over and above however far it is being held open by somebody
 * looking in. It falls shut again by itself.
 */
public class HungryChestBlockEntity extends ChestBlockEntity {
    /** The block event that says it has just eaten. */
    public static final int SNAPS = 2;
    /** How far the lid snaps open, and how fast it falls back, as shares of fully open. */
    private static final float SNAP = 0.2F;
    private static final float FALLS = 0.1F;

    private float snap;
    private float wasSnap;

    public HungryChestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HUNGRY_CHEST.get(), pos, state);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.alchemia.hungry_chest");
    }

    public static void lidTick(Level level, BlockPos pos, BlockState state, HungryChestBlockEntity chest) {
        ChestBlockEntity.lidAnimateTick(level, pos, state, chest);
        chest.wasSnap = chest.snap;
        chest.snap = Math.max(0.0F, chest.snap - FALLS);
    }

    @Override
    public boolean triggerEvent(int id, int type) {
        if (id == SNAPS) {
            snap = Math.max(snap, SNAP);
            return true;
        }
        return super.triggerEvent(id, type);
    }

    /** How far open the lid is: held open by somebody, or snapped open by a mouthful, whichever is wider. */
    public float lid(float partial) {
        return Math.max(getOpenNess(partial), wasSnap + (snap - wasSnap) * partial);
    }
}
