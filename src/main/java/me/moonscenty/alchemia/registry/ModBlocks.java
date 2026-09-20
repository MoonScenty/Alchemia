package me.moonscenty.alchemia.registry;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.CrystalBlock;
import me.moonscenty.alchemia.block.CrystalType;
import me.moonscenty.alchemia.block.ShimmerleafBlock;
import me.moonscenty.alchemia.block.VishroomBlock;
import me.moonscenty.alchemia.block.CinderpearlBlock;
import me.moonscenty.alchemia.worldgen.ModWorldgen;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
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

    public static final Map<CrystalType, DeferredBlock<CrystalBlock>> CRYSTALS = registerCrystals();

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

    private static Map<CrystalType, DeferredBlock<CrystalBlock>> registerCrystals() {
        Map<CrystalType, DeferredBlock<CrystalBlock>> crystals = new EnumMap<>(CrystalType.class);
        for (CrystalType type : CrystalType.values()) {
            crystals.put(type, register(type.getName() + "_crystal", () -> new CrystalBlock(type, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.QUARTZ)
                    .strength(0.25F)
                    .sound(SoundType.AMETHYST_CLUSTER)
                    .lightLevel(CrystalBlock::getLightLevel)
                    .forceSolidOn()
                    .noOcclusion()
                    .pushReaction(PushReaction.DESTROY))));
        }
        return Collections.unmodifiableMap(crystals);
    }

    private static <T extends Block> DeferredBlock<T> register(String name, Supplier<T> block) {
        DeferredBlock<T> registered = BLOCKS.register(name, block);
        ModItems.ITEMS.register(name, () -> new BlockItem(registered.get(), new Item.Properties()));
        return registered;
    }
}
