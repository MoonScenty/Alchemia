package me.moonscenty.alchemia.registry;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.ArcanePedestalBlock;
import me.moonscenty.alchemia.block.ArcanePillarBlock;
import me.moonscenty.alchemia.block.ArcaneWorkbenchBlock;
import me.moonscenty.alchemia.block.ArcaneWorkbenchChargerBlock;
import me.moonscenty.alchemia.block.BufferTubeBlock;
import me.moonscenty.alchemia.block.FilterTubeBlock;
import me.moonscenty.alchemia.block.OnewayTubeBlock;
import me.moonscenty.alchemia.block.RestrictTubeBlock;
import me.moonscenty.alchemia.block.ValveTubeBlock;
import me.moonscenty.alchemia.block.AlembicBlock;
import me.moonscenty.alchemia.block.CrucibleBlock;
import me.moonscenty.alchemia.block.JarBlock;
import me.moonscenty.alchemia.item.JarBlockItem;
import me.moonscenty.alchemia.block.TubeBlock;
import me.moonscenty.alchemia.block.EssentiaSmelterBlock;
import me.moonscenty.alchemia.block.InfusionMatrixBlock;
import me.moonscenty.alchemia.block.CrystalBlock;
import me.moonscenty.alchemia.block.CrystalType;
import me.moonscenty.alchemia.block.HoleBlock;
import me.moonscenty.alchemia.block.NodeStabilizerBlock;
import me.moonscenty.alchemia.block.ResearchTableBlock;
import me.moonscenty.alchemia.block.ShimmerleafBlock;
import me.moonscenty.alchemia.block.VishroomBlock;
import me.moonscenty.alchemia.block.taint.FluxGooBlock;
import me.moonscenty.alchemia.block.taint.TaintFibreBlock;
import me.moonscenty.alchemia.block.taint.TaintGroundBlock;
import me.moonscenty.alchemia.block.taint.TaintLogBlock;
import me.moonscenty.alchemia.block.CinderpearlBlock;
import me.moonscenty.alchemia.block.NitorBlock;
import me.moonscenty.alchemia.worldgen.ModWorldgen;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Alchemia.MODID);

    public static final DeferredBlock<Block> AMBER_ORE = register("amber_ore",
            () -> new DropExperienceBlock(UniformInt.of(1, 4), BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)));
    public static final DeferredBlock<Block> DEEPSLATE_AMBER_ORE = register("deepslate_amber_ore",
            () -> new DropExperienceBlock(UniformInt.of(1, 4), BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE)));
    public static final DeferredBlock<Block> CINNABAR_ORE = register("cinnabar_ore",
            () -> new DropExperienceBlock(ConstantInt.of(0), BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)));
    public static final DeferredBlock<Block> DEEPSLATE_CINNABAR_ORE = register("deepslate_cinnabar_ore",
            () -> new DropExperienceBlock(ConstantInt.of(0), BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE)));

    public static final DeferredBlock<ResearchTableBlock> RESEARCH_TABLE = register("research_table",
            () -> new ResearchTableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)
                    .mapColor(MapColor.COLOR_BROWN)
                    .noOcclusion()));

    public static final DeferredBlock<ArcaneWorkbenchBlock> ARCANE_WORKBENCH = register("arcane_workbench",
            () -> new ArcaneWorkbenchBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE)
                    .mapColor(MapColor.COLOR_BROWN)
                    .noOcclusion()));

    public static final DeferredBlock<CrucibleBlock> CRUCIBLE = register("crucible",
            () -> new CrucibleBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAULDRON)
                    .mapColor(MapColor.METAL)
                    .noOcclusion()));

    public static final DeferredBlock<EssentiaSmelterBlock> ESSENTIA_SMELTER = register("essentia_smelter",
            () -> new EssentiaSmelterBlock(smelter(MapColor.STONE), EssentiaSmelterBlock.Tier.BASIC));
    public static final DeferredBlock<EssentiaSmelterBlock> ALCHEMIUM_SMELTER = register("alchemium_smelter",
            () -> new EssentiaSmelterBlock(smelter(MapColor.COLOR_PURPLE), EssentiaSmelterBlock.Tier.ALCHEMIUM));
    public static final DeferredBlock<EssentiaSmelterBlock> VOID_SMELTER = register("void_smelter",
            () -> new EssentiaSmelterBlock(smelter(MapColor.COLOR_BLACK), EssentiaSmelterBlock.Tier.VOID));
    public static final DeferredBlock<me.moonscenty.alchemia.block.InfernalFurnaceBlock> INFERNAL_FURNACE =
            registerBlockOnly("infernal_furnace", () -> new me.moonscenty.alchemia.block.InfernalFurnaceBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.FIRE).strength(2.0F, 12.0F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE).lightLevel(state -> 13)
                            .noOcclusion().requiresCorrectToolForDrops()));
    public static final DeferredBlock<me.moonscenty.alchemia.block.FurnaceShellBlock> FURNACE_SHELL =
            registerBlockOnly("furnace_shell", () -> new me.moonscenty.alchemia.block.FurnaceShellBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.NETHER).strength(2.5F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE).noOcclusion()
                            .requiresCorrectToolForDrops()));

    public static final DeferredBlock<me.moonscenty.alchemia.block.MirrorBlock> MIRROR = register("mirror",
            () -> new me.moonscenty.alchemia.block.MirrorBlock(mirror(), false),
            me.moonscenty.alchemia.item.MirrorBlockItem::new);
    public static final DeferredBlock<me.moonscenty.alchemia.block.MirrorBlock> MIRROR_ESSENTIA =
            register("mirror_essentia", () -> new me.moonscenty.alchemia.block.MirrorBlock(mirror(), true),
                    me.moonscenty.alchemia.item.MirrorBlockItem::new);

    public static final DeferredBlock<me.moonscenty.alchemia.block.ArcaneBoreBaseBlock> ARCANE_BORE_BASE =
            register("arcane_bore_base", () -> new me.moonscenty.alchemia.block.ArcaneBoreBaseBlock(device()));
    public static final DeferredBlock<me.moonscenty.alchemia.block.ArcaneBoreBlock> ARCANE_BORE =
            register("arcane_bore", () -> new me.moonscenty.alchemia.block.ArcaneBoreBlock(device()));

    public static final DeferredBlock<me.moonscenty.alchemia.block.CentrifugeBlock> CENTRIFUGE =
            register("centrifuge", () -> new me.moonscenty.alchemia.block.CentrifugeBlock(device()));
    public static final DeferredBlock<me.moonscenty.alchemia.block.CrystallizerBlock> CRYSTALLIZER =
            register("crystallizer", () -> new me.moonscenty.alchemia.block.CrystallizerBlock(device()));
    public static final DeferredBlock<me.moonscenty.alchemia.block.ThaumatoriumBlock> THAUMATORIUM =
            registerBlockOnly("thaumatorium", () -> new me.moonscenty.alchemia.block.ThaumatoriumBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).strength(2.0F, 12.0F).noOcclusion()));
    public static final DeferredBlock<me.moonscenty.alchemia.block.MnemonicMatrixBlock> MNEMONIC_MATRIX =
            register("mnemonic_matrix", () -> new me.moonscenty.alchemia.block.MnemonicMatrixBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).strength(1.0F, 10.0F).noOcclusion()));
    public static final DeferredBlock<me.moonscenty.alchemia.block.SmelterAuxBlock> SMELTER_AUX =
            register("smelter_aux", () -> new me.moonscenty.alchemia.block.SmelterAuxBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).strength(1.0F, 10.0F).noOcclusion()));
    public static final DeferredBlock<me.moonscenty.alchemia.block.SmelterVentBlock> SMELTER_VENT =
            register("smelter_vent", () -> new me.moonscenty.alchemia.block.SmelterVentBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).strength(1.0F, 10.0F).noOcclusion()));

    public static final DeferredBlock<AlembicBlock> ALEMBIC = register("alembic",
            () -> new AlembicBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS)
                    .mapColor(MapColor.STONE)
                    .noOcclusion()));

    public static final DeferredBlock<me.moonscenty.alchemia.block.BrainJarBlock> BRAIN_JAR = register("brain_jar",
            () -> new me.moonscenty.alchemia.block.BrainJarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)
                    .mapColor(MapColor.NONE)
                    .noOcclusion()),
            me.moonscenty.alchemia.item.BrainJarItem::new);

    public static final DeferredBlock<JarBlock> JAR = register("jar",
            () -> new JarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)
                    .mapColor(MapColor.NONE)
                    .noOcclusion()),
            JarBlockItem::new);

    public static final DeferredBlock<me.moonscenty.alchemia.block.VoidJarBlock> VOID_JAR = register("void_jar",
            () -> new me.moonscenty.alchemia.block.VoidJarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)
                    .mapColor(MapColor.NONE)
                    .noOcclusion()),
            JarBlockItem::new);

    public static final DeferredBlock<TubeBlock> TUBE = register("tube", () -> new TubeBlock(tubing()));

    public static final DeferredBlock<ValveTubeBlock> TUBE_VALVE =
            register("tube_valve", () -> new ValveTubeBlock(tubing()));

    public static final DeferredBlock<OnewayTubeBlock> TUBE_ONEWAY =
            register("tube_oneway", () -> new OnewayTubeBlock(tubing()));

    public static final DeferredBlock<RestrictTubeBlock> TUBE_RESTRICT =
            register("tube_restrict", () -> new RestrictTubeBlock(tubing()));

    public static final DeferredBlock<FilterTubeBlock> TUBE_FILTER =
            register("tube_filter", () -> new FilterTubeBlock(tubing()));

    public static final DeferredBlock<BufferTubeBlock> TUBE_BUFFER =
            register("tube_buffer", () -> new BufferTubeBlock(tubing()));

    // --- infusion altar ---

    public static final DeferredBlock<ArcanePedestalBlock> ARCANE_PEDESTAL = register("arcane_pedestal",
            () -> new ArcanePedestalBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS)
                    .mapColor(MapColor.STONE)
                    .noOcclusion()));

    public static final DeferredBlock<InfusionMatrixBlock> INFUSION_MATRIX = register("infusion_matrix",
            () -> new InfusionMatrixBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS)
                    .mapColor(MapColor.STONE)
                    .lightLevel(state -> 7)
                    .noOcclusion()));

    public static final DeferredBlock<ArcanePillarBlock> ARCANE_PILLAR = registerBlockOnly("arcane_pillar",
            () -> new ArcanePillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS)
                    .mapColor(MapColor.STONE)
                    .noOcclusion()));

    public static final DeferredBlock<me.moonscenty.alchemia.block.FocalManipulatorBlock> FOCAL_MANIPULATOR =
            register("focal_manipulator",
                    () -> new me.moonscenty.alchemia.block.FocalManipulatorBlock(
                            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS)
                                    .mapColor(MapColor.STONE)
                                    .noOcclusion()));

    // --- small devices ---

    public static final DeferredBlock<me.moonscenty.alchemia.block.LampBlock> ARCANE_LAMP = register("arcane_lamp",
            () -> lamp(me.moonscenty.alchemia.block.LampBlock.Kind.ARCANE));
    public static final DeferredBlock<me.moonscenty.alchemia.block.LampBlock> GROWTH_LAMP = register("growth_lamp",
            () -> lamp(me.moonscenty.alchemia.block.LampBlock.Kind.GROWTH));
    public static final DeferredBlock<me.moonscenty.alchemia.block.LampBlock> FERTILITY_LAMP =
            register("fertility_lamp", () -> lamp(me.moonscenty.alchemia.block.LampBlock.Kind.FERTILITY));

    public static final DeferredBlock<me.moonscenty.alchemia.block.LampLightBlock> LAMP_LIGHT =
            registerBlockOnly("lamp_light", () -> new me.moonscenty.alchemia.block.LampLightBlock(
                    BlockBehaviour.Properties.of()
                            .replaceable()
                            .noCollission()
                            .noLootTable()
                            .lightLevel(state -> 15)
                            .instabreak()
                            .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)));

    public static final DeferredBlock<me.moonscenty.alchemia.block.HungryChestBlock> HUNGRY_CHEST =
            register("hungry_chest", () -> new me.moonscenty.alchemia.block.HungryChestBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.CHEST).mapColor(MapColor.WOOD)));

    public static final DeferredBlock<me.moonscenty.alchemia.block.WaterJugBlock> WATER_JUG = register("water_jug",
            () -> new me.moonscenty.alchemia.block.WaterJugBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.TERRACOTTA).mapColor(MapColor.TERRACOTTA_ORANGE)
                            .noOcclusion()));

    public static final DeferredBlock<me.moonscenty.alchemia.block.LevitatorBlock> LEVITATOR = register("levitator",
            () -> new me.moonscenty.alchemia.block.LevitatorBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)
                    .mapColor(MapColor.WOOD)
                    .noOcclusion()));

    public static final DeferredBlock<me.moonscenty.alchemia.block.RedstoneRelayBlock> REDSTONE_RELAY =
            register("redstone_relay", () -> new me.moonscenty.alchemia.block.RedstoneRelayBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.REPEATER)));

    public static final DeferredBlock<me.moonscenty.alchemia.block.BarrierStoneBlock> BARRIER_STONE = register("barrier_stone",
            () -> new me.moonscenty.alchemia.block.BarrierStoneBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .sound(net.minecraft.world.level.block.SoundType.STONE)
                    .strength(2.5F)));
    public static final DeferredBlock<me.moonscenty.alchemia.block.TravelStoneBlock> TRAVEL_STONE = register("travel_stone",
            () -> new me.moonscenty.alchemia.block.TravelStoneBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .sound(net.minecraft.world.level.block.SoundType.STONE)
                    .strength(2.5F)));
    public static final DeferredBlock<me.moonscenty.alchemia.block.WardingAuraBlock> WARDING_AURA = registerBlockOnly("warding_aura",
            () -> new me.moonscenty.alchemia.block.WardingAuraBlock(BlockBehaviour.Properties.of().mapColor(MapColor.NONE)
                    .noOcclusion().dynamicShape().noLootTable().replaceable().strength(0.0F)
                    .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
                    .isValidSpawn(Blocks::never)));

    public static final DeferredBlock<me.moonscenty.alchemia.block.ArcaneEarBlock> ARCANE_EAR = register("arcane_ear",
            () -> new me.moonscenty.alchemia.block.ArcaneEarBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .sound(net.minecraft.world.level.block.SoundType.WOOD)
                    .strength(1.0F)
                    .noOcclusion()
                    .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)));

    public static final DeferredBlock<me.moonscenty.alchemia.block.SpaBlock> SPA = register("spa",
            () -> new me.moonscenty.alchemia.block.SpaBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS)
                    .mapColor(MapColor.QUARTZ)));

    public static final DeferredBlock<me.moonscenty.alchemia.block.PurifyingFluidBlock> PURIFYING_FLUID =
            registerBlockOnly("purifying_fluid", () -> new me.moonscenty.alchemia.block.PurifyingFluidBlock(
                    ModFluids.PURIFYING.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER)
                            .mapColor(MapColor.COLOR_LIGHT_BLUE)
                            .lightLevel(state -> 5)
                            .noLootTable()));

    // --- aura devices ---

    public static final DeferredBlock<me.moonscenty.alchemia.block.RechargePedestalBlock> RECHARGE_PEDESTAL =
            register("recharge_pedestal", () -> new me.moonscenty.alchemia.block.RechargePedestalBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS).mapColor(MapColor.STONE)
                            .noOcclusion()));

    public static final DeferredBlock<me.moonscenty.alchemia.block.DioptraBlock> DIOPTRA = register("dioptra",
            () -> new me.moonscenty.alchemia.block.DioptraBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS)
                    .mapColor(MapColor.COLOR_BLACK)
                    .noOcclusion()));

    public static final DeferredBlock<me.moonscenty.alchemia.block.AuraTotemBlock> AURA_TOTEM_PUSH =
            register("aura_totem_push", () -> totem(me.moonscenty.alchemia.block.AuraTotemBlock.Kind.PUSH));
    public static final DeferredBlock<me.moonscenty.alchemia.block.AuraTotemBlock> AURA_TOTEM_PULL =
            register("aura_totem_pull", () -> totem(me.moonscenty.alchemia.block.AuraTotemBlock.Kind.PULL));
    public static final DeferredBlock<me.moonscenty.alchemia.block.AuraTotemBlock> AURA_TOTEM_POLE_INNER =
            register("aura_totem_pole_inner",
                    () -> totem(me.moonscenty.alchemia.block.AuraTotemBlock.Kind.POLE_INNER));
    public static final DeferredBlock<me.moonscenty.alchemia.block.AuraTotemBlock> AURA_TOTEM_POLE_OUTER =
            register("aura_totem_pole_outer",
                    () -> totem(me.moonscenty.alchemia.block.AuraTotemBlock.Kind.POLE_OUTER));
    public static final DeferredBlock<me.moonscenty.alchemia.block.AuraTotemBlock> AURA_TOTEM_POLE_PURE =
            register("aura_totem_pole_pure",
                    () -> totem(me.moonscenty.alchemia.block.AuraTotemBlock.Kind.POLE_PURE));

    private static BlockBehaviour.Properties smelter(MapColor colour) {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.FURNACE)
                .mapColor(colour)
                .lightLevel(state -> state.getValue(EssentiaSmelterBlock.LIT) ? 13 : 0);
    }

    private static BlockBehaviour.Properties mirror() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(0.1F, 12.0F)
                .sound(net.minecraft.world.level.block.SoundType.GLASS).noOcclusion().noCollission();
    }

    private static BlockBehaviour.Properties device() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .sound(net.minecraft.world.level.block.SoundType.WOOD)
                .strength(2.0F, 12.0F)
                .noOcclusion();
    }

    private static me.moonscenty.alchemia.block.AuraTotemBlock totem(
            me.moonscenty.alchemia.block.AuraTotemBlock.Kind kind) {
        return new me.moonscenty.alchemia.block.AuraTotemBlock(kind, BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .sound(net.minecraft.world.level.block.SoundType.WOOD)
                .strength(0.5F, 5.0F)
                .noOcclusion());
    }

    private static me.moonscenty.alchemia.block.LampBlock lamp(me.moonscenty.alchemia.block.LampBlock.Kind kind) {
        return new me.moonscenty.alchemia.block.LampBlock(kind, BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .sound(net.minecraft.world.level.block.SoundType.METAL)
                .strength(1.0F)
                .noOcclusion()
                .lightLevel(state -> state.getValue(me.moonscenty.alchemia.block.LampBlock.ENABLED) ? 15 : 0));
    }

    public static final DeferredBlock<me.moonscenty.alchemia.block.BellowsBlock> BELLOWS = register("bellows",
            () -> new me.moonscenty.alchemia.block.BellowsBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)
                    .mapColor(MapColor.WOOD)
                    .strength(1.0F)
                    .noOcclusion()));

    public static final DeferredBlock<ArcaneWorkbenchChargerBlock> ARCANE_WORKBENCH_CHARGER =
            register("arcane_workbench_charger",
                    () -> new ArcaneWorkbenchChargerBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)
                            .mapColor(MapColor.COLOR_BROWN)
                            .lightLevel(state -> 6)
                            .noOcclusion()));

    public static final DeferredBlock<NodeStabilizerBlock> NODE_STABILIZER = register("node_stabilizer",
            () -> new NodeStabilizerBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS)
                    .mapColor(MapColor.STONE)
                    .lightLevel(state -> 5)
                    .noOcclusion()));

    // --- taint ---

    public static final DeferredBlock<TaintFibreBlock> TAINT_FIBRE = register("taint_fibre",
            () -> new TaintFibreBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(1.0F)
                    .sound(SoundType.SLIME_BLOCK)
                    .lightLevel(TaintFibreBlock::lightOf)
                    .noCollission()
                    .noOcclusion()
                    .replaceable()
                    .randomTicks()
                    .pushReaction(PushReaction.DESTROY)));

    public static final DeferredBlock<TaintGroundBlock> TAINT_SOIL = registerTaintGround("taint_soil", TaintGroundBlock.Kind.SOIL);
    public static final DeferredBlock<TaintGroundBlock> TAINT_CRUST = registerTaintGround("taint_crust", TaintGroundBlock.Kind.CRUST);
    public static final DeferredBlock<TaintGroundBlock> TAINT_ROCK = registerTaintGround("taint_rock", TaintGroundBlock.Kind.ROCK);
    public static final DeferredBlock<TaintGroundBlock> TAINT_GEYSER = registerTaintGround("taint_geyser",
            TaintGroundBlock.Kind.GEYSER);

    public static final DeferredBlock<FluxGooBlock> FLUX_GOO = register("flux_goo",
            () -> new FluxGooBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(0.5F)
                    .sound(SoundType.SLIME_BLOCK)
                    .noOcclusion()
                    .noCollission()
                    .replaceable()
                    .randomTicks()
                    .pushReaction(PushReaction.DESTROY)));

    public static final DeferredBlock<TaintLogBlock> TAINT_LOG = register("taint_log",
            () -> new TaintLogBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(3.0F)
                    .sound(SoundType.SLIME_BLOCK)
                    .randomTicks()));

    public static final Map<CrystalType, DeferredBlock<CrystalBlock>> CRYSTALS = registerCrystals();

    public static final Map<DyeColor, DeferredBlock<NitorBlock>> NITOR = registerNitor();

    public static final DeferredBlock<HoleBlock> HOLE = registerBlockOnly("hole",
            () -> new HoleBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE)
                    .strength(-1.0F, 3600000.0F)
                    .noCollission()
                    .noOcclusion()
                    .noLootTable()
                    .air()
                    .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)));

    private static final float METAL_HARDNESS = 4.0F;
    private static final float METAL_RESISTANCE = 6.0F;

    public static final DeferredBlock<Block> ALCHEMIUM_BLOCK = register("alchemium_block",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).mapColor(MapColor.COLOR_PURPLE)
                    .strength(METAL_HARDNESS, METAL_RESISTANCE)));
    public static final DeferredBlock<Block> BRASS_BLOCK = register("brass_block",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).mapColor(MapColor.GOLD)
                    .strength(METAL_HARDNESS, METAL_RESISTANCE)));
    public static final DeferredBlock<Block> VOID_METAL_BLOCK = register("void_metal_block",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).mapColor(MapColor.COLOR_BLACK)
                    .strength(METAL_HARDNESS, METAL_RESISTANCE)));
    public static final DeferredBlock<Block> ALCHEMICAL_CONSTRUCT = register("alchemical_construct",
            () -> new Block(construct()));
    public static final DeferredBlock<Block> ADVANCED_ALCHEMICAL_CONSTRUCT =
            register("advanced_alchemical_construct", () -> new Block(construct()));

    private static BlockBehaviour.Properties construct() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(METAL_HARDNESS, METAL_RESISTANCE).sound(SoundType.METAL)
                .requiresCorrectToolForDrops();
    }

    public static final DeferredBlock<me.moonscenty.alchemia.block.AmbientGrassBlock> AMBIENT_GRASS =
            register("ambient_grass", () -> new me.moonscenty.alchemia.block.AmbientGrassBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK)));
    public static final DeferredBlock<me.moonscenty.alchemia.block.WoodenTableBlock> WOODEN_TABLE = register("wooden_table",
            () -> new me.moonscenty.alchemia.block.WoodenTableBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD).sound(net.minecraft.world.level.block.SoundType.WOOD)
                    .strength(2.0F).noOcclusion().ignitedByLava()));
    public static final DeferredBlock<Block> STONE_TABLE = register("stone_table",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(2.5F, 6.0F).noOcclusion()));
    public static final DeferredBlock<me.moonscenty.alchemia.block.GolemPressBlock> GOLEM_PRESS =
            registerBlockOnly("golem_press", () -> new me.moonscenty.alchemia.block.GolemPressBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(2.5F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE).noOcclusion()));
    public static final DeferredBlock<me.moonscenty.alchemia.block.GolemPressPartBlock> GOLEM_PRESS_PART =
            registerBlockOnly("golem_press_part", () -> new me.moonscenty.alchemia.block.GolemPressPartBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(2.5F)
                            .sound(net.minecraft.world.level.block.SoundType.STONE).noOcclusion()));

    public static final WoodSet GREATWOOD = registerWood("greatwood", Blocks.DARK_OAK_LOG, MapColor.COLOR_BROWN, 0, ModWorldgen.GREATWOOD_TREE);
    public static final WoodSet SILVERWOOD = registerWood("silverwood", Blocks.BIRCH_LOG, MapColor.SAND, 6, ModWorldgen.SILVERWOOD_TREE);
    public static final List<WoodSet> WOODS = List.of(GREATWOOD, SILVERWOOD);

    public static final StoneSet ARCANE_STONE = registerStoneSet("arcane_stone", MapColor.COLOR_BLACK);
    public static final StoneSet ARCANE_STONE_BRICKS = registerStoneSet("arcane_stone_bricks", MapColor.COLOR_BLACK);
    public static final StoneSet ANCIENT_STONE = registerStoneSet("ancient_stone", MapColor.COLOR_GRAY);
    public static final StoneSet ELDRITCH_STONE = registerStoneSet("eldritch_stone",
            () -> stone(MapColor.COLOR_BLACK).strength(15.0F, 1000.0F));
    public static final List<StoneSet> STONE_SETS = List.of(ARCANE_STONE, ARCANE_STONE_BRICKS, ANCIENT_STONE,
            ELDRITCH_STONE);

    public static final DeferredBlock<Block> ANCIENT_ROCK = register("ancient_rock",
            () -> new Block(stone(MapColor.COLOR_GRAY).strength(6.0F, 20.0F).isValidSpawn(ModBlocks::never)));
    public static final DeferredBlock<Block> ANCIENT_TILE = register("ancient_tile",
            () -> new Block(stone(MapColor.COLOR_GRAY).isValidSpawn(ModBlocks::never)));
    public static final DeferredBlock<Block> GLYPHED_STONE = register("glyphed_stone",
            () -> new Block(stone(MapColor.COLOR_GRAY).isValidSpawn(ModBlocks::never)));
    public static final DeferredBlock<Block> ANCIENT_DOORWAY = register("ancient_doorway",
            () -> new Block(stone(MapColor.COLOR_GRAY).strength(-1.0F, 6.0F).noLootTable()
                    .isValidSpawn(ModBlocks::never)));
    public static final DeferredBlock<Block> CRUSTED_STONE = register("crusted_stone",
            () -> new Block(stone(MapColor.COLOR_PURPLE).strength(1.0F, 5.0F)));
    public static final DeferredBlock<Block> GLOWING_CRUSTED_STONE = register("glowing_crusted_stone",
            () -> new Block(stone(MapColor.COLOR_PURPLE).strength(1.0F, 5.0F).lightLevel(state -> 9)));
    public static final DeferredBlock<me.moonscenty.alchemia.block.PorousStoneBlock> POROUS_STONE = register(
            "porous_stone", () -> new me.moonscenty.alchemia.block.PorousStoneBlock(
                    stone(MapColor.STONE).strength(0.5F, 5.0F)));
    public static final DeferredBlock<me.moonscenty.alchemia.block.LootBlock> LOOT_URN = register("loot_urn",
            () -> new me.moonscenty.alchemia.block.LootBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_ORANGE)
                    .strength(0.15F, 0.0F).noOcclusion().sound(new net.neoforged.neoforge.common.util.DeferredSoundType(
                            1.0F, 1.0F, me.moonscenty.alchemia.registry.ModSounds.URN_BREAK,
                            () -> net.minecraft.sounds.SoundEvents.STONE_STEP,
                            () -> net.minecraft.sounds.SoundEvents.STONE_PLACE,
                            () -> net.minecraft.sounds.SoundEvents.STONE_HIT,
                            () -> net.minecraft.sounds.SoundEvents.STONE_FALL)), true));
    public static final DeferredBlock<me.moonscenty.alchemia.block.LootBlock> LOOT_CRATE = register("loot_crate",
            () -> new me.moonscenty.alchemia.block.LootBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD)
                    .strength(0.15F, 0.0F).noOcclusion().sound(SoundType.WOOD).ignitedByLava(), false));

    public static final DeferredBlock<ArcanePedestalBlock> ANCIENT_PEDESTAL = register("ancient_pedestal",
            () -> new ArcanePedestalBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS)
                    .mapColor(MapColor.COLOR_GRAY).noOcclusion(), me.moonscenty.alchemia.block.PillarKind.ANCIENT));
    public static final DeferredBlock<ArcanePedestalBlock> ELDRITCH_PEDESTAL = register("eldritch_pedestal",
            () -> new ArcanePedestalBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS)
                    .mapColor(MapColor.COLOR_BLACK).noOcclusion(), me.moonscenty.alchemia.block.PillarKind.ELDRITCH));

    public static final DeferredBlock<me.moonscenty.alchemia.block.eldritch.EldritchStoneworkBlock> ELDRITCH_ALTAR = registerBlockOnly("eldritch_altar",
            () -> new me.moonscenty.alchemia.block.eldritch.EldritchStoneworkBlock(eldritch(), me.moonscenty.alchemia.block.eldritch.EldritchStoneworkBlock.Part.ALTAR));
    public static final DeferredBlock<me.moonscenty.alchemia.block.eldritch.EldritchStoneworkBlock> ELDRITCH_CAP = registerBlockOnly("eldritch_cap",
            () -> new me.moonscenty.alchemia.block.eldritch.EldritchStoneworkBlock(eldritch(), me.moonscenty.alchemia.block.eldritch.EldritchStoneworkBlock.Part.CAP));
    public static final DeferredBlock<me.moonscenty.alchemia.block.eldritch.EldritchStoneworkBlock> ELDRITCH_OBELISK = registerBlockOnly(
            "eldritch_obelisk", () -> new me.moonscenty.alchemia.block.eldritch.EldritchStoneworkBlock(eldritch(),
                    me.moonscenty.alchemia.block.eldritch.EldritchStoneworkBlock.Part.OBELISK));
    public static final DeferredBlock<me.moonscenty.alchemia.block.eldritch.EldritchStoneworkBlock> ELDRITCH_OBELISK_BODY = registerBlockOnly(
            "eldritch_obelisk_body", () -> new me.moonscenty.alchemia.block.eldritch.EldritchStoneworkBlock(eldritch(),
                    me.moonscenty.alchemia.block.eldritch.EldritchStoneworkBlock.Part.OBELISK_BODY));
    public static final DeferredBlock<me.moonscenty.alchemia.block.eldritch.EldritchPortalBlock> ELDRITCH_PORTAL = registerBlockOnly("eldritch_portal",
            () -> new me.moonscenty.alchemia.block.eldritch.EldritchPortalBlock(eldritch().strength(-1.0F, 3600000.0F).noCollission()));
    public static final DeferredBlock<me.moonscenty.alchemia.block.eldritch.AncientBedrockBlock> ANCIENT_BEDROCK = register("ancient_bedrock",
            () -> new me.moonscenty.alchemia.block.eldritch.AncientBedrockBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK)
                    .strength(-1.0F, 3600000.0F).noLootTable().isValidSpawn(ModBlocks::never)
                    .sound(SoundType.STONE)));
    public static final DeferredBlock<me.moonscenty.alchemia.block.eldritch.VacuumBlock> VACUUM = registerBlockOnly("vacuum",
            () -> new me.moonscenty.alchemia.block.eldritch.VacuumBlock(BlockBehaviour.Properties.of().mapColor(MapColor.NONE).noCollission()
                    .strength(-1.0F, 999.0F).noLootTable().randomTicks().replaceable().air()
                    .pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK)));
    public static final DeferredBlock<me.moonscenty.alchemia.block.eldritch.EldritchLockBlock> ELDRITCH_LOCK = registerBlockOnly("eldritch_lock",
            () -> new me.moonscenty.alchemia.block.eldritch.EldritchLockBlock(eldritch().strength(-1.0F, 3600000.0F)));
    public static final DeferredBlock<Block> ELDRITCH_DOOR = registerBlockOnly("eldritch_door",
            () -> new Block(eldritch().strength(-1.0F, 3600000.0F)) {
                @Override
                protected net.minecraft.world.level.block.RenderShape getRenderShape(
                        net.minecraft.world.level.block.state.BlockState state) {
                    return net.minecraft.world.level.block.RenderShape.INVISIBLE;
                }
            });
    public static final DeferredBlock<me.moonscenty.alchemia.block.eldritch.SappingBlock> SAPPING = registerBlockOnly("sapping",
            () -> new me.moonscenty.alchemia.block.eldritch.SappingBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE).noCollission().replaceable().strength(-1.0F, 999.0F).noLootTable()
                    .lightLevel(state -> 7).randomTicks().air()));
    public static final DeferredBlock<me.moonscenty.alchemia.block.eldritch.CrabSpawnerBlock> CRAB_SPAWNER = registerBlockOnly("crab_spawner",
            () -> new me.moonscenty.alchemia.block.eldritch.CrabSpawnerBlock(eldritch().strength(25.0F, 1200.0F)));
    public static final DeferredBlock<me.moonscenty.alchemia.block.eldritch.LiquidDeathBlock> LIQUID_DEATH = registerBlockOnly("liquid_death",
            () -> new me.moonscenty.alchemia.block.eldritch.LiquidDeathBlock(ModFluids.DEATH.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.LAVA)
                    .mapColor(MapColor.COLOR_PURPLE).lightLevel(state -> 4)));
    public static final DeferredBlock<me.moonscenty.alchemia.block.BannerBlock> BANNER = registerBlockOnly("banner",
            () -> new me.moonscenty.alchemia.block.BannerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD).strength(1.0F).sound(SoundType.WOOD).noCollission().noOcclusion()
                    .ignitedByLava()));

    public static final DeferredBlock<me.moonscenty.alchemia.block.TransfuserBlock> ESSENTIA_INPUT =
            register("essentia_input", () -> new me.moonscenty.alchemia.block.TransfuserBlock(true, transfuser()));
    public static final DeferredBlock<me.moonscenty.alchemia.block.TransfuserBlock> ESSENTIA_OUTPUT =
            register("essentia_output", () -> new me.moonscenty.alchemia.block.TransfuserBlock(false, transfuser()));

    private static BlockBehaviour.Properties transfuser() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1.0F, 10.0F).sound(SoundType.METAL)
                .noOcclusion();
    }

    public static final DeferredBlock<me.moonscenty.alchemia.block.taint.TaintDustBlock> TAINT_DUST =
            register("taint_dust", () -> new me.moonscenty.alchemia.block.taint.TaintDustBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(0.3F)
                            .sound(SoundType.SAND).replaceable().randomTicks().noLootTable().noOcclusion()));

    public static final DeferredBlock<Block> FLESH_BLOCK = register("flesh_block",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(0.25F, 2.0F)
                    .sound(new net.neoforged.neoforge.common.util.DeferredSoundType(0.5F, 0.8F, ModSounds.GORE,
                            ModSounds.GORE, ModSounds.GORE, ModSounds.GORE, ModSounds.GORE))));

    public static final Map<net.minecraft.world.item.DyeColor, DeferredBlock<me.moonscenty.alchemia.block.TallowCandleBlock>>
            TALLOW_CANDLES = registerCandles();

    public static final DeferredBlock<me.moonscenty.alchemia.block.PatternCrafterBlock> PATTERN_CRAFTER =
            register("pattern_crafter", () -> new me.moonscenty.alchemia.block.PatternCrafterBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0F, 20.0F)
                            .sound(SoundType.METAL).noOcclusion()));

    private static Map<net.minecraft.world.item.DyeColor, DeferredBlock<me.moonscenty.alchemia.block.TallowCandleBlock>> registerCandles() {
        Map<net.minecraft.world.item.DyeColor, DeferredBlock<me.moonscenty.alchemia.block.TallowCandleBlock>> made =
                new EnumMap<>(net.minecraft.world.item.DyeColor.class);
        for (net.minecraft.world.item.DyeColor colour : net.minecraft.world.item.DyeColor.values()) {
            made.put(colour, register(colour.getSerializedName() + "_tallow_candle",
                    () -> new me.moonscenty.alchemia.block.TallowCandleBlock(colour, BlockBehaviour.Properties.of()
                            .mapColor(colour.getMapColor()).strength(0.1F).sound(SoundType.WOOL).noCollission()
                            .noOcclusion().lightLevel(state -> 14).pushReaction(
                                    net.minecraft.world.level.material.PushReaction.DESTROY))));
        }
        return Collections.unmodifiableMap(made);
    }

    private static BlockBehaviour.Properties eldritch() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(50.0F, 1200.0F)
                .sound(SoundType.STONE).lightLevel(state -> 8).noOcclusion().noLootTable()
                .isValidSpawn(ModBlocks::never);
    }

    private static boolean never(net.minecraft.world.level.block.state.BlockState state,
            net.minecraft.world.level.BlockGetter level, net.minecraft.core.BlockPos pos,
            net.minecraft.world.entity.EntityType<?> type) {
        return false;
    }

    public static final DeferredBlock<Block> INFUSION_SPEED_STONE =
            register("infusion_speed_stone", () -> new Block(stone(MapColor.COLOR_BLACK)));
    public static final DeferredBlock<Block> INFUSION_COST_STONE =
            register("infusion_cost_stone", () -> new Block(stone(MapColor.COLOR_BLACK)));

    public static final DeferredBlock<HalfTransparentBlock> AMBER_BLOCK = register("amber_block", () -> new HalfTransparentBlock(amber()));
    public static final DeferredBlock<HalfTransparentBlock> AMBER_BRICKS = register("amber_bricks", () -> new HalfTransparentBlock(amber()));

    private static StoneSet registerStoneSet(String name, MapColor color) {
        return registerStoneSet(name, () -> stone(color));
    }

    private static StoneSet registerStoneSet(String name,
            java.util.function.Supplier<BlockBehaviour.Properties> properties) {
        DeferredBlock<Block> base = register(name, () -> new Block(properties.get()));
        return new StoneSet(base,
                register(name + "_stairs", () -> new StairBlock(base.get().defaultBlockState(), properties.get())),
                register(name + "_slab", () -> new SlabBlock(properties.get())));
    }

    private static BlockBehaviour.Properties stone(MapColor color) {
        return BlockBehaviour.Properties.of()
                .mapColor(color)
                .instrument(NoteBlockInstrument.BASEDRUM)
                .requiresCorrectToolForDrops()
                .strength(2.0F, 10.0F)
                .sound(SoundType.STONE);
    }

    private static BlockBehaviour.Properties amber() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_ORANGE)
                .strength(0.5F)
                .sound(SoundType.STONE)
                .noOcclusion()
                .isValidSpawn(Blocks::never)
                .isRedstoneConductor((state, level, pos) -> false)
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false);
    }

    public static final DeferredBlock<ShimmerleafBlock> SHIMMERLEAF = register("shimmerleaf", () -> new ShimmerleafBlock(plant(6)));
    public static final DeferredBlock<CinderpearlBlock> CINDERPEARL = register("cinderpearl", () -> new CinderpearlBlock(plant(7)));
    public static final DeferredBlock<VishroomBlock> VISHROOM = register("vishroom", () -> new VishroomBlock(plant(6)));
    public static final List<DeferredBlock<? extends Block>> PLANTS = List.of(SHIMMERLEAF, CINDERPEARL, VISHROOM);
    public static final DeferredBlock<me.moonscenty.alchemia.block.EtherealBloomBlock> ETHEREAL_BLOOM = register("ethereal_bloom",
            () -> new me.moonscenty.alchemia.block.EtherealBloomBlock(plant(12)));

    private static BlockBehaviour.Properties plant(int light) {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.POPPY).lightLevel(state -> light);
    }

    private static WoodSet registerWood(String name, Block logTemplate, MapColor plankColor, int logLight, ResourceKey<ConfiguredFeature<?, ?>> tree) {
        TreeGrower grower = new TreeGrower(Alchemia.MODID + ":" + name, Optional.empty(), Optional.of(tree), Optional.empty());
        DeferredBlock<Block> planks = register(name + "_planks",
                () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).mapColor(plankColor)));
        return new WoodSet(name,
                register(name + "_log", () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(logTemplate).lightLevel(state -> logLight))),
                planks,
                register(name + "_leaves", () -> new LeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DARK_OAK_LEAVES))),
                register(name + "_sapling", () -> new SaplingBlock(grower, BlockBehaviour.Properties.ofFullCopy(Blocks.DARK_OAK_SAPLING))),
                register(name + "_stairs", () -> new StairBlock(planks.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(planks.get()))),
                register(name + "_slab", () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(planks.get()))));
    }

    private static BlockBehaviour.Properties tubing() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS)
                .mapColor(MapColor.GOLD)
                .noOcclusion();
    }

    private static Map<DyeColor, DeferredBlock<NitorBlock>> registerNitor() {
        Map<DyeColor, DeferredBlock<NitorBlock>> flames = new EnumMap<>(DyeColor.class);
        for (DyeColor colour : DyeColor.values()) {
            flames.put(colour, register(name(colour), () -> new NitorBlock(BlockBehaviour.Properties.of()
                    .mapColor(colour)
                    .strength(0.1F)
                    .sound(SoundType.WOOL)
                    .lightLevel(state -> 15)
                    .noCollission()
                    .noOcclusion()
                    .instabreak()
                    .pushReaction(PushReaction.DESTROY), colour)));
        }
        // unmodifiableMap, not Map.copyOf: copyOf's iteration order varies per run and shuffles datagen output
        return Collections.unmodifiableMap(flames);
    }

    public static String name(DyeColor colour) {
        return colour == DyeColor.WHITE ? "nitor" : colour.getName() + "_nitor";
    }

    private static Map<CrystalType, DeferredBlock<CrystalBlock>> registerCrystals() {
        Map<CrystalType, DeferredBlock<CrystalBlock>> crystals = new EnumMap<>(CrystalType.class);
        for (CrystalType type : CrystalType.values()) {
            crystals.put(type, register(type.getName() + "_crystal", () -> new CrystalBlock(type, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.QUARTZ)
                    .strength(0.25F)
                    .sound(SoundType.AMETHYST_CLUSTER)
                    .lightLevel(CrystalBlock::getLightLevel)
                    .randomTicks()
                    .forceSolidOn()
                    .noOcclusion()
                    .pushReaction(PushReaction.DESTROY))));
        }
        return Collections.unmodifiableMap(crystals);
    }

    private static DeferredBlock<TaintGroundBlock> registerTaintGround(String name, TaintGroundBlock.Kind kind) {
        return register(name, () -> new TaintGroundBlock(kind, BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)
                .strength(10.0F, 100.0F)
                .sound(SoundType.SLIME_BLOCK)
                .requiresCorrectToolForDrops()
                .randomTicks()));
    }

    private static <T extends Block> DeferredBlock<T> registerBlockOnly(String name, Supplier<T> block) {
        return BLOCKS.register(name, block);
    }

    private static <T extends Block> DeferredBlock<T> register(String name, Supplier<T> block) {
        return register(name, block, BlockItem::new);
    }

    private static <T extends Block> DeferredBlock<T> register(String name, Supplier<T> block,
            BiFunction<Block, Item.Properties, BlockItem> asItem) {
        DeferredBlock<T> registered = BLOCKS.register(name, block);
        ModItems.ITEMS.register(name, () -> asItem.apply(registered.get(), new Item.Properties()));
        return registered;
    }
}
