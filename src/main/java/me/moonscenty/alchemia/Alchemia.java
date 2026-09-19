package me.moonscenty.alchemia;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModCreativeTabs;
import me.moonscenty.alchemia.registry.ModFeatures;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(Alchemia.MODID)
public class Alchemia {
    public static final String MODID = "alchemia";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Alchemia(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ModFeatures.FEATURES.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, AlchemiaConfig.SPEC);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
