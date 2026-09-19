package me.moonscenty.alchemia.datagen;

import java.util.Set;

import me.moonscenty.alchemia.block.CrystalBlock;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public class ModBlockLootProvider extends BlockLootSubProvider {
    public ModBlockLootProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        add(ModBlocks.AMBER_ORE.get(), this::createAmberOreDrops);
        add(ModBlocks.DEEPSLATE_AMBER_ORE.get(), this::createAmberOreDrops);
        add(ModBlocks.CINNABAR_ORE.get(), block -> createOreDrop(block, ModItems.RAW_CINNABAR.get()));
        add(ModBlocks.DEEPSLATE_CINNABAR_ORE.get(), block -> createOreDrop(block, ModItems.RAW_CINNABAR.get()));

        ModBlocks.CRYSTALS.forEach((type, crystal) -> add(crystal.get(), createCrystalDrops(crystal.get(), ModItems.SHARDS.get(type).get())));
    }

    private LootTable.Builder createAmberOreDrops(Block block) {
        Holder<Enchantment> fortune = registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE);
        return createSilkTouchDispatchTable(block, applyExplosionDecay(block, LootItem.lootTableItem(ModItems.AMBER.get())
                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2)))
                .apply(ApplyBonusCount.addOreBonusCount(fortune))));
    }

    /** One shard, plus one more per growth stage. */
    private LootTable.Builder createCrystalDrops(Block block, Item shard) {
        LootPoolSingletonContainer.Builder<?> entry = LootItem.lootTableItem(shard);
        for (int age = 1; age <= CrystalBlock.MAX_AGE; age++) {
            entry.apply(SetItemCountFunction.setCount(ConstantValue.exactly(age + 1))
                    .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                            .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CrystalBlock.AGE, age))));
        }
        return LootTable.lootTable().withPool(LootPool.lootPool().add(applyExplosionDecay(block, entry)));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().<Block>map(Holder::value).toList();
    }
}
