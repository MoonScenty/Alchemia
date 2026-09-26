package me.moonscenty.alchemia.block.entity;

import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Fills the wand lying on the bench below, a little at a time, for as long as there is aura to be had.
 * <p>
 * It pulls the same way a wand in a hand does, so standing in a worked-out chunk gets nothing here either. What it
 * buys is not speed so much as not having to hold the thing.
 */
public class ArcaneWorkbenchChargerBlockEntity extends BlockEntity {
    /** How often it pulls, in ticks, and how much it asks for each time. */
    private static final int EVERY = 10;
    private static final int AMOUNT = 5;

    private int counter;

    public ArcaneWorkbenchChargerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ARCANE_WORKBENCH_CHARGER.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ArcaneWorkbenchChargerBlockEntity charger) {
        if (charger.counter++ % EVERY != 0 || !(level instanceof ServerLevel served)) {
            return;
        }
        if (!(level.getBlockEntity(pos.below()) instanceof ArcaneWorkbenchBlockEntity bench)) {
            return;
        }
        ItemStack wand = bench.wand();
        if (!(wand.getItem() instanceof WandItem)) {
            return;
        }
        if (WandItem.charge(wand, served, pos, AMOUNT)) {
            bench.setChanged();
            // a sparkle down the middle, where the crystal hangs
            served.sendParticles(ParticleTypes.END_ROD,
                    pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5, 2, 0.1, 0.1, 0.1, 0.01);
        }
    }
}
