package me.moonscenty.alchemia.block;

import java.util.List;

import javax.annotation.Nullable;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModDataComponents;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * A banner in the hand. Put on the top of something it stands, turned whichever of sixteen ways its placer faced; put
 * on the side of something it hangs there. The dyed one comes in sixteen colours; the Crimson Cult's has none.
 */
public class BannerItem extends Item {
    private final boolean cult;

    public BannerItem(Properties properties, boolean cult) {
        super(properties);
        this.cult = cult;
    }

    /** A banner of this colour with this painted on it, or the cult's if there is no colour. */
    public static ItemStack of(@Nullable DyeColor colour, @Nullable Holder<Aspect> aspect) {
        if (colour == null) {
            return new ItemStack(ModItems.CRIMSON_BANNER.get());
        }
        ItemStack banner = new ItemStack(ModItems.BANNER.get());
        banner.set(DataComponents.BASE_COLOR, colour);
        if (aspect != null) {
            banner.set(ModDataComponents.ESSENTIA.get(), aspect);
        }
        return banner;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Direction face = context.getClickedFace();
        BlockPos on = context.getClickedPos();
        if (face == Direction.DOWN || !level.getBlockState(on).isSolid()) {
            return InteractionResult.FAIL;
        }
        BlockPos at = on.relative(face);
        Player player = context.getPlayer();
        ItemStack held = context.getItemInHand();
        if (player != null && !player.mayUseItemAt(at, face, held) || !level.getBlockState(at).canBeReplaced()) {
            return InteractionResult.FAIL;
        }
        if (!level.isClientSide) {
            level.setBlockAndUpdate(at, ModBlocks.BANNER.get().defaultBlockState());
            if (level.getBlockEntity(at) instanceof BannerBlockEntity banner) {
                if (face == Direction.UP) {
                    float yaw = player == null ? 0.0F : player.getYRot();
                    banner.hang(Mth.floor((yaw + 180.0F) * 16.0F / 360.0F + 0.5F) & 15, false);
                } else {
                    banner.hang(switch (face) {
                        case NORTH -> 8;
                        case WEST -> 4;
                        case EAST -> 12;
                        default -> 0;
                    }, true);
                }
                banner.setColour(cult ? null : held.getOrDefault(DataComponents.BASE_COLOR, DyeColor.WHITE));
                banner.paint(cult ? null : held.get(ModDataComponents.ESSENTIA.get()));
            }
            held.consume(1, player);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public Component getName(ItemStack stack) {
        if (cult) {
            return super.getName(stack);
        }
        DyeColor colour = stack.getOrDefault(DataComponents.BASE_COLOR, DyeColor.WHITE);
        return Component.translatable(getDescriptionId() + "." + colour.getName());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        Holder<Aspect> aspect = stack.get(ModDataComponents.ESSENTIA.get());
        if (aspect != null) {
            lines.add(aspect.value().displayName());
        }
    }
}
