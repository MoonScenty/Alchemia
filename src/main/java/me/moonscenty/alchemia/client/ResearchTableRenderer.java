package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.ResearchTableBlock;
import me.moonscenty.alchemia.block.entity.ResearchTableBlockEntity;
import me.moonscenty.alchemia.client.legacy.LegacyAssets;
import me.moonscenty.alchemia.client.legacy.LegacyCached;
import me.moonscenty.alchemia.client.legacy.LegacyModels;
import me.moonscenty.alchemia.client.legacy.model.LegacyModelBaker;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class ResearchTableRenderer implements BlockEntityRenderer<ResearchTableBlockEntity> {
    public static final ModelResourceLocation QUILL = ModelResourceLocation.standalone(Alchemia.id("block/research_table_quill"));
    private static final ResourceLocation LEGACY_TEXTURE = Alchemia.id("textures/entity/research_table.png");
    private static final int RIBBON = 0xFF999999;
    private static final float LEGACY_QUILL_SCALE = 0.5F;
    private static final float LEGACY_QUILL_THICK = 0.0625F;

    private final Minecraft client = Minecraft.getInstance();
    private ItemStack carrier = ItemStack.EMPTY;

    private static final LegacyCached<Parts> PARTS = new LegacyCached<>(() -> LegacyModels.get(
            LegacyAssets.RESEARCH_TABLE).map(read -> new Parts(LegacyModelBaker.field(read, "Inkwell"),
            LegacyModelBaker.field(read, "ScrollTube"), LegacyModelBaker.field(read, "ScrollRibbon"))).orElse(null));

    // the item renderer skips empty stacks, so any stack carries the quill model
    private ItemStack carrier() {
        if (carrier.isEmpty()) {
            carrier = new ItemStack(ModItems.SCRIBING_TOOLS.get());
        }
        return carrier;
    }

    public ResearchTableRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ResearchTableBlockEntity table, float partialTick, PoseStack pose, MultiBufferSource buffers,
            int light, int overlay) {
        BlockState state = table.getBlockState();
        boolean tools = state.getValue(ResearchTableBlock.HAS_TOOLS);
        boolean notes = state.getValue(ResearchTableBlock.HAS_NOTES);
        if (!tools && !notes) {
            return;
        }
        Direction facing = state.getValue(ResearchTableBlock.FACING);
        Parts parts = PARTS.get();
        if (parts != null) {
            renderLegacy(parts, facing, tools, notes, pose, buffers, light);
        }
    }

    private void renderLegacy(Parts parts, Direction facing, boolean tools, boolean notes, PoseStack pose,
            MultiBufferSource buffers, int light) {
        pose.pushPose();
        pose.translate(0.5F, 1.0F, 0.5F);
        pose.mulPose(Axis.XP.rotationDegrees(180F));
        pose.mulPose(Axis.YP.rotationDegrees(switch (facing) {
            case EAST -> 90F;
            case WEST -> 270F;
            case SOUTH -> 180F;
            default -> 0F;
        }));
        var buffer = buffers.getBuffer(RenderType.entityCutoutNoCull(LEGACY_TEXTURE));
        if (notes) {
            parts.scrollTube().render(pose, buffer, light, OverlayTexture.NO_OVERLAY);
            parts.scrollRibbon().render(pose, buffer, light, OverlayTexture.NO_OVERLAY, RIBBON);
        }
        if (tools) {
            parts.inkwell().render(pose, buffer, light, OverlayTexture.NO_OVERLAY);

            pose.pushPose();
            pose.mulPose(Axis.XP.rotationDegrees(180F));
            pose.translate(-0.5F, 0.1F, 0.125F);
            pose.mulPose(Axis.YP.rotationDegrees(60F));
            pose.scale(LEGACY_QUILL_SCALE, LEGACY_QUILL_SCALE, LEGACY_QUILL_SCALE);
            pose.translate(0.5F, 0.5F, -LEGACY_QUILL_THICK / 2F);
            pose.mulPose(Axis.YP.rotationDegrees(180F));
            client.getItemRenderer().render(carrier(), ItemDisplayContext.NONE, false, pose, buffers, light,
                    OverlayTexture.NO_OVERLAY, client.getModelManager().getModel(QUILL));
            pose.popPose();
        }
        pose.popPose();
    }

    private record Parts(ModelPart inkwell, ModelPart scrollTube, ModelPart scrollRibbon) {
    }
}
