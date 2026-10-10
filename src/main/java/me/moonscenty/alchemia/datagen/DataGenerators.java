package me.moonscenty.alchemia.datagen;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.client.legacy.LegacyAsset;
import me.moonscenty.alchemia.client.legacy.LegacyAssets;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.model.generators.ModelProvider;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = Alchemia.MODID)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        knowImported(existingFileHelper);

        generator.addProvider(event.includeClient(), new ModBlockStateProvider(output, existingFileHelper));
        generator.addProvider(event.includeClient(), new ModItemModelProvider(output, existingFileHelper));
        generator.addProvider(event.includeClient(), new ModLanguageProvider.English(output));
        generator.addProvider(event.includeClient(), new ModLanguageProvider.Korean(output));

        ModBlockTagsProvider blockTags = generator.addProvider(event.includeServer(), new ModBlockTagsProvider(output, lookupProvider, existingFileHelper));
        generator.addProvider(event.includeServer(), new ModItemTagsProvider(output, lookupProvider, blockTags.contentsGetter(), existingFileHelper));
        generator.addProvider(event.includeServer(), new ModRecipeProvider(output, lookupProvider));
        generator.addProvider(event.includeServer(), new ModBiomeTagsProvider(output, lookupProvider, existingFileHelper));
        generator.addProvider(event.includeServer(), new ModDataMapProvider(output, lookupProvider));
        generator.addProvider(event.includeServer(), new LootTableProvider(output, Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(ModBlockLootProvider::new, LootContextParamSets.BLOCK)), lookupProvider));
        generator.addProvider(event.includeServer(), new DatapackBuiltinEntriesProvider(output, lookupProvider, ModDatapackEntries.BUILDER, Set.of(Alchemia.MODID)));
    }

    /**
     * Tells the checks that every picture and model taken from the original's jars is there. They come from the
     * player's jars when the game starts and are never in the repository, so writing a model that names one would
     * otherwise be refused as naming a file that does not exist.
     */
    private static void knowImported(ExistingFileHelper files) {
        ExistingFileHelper.ResourceType models =
                new ExistingFileHelper.ResourceType(PackType.CLIENT_RESOURCES, ".json", "models");
        for (LegacyAsset asset : LegacyAssets.ALL) {
            String path = asset.target();
            if (path.startsWith("textures/") && path.endsWith(".png")) {
                files.trackGenerated(Alchemia.id(path.substring("textures/".length(), path.length() - ".png".length())),
                        ModelProvider.TEXTURE);
            } else if (path.startsWith("models/") && path.endsWith(".json")) {
                files.trackGenerated(Alchemia.id(path.substring("models/".length(), path.length() - ".json".length())),
                        models);
            }
        }
    }
}
