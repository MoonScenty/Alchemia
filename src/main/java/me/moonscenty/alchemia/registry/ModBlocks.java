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
            () -> new EssentiaSmelterBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.FURNACE)
                    .mapColor(MapColor.STONE)
                    .lightLevel(state -> state.getValue(EssentiaSmelterBlock.LIT) ? 13 : 0)));

    public static final DeferredBlock<AlembicBlock> ALEMBIC = register("alembic",
            () -> new AlembicBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS)
                    .mapColor(MapColor.STONE)
                    .noOcclusion()));

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

    public static final DeferredBlock<Block> ALCHEMIUM_BLOCK = register("alchemium_block",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).mapColor(MapColor.COLOR_PURPLE)));
    public static final DeferredBlock<Block> BRASS_BLOCK = register("brass_block",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).mapColor(MapColor.GOLD)));

    // Silverwood glows faintly
    public static final WoodSet GREATWOOD = registerWood("greatwood", Blocks.DARK_OAK_LOG, MapColor.COLOR_BROWN, 0, ModWorldgen.GREATWOOD_TREE);
    public static final WoodSet SILVERWOOD = registerWood("silverwood", Blocks.BIRCH_LOG, MapColor.SAND, 6, ModWorldgen.SILVERWOOD_TREE);
    public static final List<WoodSet> WOODS = List.of(GREATWOOD, SILVERWOOD);

    public static final StoneSet ARCANE_STONE = registerStoneSet("arcane_stone", MapColor.COLOR_BLACK);
    public static final StoneSet ARCANE_STONE_BRICKS = registerStoneSet("arcane_stone_bricks", MapColor.COLOR_BLACK);
    public static final List<StoneSet> STONE_SETS = List.of(ARCANE_STONE, ARCANE_STONE_BRICKS);

    // Amber is see-through, and soft enough to break by hand
    public static final DeferredBlock<HalfTransparentBlock> AMBER_BLOCK = register("amber_block", () -> new HalfTransparentBlock(amber()));
    public static final DeferredBlock<HalfTransparentBlock> AMBER_BRICKS = register("amber_bricks", () -> new HalfTransparentBlock(amber()));

    private static StoneSet registerStoneSet(String name, MapColor color) {
        DeferredBlock<Block> base = register(name, () -> new Block(stone(color)));
        return new StoneSet(base,
                register(name + "_stairs", () -> new StairBlock(base.get().defaultBlockState(), stone(color))),
                register(name + "_slab", () -> new SlabBlock(stone(color))));
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
        return Map.copyOf(flames);
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
