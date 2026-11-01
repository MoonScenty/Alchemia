package me.moonscenty.alchemia.datagen;

import java.util.stream.Stream;

import me.moonscenty.alchemia.registry.ModEntities;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithEnchantedBonusCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

public class ModEntityLootProvider extends EntityLootSubProvider {
    public ModEntityLootProvider(HolderLookup.Provider registries) {
        super(FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    public void generate() {
        LootTable.Builder angry = LootTable.lootTable();
        for (int i = 0; i < 3; i++) {
            angry.withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(Items.ROTTEN_FLESH))
                    .when(LootItemRandomChanceCondition.randomChance(0.5F)));
        }
        angry.withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ModItems.ZOMBIE_BRAIN.get()))
                .when(LootItemRandomChanceWithEnchantedBonusCondition.randomChanceAndLootingBoost(registries, 0.5F,
                        0.1F)));
        add(ModEntities.ANGRY_ZOMBIE.get(), rare(angry));

        add(ModEntities.FURIOUS_ZOMBIE.get(), rare(LootTable.lootTable().withPool(LootPool.lootPool()
                .setRolls(ConstantValue.exactly(12))
                .add(LootItem.lootTableItem(Items.ROTTEN_FLESH)
                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(2)))
                        .when(LootItemRandomChanceCondition.randomChance(0.5F))))));
    }

    private LootTable.Builder rare(LootTable.Builder table) {
        return table.withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(Items.IRON_INGOT))
                .add(LootItem.lootTableItem(Items.CARROT))
                .add(LootItem.lootTableItem(Items.POTATO))
                .when(LootItemKilledByPlayerCondition.killedByPlayer())
                .when(LootItemRandomChanceWithEnchantedBonusCondition.randomChanceAndLootingBoost(registries, 0.025F,
                        0.01F)));
    }

    @Override
    protected Stream<EntityType<?>> getKnownEntityTypes() {
        return Stream.of(ModEntities.ANGRY_ZOMBIE.get(), ModEntities.FURIOUS_ZOMBIE.get());
    }
}
