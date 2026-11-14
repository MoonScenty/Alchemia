package me.moonscenty.alchemia.research;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

@EventBusSubscriber(modid = Alchemia.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModResearch {
    public static final ResourceKey<Registry<ResearchCategory>> CATEGORY_KEY =
            ResourceKey.createRegistryKey(Alchemia.id("research_category"));
    public static final ResourceKey<Registry<ResearchEntry>> ENTRY_KEY =
            ResourceKey.createRegistryKey(Alchemia.id("research"));

    public static final ResourceKey<ResearchCategory> BASICS = category("basics");
    public static final ResourceKey<ResearchCategory> THAUMATURGY = category("thaumaturgy");
    public static final ResourceKey<ResearchCategory> ALCHEMY = category("alchemy");
    public static final ResourceKey<ResearchCategory> ARTIFICE = category("artifice");
    public static final ResourceKey<ResearchCategory> GOLEMANCY = category("golemancy");
    public static final ResourceKey<ResearchCategory> ELDRITCH = category("eldritch");

    @SubscribeEvent
    public static void registerDataPackRegistries(DataPackRegistryEvent.NewRegistry event) {
        // synced too: the book and the scanner read these on the client
        event.dataPackRegistry(CATEGORY_KEY, ResearchCategory.CODEC, ResearchCategory.CODEC);
        event.dataPackRegistry(ENTRY_KEY, ResearchEntry.CODEC, ResearchEntry.CODEC);
    }

    private static ResourceKey<ResearchCategory> category(String name) {
        return ResourceKey.create(CATEGORY_KEY, Alchemia.id(name));
    }
}
