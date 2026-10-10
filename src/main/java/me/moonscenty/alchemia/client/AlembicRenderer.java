package me.moonscenty.alchemia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.AlembicBlock;
import me.moonscenty.alchemia.block.EssentiaSmelterBlock;
import me.moonscenty.alchemia.block.entity.AlembicBlockEntity;
import me.moonscenty.alchemia.client.legacy.LegacyAssets;
import me.moonscenty.alchemia.client.legacy.LegacyModels;
import me.moonscenty.alchemia.client.legacy.model.LegacyMesh;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

/**
 * The alembic as Thaumcraft 4 drew it, when its jar is there: a pot with a panel on its face, and underneath it
 * whatever it stands on. Over a smelter it has its legs and the wide tube down into the smelter; over another alembic
 * the wide tube and a narrow one, and no legs, since it sits on the one below; anywhere else just its legs.
 * <p>
 * Thaumcraft 5 had an alembic of its own, but the 4 one is the one asked for. Its renderer drew the whole thing and
 * never a block, so with its jar our own block model steps aside and this draws it all.
 * <p>
 * The original also hung a label with the aspect it was set to on its face, and a nozzle towards any vessel beside
 * it. Ours are not set to an aspect and pass essentia by tube, so neither is drawn.
 */
public class AlembicRenderer implements BlockEntityRenderer<AlembicBlockEntity> {
    private static final ResourceLocation TEXTURE = Alchemia.id("textures/entity/alembic.png");

    /** The original's mesh as of the import {@link #meshFrom} names, turned over for the loader that drew it. */
    private static int meshFrom = -1;
    private static LegacyMesh mesh;

    public AlembicRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(AlembicBlockEntity alembic, float partialTick, PoseStack pose, MultiBufferSource buffers,
            int light, int overlay) {
        LegacyMesh legacy = legacyMesh();
        if (legacy == null || alembic.getLevel() == null) {
            return;
        }
        Block below = alembic.getLevel().getBlockState(alembic.getBlockPos().below()).getBlock();
        Direction facing = alembic.getBlockState().getValue(AlembicBlock.FACING);
        var buffer = buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));

        pose.pushPose();
        pose.translate(0.5F, 0.0F, 0.5F);
        // the mesh is modelled lying down; stood up, its panel faces west, and it is turned to face the way ours does
        pose.mulPose(Axis.XP.rotationDegrees(-90.0F));
        pose.mulPose(Axis.ZP.rotationDegrees(switch (facing) {
            case NORTH -> 270.0F;
            case SOUTH -> 90.0F;
            case EAST -> 180.0F;
            default -> 0.0F;
        }));
        if (below instanceof EssentiaSmelterBlock) {
            part(legacy, "TubeMain", pose, buffer, light, overlay);
            part(legacy, "Legs", pose, buffer, light, overlay);
        } else if (below instanceof AlembicBlock) {
            part(legacy, "TubeMain", pose, buffer, light, overlay);
            part(legacy, "TubeSmall", pose, buffer, light, overlay);
        } else {
            part(legacy, "Legs", pose, buffer, light, overlay);
        }
        part(legacy, "Pot", pose, buffer, light, overlay);
        part(legacy, "Panel", pose, buffer, light, overlay);
        pose.popPose();
    }

    private static void part(LegacyMesh mesh, String name, PoseStack pose,
            VertexConsumer buffer, int light, int overlay) {
        int group = mesh.group(name);
        if (group >= 0) {
            mesh.render(group, pose.last(), buffer, -1, light, overlay);
        }
    }

    /**
     * The original's mesh, once per import. Thaumcraft 4 read it with the game's own loader of the time, which turned
     * the texture corners over, so it is turned over here too.
     */
    private static LegacyMesh legacyMesh() {
        int generation = LegacyModels.generation();
        if (generation != meshFrom) {
            meshFrom = generation;
            mesh = LegacyModels.mesh(LegacyAssets.ALEMBIC_MESH).map(LegacyMesh::flippedV).orElse(null);
        }
        return mesh;
    }
}
