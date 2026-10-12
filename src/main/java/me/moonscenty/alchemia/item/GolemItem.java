package me.moonscenty.alchemia.item;

import java.util.List;

import me.moonscenty.alchemia.golem.Golem;
import me.moonscenty.alchemia.golem.GolemDesign;
import me.moonscenty.alchemia.golem.GolemParts;
import me.moonscenty.alchemia.golem.GolemTrait;
import me.moonscenty.alchemia.registry.ModDataComponents;
import me.moonscenty.alchemia.registry.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * A golem as it comes off the press, or as a wand takes one back up: set it down on solid ground and it is yours.
 *
 * <p>It is tinted the colour of what the golem is made of, and says what that is and what the golem is like. A
 * smart one also says how far it has come on.
 */
public class GolemItem extends Item implements me.moonscenty.alchemia.golem.seal.SealDisplayer {
    public GolemItem(Properties properties) {
        super(properties);
    }

    public static GolemDesign design(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.GOLEM_DESIGN.get(), GolemDesign.BASIC);
    }

    /** One of this design, fresh off the press. */
    public static ItemStack of(GolemDesign design) {
        ItemStack stack = new ItemStack(me.moonscenty.alchemia.registry.ModItems.GOLEM.get());
        stack.set(ModDataComponents.GOLEM_DESIGN.get(), design);
        return stack;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos against = context.getClickedPos();
        Direction face = context.getClickedFace();
        Player player = context.getPlayer();
        if (player == null || !level.getBlockState(against).isSolid()) {
            return InteractionResult.PASS;
        }
        BlockPos at = against.relative(face);
        ItemStack stack = context.getItemInHand();
        if (!player.mayUseItemAt(at, face, stack)) {
            return InteractionResult.FAIL;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        Golem golem = ModEntities.GOLEM.get().create(level);
        if (golem == null) {
            return InteractionResult.FAIL;
        }
        golem.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 0.0F, 0.0F);
        golem.placed(player.getUUID(), design(stack), stack.getOrDefault(ModDataComponents.GOLEM_XP.get(), 0), at);
        if (level.addFreshEntity(golem)) {
            stack.consume(1, player);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        if (!stack.has(ModDataComponents.GOLEM_DESIGN.get())) {
            return;
        }
        GolemDesign design = design(stack);
        if (design.has(GolemTrait.SMART)) {
            if (design.rank() >= GolemDesign.TOP_RANK) {
                lines.add(Component.translatable("item.alchemia.golem.rank", design.rank())
                        .withStyle(ChatFormatting.GOLD));
            } else {
                lines.add(Component.translatable("item.alchemia.golem.rank", design.rank())
                        .withStyle(ChatFormatting.GOLD)
                        .append(Component.literal(" (" + stack.getOrDefault(ModDataComponents.GOLEM_XP.get(), 0)
                                + "/" + design.xpToNextRank() + ")").withStyle(ChatFormatting.DARK_GREEN)));
            }
        }
        GolemParts.Material material = design.material();
        lines.add(Component.translatable(material.translationKey()).withStyle(ChatFormatting.GREEN));
        for (GolemTrait trait : design.traits()) {
            lines.add(Component.literal("-").append(Component.translatable(trait.translationKey()))
                    .withStyle(ChatFormatting.BLUE));
        }
    }
}
