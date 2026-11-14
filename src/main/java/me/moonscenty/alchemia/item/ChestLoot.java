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

// a pool beside each chest's own, padded with nothing so items turn up about as often as in the original
@EventBusSubscriber(modid = Alchemia.MODID)
public final class ChestLoot {
    // weights: common, uncommon
    private static final Map<String, int[]> CHESTS = Map.of(
            "chests/simple_dungeon", new int[] {5, 4},
            "chests/jungle_temple", new int[] {5, 4},
            "chests/desert_pyramid", new int[] {5, 4},
            "chests/abandoned_mineshaft", new int[] {4, 3},
            "chests/stronghold_corridor", new int[] {4, 3},
            "chests/stronghold_crossing", new int[] {4, 3},
            "chests/stronghold_library", new int[] {4, 3});
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
            for (ItemLike common : new ItemLike[] {ModItems.LOOT_BAG_COMMON, ModItems.THAUMIUM_INGOT, ModItems.AMBER}) {
                pool.add(LootItem.lootTableItem(common).setWeight(weights[0])
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))));
            }
            for (ItemLike uncommon : new ItemLike[] {ModItems.LOOT_BAG_UNCOMMON, ModItems.KNOWLEDGE_FRAGMENT,
                    ModItems.MUNDANE_AMULET, ModItems.MUNDANE_RING, ModItems.MUNDANE_BELT}) {
                pool.add(LootItem.lootTableItem(uncommon).setWeight(weights[1])
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))));
            }
            for (ItemLike rare : new ItemLike[] {ModItems.LOOT_BAG_RARE, ModItems.THAUMONOMICON,
                    ModItems.PROTECTION_RING, ModItems.VIS_STONE, ModItems.APPRENTICE_RINGS.get("air"),
                    ModItems.APPRENTICE_RINGS.get("earth"), ModItems.APPRENTICE_RINGS.get("fire"),
                    ModItems.APPRENTICE_RINGS.get("water"), ModItems.APPRENTICE_RINGS.get("order"),
                    ModItems.APPRENTICE_RINGS.get("entropy"),
                    ModItems.METAL_TOOLS.get("thaumium_sword"), ModItems.METAL_TOOLS.get("thaumium_axe"),
                    ModItems.METAL_TOOLS.get("thaumium_hoe"), ModItems.METAL_TOOLS.get("thaumium_pickaxe")}) {
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
                    .add(LootItem.lootTableItem(ModItems.THAUMIUM_INGOT).setWeight(10)
                            .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))))
                    .build());
        }
    }
}
