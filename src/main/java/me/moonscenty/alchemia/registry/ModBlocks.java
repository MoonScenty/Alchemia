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
    /** The two better smelters: the same fire and the same fifty, less lost on the way. */
    public static final DeferredBlock<EssentiaSmelterBlock> ALCHEMIUM_SMELTER = register("alchemium_smelter",
            () -> new EssentiaSmelterBlock(smelter(MapColor.COLOR_PURPLE), EssentiaSmelterBlock.Tier.ALCHEMIUM));
    public static final DeferredBlock<EssentiaSmelterBlock> VOID_SMELTER = register("void_smelter",
            () -> new EssentiaSmelterBlock(smelter(MapColor.COLOR_BLACK), EssentiaSmelterBlock.Tier.VOID));
    /** What goes against a smelter's sides: a pump that pours one more column, and a vent that catches its flux. */
    /**
     * An infernal furnace: its molten heart, where the lava was, and the shell of what was nether brick and obsidian
     * round it. Neither has an item; both are made where they stand.
     */
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

    /** The two mirrors: one that passes items to its partner, one that passes essentia. */
    public static final DeferredBlock<me.moonscenty.alchemia.block.MirrorBlock> MIRROR = register("mirror",
            () -> new me.moonscenty.alchemia.block.MirrorBlock(mirror(), false),
            me.moonscenty.alchemia.item.MirrorBlockItem::new);
    public static final DeferredBlock<me.moonscenty.alchemia.block.MirrorBlock> MIRROR_ESSENTIA =
            register("mirror_essentia", () -> new me.moonscenty.alchemia.block.MirrorBlock(mirror(), true),
                    me.moonscenty.alchemia.item.MirrorBlockItem::new);

    /** The arcane bore and the base it stands on or hangs from. Wood, as the original's were. */
    public static final DeferredBlock<me.moonscenty.alchemia.block.ArcaneBoreBaseBlock> ARCANE_BORE_BASE =
            register("arcane_bore_base", () -> new me.moonscenty.alchemia.block.ArcaneBoreBaseBlock(device()));
    public static final DeferredBlock<me.moonscenty.alchemia.block.ArcaneBoreBlock> ARCANE_BORE =
            register("arcane_bore", () -> new me.moonscenty.alchemia.block.ArcaneBoreBlock(device()));

    /** A centrifuge that splits a compound into one of its parts, and a crystallizer that sets essentia hard. */
    public static final DeferredBlock<me.moonscenty.alchemia.block.CentrifugeBlock> CENTRIFUGE =
            register("centrifuge", () -> new me.moonscenty.alchemia.block.CentrifugeBlock(device()));
    public static final DeferredBlock<me.moonscenty.alchemia.block.CrystallizerBlock> CRYSTALLIZER =
            register("crystallizer", () -> new me.moonscenty.alchemia.block.CrystallizerBlock(device()));
    /**
     * A crucible that works on its own, made with a wand out of a crucible and two blocks of brass, and the matrix
     * that lets it keep more recipes in mind. The thaumatorium has no item: it is made where it stands.
     */
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

    /** A warded jar with a zombie's brain in it, which eats experience. */
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

    public static final DeferredBlock<TubeBlock> TUBE = register("tube", () -> new TubeBlock(tubing()));

    /** Shut by a redstone signal, or by hand. */
    public static final DeferredBlock<ValveTubeBlock> TUBE_VALVE =
            register("tube_valve", () -> new ValveTubeBlock(tubing()));

    /** Lets essentia by one way only. */
    public static final DeferredBlock<OnewayTubeBlock> TUBE_ONEWAY =
            register("tube_oneway", () -> new OnewayTubeBlock(tubing()));

    /** Narrows the way through, so a run that comes through it carries less. */
    public static final DeferredBlock<RestrictTubeBlock> TUBE_RESTRICT =
            register("tube_restrict", () -> new RestrictTubeBlock(tubing()));

    /** Lets one aspect by and turns the rest back. */
    public static final DeferredBlock<FilterTubeBlock> TUBE_FILTER =
            register("tube_filter", () -> new FilterTubeBlock(tubing()));

    /** Keeps a few points of its own, so the works has something to draw on between boilings. */
    public static final DeferredBlock<BufferTubeBlock> TUBE_BUFFER =
            register("tube_buffer", () -> new BufferTubeBlock(tubing()));

    // --- the infusion altar ---------------------------------------------------------------------------------

    /** Holds up one thing where a matrix can reach it. */
    public static final DeferredBlock<ArcanePedestalBlock> ARCANE_PEDESTAL = register("arcane_pedestal",
            () -> new ArcanePedestalBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS)
                    .mapColor(MapColor.STONE)
                    .noOcclusion()));

    /** Eight stones turning in the air, which is where everything dear is made. */
    public static final DeferredBlock<InfusionMatrixBlock> INFUSION_MATRIX = register("infusion_matrix",
            () -> new InfusionMatrixBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS)
                    .mapColor(MapColor.STONE)
                    .lightLevel(state -> 7)
                    .noOcclusion()));

    /**
     * A corner of an altar. Never placed by hand: the matrix makes one out of arcane stone when it wakes, and
     * gives the stone back when it sleeps, so there is no item and no recipe.
     */
    public static final DeferredBlock<ArcanePillarBlock> ARCANE_PILLAR = registerBlockOnly("arcane_pillar",
            () -> new ArcanePillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS)
                    .mapColor(MapColor.STONE)
                    .noOcclusion()));

    /** The bench a focus is worked on, which is the only way anything is ever screwed into one. */
    public static final DeferredBlock<me.moonscenty.alchemia.block.FocalManipulatorBlock> FOCAL_MANIPULATOR =
            register("focal_manipulator",
                    () -> new me.moonscenty.alchemia.block.FocalManipulatorBlock(
                            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS)
                                    .mapColor(MapColor.STONE)
                                    .noOcclusion()));

    // --- the small devices ---------------------------------------------------------------------------------

    /** The three lamps: one lights the dark, one hurries what grows, one brings animals together. */
    public static final DeferredBlock<me.moonscenty.alchemia.block.LampBlock> ARCANE_LAMP = register("arcane_lamp",
            () -> lamp(me.moonscenty.alchemia.block.LampBlock.Kind.ARCANE));
    public static final DeferredBlock<me.moonscenty.alchemia.block.LampBlock> GROWTH_LAMP = register("growth_lamp",
            () -> lamp(me.moonscenty.alchemia.block.LampBlock.Kind.GROWTH));
    public static final DeferredBlock<me.moonscenty.alchemia.block.LampBlock> FERTILITY_LAMP =
            register("fertility_lamp", () -> lamp(me.moonscenty.alchemia.block.LampBlock.Kind.FERTILITY));

    /** A point of light an arcane lamp has set down in the dark. Not an item; nobody places these by hand. */
    public static final DeferredBlock<me.moonscenty.alchemia.block.LampLightBlock> LAMP_LIGHT =
            registerBlockOnly("lamp_light", () -> new me.moonscenty.alchemia.block.LampLightBlock(
                    BlockBehaviour.Properties.of()
                            .replaceable()
                            .noCollission()
                            .noLootTable()
                            .lightLevel(state -> 15)
                            .instabreak()
                            .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)));

    /** A chest that eats what is dropped on it. */
    public static final DeferredBlock<me.moonscenty.alchemia.block.HungryChestBlock> HUNGRY_CHEST =
            register("hungry_chest", () -> new me.moonscenty.alchemia.block.HungryChestBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.CHEST).mapColor(MapColor.WOOD)));

    /** A clay jug that is never empty. */
    public static final DeferredBlock<me.moonscenty.alchemia.block.WaterJugBlock> WATER_JUG = register("water_jug",
            () -> new me.moonscenty.alchemia.block.WaterJugBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.TERRACOTTA).mapColor(MapColor.TERRACOTTA_ORANGE)
                            .noOcclusion()));

    /** A box of wood and brass that lifts and carries what is in front of it. */
    public static final DeferredBlock<me.moonscenty.alchemia.block.LevitatorBlock> LEVITATOR = register("levitator",
            () -> new me.moonscenty.alchemia.block.LevitatorBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)
                    .mapColor(MapColor.WOOD)
                    .noOcclusion()));

    /** A repeater with a say in how strong a signal it takes and gives. */
    public static final DeferredBlock<me.moonscenty.alchemia.block.RedstoneRelayBlock> REDSTONE_RELAY =
            register("redstone_relay", () -> new me.moonscenty.alchemia.block.RedstoneRelayBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.REPEATER)));

    /** A wooden ear that gives a signal when it hears its note played. */
    public static final DeferredBlock<me.moonscenty.alchemia.block.ArcaneEarBlock> ARCANE_EAR = register("arcane_ear",
            () -> new me.moonscenty.alchemia.block.ArcaneEarBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .sound(net.minecraft.world.level.block.SoundType.WOOD)
                    .strength(1.0F)
                    .noOcclusion()
                    .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)));

    /** A tank that mixes bath salts into water and pumps the purifying fluid it makes into a pool above it. */
    public static final DeferredBlock<me.moonscenty.alchemia.block.SpaBlock> SPA = register("spa",
            () -> new me.moonscenty.alchemia.block.SpaBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS)
                    .mapColor(MapColor.QUARTZ)));

    /** Purifying fluid standing in the world. Not an item; it comes in a bucket. */
    public static final DeferredBlock<me.moonscenty.alchemia.block.PurifyingFluidBlock> PURIFYING_FLUID =
            registerBlockOnly("purifying_fluid", () -> new me.moonscenty.alchemia.block.PurifyingFluidBlock(
                    ModFluids.PURIFYING.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER)
                            .mapColor(MapColor.COLOR_LIGHT_BLUE)
                            .lightLevel(state -> 5)
                            .noLootTable()));

    // --- the aura devices --------------------------------------------------------------------------------

    /** A pedestal that fills the wand set on it from the aura. */
    public static final DeferredBlock<me.moonscenty.alchemia.block.RechargePedestalBlock> RECHARGE_PEDESTAL =
            register("recharge_pedestal", () -> new me.moonscenty.alchemia.block.RechargePedestalBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS).mapColor(MapColor.STONE)
                            .noOcclusion()));

    /** Arcane stone with a crystal in its top that draws a map of the aura round it. */
    public static final DeferredBlock<me.moonscenty.alchemia.block.DioptraBlock> DIOPTRA = register("dioptra",
            () -> new me.moonscenty.alchemia.block.DioptraBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS)
                    .mapColor(MapColor.COLOR_BLACK)
                    .noOcclusion()));

    /** The two aura totems, which move vis between rings of chunks, and the three poles they stand on. */
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

    /** A smelter: a furnace in all but what it makes, lit when it burns. */
    private static BlockBehaviour.Properties smelter(MapColor colour) {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.FURNACE)
                .mapColor(colour)
                .lightLevel(state -> state.getValue(EssentiaSmelterBlock.LIT) ? 13 : 0);
    }

    /** A mirror: a pane of glass and iron that nothing bumps into, broken with a touch. */
    private static BlockBehaviour.Properties mirror() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(0.1F, 12.0F)
                .sound(net.minecraft.world.level.block.SoundType.GLASS).noOcclusion().noCollission();
    }

    /** Two essentia devices drawn wholly by their renderers, of wood as the original's were. */
    private static BlockBehaviour.Properties device() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .sound(net.minecraft.world.level.block.SoundType.WOOD)
                .strength(2.0F, 12.0F)
                .noOcclusion();
    }

    /** Soft wood but hard to blow apart, as the original's. */
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

    /** Bellows, blowing on a furnace or a smelter to hurry it. */
    public static final DeferredBlock<me.moonscenty.alchemia.block.BellowsBlock> BELLOWS = register("bellows",
            () -> new me.moonscenty.alchemia.block.BellowsBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)
                    .mapColor(MapColor.WOOD)
                    .strength(1.0F)
                    .noOcclusion()));

    /** Stands on top of a workbench and fills the wand left on it. */
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

    // --- taint ----------------------------------------------------------------------------------------------

    /** The creeping edge of the taint. Soft, no collision, and it can be built over. */
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
    /** A vent in tainted wood that lets up swarms. */
    public static final DeferredBlock<TaintGroundBlock> TAINT_GEYSER = registerTaintGround("taint_geyser",
            TaintGroundBlock.Kind.GEYSER);

    /** Spilt flux. A puddle in eight depths that runs downhill, levels out and dries into taint. */
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

    /** One to a dye, sharing a grey picture that each paints with its own colour. */
    public static final Map<DyeColor, DeferredBlock<NitorBlock>> NITOR = registerNitor();

    /**
     * A gap in a wall, held open by a working and remembering what it took.
     * <p>
     * Not placed and not picked up: it is made by a focus and it closes itself. There is no item and no recipe.
     */
    public static final DeferredBlock<HoleBlock> HOLE = registerBlockOnly("hole",
            () -> new HoleBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE)
                    .strength(-1.0F, 3600000.0F)
                    .noCollission()
                    .noOcclusion()
                    .noLootTable()
                    .air()
                    .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)));

    public static final DeferredBlock<Block> ALCHEMIUM_BLOCK = register("alchemium_block",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).mapColor(MapColor.COLOR_PURPLE)));
    public static final DeferredBlock<Block> BRASS_BLOCK = register("brass_block",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).mapColor(MapColor.GOLD)));

    // Silverwood glows faintly
    /** Grass in the magical forest that glimmers at night. */
    public static final DeferredBlock<me.moonscenty.alchemia.block.AmbientGrassBlock> AMBIENT_GRASS =
            register("ambient_grass", () -> new me.moonscenty.alchemia.block.AmbientGrassBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK)));
    /** A stone table: a slab of stone on four legs, and the anvil's bench in a golem press. */
    public static final DeferredBlock<Block> STONE_TABLE = register("stone_table",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).noOcclusion()));
    /**
     * A golem press: its heart, where the piston was, and the four blocks round it that stand in for the bars,
     * the table, the anvil and the cauldron. Neither has an item; both are made where they stand.
     */
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
    /** The ancient stone of the Outer Lands, and the eldritch stone of its rings and altars, which barely breaks. */
    public static final StoneSet ANCIENT_STONE = registerStoneSet("ancient_stone", MapColor.COLOR_GRAY);
    public static final StoneSet ELDRITCH_STONE = registerStoneSet("eldritch_stone",
            () -> stone(MapColor.COLOR_BLACK).strength(15.0F, 1000.0F));
    public static final List<StoneSet> STONE_SETS = List.of(ARCANE_STONE, ARCANE_STONE_BRICKS, ANCIENT_STONE,
            ELDRITCH_STONE);

    /** The rest of the Outer Lands' stone: some of it nothing will spawn on, and one door that nothing breaks. */
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
    /** What tainted rock leaves when the taint dies: soft stone with things caught in it. */
    public static final DeferredBlock<me.moonscenty.alchemia.block.PorousStoneBlock> POROUS_STONE = register(
            "porous_stone", () -> new me.moonscenty.alchemia.block.PorousStoneBlock(
                    stone(MapColor.STONE).strength(0.5F, 5.0F)));
    /** An urn and a crate of the dead, in the mound, that give up loot when broken. */
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

    /** The pedestal built of ancient stone, and of eldritch: each changes a working, as the corners do. */
    public static final DeferredBlock<ArcanePedestalBlock> ANCIENT_PEDESTAL = register("ancient_pedestal",
            () -> new ArcanePedestalBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS)
                    .mapColor(MapColor.COLOR_GRAY).noOcclusion(), me.moonscenty.alchemia.block.PillarKind.ANCIENT));
    public static final DeferredBlock<ArcanePedestalBlock> ELDRITCH_PEDESTAL = register("eldritch_pedestal",
            () -> new ArcanePedestalBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS)
                    .mapColor(MapColor.COLOR_BLACK).noOcclusion(), me.moonscenty.alchemia.block.PillarKind.ELDRITCH));

    /** The stonework of an eldritch ring: drawn by its renderers, slow to break, and leaving nothing. */
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
    /** The way to the Outer Lands: nothing breaks it, and nothing bumps into it. */
    public static final DeferredBlock<me.moonscenty.alchemia.block.eldritch.EldritchPortalBlock> ELDRITCH_PORTAL = registerBlockOnly("eldritch_portal",
            () -> new me.moonscenty.alchemia.block.eldritch.EldritchPortalBlock(eldritch().strength(-1.0F, 3600000.0F).noCollission()));
    /** What the Outer Lands are made of: unbreakable, and laid bare it gives way to the void. */
    public static final DeferredBlock<me.moonscenty.alchemia.block.eldritch.AncientBedrockBlock> ANCIENT_BEDROCK = register("ancient_bedrock",
            () -> new me.moonscenty.alchemia.block.eldritch.AncientBedrockBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK)
                    .strength(-1.0F, 3600000.0F).noLootTable().isValidSpawn(ModBlocks::never)
                    .sound(SoundType.STONE)));
    /** The deadly void: unseen, untouchable, and pulling everything toward where it pours from. */
    public static final DeferredBlock<me.moonscenty.alchemia.block.eldritch.VacuumBlock> VACUUM = registerBlockOnly("vacuum",
            () -> new me.moonscenty.alchemia.block.eldritch.VacuumBlock(BlockBehaviour.Properties.of().mapColor(MapColor.NONE).noCollission()
                    .strength(-1.0F, 999.0F).noLootTable().randomTicks().replaceable().air()
                    .pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK)));
    /** A crab vent in the crust. */
    public static final DeferredBlock<me.moonscenty.alchemia.block.eldritch.CrabSpawnerBlock> CRAB_SPAWNER = registerBlockOnly("crab_spawner",
            () -> new me.moonscenty.alchemia.block.eldritch.CrabSpawnerBlock(eldritch().strength(25.0F, 1200.0F)));
    /** Liquid death, that dissolves whatever stands in it. */
    public static final DeferredBlock<me.moonscenty.alchemia.block.eldritch.LiquidDeathBlock> LIQUID_DEATH = registerBlockOnly("liquid_death",
            () -> new me.moonscenty.alchemia.block.eldritch.LiquidDeathBlock(ModFluids.DEATH.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.LAVA)
                    .mapColor(MapColor.COLOR_PURPLE).lightLevel(state -> 4)));
    /** A banner, dyed or the Crimson Cult's. Its two items place it. */
    public static final DeferredBlock<me.moonscenty.alchemia.block.BannerBlock> BANNER = registerBlockOnly("banner",
            () -> new me.moonscenty.alchemia.block.BannerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD).strength(1.0F).sound(SoundType.WOOD).noCollission().noOcclusion()
                    .ignitedByLava()));

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

    /**
     * The two stones an altar is built on top of, laid two below its corners.
     * <p>
     * They are arcane stone with a mark cut in it and nothing else -- no block entity, no state. What they do is
     * done by the matrix, which counts them when a working starts.
     */
    public static final DeferredBlock<Block> INFUSION_SPEED_STONE =
            register("infusion_speed_stone", () -> new Block(stone(MapColor.COLOR_BLACK)));
    public static final DeferredBlock<Block> INFUSION_COST_STONE =
            register("infusion_cost_stone", () -> new Block(stone(MapColor.COLOR_BLACK)));

    // Amber is see-through, and soft enough to break by hand
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

    // Light levels match the original: the desert bloom burns brightest
    public static final DeferredBlock<ShimmerleafBlock> SHIMMERLEAF = register("shimmerleaf", () -> new ShimmerleafBlock(plant(6)));
    public static final DeferredBlock<CinderpearlBlock> CINDERPEARL = register("cinderpearl", () -> new CinderpearlBlock(plant(7)));
    public static final DeferredBlock<VishroomBlock> VISHROOM = register("vishroom", () -> new VishroomBlock(plant(6)));
    public static final List<DeferredBlock<? extends Block>> PLANTS = List.of(SHIMMERLEAF, CINDERPEARL, VISHROOM);

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

    /** Brass pipework: all six kinds of tube are the same thing to hit and the same thing to look at. */
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
        // unmodifiableMap rather than Map.copyOf: a copied map hands its entries back in an order the
        // running machine picks for itself, and everything written out of this one -- tag files above all
        // -- would come out shuffled from one run to the next
        return Collections.unmodifiableMap(flames);
    }

    /** White is simply "nitor"; the other fifteen say which dye went into them. */
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

    /** Tainted ground is tough (ten, against stone's one and a half) but gives to a pickaxe the same. */
    private static DeferredBlock<TaintGroundBlock> registerTaintGround(String name, TaintGroundBlock.Kind kind) {
        return register(name, () -> new TaintGroundBlock(kind, BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)
                .strength(10.0F, 100.0F)
                .sound(SoundType.SLIME_BLOCK)
                .requiresCorrectToolForDrops()
                .randomTicks()));
    }

    /** A block with no item to it: something the world puts down, never a player. */
    private static <T extends Block> DeferredBlock<T> registerBlockOnly(String name, Supplier<T> block) {
        return BLOCKS.register(name, block);
    }

    private static <T extends Block> DeferredBlock<T> register(String name, Supplier<T> block) {
        return register(name, block, BlockItem::new);
    }

    /** The same, for a block whose item has something of its own to say. */
    private static <T extends Block> DeferredBlock<T> register(String name, Supplier<T> block,
            BiFunction<Block, Item.Properties, BlockItem> asItem) {
        DeferredBlock<T> registered = BLOCKS.register(name, block);
        ModItems.ITEMS.register(name, () -> asItem.apply(registered.get(), new Item.Properties()));
        return registered;
    }
}
