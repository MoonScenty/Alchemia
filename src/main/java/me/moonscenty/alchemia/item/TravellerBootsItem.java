package me.moonscenty.alchemia.item;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public class TravellerBootsItem extends ArmorItem {
    // added to vanilla's 0.6 step height
    public static final double STRIDES = 0.4;
    // original's value
    public static final int LASTS = 350;

    private final ItemAttributeModifiers worn;

    public TravellerBootsItem(Holder<ArmorMaterial> material, Properties properties) {
        super(material, Type.BOOTS, properties.durability(LASTS).rarity(Rarity.RARE));
        this.worn = ItemAttributeModifiers.builder()
                .add(Attributes.STEP_HEIGHT,
                        new AttributeModifier(Alchemia.id("traveller_stride"), STRIDES,
                                AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.FEET)
                .build();
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        ItemAttributeModifiers.Builder both = ItemAttributeModifiers.builder();
        super.getDefaultAttributeModifiers(stack).modifiers()
                .forEach(entry -> both.add(entry.attribute(), entry.modifier(), entry.slot()));
        worn.modifiers().forEach(entry -> both.add(entry.attribute(), entry.modifier(), entry.slot()));
        return both.build();
    }
}
