package me.moonscenty.alchemia;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import me.moonscenty.alchemia.player.ModAttachments;
import me.moonscenty.alchemia.player.effect.ModEffects;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModCreativeTabs;
import me.moonscenty.alchemia.registry.ModDataComponents;
import me.moonscenty.alchemia.registry.ModFeatures;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModMenus;
import me.moonscenty.alchemia.registry.WoodSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(Alchemia.MODID)
public class Alchemia {
    public static final String MODID = "alchemia";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Alchemia(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModDataComponents.COMPONENTS.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ModFeatures.FEATURES.register(modEventBus);
        ModFeatures.TRUNK_PLACERS.register(modEventBus);
        ModFeatures.FOLIAGE_PLACERS.register(modEventBus);
        ModFeatures.TREE_DECORATORS.register(modEventBus);
        ModAspects.ASPECTS.register(modEventBus);
        ModAttachments.ATTACHMENTS.register(modEventBus);
        ModEffects.EFFECTS.register(modEventBus);
        modEventBus.addListener(this::commonSetup);

        modContainer.registerConfig(ModConfig.Type.COMMON, AlchemiaConfig.COMMON_SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, AlchemiaConfig.CLIENT_SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            FireBlock fire = (FireBlock) Blocks.FIRE;
            for (WoodSet wood : ModBlocks.WOODS) {
                fire.setFlammable(wood.log().get(), 5, 5);
                fire.setFlammable(wood.planks().get(), 5, 20);
                fire.setFlammable(wood.stairs().get(), 5, 20);
                fire.setFlammable(wood.slab().get(), 5, 20);
                fire.setFlammable(wood.leaves().get(), 30, 60);
            }
        });
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
