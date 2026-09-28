package me.moonscenty.alchemia.datagen;

import java.util.List;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.CrystalBlock;
import me.moonscenty.alchemia.block.ResearchTableBlock;
import me.moonscenty.alchemia.block.taint.FluxGooBlock;
import me.moonscenty.alchemia.block.taint.TaintFibreBlock;
import me.moonscenty.alchemia.block.taint.TaintLogBlock;
import me.moonscenty.alchemia.block.AlembicBlock;
import me.moonscenty.alchemia.block.OnewayTubeBlock;
import me.moonscenty.alchemia.block.ValveTubeBlock;
import me.moonscenty.alchemia.block.EssentiaSmelterBlock;
import me.moonscenty.alchemia.block.CrucibleBlock;
import me.moonscenty.alchemia.block.JarBlock;
import me.moonscenty.alchemia.block.TubeBlock;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.StoneSet;
import me.moonscenty.alchemia.registry.WoodSet;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.MultiPartBlockStateBuilder;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Alchemia.MODID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        simpleBlockWithItem(ModBlocks.AMBER_ORE);
        simpleBlockWithItem(ModBlocks.DEEPSLATE_AMBER_ORE);
        simpleBlockWithItem(ModBlocks.CINNABAR_ORE);
        simpleBlockWithItem(ModBlocks.DEEPSLATE_CINNABAR_ORE);

        ModBlocks.CRYSTALS.values().forEach(this::crystal);

        simpleBlockWithItem(ModBlocks.ALCHEMIUM_BLOCK);
        simpleBlockWithItem(ModBlocks.BRASS_BLOCK);

        ModBlocks.WOODS.forEach(this::wood);

        ModBlocks.PLANTS.forEach(this::plant);

        researchTable();
        crucible();
        tube();
        jar();
        essentiaSmelter();
        alembic();
        arcaneWorkbench();
        arcaneWorkbenchCharger();
        nodeStabilizer();
        altar();
        taint();

        ModBlocks.STONE_SETS.forEach(this::stoneSet);
        translucentBlock(ModBlocks.AMBER_BLOCK);
        translucentBlock(ModBlocks.AMBER_BRICKS);
    }

    private void simpleBlockWithItem(DeferredBlock<?> block) {
        simpleBlockWithItem(block.get(), cubeAll(block.get()));
    }

    /**
     * The taint. Ground and bark come in three looks each, picked at random by position, so a patch is not a tiled
     * repeat; most of it is plain, with a boil here and there.
     */
    private void taint() {
        spotted(ModBlocks.TAINT_SOIL);
        spotted(ModBlocks.TAINT_CRUST);
        simpleBlockWithItem(ModBlocks.TAINT_ROCK.get(), models().cubeAll("taint_rock", modLoc("block/taint_rock")));
        taintLog();
        taintFibre();
        fluxGoo();
    }

    private static final int[] SPOT_WEIGHTS = {4, 1, 1};
    private static final String[] SPOT_SUFFIX = {"", "_1", "_2"};

    private void spotted(DeferredBlock<? extends Block> block) {
        String name = block.getId().getPath();
        ConfiguredModel[] looks = new ConfiguredModel[SPOT_SUFFIX.length];
        for (int i = 0; i < looks.length; i++) {
            looks[i] = ConfiguredModel.builder()
                    .modelFile(models().cubeAll(name + SPOT_SUFFIX[i], modLoc("block/" + name + SPOT_SUFFIX[i])))
                    .weight(SPOT_WEIGHTS[i])
                    .buildLast();
        }
        getVariantBuilder(block.get()).partialState().setModels(looks);
        simpleBlockItem(block.get(), models().getExistingFile(modLoc("block/" + name)));
    }

    private void taintLog() {
        ResourceLocation top = modLoc("block/taint_log_top");
        for (Direction.Axis axis : Direction.Axis.values()) {
            ConfiguredModel[] looks = new ConfiguredModel[SPOT_SUFFIX.length];
            for (int i = 0; i < looks.length; i++) {
                ModelFile model = models().cubeColumn("taint_log" + SPOT_SUFFIX[i], modLoc("block/taint_log" + SPOT_SUFFIX[i]), top);
                looks[i] = ConfiguredModel.builder()
                        .modelFile(model)
                        .rotationX(axis == Direction.Axis.Y ? 0 : 90)
                        .rotationY(axis == Direction.Axis.X ? 90 : 0)
                        .weight(SPOT_WEIGHTS[i])
                        .buildLast();
            }
            getVariantBuilder(ModBlocks.TAINT_LOG.get()).partialState()
                    .with(TaintLogBlock.AXIS, axis)
                    .setModels(looks);
        }
        simpleBlockItem(ModBlocks.TAINT_LOG.get(), models().getExistingFile(modLoc("block/taint_log")));
    }

    /** Goo is a slab of varying height wearing the animated ripple on every face. */
    private void fluxGoo() {
        ResourceLocation still = modLoc("block/flux_goo_still");
        getVariantBuilder(ModBlocks.FLUX_GOO.get()).forAllStates(state -> {
            int depth = state.getValue(FluxGooBlock.LEVEL);
            int height = 2 + depth * 2;
            ModelFile model = models().getBuilder("flux_goo_" + depth)
                    .parent(models().getExistingFile(mcLoc("block/block")))
                    .texture("particle", still)
                    .texture("goo", still)
                    .renderType("translucent")
                    .element().from(0, 0, 0).to(16, height, 16)
                    .allFaces((side, face) -> face.texture("#goo").cullface(side == Direction.UP ? null : side))
                    .end();
            return ConfiguredModel.builder().modelFile(model).build();
        });
        simpleBlockItem(ModBlocks.FLUX_GOO.get(), models().getExistingFile(modLoc("block/flux_goo_7")));
    }

    /** Fibres are a vine: a face for each side they cling to, and a sprout on top of that for the growths. */
    private void taintFibre() {
        ModelFile side = models().getExistingFile(modLoc("block/taint/fibre_side"));
        ModelFile up = models().getExistingFile(modLoc("block/taint/fibre_up"));
        ModelFile down = models().getExistingFile(modLoc("block/taint/fibre_down"));
        ModelFile sproutOne = models().cross("taint_growth_1", modLoc("block/taint_growth_1")).renderType("cutout");
        ModelFile sproutTwo = models().cross("taint_growth_2", modLoc("block/taint_growth_2")).renderType("cutout");

        var builder = getMultipartBuilder(ModBlocks.TAINT_FIBRE.get());
        builder.part().modelFile(side).addModel().condition(TaintFibreBlock.NORTH, true).end();
        builder.part().modelFile(side).rotationY(90).uvLock(true).addModel().condition(TaintFibreBlock.EAST, true).end();
        builder.part().modelFile(side).rotationY(180).uvLock(true).addModel().condition(TaintFibreBlock.SOUTH, true).end();
        builder.part().modelFile(side).rotationY(270).uvLock(true).addModel().condition(TaintFibreBlock.WEST, true).end();
        builder.part().modelFile(up).addModel().condition(TaintFibreBlock.UP, true).end();
        builder.part().modelFile(down).addModel().condition(TaintFibreBlock.DOWN, true).end();
        // the three floor growths share two pictures; the hanging one is the second picture upside down
        builder.part().modelFile(sproutOne).addModel().condition(TaintFibreBlock.GROWTH, 1, 3).end();
        builder.part().modelFile(sproutTwo).addModel().condition(TaintFibreBlock.GROWTH, 2).end();
        builder.part().modelFile(sproutTwo).rotationX(180).addModel().condition(TaintFibreBlock.GROWTH, TaintFibreBlock.HANGING).end();
    }

    /**
     * The pot, dry and then at each of its three depths.
     * <p>
     * Built on vanilla's cauldron models rather than drawn again: they already have the shape and, for the wet
     * ones, the liquid face with a tint on it. Only the pictures are ours, and the liquid takes its colour from
     * what is dissolved in the pot rather than from the water texture.
     */
    private void crucible() {
        ModelFile dry = crucibleModel("crucible", "block/cauldron", false);
        ModelFile[] wet = {
                crucibleModel("crucible_level1", "block/template_cauldron_level1", true),
                crucibleModel("crucible_level2", "block/template_cauldron_level2", true),
                crucibleModel("crucible_full", "block/template_cauldron_full", true),
        };
        getVariantBuilder(ModBlocks.CRUCIBLE.get()).forAllStates(state -> {
            int filled = state.getValue(CrucibleBlock.LEVEL);
            return ConfiguredModel.builder()
                    .modelFile(filled == 0 ? dry : wet[filled - 1])
                    .build();
        });
    }

    private ModelFile crucibleModel(String name, String parent, boolean wet) {
        var model = models().withExistingParent(name, mcLoc(parent))
                // what flies off when it is broken or stood on: the stone it is set into rather than its own iron
                .texture("particle", modLoc("block/arcane_stone"))
                .texture("top", modLoc("block/crucible_top"))
                .texture("side", modLoc("block/crucible_side"))
                .texture("bottom", modLoc("block/crucible_bottom"))
                .texture("inside", modLoc("block/crucible_inner"));
        return wet ? model.texture("content", mcLoc("block/water_still")) : model;
    }

    /**
     * The jar, the essentia standing in it at four heights, and the label stuck on it.
     * <p>
     * The label is drawn on the north face of the model, so the side it ends up on is a matter of how far the whole
     * jar is turned. An unlabelled jar is turned too, and nobody can tell.
     */
    private void jar() {
        getVariantBuilder(ModBlocks.JAR.get()).forAllStates(state -> {
            int fill = state.getValue(JarBlock.FILL);
            String label = state.getValue(JarBlock.LABELLED) ? "jar_labelled" : "jar";
            return ConfiguredModel.builder()
                    .modelFile(models().getExistingFile(
                            modLoc("block/jar/" + label + (fill == 0 ? "" : "_" + fill))))
                    // the label is drawn facing north, so every other bearing is that far round from it
                    .rotationY(((int) state.getValue(JarBlock.FACING).toYRot() + 180) % 360)
                    .build();
        });
    }

    /**
     * The pipe, put together a side at a time.
     * <p>
     * Every kind of pipe is put together the same way and out of the same pieces, as they were in the original: what
     * a valve or a filter does is not something you can see from the outside.
     * <p>
     * The middle is always there; each side adds an arm, and a plain arm or one with a collar depending on what it
     * is up against. The four horizontal arms are the one drawn model turned about the upright; up and down are
     * models of their own, since a blockstate cannot turn an east-pointing thing to face up.
     */
    private void tube() {
        tube(ModBlocks.TUBE.get(), "plain");
        tube(ModBlocks.TUBE_VALVE.get(), "plain");
        tube(ModBlocks.TUBE_ONEWAY.get(), "plain");
        tube(ModBlocks.TUBE_RESTRICT.get(), "plain");
        tube(ModBlocks.TUBE_FILTER.get(), "filter");
        tube(ModBlocks.TUBE_BUFFER.get(), "buffer");

        // the arrow only where it points, and only if there is an arm there for it to sit on
        for (Direction side : Direction.values()) {
            var part = getMultipartBuilder(ModBlocks.TUBE_ONEWAY.get()).part();
            reaching(part, "arrow", side).addModel()
                    .condition(OnewayTubeBlock.FACING, side)
                    .condition(TubeBlock.SIDES.get(side), TubeBlock.Link.TUBE, TubeBlock.Link.BLOCK)
                    .end();
        }
        // the bands on every side that is joined to something
        for (Direction side : Direction.values()) {
            var part = getMultipartBuilder(ModBlocks.TUBE_RESTRICT.get()).part();
            reaching(part, "band", side).addModel()
                    .condition(TubeBlock.SIDES.get(side), TubeBlock.Link.TUBE, TubeBlock.Link.BLOCK)
                    .end();
        }
        // the handle, on the side it stands on, lying over or standing up as the valve is shut or open
        for (Direction side : Direction.values()) {
            for (boolean open : new boolean[] {false, true}) {
                var part = getMultipartBuilder(ModBlocks.TUBE_VALVE.get()).part();
                reaching(part, "handle_" + (open ? "open" : "shut"), side).addModel()
                        .condition(ValveTubeBlock.FACING, side)
                        .condition(ValveTubeBlock.OPEN, open)
                        .end();
            }
        }
    }

    /**
     * The pipe, put together a side at a time.
     * <p>
     * The middle is always there; each side that is joined to anything adds an arm, and a side that meets a vessel
     * rather than a pipe adds a collar on the end of it. Every kind of tube is built the same way out of the same
     * pieces -- only the middle tells them apart, which is as it was in the original.
     */
    private void tube(TubeBlock block, String middle) {
        var builder = getMultipartBuilder(block);
        builder.part().modelFile(models().getExistingFile(modLoc("block/tube/middle_" + middle)))
                .addModel().end();

        for (Direction side : Direction.values()) {
            reaching(builder.part(), "arm", side).addModel()
                    .condition(TubeBlock.SIDES.get(side), TubeBlock.Link.TUBE, TubeBlock.Link.BLOCK)
                    .end();
            reaching(builder.part(), "collar", side).addModel()
                    .condition(TubeBlock.SIDES.get(side), TubeBlock.Link.BLOCK)
                    .end();
        }
    }

    /**
     * One piece pointed at one side.
     * <p>
     * The four bearings round the compass are the drawn model turned about the upright; up and down are models of
     * their own, since a blockstate cannot turn an east-pointing thing to face up.
     */
    private ConfiguredModel.Builder<MultiPartBlockStateBuilder.PartBuilder> reaching(
            ConfiguredModel.Builder<MultiPartBlockStateBuilder.PartBuilder> part, String piece, Direction side) {
        if (side.getAxis().isVertical()) {
            return part.modelFile(models().getExistingFile(
                    modLoc("block/tube/" + piece + (side == Direction.UP ? "_up" : "_down"))));
        }
        // the piece is drawn reaching east, so every other bearing is that many quarter turns on
        return part.modelFile(models().getExistingFile(modLoc("block/tube/" + piece)))
                .rotationY(((int) side.toYRot() + 90) % 360);
    }

    /**
     * The pedestal, and the matrix that has no model of its own.
     * <p>
     * The pillars are not here because they are not blocks. A woken matrix draws four of them over the corners of
     * its altar, and the corner it draws over is a block that draws nothing at all.
     */
    private void altar() {
        simpleBlock(ModBlocks.ARCANE_PEDESTAL.get(), models().getExistingFile(modLoc("block/pedestal")));
        simpleBlock(ModBlocks.INFUSION_MATRIX.get(), models().getExistingFile(modLoc("block/infusion_matrix")));
        // the corner draws nothing either: the pillar standing on it is drawn by the matrix
        simpleBlock(ModBlocks.ARCANE_PILLAR.get(), models().getExistingFile(modLoc("block/infusion_matrix")));
    }

    /** A furnace in every way the blockstate cares about: it faces somewhere, and it is lit or it is not. */
    private void essentiaSmelter() {
        ModelFile off = models().orientable("essentia_smelter",
                modLoc("block/smelter_side"), modLoc("block/smelter_front"), modLoc("block/smelter_top"));
        ModelFile on = models().orientable("essentia_smelter_on",
                modLoc("block/smelter_side"), modLoc("block/smelter_front_on"), modLoc("block/smelter_top"));
        horizontalBlock(ModBlocks.ESSENTIA_SMELTER.get(),
                state -> state.getValue(EssentiaSmelterBlock.LIT) ? on : off);
    }

    /**
     * The vessel, with and without the stand it would otherwise be standing on.
     * <p>
     * MoonScenty drew it facing west, which is where its filter is. A model drawn facing north is turned by the
     * yaw plus a half turn; this one wants a further quarter on top of that to bring its west round to the front.
     */
    private void alembic() {
        ModelFile body = models().getExistingFile(modLoc("block/alembic/block"));
        ModelFile legs = models().getExistingFile(modLoc("block/alembic/leg"));
        var builder = getMultipartBuilder(ModBlocks.ALEMBIC.get());
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            int turn = ((int) facing.toYRot() + 270) % 360;
            builder.part().modelFile(body).rotationY(turn).addModel()
                    .condition(AlembicBlock.FACING, facing).end();
            builder.part().modelFile(legs).rotationY(turn).addModel()
                    .condition(AlembicBlock.FACING, facing).condition(AlembicBlock.LEGS, true).end();
        }
    }

    /** Drawn from a model made in Blockbench, so the blockstate only has to point at it. */
    private void arcaneWorkbench() {
        simpleBlock(ModBlocks.ARCANE_WORKBENCH.get(), models().getExistingFile(modLoc("block/arcane_workbench")));
    }

    /** Four posts and a crystal, built into one obj by thaumref/tools/gen_charger.py. */
    private void arcaneWorkbenchCharger() {
        simpleBlock(ModBlocks.ARCANE_WORKBENCH_CHARGER.get(),
                models().getExistingFile(modLoc("block/charger/block")));
    }

    /** Built from an obj rather than a cube, so the blockstate only has to point at it. */
    private void nodeStabilizer() {
        simpleBlock(ModBlocks.NODE_STABILIZER.get(), models().getExistingFile(modLoc("block/node_stabilizer/block")));
    }

    /** The desk, plus an inkwell and a spread note that only show when the state says they are there. */
    private void researchTable() {
        ResearchTableBlock block = ModBlocks.RESEARCH_TABLE.get();
        ModelFile desk = models().getExistingFile(modLoc("block/research_table"));
        ModelFile inkwell = models().getExistingFile(modLoc("block/research_table_inkwell"));
        ModelFile scroll = models().getExistingFile(modLoc("block/research_table_scroll"));

        var builder = getMultipartBuilder(block);
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            // the model is drawn facing north, whose yaw is 180, so take that back out
            int turn = ((int) facing.toYRot() + 180) % 360;
            builder.part().modelFile(desk).rotationY(turn).addModel()
                    .condition(ResearchTableBlock.FACING, facing).end();
            builder.part().modelFile(inkwell).rotationY(turn).addModel()
                    .condition(ResearchTableBlock.FACING, facing)
                    .condition(ResearchTableBlock.HAS_TOOLS, true).end();
            builder.part().modelFile(scroll).rotationY(turn).addModel()
                    .condition(ResearchTableBlock.FACING, facing)
                    .condition(ResearchTableBlock.HAS_NOTES, true).end();
        }
    }

    private void stoneSet(StoneSet set) {
        simpleBlockWithItem(set.block());

        ResourceLocation texture = blockTexture(set.block().get());
        stairsBlock(set.stairs().get(), texture);
        simpleBlockItem(set.stairs().get(), models().getExistingFile(set.stairs().getId().withPrefix("block/")));
        slabBlock(set.slab().get(), texture, texture);
        simpleBlockItem(set.slab().get(), models().getExistingFile(set.slab().getId().withPrefix("block/")));
    }

    private void translucentBlock(DeferredBlock<? extends Block> block) {
        String name = block.getId().getPath();
        simpleBlockWithItem(block.get(), models().cubeAll(name, blockTexture(block.get())).renderType("translucent"));
    }

    private void plant(DeferredBlock<? extends Block> block) {
        String name = block.getId().getPath();
        simpleBlock(block.get(), models().cross(name, blockTexture(block.get())).renderType("cutout"));
    }

    private void wood(WoodSet wood) {
        logBlock(wood.log().get());
        simpleBlockItem(wood.log().get(), models().getExistingFile(wood.log().getId().withPrefix("block/")));
        simpleBlockWithItem(wood.planks());

        ResourceLocation planks = blockTexture(wood.planks().get());
        stairsBlock(wood.stairs().get(), planks);
        simpleBlockItem(wood.stairs().get(), models().getExistingFile(wood.stairs().getId().withPrefix("block/")));
        slabBlock(wood.slab().get(), planks, planks);
        simpleBlockItem(wood.slab().get(), models().getExistingFile(wood.slab().getId().withPrefix("block/")));

        // the leaf color is part of the texture, so there is no biome tint
        simpleBlockWithItem(wood.leaves().get(), models().leaves(wood.leaves().getId().getPath(), blockTexture(wood.leaves().get())).renderType("cutout_mipped"));
        simpleBlock(wood.sapling().get(), models().cross(wood.sapling().getId().getPath(), blockTexture(wood.sapling().get())).renderType("cutout"));
    }

    private void crystal(DeferredBlock<CrystalBlock> block) {
        String name = block.getId().getPath();
        ModelFile[] models = new ModelFile[CrystalBlock.MAX_AGE + 1];
        for (int age = 0; age <= CrystalBlock.MAX_AGE; age++) {
            models[age] = models().cross(name + "_stage" + age, modLoc("block/crystal/" + name + "_stage" + age)).renderType("cutout");
        }

        getVariantBuilder(block.get()).forAllStatesExcept(state -> {
            Direction facing = state.getValue(CrystalBlock.FACING);
            return ConfiguredModel.builder()
                    .modelFile(models[state.getValue(CrystalBlock.AGE)])
                    .rotationX(facing == Direction.UP ? 0 : facing == Direction.DOWN ? 180 : 90)
                    .rotationY(facing.getAxis().isVertical() ? 0 : ((int) facing.toYRot() + 180) % 360)
                    .build();
        }, CrystalBlock.WATERLOGGED, CrystalBlock.GENERATION);
    }
}
