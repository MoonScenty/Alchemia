package me.moonscenty.alchemia.datagen;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.CrystalBlock;
import me.moonscenty.alchemia.block.ResearchTableBlock;
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

        ModBlocks.STONE_SETS.forEach(this::stoneSet);
        translucentBlock(ModBlocks.AMBER_BLOCK);
        translucentBlock(ModBlocks.AMBER_BRICKS);
    }

    private void simpleBlockWithItem(DeferredBlock<?> block) {
        simpleBlockWithItem(block.get(), cubeAll(block.get()));
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
        }, CrystalBlock.WATERLOGGED);
    }
}
