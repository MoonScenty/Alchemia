package me.moonscenty.alchemia.datagen;

import net.minecraft.world.level.block.Blocks;
import java.util.Set;

import me.moonscenty.alchemia.block.CrystalBlock;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModDataComponents;
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
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
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
        dropSelf(ModBlocks.CRUCIBLE.get());
        dropSelf(ModBlocks.TUBE.get());
        dropSelf(ModBlocks.TUBE_VALVE.get());
        dropSelf(ModBlocks.TUBE_ONEWAY.get());
        dropSelf(ModBlocks.TUBE_RESTRICT.get());
        dropSelf(ModBlocks.TUBE_FILTER.get());
        dropSelf(ModBlocks.TUBE_BUFFER.get());
        // a jar keeps what is in it and what is written on it when it is taken up off the floor
        for (var jar : java.util.List.of(ModBlocks.JAR, ModBlocks.VOID_JAR)) {
            add(jar.get(), block -> createSingleItemTable(block).apply(
                    CopyComponentsFunction.copyComponents(CopyComponentsFunction.Source.BLOCK_ENTITY)
                            .include(ModDataComponents.CONTENTS.get())
                            .include(ModDataComponents.ESSENTIA.get())));
        }
        // a brain in a jar keeps what it has eaten
        add(ModBlocks.BRAIN_JAR.get(), block -> createSingleItemTable(block).apply(
                CopyComponentsFunction.copyComponents(CopyComponentsFunction.Source.BLOCK_ENTITY)
                        .include(ModDataComponents.STORED_XP.get())));
        dropSelf(ModBlocks.ESSENTIA_SMELTER.get());
        dropSelf(ModBlocks.ALCHEMIUM_SMELTER.get());
        dropSelf(ModBlocks.VOID_SMELTER.get());
        dropSelf(ModBlocks.SMELTER_AUX.get());
        dropSelf(ModBlocks.SMELTER_VENT.get());
        dropSelf(ModBlocks.ALEMBIC.get());
        dropSelf(ModBlocks.ARCANE_WORKBENCH.get());
        dropSelf(ModBlocks.FOCAL_MANIPULATOR.get());
        dropSelf(ModBlocks.BELLOWS.get());
        dropSelf(ModBlocks.CENTRIFUGE.get());
        // a mirror taken down from a pair remembers its partner
        for (var mirror : java.util.List.of(ModBlocks.MIRROR.get(), ModBlocks.MIRROR_ESSENTIA.get())) {
            add(mirror, block -> createSingleItemTable(block).apply(
                    CopyComponentsFunction.copyComponents(CopyComponentsFunction.Source.BLOCK_ENTITY)
                            .include(ModDataComponents.MIRROR_LINK.get())));
        }
        // the heart gives nothing; the shell gives back what it was
        add(ModBlocks.INFERNAL_FURNACE.get(), noDrop());
        add(ModBlocks.FURNACE_SHELL.get(), block -> LootTable.lootTable()
                .withPool(applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(Blocks.OBSIDIAN)
                                .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                        .setProperties(StatePropertiesPredicate.Builder.properties()
                                                .hasProperty(me.moonscenty.alchemia.block.FurnaceShellBlock.OBSIDIAN,
                                                        true)))
                                .otherwise(LootItem.lootTableItem(Blocks.NETHER_BRICKS))))));
        // the stone table gives itself; a golem press gives back what each part of it was made from
        dropSelf(ModBlocks.STONE_TABLE.get());
        dropSelf(ModBlocks.WOODEN_TABLE.get());
        dropSelf(ModBlocks.ETHEREAL_BLOOM.get());
        add(ModBlocks.AMBIENT_GRASS.get(), block -> createSingleItemTableWithSilkTouch(block, Blocks.DIRT));
        dropOther(ModBlocks.GOLEM_PRESS.get(), Blocks.PISTON);
        add(ModBlocks.GOLEM_PRESS_PART.get(), block -> {
            LootPool.Builder pool = LootPool.lootPool().setRolls(ConstantValue.exactly(1));
            for (me.moonscenty.alchemia.block.GolemPressPartBlock.Part part
                    : me.moonscenty.alchemia.block.GolemPressPartBlock.Part.values()) {
                pool.add(LootItem.lootTableItem(part.original().getBlock())
                        .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                .setProperties(StatePropertiesPredicate.Builder.properties()
                                        .hasProperty(me.moonscenty.alchemia.block.GolemPressPartBlock.PART, part))));
            }
            return LootTable.lootTable().withPool(applyExplosionCondition(block, pool));
        });
        dropSelf(ModBlocks.ARCANE_BORE.get());
        dropSelf(ModBlocks.ARCANE_BORE_BASE.get());
        // each half of a thaumatorium was a block of brass, and gives it back
        dropOther(ModBlocks.THAUMATORIUM.get(), ModBlocks.ALCHEMICAL_CONSTRUCT.get());
        dropSelf(ModBlocks.ALCHEMICAL_CONSTRUCT.get());
        dropSelf(ModBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.get());
        dropSelf(ModBlocks.MNEMONIC_MATRIX.get());
        dropSelf(ModBlocks.CRYSTALLIZER.get());
        dropSelf(ModBlocks.ARCANE_LAMP.get());
        dropSelf(ModBlocks.GROWTH_LAMP.get());
        dropSelf(ModBlocks.FERTILITY_LAMP.get());
        dropSelf(ModBlocks.HUNGRY_CHEST.get());
        dropSelf(ModBlocks.WATER_JUG.get());
        dropSelf(ModBlocks.LEVITATOR.get());
        dropSelf(ModBlocks.REDSTONE_RELAY.get());
        dropSelf(ModBlocks.ARCANE_EAR.get());
        dropSelf(ModBlocks.BARRIER_STONE.get());
        dropSelf(ModBlocks.TRAVEL_STONE.get());
        dropSelf(ModBlocks.FLESH_BLOCK.get());
        dropSelf(ModBlocks.PATTERN_CRAFTER.get());
        dropSelf(ModBlocks.ESSENTIA_INPUT.get());
        dropSelf(ModBlocks.ESSENTIA_OUTPUT.get());
        ModBlocks.TALLOW_CANDLES.values().forEach(candle -> dropSelf(candle.get()));
        dropSelf(ModBlocks.SPA.get());
        dropSelf(ModBlocks.RECHARGE_PEDESTAL.get());
        dropSelf(ModBlocks.DIOPTRA.get());
        dropSelf(ModBlocks.AURA_TOTEM_PUSH.get());
        dropSelf(ModBlocks.AURA_TOTEM_PULL.get());
        dropSelf(ModBlocks.AURA_TOTEM_POLE_INNER.get());
        dropSelf(ModBlocks.AURA_TOTEM_POLE_OUTER.get());
        dropSelf(ModBlocks.AURA_TOTEM_POLE_PURE.get());
        dropSelf(ModBlocks.ARCANE_WORKBENCH_CHARGER.get());
        dropSelf(ModBlocks.NODE_STABILIZER.get());
        dropSelf(ModBlocks.ARCANE_PEDESTAL.get());
        dropSelf(ModBlocks.INFUSION_MATRIX.get());
        // a corner is arcane stone that has been told what it is for; broken, it is arcane stone again
        // a pillar gives back the stone it was built of
        add(ModBlocks.ARCANE_PILLAR.get(), block -> {
            net.minecraft.world.level.storage.loot.LootTable.Builder table =
                    net.minecraft.world.level.storage.loot.LootTable.lootTable();
            for (me.moonscenty.alchemia.block.PillarKind kind : me.moonscenty.alchemia.block.PillarKind.values()) {
                table.withPool(applyExplosionCondition(block, net.minecraft.world.level.storage.loot.LootPool.lootPool()
                        .add(net.minecraft.world.level.storage.loot.entries.LootItem.lootTableItem(kind.stone()))
                        .when(net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition
                                .hasBlockStateProperties(block).setProperties(
                                        net.minecraft.advancements.critereon.StatePropertiesPredicate.Builder
                                                .properties().hasProperty(
                                                        me.moonscenty.alchemia.block.ArcanePillarBlock.KIND, kind)))));
            }
            return table;
        });
        for (var stone : java.util.List.of(ModBlocks.ANCIENT_ROCK, ModBlocks.ANCIENT_TILE, ModBlocks.GLYPHED_STONE,
                ModBlocks.CRUSTED_STONE, ModBlocks.GLOWING_CRUSTED_STONE)) {
            dropSelf(stone.get());
        }
        // what it drops it works out for itself
        add(ModBlocks.POROUS_STONE.get(), noDrop());
        add(ModBlocks.LOOT_URN.get(), noDrop());
        add(ModBlocks.LOOT_CRATE.get(), noDrop());
        dropSelf(ModBlocks.ANCIENT_PEDESTAL.get());
        // a banner works out for itself which banner it is
        add(ModBlocks.BANNER.get(), noDrop());
        dropSelf(ModBlocks.ELDRITCH_PEDESTAL.get());

        // nothing tainted is worth keeping; what the growths gave in the original arrives with those items
        add(ModBlocks.TAINT_FIBRE.get(), noDrop());
        add(ModBlocks.TAINT_SOIL.get(), noDrop());
        add(ModBlocks.TAINT_CRUST.get(), noDrop());
        add(ModBlocks.TAINT_ROCK.get(), noDrop());
        add(ModBlocks.TAINT_GEYSER.get(), noDrop());
        add(ModBlocks.TAINT_LOG.get(), noDrop());
        add(ModBlocks.FLUX_GOO.get(), noDrop());
        dropSelf(ModBlocks.AMBER_BLOCK.get());
        dropSelf(ModBlocks.AMBER_BRICKS.get());

        ModBlocks.PLANTS.forEach(plant -> dropSelf(plant.get()));
        ModBlocks.NITOR.values().forEach(flame -> dropSelf(flame.get()));
        dropSelf(ModBlocks.INFUSION_SPEED_STONE.get());
        dropSelf(ModBlocks.INFUSION_COST_STONE.get());

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
