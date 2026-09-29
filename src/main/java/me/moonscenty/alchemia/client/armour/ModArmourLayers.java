package me.moonscenty.alchemia.client.armour;

import java.util.Map;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;

/** The pieces of armour this mod draws as models, and the shapes each is built from. */
public final class ModArmourLayers {
    public static final ModelLayerLocation TRAVELLER_BOOTS =
            new ModelLayerLocation(Alchemia.id("traveller_boots"), "main");
    public static final ModelLayerLocation GOGGLES =
            new ModelLayerLocation(Alchemia.id("goggles_of_revealing"), "main");

    public static final WornExtensions BOOTS_DRAWN = new WornExtensions(TRAVELLER_BOOTS);
    public static final WornExtensions GOGGLES_DRAWN = new WornExtensions(GOGGLES);

    private ModArmourLayers() {
    }

    /** One boot to a leg; the pair in the drawing are mirrors, so the same shape serves for both. */
    public static LayerDefinition boots() {
        return WornShape.only(Map.of(
                "right_leg", TravellerBootsShape.boot(),
                "left_leg", TravellerBootsShape.boot()));
    }

    public static LayerDefinition goggles() {
        return WornShape.only(Map.of("head", GogglesShape.goggles()));
    }
}
