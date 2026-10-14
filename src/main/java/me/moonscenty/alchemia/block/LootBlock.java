package me.moonscenty.alchemia.block;

import java.util.ArrayList;
import java.util.List;

import me.moonscenty.alchemia.item.LootBagItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * An old urn or crate, left where the dead were put, and worth breaking: it gives up one to three things -- one more
 * for each step of rarity -- out of the same tables as the bags of loot, common, uncommon or rare. It takes no tool
 * and almost no effort, and an urn breaks with a crash of pottery.
 */
public class LootBlock extends Block {
    /** Common, uncommon or rare, as a bag of loot is. */
    public static final IntegerProperty RARITY = IntegerProperty.create("rarity", 0, 2);

    private static final VoxelShape URN = Shapes.or(box(3, 0, 3, 13, 1, 13), box(2, 1, 2, 14, 13, 14),
            box(4, 13, 4, 12, 16, 12));
    private static final VoxelShape CRATE = box(1, 0, 1, 15, 14, 15);

    private final boolean urn;

    public LootBlock(Properties properties, boolean urn) {
        super(properties);
        this.urn = urn;
        registerDefaultState(stateDefinition.any().setValue(RARITY, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(RARITY);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return urn ? URN : CRATE;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        ServerLevel level = params.getLevel();
        RandomSource random = level.getRandom();
        int rarity = state.getValue(RARITY);
        int count = 1 + rarity + random.nextInt(3);
        List<ItemStack> drops = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ItemStack thing = LootBagItem.loot(level, rarity, random);
            if (!thing.isEmpty()) {
                drops.add(thing);
            }
        }
        return drops;
    }

    /** The original's odds for what a mound holds: rare one time in ten, uncommon a further one in four or so. */
    public static int rollRarity(RandomSource random) {
        float roll = random.nextFloat();
        return roll < 0.1F ? 2 : roll < 0.33F ? 1 : 0;
    }
}
