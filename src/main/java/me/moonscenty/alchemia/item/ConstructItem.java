package me.moonscenty.alchemia.item;

import java.util.function.Supplier;

import me.moonscenty.alchemia.entity.Construct;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * A construct folded up to carry, the original's {@code ItemTurretPlacer}: an automated crossbow or a node magnet. It
 * stands on its tripod, so it goes down only where there is room and solid ground under the spot, and whoever set it
 * down owns it.
 */
public class ConstructItem extends Item {
    private final Supplier<? extends EntityType<? extends Construct>> type;

    public ConstructItem(Supplier<? extends EntityType<? extends Construct>> type, Properties properties) {
        super(properties);
        this.type = type;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos clicked = context.getClickedPos();
        BlockPos at = clicked.relative(context.getClickedFace());
        if (player == null || !level.getBlockState(clicked).isSolid()
                || !level.getBlockState(at).canBeReplaced()
                || !level.getBlockState(at.below()).isSolid()
                || !player.mayUseItemAt(at, context.getClickedFace(), context.getItemInHand())) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        Construct construct = type.get().create(level);
        if (construct == null) {
            return InteractionResult.FAIL;
        }
        level.removeBlock(at, false);
        construct.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 0.0F, 0.0F);
        construct.setOwner(player.getUUID());
        level.addFreshEntity(construct);
        if (!player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.CONSUME;
    }
}
