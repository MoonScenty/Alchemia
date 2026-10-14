package me.moonscenty.alchemia.item;

import java.util.Map;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.LootTableLoadEvent;

/**
 * What the original slipped into the game's own chests: in dungeons, temples, mineshafts and strongholds a chance of
 * a bag of loot, alchemium or amber, an uncommon bag or a fragment of knowledge, and once in a long while a rare bag,
 * a book of the art or an alchemium tool; fragments by the handful in a stronghold's library; alchemium at the smith.
 * <p>
 * The original put these among the chest's own things; a chest's things are their own pool now, so these are a pool
 * of their own beside it, mostly empty so that they turn up about as often as they did.
 */
@EventBusSubscriber(modid = Alchemia.MODID)
public final class ChestLoot {
    /** Which chests, and how heavily each kind weighs in them: common, uncommon. */
    private static final Map<String, int[]> CHESTS = Map.of(
            "chests/simple_dungeon", new int[] {5, 4},
            "chests/jungle_temple", new int[] {5, 4},
            "chests/desert_pyramid", new int[] {5, 4},
            "chests/abandoned_mineshaft", new int[] {4, 3},
            "chests/stronghold_corridor", new int[] {4, 3},
            "chests/stronghold_crossing", new int[] {4, 3},
            "chests/stronghold_library", new int[] {4, 3});
    /** How much of nothing the pool holds, so that the rest comes about as often as among the chest's own. */
    private static final int NOTHING = 90;

    private ChestLoot() {
    }

    @SubscribeEvent
    public static void loaded(LootTableLoadEvent event) {
        ResourceLocation name = event.getName();
        if (!"minecraft".equals(name.getNamespace())) {
            return;
        }
        int[] weights = CHESTS.get(name.getPath());
        if (weights != null) {
            LootPool.Builder pool = LootPool.lootPool().name(Alchemia.MODID + ":chest").setRolls(ConstantValue.exactly(1))
                    .add(EmptyLootItem.emptyItem().setWeight(NOTHING));
            for (ItemLike common : new ItemLike[] {ModItems.LOOT_BAG_COMMON, ModItems.ALCHEMIUM_INGOT, ModItems.AMBER}) {
                pool.add(LootItem.lootTableItem(common).setWeight(weights[0])
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))));
            }
            for (ItemLike uncommon : new ItemLike[] {ModItems.LOOT_BAG_UNCOMMON, ModItems.KNOWLEDGE_FRAGMENT}) {
                pool.add(LootItem.lootTableItem(uncommon).setWeight(weights[1])
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))));
            }
            for (ItemLike rare : new ItemLike[] {ModItems.LOOT_BAG_RARE, ModItems.ALCHEMONOMICON,
                    ModItems.METAL_TOOLS.get("alchemium_sword"), ModItems.METAL_TOOLS.get("alchemium_axe"),
                    ModItems.METAL_TOOLS.get("alchemium_hoe"), ModItems.METAL_TOOLS.get("alchemium_pickaxe")}) {
                pool.add(LootItem.lootTableItem(rare).setWeight(1));
            }
            event.getTable().addPool(pool.build());
        }
        if (name.getPath().equals("chests/stronghold_library")) {
            event.getTable().addPool(LootPool.lootPool().name(Alchemia.MODID + ":fragments")
                    .setRolls(ConstantValue.exactly(1))
                    .add(EmptyLootItem.emptyItem().setWeight(NOTHING))
                    .add(LootItem.lootTableItem(ModItems.KNOWLEDGE_FRAGMENT).setWeight(20)
                            .apply(SetItemCountFunction.setCount(UniformGenerator.between(3, 6))))
                    .build());
        }
        if (name.getPath().equals("chests/village/village_weaponsmith")
                || name.getPath().equals("chests/village/village_toolsmith")
                || name.getPath().equals("chests/village/village_armorer")) {
            event.getTable().addPool(LootPool.lootPool().name(Alchemia.MODID + ":smith")
                    .setRolls(ConstantValue.exactly(1))
                    .add(EmptyLootItem.emptyItem().setWeight(NOTHING))
                    .add(LootItem.lootTableItem(ModItems.ALCHEMIUM_INGOT).setWeight(10)
                            .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))))
                    .build());
        }
    }
}
