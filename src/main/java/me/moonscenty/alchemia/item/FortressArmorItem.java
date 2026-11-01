package me.moonscenty.alchemia.item;

import java.util.List;
import java.util.Optional;

import me.moonscenty.alchemia.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

public class FortressArmorItem extends ArmorItem {
    public FortressArmorItem(Holder<ArmorMaterial> material, Type type, int lasts, Properties properties) {
        super(material, type, properties.durability(type.getDurability(lasts)).rarity(Rarity.RARE));
    }

    public static Optional<HelmFitting> fitting(ItemStack stack) {
        return Optional.ofNullable(stack.get(ModDataComponents.HELM_FITTING.get()));
    }

    public static ItemStack wearing(ItemStack helm, HelmFitting fitting) {
        ItemStack worked = helm.copy();
        worked.set(ModDataComponents.HELM_FITTING.get(), fitting);
        return worked;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("item.alchemia.fortress.turns",
                        share(FortressArmourEvents.TURNS_BLOWS),
                        share(FortressArmourEvents.TURNS_BURNING),
                        share(FortressArmourEvents.TURNS_WORKINGS))
                .withStyle(ChatFormatting.AQUA));
        fitting(stack).ifPresent(worked -> lines.add(
                Component.translatable("item.alchemia.fortress.fitted",
                                Component.translatable("helm_fitting.alchemia." + worked.getSerializedName()))
                        .withStyle(ChatFormatting.LIGHT_PURPLE)));
    }

    private static String share(float of) {
        return String.format("%.4g", of * 100.0F);
    }
}
