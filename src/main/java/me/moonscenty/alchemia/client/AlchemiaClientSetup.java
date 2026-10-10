package me.moonscenty.alchemia.client;

import java.util.List;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.block.entity.CrucibleBlockEntity;
import me.moonscenty.alchemia.block.entity.FilterTubeBlockEntity;
import me.moonscenty.alchemia.registry.ModDataComponents;
import net.minecraft.core.Holder;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import me.moonscenty.alchemia.block.entity.JarBlockEntity;
import me.moonscenty.alchemia.client.particle.MoteParticle;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.registry.ModWandParts;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.item.AlchemonomiconItem;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import me.moonscenty.alchemia.registry.ModEntities;
import me.moonscenty.alchemia.registry.ModMenus;
import me.moonscenty.alchemia.registry.ModParticles;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.item.ItemProperties;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import me.moonscenty.alchemia.client.armour.FortressExtensions;
import me.moonscenty.alchemia.client.armour.RobeExtensions;
import me.moonscenty.alchemia.client.legacy.LegacyAssets;
import me.moonscenty.alchemia.item.RobeItem;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

/** Client-side wiring: extra models to bake, and who draws what. */
@EventBusSubscriber(modid = Alchemia.MODID, value = Dist.CLIENT)
public class AlchemiaClientSetup {
    /**
     * Every crystal's models are wrapped in one that draws the original's shards when its jar is there, and the
     * stabiliser's in one that steps aside for the original's renderer.
     * Whether the jar is there is asked each time, since this runs once per reload and the jar can come or go
     * between them.
     */
    @SubscribeEvent
    public static void wrapCrystals(ModelEvent.ModifyBakingResult event) {
        ModBlocks.CRYSTALS.values().forEach(crystal -> crystal.get().getStateDefinition().getPossibleStates()
                .forEach(state -> event.getModels().computeIfPresent(BlockModelShaper.stateToModelLocation(state),
                        (location, ours) -> new LegacyCrystalModel(ours, state))));
        // the original drew the whole stabiliser in its renderer, so with its jar ours steps aside
        ModBlocks.NODE_STABILIZER.get().getStateDefinition().getPossibleStates().forEach(state -> event.getModels()
                .computeIfPresent(BlockModelShaper.stateToModelLocation(state),
                        (location, ours) -> new LegacyHiddenModel(ours, LegacyAssets.STABILIZER_MESH)));
    }

    /** What stands on the research table is drawn by its renderer, so its models have to be asked for by hand. */
    @SubscribeEvent
    public static void registerExtraModels(ModelEvent.RegisterAdditional event) {
        event.register(ResearchTableRenderer.QUILL);
        event.register(ResearchTableRenderer.INKWELL);
        event.register(ResearchTableRenderer.SCROLL);
        // the matrix has no block model of its own; its eight stones are asked for here and drawn by hand
        event.register(InfusionMatrixRenderer.CUBE);
        // an altar's pillars are a picture a woken matrix draws, not four blocks somebody placed
        event.register(InfusionMatrixRenderer.PILLAR);
        // a valve's wheel is turned by hand in code, so it is asked for rather than named by a blockstate
        event.register(ValveHandleRenderer.HANDLE);
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
        ItemProperties.register(ModBlocks.JAR.get().asItem(), Alchemia.id("filled"),
                (stack, level, holder, seed) -> stack.has(ModDataComponents.CONTENTS.get()) ? 1 : 0);
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
        // a crystal is drawn in grey and painted by the one point it holds
        event.register((stack, tint) -> tint == 0 ? named(stack) : PLAIN, ModItems.CRYSTALLIZED_ESSENCE.get());
        event.register((stack, tint) -> tint == 1 ? named(stack) : PLAIN, ModItems.JAR_LABEL.get());
        // the liquid standing in a jar held in the hand, in the colour of whatever it is
        event.register((stack, tint) -> tint == 0 ? inside(stack) : PLAIN, ModBlocks.JAR.get().asItem());
        // a flame in a slot is two layers: the grey flame, which takes the dye, and its bead, which does not.
        // a dye's colour is already opaque, so unlike the phial and the label it needs no wrapping
        ModBlocks.NITOR.forEach((colour, flame) -> event.register(
                (stack, tint) -> tint == 0 ? colour.getTextureDiffuseColor() : PLAIN, flame.get().asItem()));
        // a robe in a slot is its cloth, which takes the dye, and the trim over it, which does not
        ModItems.ROBES.values().forEach(robe -> event.register(
                (stack, tint) -> tint == 0 ? FastColor.ARGB32.opaque(RobeItem.dyed(stack)) : PLAIN, robe.get()));
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

    /** The colour of what is in a jar that has been picked up. */
    private static int inside(ItemStack stack) {
        AspectList held = stack.get(ModDataComponents.CONTENTS.get());
        return held == null || held.isEmpty() ? PLAIN
                : FastColor.ARGB32.opaque(held.sortedByAmount().getFirst().value().color());
    }

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
        event.register(ModMenus.FOCUS_POUCH.get(), FocusPouchScreen::new);
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

        // a filter tube wears the colour of the one thing it lets by, which is the only way to read a run of pipe
        event.register((state, level, pos, tint) ->
                level != null && pos != null && level.getBlockEntity(pos) instanceof FilterTubeBlockEntity filter
                        ? filter.only().map(aspect -> aspect.value().color()).orElse(0xFFFFFF)
                        : 0xFFFFFF, ModBlocks.TUBE_FILTER.get());

        // the liquid standing in a jar, in the colour of whatever the jar is holding
        event.register((state, level, pos, tint) ->
                level != null && pos != null && level.getBlockEntity(pos) instanceof JarBlockEntity jar
                        ? (tint == 1 ? jar.label().map(aspect -> aspect.value().color()).orElse(0xFFFFFF)
                                     : jar.colour())
                        : 0xFFFFFF, ModBlocks.JAR.get());

        // the original's crystal is grey and takes its aspect's colour; our own crystals are already coloured and
        // carry no tint, so this only ever touches the original's
        ModBlocks.CRYSTALS.forEach((type, crystal) -> event.register(
                (state, level, pos, tint) -> tint == LegacyCrystalModel.TINT ? type.aspect().value().color() : -1,
                crystal.get()));

        // a flame is drawn in grey and painted by the kind of flame it is; its bead is a second layer, undyed
        ModBlocks.NITOR.forEach((colour, flame) -> event.register(
                (state, level, pos, tint) -> tint == 0 ? colour.getTextureDiffuseColor() : -1, flame.get()));
    }

    /**
     * The blocks whose breaking chips must not be dyed by the colour they give out.
     * <p>
     * See {@link PlainBreakParticles} for why. All three are tinted by what stands inside them rather than by
     * anything about the block, and all three are broken often enough for it to show.
     */
    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerBlock(PlainBreakParticles.INSTANCE,
                ModBlocks.CRUCIBLE.get(), ModBlocks.JAR.get(), ModBlocks.TUBE_FILTER.get());
        // robes in the original's violet until dyed; the void robe on the original's model when its jar is there
        ModItems.ROBES.values().forEach(robe -> event.registerItem(
                robe.get().drab() ? RobeExtensions.VOID : RobeExtensions.CLOTH, robe.get()));
        // fortress armour on the original's own model when its jar is there, plain armour when not
        ModItems.FORTRESS.values().forEach(piece -> event.registerItem(FortressExtensions.INSTANCE, piece.get()));
    }

    /** What draws a mote of light. */
    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.MOTE.get(), MoteParticle.Maker::new);
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
        event.registerBlockEntityRenderer(ModBlockEntities.TUBE_VALVE.get(), ValveHandleRenderer::new);
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
