package me.moonscenty.alchemia.datagen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.CrystalBlock;
import me.moonscenty.alchemia.block.ResearchTableBlock;
import me.moonscenty.alchemia.block.taint.FluxGooBlock;
import me.moonscenty.alchemia.block.taint.TaintFibreBlock;
import me.moonscenty.alchemia.block.taint.TaintLogBlock;
import me.moonscenty.alchemia.block.AlembicBlock;
import me.moonscenty.alchemia.block.EssentiaSmelterBlock;
import me.moonscenty.alchemia.block.CrucibleBlock;
import me.moonscenty.alchemia.block.JarBlock;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.StoneSet;
import me.moonscenty.alchemia.registry.WoodSet;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
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
        nitor();

        ModBlocks.STONE_SETS.forEach(this::stoneSet);
        simpleBlockWithItem(ModBlocks.INFUSION_SPEED_STONE);
        simpleBlockWithItem(ModBlocks.INFUSION_COST_STONE);
        // the original's amber block wears its own picture on top and bottom
        simpleBlockWithItem(ModBlocks.AMBER_BLOCK.get(), models().cubeBottomTop("amber_block",
                modLoc("block/amber_block"), modLoc("block/amber_block_top"), modLoc("block/amber_block_top"))
                .renderType("translucent"));
        translucentBlock(ModBlocks.AMBER_BRICKS);
    }

    private void simpleBlockWithItem(DeferredBlock<?> block) {
        simpleBlockWithItem(block.get(), cubeAll(block.get()));
    }

    /**
     * The taint, picked at random by position as the original's blockstates picked it: ground in three looks, mostly
     * the plain one (crust eight to one, soil sixteen to one), and every look, the rock's too, laid at any of four
     * turns so a patch is not a tiled repeat.
     */
    private void taint() {
        spotted(ModBlocks.TAINT_CRUST, 8);
        spotted(ModBlocks.TAINT_SOIL, 16);
        spotted(ModBlocks.TAINT_ROCK, 0);
        taintLog();
        taintFibre();
        fluxGoo();
    }

    /**
     * Sixteen flames off one drawing.
     * <p>
     * The picture is grey and the colour is put on when it is drawn, so all sixteen name the same model and the
     * same two pictures. The item is the flame and its bead as two flat layers, the first of which takes the dye.
     */
    private void nitor() {
        ModelFile flame = models().getExistingFile(modLoc("block/nitor"));
        ModBlocks.NITOR.forEach((colour, block) -> {
            simpleBlock(block.get(), flame);
            itemModels().getBuilder(ModBlocks.name(colour))
                    .parent(itemModels().getExistingFile(mcLoc("item/generated")))
                    .texture("layer0", modLoc("block/nitor"))
                    .texture("layer1", modLoc("block/nitor_core"));
        });
    }

    /** The four turns the original laid a taint block at. */
    private static final int[][] TURNS = {{0, 0}, {90, 0}, {0, 90}, {90, 90}};

    /**
     * A block in its plain look and, if {@code plain} is not zero, two spotted ones, each at the four turns; the plain
     * one {@code plain} times as likely as each spotted one.
     */
    private void spotted(DeferredBlock<? extends Block> block, int plain) {
        String name = block.getId().getPath();
        List<ConfiguredModel> looks = new ArrayList<>();
        String[] suffixes = plain == 0 ? new String[] {""} : new String[] {"", "_1", "_2"};
        for (String suffix : suffixes) {
            ModelFile model = models().cubeAll(name + suffix, modLoc("block/" + name + suffix));
            for (int[] turn : TURNS) {
                looks.add(ConfiguredModel.builder().modelFile(model).rotationX(turn[0]).rotationY(turn[1])
                        .weight(suffix.isEmpty() && plain != 0 ? plain : 1).buildLast());
            }
        }
        getVariantBuilder(block.get()).partialState().setModels(looks.toArray(ConfiguredModel[]::new));
        simpleBlockItem(block.get(), models().getExistingFile(modLoc("block/" + name)));
    }

    /**
     * The tainted log, as the original's: plain bark all round, except that one side, any of the four, wears one of
     * the two blistered pictures. The item and the plain model are bark all round.
     */
    private void taintLog() {
        ResourceLocation top = modLoc("block/taint_log_top");
        ResourceLocation bark = modLoc("block/taint_log");
        models().cube("taint_log", top, top, bark, bark, bark, bark).texture("particle", top);
        List<ModelFile> blistered = new ArrayList<>();
        for (String spot : List.of("_1", "_2")) {
            ResourceLocation sore = modLoc("block/taint_log" + spot);
            for (Direction side : Direction.Plane.HORIZONTAL) {
                blistered.add(models().cube("taint_log" + spot + "_" + side.getName(), top, top,
                        side == Direction.NORTH ? sore : bark, side == Direction.SOUTH ? sore : bark,
                        side == Direction.EAST ? sore : bark, side == Direction.WEST ? sore : bark)
                        .texture("particle", top));
            }
        }
        for (Direction.Axis axis : Direction.Axis.values()) {
            ConfiguredModel[] looks = blistered.stream().map(model -> ConfiguredModel.builder()
                    .modelFile(model)
                    .rotationX(axis == Direction.Axis.Y ? 0 : 90)
                    .rotationY(axis == Direction.Axis.X ? 90 : 0)
                    .buildLast()).toArray(ConfiguredModel[]::new);
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
    /**
     * The fibre as the original put it together: its crust, a flat sheet lying on the floor, turned onto each side
     * the fibre clings to as the original turned it, and over that whichever of its four growths stands there. The
     * crust and the growths are the original's model files, imported.
     */
    private void taintFibre() {
        ModelFile crust = models().getExistingFile(modLoc("block/taint_fibre"));
        var builder = getMultipartBuilder(ModBlocks.TAINT_FIBRE.get());
        builder.part().modelFile(crust).addModel().condition(TaintFibreBlock.DOWN, true).end();
        builder.part().modelFile(crust).rotationX(180).addModel().condition(TaintFibreBlock.UP, true).end();
        builder.part().modelFile(crust).rotationX(90).rotationY(180).addModel().condition(TaintFibreBlock.NORTH, true).end();
        builder.part().modelFile(crust).rotationX(90).addModel().condition(TaintFibreBlock.SOUTH, true).end();
        builder.part().modelFile(crust).rotationX(270).rotationY(90).addModel().condition(TaintFibreBlock.EAST, true).end();
        builder.part().modelFile(crust).rotationX(90).rotationY(90).addModel().condition(TaintFibreBlock.WEST, true).end();
        for (int growth = 1; growth <= TaintFibreBlock.HANGING; growth++) {
            builder.part().modelFile(models().getExistingFile(modLoc("block/taint_growth_" + growth))).addModel()
                    .condition(TaintFibreBlock.GROWTH, growth).end();
        }
    }

    /**
     * The pot is the original's model file at every depth. Its water is not part of the model: the original drew it
     * as a surface that rises and darkens with what is in it ({@code CrucibleRenderer}).
     */
    private void crucible() {
        simpleBlock(ModBlocks.CRUCIBLE.get(), models().getExistingFile(modLoc("block/crucible")));
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
     * The tubes. Their pipe is the original's mesh, put together a side at a time by LegacyTubeModel, and the marks
     * the original drew separately are drawn separately here too: the valve's wheel by ValveHandleRenderer and the
     * one-way's stub by OnewayMarkRenderer. The blockstate only gives each a model to build on.
     */
    private void tube() {
        // nothing in it but the picture a broken tube throws about
        ModelFile bare = models().getBuilder("tube").texture("particle", modLoc("block/legacy/tube"));
        for (var tube : List.of(ModBlocks.TUBE, ModBlocks.TUBE_VALVE, ModBlocks.TUBE_ONEWAY, ModBlocks.TUBE_RESTRICT,
                ModBlocks.TUBE_FILTER, ModBlocks.TUBE_BUFFER)) {
            simpleBlock(tube.get(), bare);
        }
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
        // the original's underside is the furnace's top
        ModelFile off = models().orientableWithBottom("essentia_smelter", modLoc("block/smelter_side"),
                modLoc("block/smelter_front"), mcLoc("block/furnace_top"), modLoc("block/smelter_top"));
        ModelFile on = models().orientableWithBottom("essentia_smelter_on", modLoc("block/smelter_side"),
                modLoc("block/smelter_front_on"), mcLoc("block/furnace_top"), modLoc("block/smelter_top"));
        horizontalBlock(ModBlocks.ESSENTIA_SMELTER.get(),
                state -> state.getValue(EssentiaSmelterBlock.LIT) ? on : off);
    }

    /**
     * A block the original drew wholly in its renderer, as ours does too: a model with nothing in it but the picture
     * a broken one throws about.
     */
    private void drawnElsewhere(DeferredBlock<? extends Block> block, String particle) {
        simpleBlock(block.get(), models().getBuilder(block.getId().getPath()).texture("particle", modLoc(particle)));
    }

    /** Drawn by AlembicRenderer, on Thaumcraft 4's mesh. */
    private void alembic() {
        drawnElsewhere(ModBlocks.ALEMBIC, "item/legacy/alembic");
    }

    /** The original's own model file, read from its jar. */
    private void arcaneWorkbench() {
        simpleBlock(ModBlocks.ARCANE_WORKBENCH.get(), models().getExistingFile(modLoc("block/arcane_workbench")));
    }

    /** Drawn by ChargerRenderer, as the original's vis relay. */
    private void arcaneWorkbenchCharger() {
        drawnElsewhere(ModBlocks.ARCANE_WORKBENCH_CHARGER, "item/legacy/vis_relay");
    }

    /** Drawn by NodeStabilizerRenderer, on the original's mesh. */
    private void nodeStabilizer() {
        drawnElsewhere(ModBlocks.NODE_STABILIZER, "item/legacy/node_stabilizer");
    }

    /**
     * The desk alone. What stands on it is drawn by its renderer, which knows whether to draw the original's inkwell
     * and scroll or our own.
     */
    private void researchTable() {
        ResearchTableBlock block = ModBlocks.RESEARCH_TABLE.get();
        ModelFile desk = models().getExistingFile(modLoc("block/research_table"));

        var builder = getMultipartBuilder(block);
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            // the model is drawn facing north, whose yaw is 180, so take that back out
            int turn = ((int) facing.toYRot() + 180) % 360;
            builder.part().modelFile(desk).rotationY(turn).addModel()
                    .condition(ResearchTableBlock.FACING, facing).end();
        }
    }

    private void stoneSet(StoneSet set) {
        if (set == ModBlocks.ARCANE_STONE) {
            arcaneStone(set);
            return;
        }
        simpleBlockWithItem(set.block());

        ResourceLocation texture = blockTexture(set.block().get());
        stairsBlock(set.stairs().get(), texture);
        simpleBlockItem(set.stairs().get(), models().getExistingFile(set.stairs().getId().withPrefix("block/")));
        slabBlock(set.slab().get(), texture, texture);
        simpleBlockItem(set.slab().get(), models().getExistingFile(set.slab().getId().withPrefix("block/")));
    }

    /**
     * Arcane stone, as the original laid its three pictures: the first on top and bottom, the second east and west,
     * the third north and south, the block at any of four turns. Its stairs and slab take the first underneath, the second on top and the third
     * round the sides.
     */
    private void arcaneStone(StoneSet set) {
        ResourceLocation one = modLoc("block/arcane_stone");
        ResourceLocation two = modLoc("block/arcane_stone_2");
        ResourceLocation three = modLoc("block/arcane_stone_3");
        ModelFile block = models().cube("arcane_stone", one, one, three, three, two, two).texture("particle", one);
        // laid at any of four turns, as the original's blockstate laid it
        getVariantBuilder(set.block().get()).partialState().setModels(Arrays.stream(TURNS)
                .map(turn -> ConfiguredModel.builder().modelFile(block).rotationX(turn[0]).rotationY(turn[1])
                        .buildLast())
                .toArray(ConfiguredModel[]::new));
        simpleBlockItem(set.block().get(), block);
        stairsBlock(set.stairs().get(), three, one, two);
        simpleBlockItem(set.stairs().get(), models().getExistingFile(set.stairs().getId().withPrefix("block/")));
        slabBlock(set.slab().get(), modLoc("block/arcane_stone"), three, one, two);
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

    /**
     * One model for every crystal and every state of it, with nothing in it but its picture: the shards themselves
     * are the original's mesh, chosen and baked by LegacyCrystalModel from the state and the place.
     */
    private void crystal(DeferredBlock<CrystalBlock> block) {
        simpleBlock(block.get(), models().getBuilder("crystal")
                .texture("particle", modLoc("block/legacy/crystal")).renderType("cutout"));
    }
}
