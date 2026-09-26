package me.moonscenty.alchemia.client;


import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.registry.ModWandParts;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.item.AlchemonomiconItem;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import me.moonscenty.alchemia.registry.ModEntities;
import me.moonscenty.alchemia.registry.ModMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
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
        registerWandVariants();
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.RESEARCH_TABLE.get(), ResearchTableScreen::new);
        event.register(ModMenus.ARCANE_WORKBENCH.get(), ArcaneWorkbenchScreen::new);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.RESEARCH_TABLE.get(), ResearchTableRenderer::new);
        event.registerEntityRenderer(ModEntities.AURA_NODE.get(), AuraNodeRenderer::new);
        // a cloud is all particles, so there is nothing to draw for the entity itself
        event.registerEntityRenderer(ModEntities.TAINT_CLOUD.get(), NoopRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.NODE_STABILIZER.get(), NodeStabilizerRenderer::new);
    }

    /**
     * Tells a wand which of its forty-five pictures to wear.
     * <p>
     * One number stands for both pieces — the rod's place in the registry times the number of caps, plus the cap's.
     * Two separate numbers would need the model to test two things at once, which item overrides cannot do.
     */
    private static void registerWandVariants() {
        ItemProperties.register(ModItems.WAND.get(), Alchemia.id("wand"), (stack, level, holder, seed) -> {
            int rod = ModWandParts.RODS.getId(WandItem.rod(stack));
            int cap = ModWandParts.CAPS.getId(WandItem.cap(stack));
            return Math.max(0, rod) * ModWandParts.CAPS.size() + Math.max(0, cap);
        });
    }
}
