package me.moonscenty.alchemia.client;

import java.util.List;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.block.entity.CrucibleBlockEntity;
import me.moonscenty.alchemia.registry.ModDataComponents;
import net.minecraft.core.Holder;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import me.moonscenty.alchemia.block.entity.JarBlockEntity;
import me.moonscenty.alchemia.registry.ModBlocks;
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
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/** Client-side wiring: extra models to bake, and who draws what. */
@EventBusSubscriber(modid = Alchemia.MODID, value = Dist.CLIENT)
public class AlchemiaClientSetup {
    /** The quill is not an item model, so it has to be asked for by hand before it can be drawn. */
    @SubscribeEvent
    public static void registerExtraModels(ModelEvent.RegisterAdditional event) {
        event.register(ResearchTableRenderer.QUILL);
        // the matrix has no block model of its own; its eight stones are asked for here and drawn by hand
        event.register(InfusionMatrixRenderer.CUBE);
        // an altar's pillars are a picture a woken matrix draws, not four blocks somebody placed
        event.register(InfusionMatrixRenderer.PILLAR);
        for (var arm : NodeStabilizerRenderer.ARMS) {
            event.register(arm);
        }
    }

    /** Tells the book how to open itself, which only the client knows how to do. */
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        AlchemonomiconItem.opener = () -> Minecraft.getInstance().setScreen(new AlchemonomiconScreen());
        registerWandVariants();

        // a phial and a label are each drawn one way empty and another way with something named on them
        for (Item item : List.of(ModItems.PHIAL.get(), ModItems.JAR_LABEL.get())) {
            ItemProperties.register(item, Alchemia.id("filled"), (stack, level, holder, seed) ->
                    stack.has(ModDataComponents.ESSENTIA.get()) ? 1 : 0);
        }
    }

    /**
     * What colour the essentia in a phial reads as, and the ink on a label.
     * <p>
     * Both are drawn grey and coloured here, the same as the liquid in a jar, so one picture serves thirty-five
     * aspects. The layer that takes the colour differs: a phial is filled behind its glass, a label is written on
     * top of its paper.
     */
    @SubscribeEvent
    public static void registerItemColours(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tint) -> tint == 0 ? named(stack) : PLAIN, ModItems.PHIAL.get());
        event.register((stack, tint) -> tint == 1 ? named(stack) : PLAIN, ModItems.JAR_LABEL.get());
    }

    /**
     * White, and said properly.
     * <p>
     * An item tint is read as four bytes, not three: the renderer takes the top byte as how opaque the face is.
     * A colour written as six digits therefore says "perfectly clear", and a phial tinted with plain white
     * vanishes out of the slot altogether. A block tint has no such byte and is read as three, which is why the
     * liquid in a jar was never troubled by this.
     */
    private static final int PLAIN = FastColor.ARGB32.opaque(0xFFFFFF);

    /** The colour of the aspect an item names, or white if it names none. */
    private static int named(ItemStack stack) {
        Holder<Aspect> aspect = stack.get(ModDataComponents.ESSENTIA.get());
        return aspect == null ? PLAIN : FastColor.ARGB32.opaque(aspect.value().color());
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.RESEARCH_TABLE.get(), ResearchTableScreen::new);
        event.register(ModMenus.ARCANE_WORKBENCH.get(), ArcaneWorkbenchScreen::new);
        event.register(ModMenus.ESSENTIA_SMELTER.get(), EssentiaSmelterScreen::new);
    }

    /**
     * What colour the water in a crucible reads as.
     * <p>
     * The model's liquid face carries a tint index, and this is what fills it in. A pot that has had nothing thrown
     * in it is plain water; after that it drifts towards whatever is dissolved, so what is in the pot can be read
     * across a room without opening anything.
     */
    @SubscribeEvent
    public static void registerBlockColours(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tint) -> {
            if (level != null && pos != null
                    && level.getBlockEntity(pos) instanceof CrucibleBlockEntity crucible) {
                return crucible.colour();
            }
            return 0x3F76E4;
        }, ModBlocks.CRUCIBLE.get());

        // the liquid standing in a jar, in the colour of whatever the jar is holding
        event.register((state, level, pos, tint) ->
                level != null && pos != null && level.getBlockEntity(pos) instanceof JarBlockEntity jar
                        ? (tint == 1 ? jar.label().map(aspect -> aspect.value().color()).orElse(0xFFFFFF)
                                     : jar.colour())
                        : 0xFFFFFF, ModBlocks.JAR.get());
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.RESEARCH_TABLE.get(), ResearchTableRenderer::new);
        event.registerEntityRenderer(ModEntities.AURA_NODE.get(), AuraNodeRenderer::new);
        // a cloud is all particles, so there is nothing to draw for the entity itself
        event.registerEntityRenderer(ModEntities.TAINT_CLOUD.get(), NoopRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.NODE_STABILIZER.get(), NodeStabilizerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ARCANE_PEDESTAL.get(), ArcanePedestalRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.INFUSION_MATRIX.get(), InfusionMatrixRenderer::new);
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
