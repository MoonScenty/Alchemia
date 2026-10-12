package me.moonscenty.alchemia.item;

import javax.annotation.Nullable;

import me.moonscenty.alchemia.golem.seal.Seal;
import me.moonscenty.alchemia.golem.seal.SealDisplayer;
import me.moonscenty.alchemia.golem.seal.SealTypes;
import me.moonscenty.alchemia.golem.seal.Seals;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

/**
 * A control seal, or a blank one waiting to be made into one. A made seal sticks to the face of a block it is used
 * on, if that is somewhere it can work, and belongs to whoever stuck it there.
 */
public class SealItem extends Item implements SealDisplayer {
    /** What kind of seal it is, or null for a blank. */
    @Nullable
    private final String kind;

    public SealItem(@Nullable String kind, Properties properties) {
        super(properties);
        this.kind = kind;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (kind == null || player == null) {
            return InteractionResult.PASS;
        }
        BlockPos pos = context.getClickedPos();
        Direction face = context.getClickedFace();
        ItemStack stack = context.getItemInHand();
        if (!player.mayUseItemAt(pos, face, stack)) {
            return InteractionResult.FAIL;
        }
        if (!(context.getLevel() instanceof ServerLevel level)) {
            return InteractionResult.SUCCESS;
        }
        Seal seal = SealTypes.make(kind);
        if (seal == null || !seal.canPlaceAt(level, pos, face)) {
            return InteractionResult.FAIL;
        }
        if (Seals.add(level, pos, face, seal, player)) {
            stack.consume(1, player);
        }
        return InteractionResult.CONSUME;
    }
}
