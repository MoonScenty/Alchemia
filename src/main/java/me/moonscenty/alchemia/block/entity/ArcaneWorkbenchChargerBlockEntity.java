package me.moonscenty.alchemia.block.entity;

import org.joml.Vector3f;

import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.particle.SparkleOptions;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ArcaneWorkbenchChargerBlockEntity extends BlockEntity {
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
        WandItem.Charged charged = WandItem.chargeFrom(wand, served, pos, AMOUNT);
        if (charged == null) {
            return;
        }
        bench.setChanged();
        if (charged.full()) {
            RandomSource random = served.random;
            BlockPos from = pos.offset(random.nextInt(3) - random.nextInt(3), random.nextInt(3),
                    random.nextInt(3) - random.nextInt(3));
            Vector3f target = new Vector3f(pos.getX() + 0.4F + random.nextFloat() * 0.2F,
                    pos.getY() + 0.4F + random.nextFloat() * 0.2F, pos.getZ() + 0.4F + random.nextFloat() * 0.2F);
            served.sendParticles(new SparkleOptions(charged.last().value().color(), target),
                    from.getX() + random.nextDouble(), from.getY() + random.nextDouble(),
                    from.getZ() + random.nextDouble(), 1, 0.0, 0.0, 0.0, 0.0);
        }
    }
}
