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
import me.moonscenty.alchemia.client.armour.ModArmourLayers;
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
import net.minecraft.client.renderer.item.ItemProperties;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.client.resources.PlayerSkin;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import me.moonscenty.alchemia.client.armour.ArmourMeshes;
import me.moonscenty.alchemia.client.armour.FortressExtensions;
import me.moonscenty.alchemia.client.armour.MeshArmourItemRenderer;
import me.moonscenty.alchemia.client.armour.MeshArmourLayer;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

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
        // a valve's wheel is turned by hand in code, so it is asked for rather than named by a blockstate
        event.register(ValveHandleRenderer.HANDLE);
        for (var arm : NodeStabilizerRenderer.ARMS) {
            event.register(arm);
        }
        // worn armour that is eight carved meshes rather than a sheet. Nothing else names them, so every set is
        // asked for here, and every sheet of every set: the meshes are shared and only the sheet differs
        ArmourMeshes.SETS.forEach((set, sheets) -> {
            for (String part : ArmourMeshes.PARTS) {
                for (String sheet : sheets) {
                    event.register(ArmourMeshes.model(set, sheet, part));
                }
            }
        });
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

    /**
     * Hangs the mesh armour layer on everything shaped like a person.
     * <p>
     * Mesh armour is drawn by a layer of our own rather than by the one that draws armour, so it has to be put on
     * each body that might wear some: both builds of player, and the stand somebody leaves a robe on.
     */
    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerSkin.Model skin : event.getSkins()) {
            if (event.getSkin(skin) instanceof PlayerRenderer drawn) {
                drawn.addLayer(new MeshArmourLayer<>(drawn));
            }
        }
        if (event.getRenderer(EntityType.ARMOR_STAND) instanceof ArmorStandRenderer stand) {
            stand.addLayer(new MeshArmourLayer<>(stand));
        }
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
        // a foot on a boot and a lens in front of an eye: models, not sheets stretched over the body
        event.registerItem(ModArmourLayers.BOOTS_DRAWN, ModItems.TRAVELLER_BOOTS.get());
        event.registerItem(ModArmourLayers.GOGGLES_DRAWN, ModItems.GOGGLES.get());
        // a piece of mesh armour in a bag is the same piece, stood up and looked at from the front
        var meshInHand = new net.neoforged.neoforge.client.extensions.common.IClientItemExtensions() {
            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return MeshArmourItemRenderer.get();
            }
        };
        ModItems.ROBES.values().forEach(robe -> event.registerItem(meshInHand, robe.get()));
        // fortress armour on the original's own model when its jar is there, plain armour when not
        ModItems.FORTRESS.values().forEach(piece -> event.registerItem(FortressExtensions.INSTANCE, piece.get()));
    }

    /** What draws a mote of light. */
    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.MOTE.get(), MoteParticle.Maker::new);
    }

    /** The shapes the pieces of armour we draw as models are built from. */
    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModArmourLayers.TRAVELLER_BOOTS, ModArmourLayers::boots);
        event.registerLayerDefinition(ModArmourLayers.GOGGLES, ModArmourLayers::goggles);
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
