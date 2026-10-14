package me.moonscenty.alchemia.block;

import java.util.ArrayList;
import java.util.List;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.item.CrystallizedEssenceItem;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

/**
 * What tainted rock leaves when the taint in it dies: stone gone soft and full of holes.
 * <p>
 * Broken, it is cobblestone fourteen times in fifteen, and once in fifteen -- once more for each level of fortune --
 * something that was caught in it: a shard, amber, a cluster, a gem, a little crystallized essence of some compound
 * aspect. The original's list and weights.
 */
public class PorousStoneBlock extends Block {
    public PorousStoneBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        int fortune = 0;
        ItemStack tool = params.getOptionalParameter(LootContextParams.TOOL);
        if (tool != null && !tool.isEmpty()) {
            fortune = EnchantmentHelper.getItemEnchantmentLevel(params.getLevel().registryAccess()
                    .registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.FORTUNE), tool);
        }
        RandomSource random = params.getLevel().getRandom();
        List<ItemStack> drops = new ArrayList<>();
        drops.add(random.nextInt(15) + fortune > 13 ? caught(random) : new ItemStack(Items.COBBLESTONE));
        return drops;
    }

    /** One thing out of the holes, by the original's weights. */
    private static ItemStack caught(RandomSource random) {
        List<ItemStack> things = new ArrayList<>();
        List<Integer> weights = new ArrayList<>();
        for (Holder.Reference<Aspect> aspect : ModAspects.REGISTRY.holders().toList()) {
            if (!aspect.value().isPrimal()) {
                things.add(CrystallizedEssenceItem.of(aspect));
                weights.add(1);
            }
        }
        for (CrystalType type : CrystalType.values()) {
            if (type != CrystalType.FLUX && ModItems.SHARDS.containsKey(type)) {
                things.add(new ItemStack(ModItems.SHARDS.get(type).get()));
                weights.add(20);
            }
        }
        add(things, weights, new ItemStack(ModItems.BALANCED_SHARD.get()), 100);
        if (ModItems.SHARDS.containsKey(CrystalType.FLUX)) {
            add(things, weights, new ItemStack(ModItems.SHARDS.get(CrystalType.FLUX).get()), 5);
        }
        add(things, weights, new ItemStack(ModItems.AMBER.get()), 20);
        add(things, weights, new ItemStack(ModItems.IRON_CLUSTER.get()), 20);
        add(things, weights, new ItemStack(ModItems.GOLD_CLUSTER.get()), 10);
        add(things, weights, new ItemStack(ModItems.CINNABAR_CLUSTER.get()), 10);
        add(things, weights, new ItemStack(ModItems.COPPER_CLUSTER.get()), 10);
        add(things, weights, new ItemStack(Items.DIAMOND), 6);
        add(things, weights, new ItemStack(Items.EMERALD), 10);
        add(things, weights, new ItemStack(Items.REDSTONE), 15);
        add(things, weights, new ItemStack(Items.PRISMARINE_CRYSTALS), 3);
        add(things, weights, new ItemStack(Items.PRISMARINE_SHARD), 3);
        add(things, weights, new ItemStack(Items.CLAY_BALL), 30);
        add(things, weights, new ItemStack(Items.QUARTZ), 15);
        int total = weights.stream().mapToInt(Integer::intValue).sum();
        int roll = random.nextInt(total);
        for (int i = 0; i < things.size(); i++) {
            roll -= weights.get(i);
            if (roll < 0) {
                return things.get(i);
            }
        }
        return new ItemStack(Items.COBBLESTONE);
    }

    private static void add(List<ItemStack> things, List<Integer> weights, ItemStack thing, int weight) {
        things.add(thing);
        weights.add(weight);
    }
}
