package me.moonscenty.alchemia.client;


import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.item.AlchemonomiconItem;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import me.moonscenty.alchemia.registry.ModEntities;
import me.moonscenty.alchemia.registry.ModMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.client.Minecraft;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/** Client-side wiring: extra models to bake, and who draws what. */
@EventBusSubscriber(modid = Alchemia.MODID, value = Dist.CLIENT)
public class AlchemiaClientSetup {
    /** The quill is not an item model, so it has to be asked for by hand before it can be drawn. */
    @SubscribeEvent
    public static void registerExtraModels(ModelEvent.RegisterAdditional event) {
        event.register(ResearchTableRenderer.QUILL);
        for (var arm : NodeStabilizerRenderer.ARMS) {
            event.register(arm);
        }
    }

    /** Tells the book how to open itself, which only the client knows how to do. */
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        AlchemonomiconItem.opener = () -> Minecraft.getInstance().setScreen(new AlchemonomiconScreen());
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.RESEARCH_TABLE.get(), ResearchTableScreen::new);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.RESEARCH_TABLE.get(), ResearchTableRenderer::new);
        event.registerEntityRenderer(ModEntities.AURA_NODE.get(), AuraNodeRenderer::new);
        // a cloud is all particles, so there is nothing to draw for the entity itself
        event.registerEntityRenderer(ModEntities.TAINT_CLOUD.get(), NoopRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.NODE_STABILIZER.get(), NodeStabilizerRenderer::new);
    }
}
