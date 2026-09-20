package me.moonscenty.alchemia.datagen;

import java.util.Set;

import me.moonscenty.alchemia.block.CrystalBlock;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.StoneSet;
import me.moonscenty.alchemia.registry.WoodSet;
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
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
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

        dropSelf(ModBlocks.ALCHEMIUM_BLOCK.get());
        dropSelf(ModBlocks.BRASS_BLOCK.get());

        for (WoodSet wood : ModBlocks.WOODS) {
            dropSelf(wood.log().get());
            dropSelf(wood.planks().get());
            dropSelf(wood.stairs().get());
            dropSelf(wood.sapling().get());
            add(wood.slab().get(), this::createSlabItemTable);
        }
        // Greatwood saplings are about as common as oak ones, silverwood saplings are rare
        add(ModBlocks.GREATWOOD.leaves().get(), block -> createLeavesDrops(block, ModBlocks.GREATWOOD.sapling().get(),
                1 / 44F, 1 / 36F, 1 / 28F, 1 / 20F));
        add(ModBlocks.SILVERWOOD.leaves().get(), block -> createSilverwoodLeavesDrops(block, ModBlocks.SILVERWOOD.sapling().get()));

        for (StoneSet set : ModBlocks.STONE_SETS) {
            dropSelf(set.block().get());
            dropSelf(set.stairs().get());
            add(set.slab().get(), this::createSlabItemTable);
        }
        dropSelf(ModBlocks.RESEARCH_TABLE.get());
        dropSelf(ModBlocks.AMBER_BLOCK.get());
        dropSelf(ModBlocks.AMBER_BRICKS.get());

        ModBlocks.PLANTS.forEach(plant -> dropSelf(plant.get()));

        ModBlocks.CRYSTALS.forEach((type, crystal) -> add(crystal.get(), createCrystalDrops(crystal.get(), ModItems.SHARDS.get(type).get())));
    }

    private LootTable.Builder createAmberOreDrops(Block block) {
        Holder<Enchantment> fortune = registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE);
        return createSilkTouchDispatchTable(block, applyExplosionDecay(block, LootItem.lootTableItem(ModItems.AMBER.get())
                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2)))
                .apply(ApplyBonusCount.addOreBonusCount(fortune))));
    }

    /** Silverwood leaves occasionally shed a drop of quicksilver. */
    private LootTable.Builder createSilverwoodLeavesDrops(Block leaves, Block sapling) {
        Holder<Enchantment> fortune = registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE);
        return createLeavesDrops(leaves, sapling, 1 / 200F, 1 / 160F, 1 / 120F, 1 / 80F)
                .withPool(LootPool.lootPool()
                        .when(HAS_SHEARS.or(hasSilkTouch()).invert())
                        .add(applyExplosionCondition(leaves, LootItem.lootTableItem(ModItems.QUICKSILVER_DROP.get()))
                                .when(BonusLevelTableCondition.bonusLevelFlatChance(fortune, 1 / 100F, 1 / 85F, 1 / 70F, 1 / 50F))));
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
