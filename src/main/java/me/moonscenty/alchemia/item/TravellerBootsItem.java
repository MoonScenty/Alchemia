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

/**
 * Boots for getting somewhere.
 * <p>
 * They stop almost nothing. What they are worth is the walking: a step up a whole block without jumping, a push
 * along the ground, better hold in the air, and a long fall that hurts less than it should. Somebody who spends
 * their day walking between a works and a mine will not take them off for a suit that stops arrows.
 * <p>
 * The stride is an attribute here because the game has one; the original set the number on the player every
 * tick, which is the same thing said the only way that version could say it. The rest is still said every tick,
 * in {@link me.moonscenty.alchemia.item.TravellerBootsEvents}, because it answers to what the wearer is doing.
 */
public class TravellerBootsItem extends ArmorItem {
    /** How high a step they make of a block. Six tenths is what a pair of legs manages without them. */
    public static final double STRIDES = 0.4;
    /** How many swings of anything they take before giving out. The original's number. */
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

    /**
     * The stride, on top of whatever the armour itself is worth.
     * <p>
     * Built once and kept. This is asked for whenever anything wants to know what a pair is worth, which is
     * several times a tick for a wearer, and the answer never changes.
     */
    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        ItemAttributeModifiers.Builder both = ItemAttributeModifiers.builder();
        super.getDefaultAttributeModifiers(stack).modifiers()
                .forEach(entry -> both.add(entry.attribute(), entry.modifier(), entry.slot()));
        worn.modifiers().forEach(entry -> both.add(entry.attribute(), entry.modifier(), entry.slot()));
        return both.build();
    }
}
