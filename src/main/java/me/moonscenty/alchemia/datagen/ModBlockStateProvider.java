package me.moonscenty.alchemia.datagen;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.CrystalBlock;
import me.moonscenty.alchemia.registry.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
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
    }

    private void simpleBlockWithItem(DeferredBlock<?> block) {
        simpleBlockWithItem(block.get(), cubeAll(block.get()));
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
