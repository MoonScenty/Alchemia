package me.moonscenty.alchemia.aspect;

import me.moonscenty.alchemia.Alchemia;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

@EventBusSubscriber(modid = Alchemia.MODID)
public class ModDataMaps {
    public static final DataMapType<Item, AspectList> ITEM_ASPECTS = DataMapType
            .builder(Alchemia.id("aspects"), Registries.ITEM, AspectList.CODEC)
            .synced(AspectList.CODEC, false)
            .build();

    public static final DataMapType<EntityType<?>, AspectList> ENTITY_ASPECTS = DataMapType
            .builder(Alchemia.id("aspects"), Registries.ENTITY_TYPE, AspectList.CODEC)
            .synced(AspectList.CODEC, false)
            .build();

    @SubscribeEvent
    public static void register(RegisterDataMapTypesEvent event) {
        event.register(ITEM_ASPECTS);
        event.register(ENTITY_ASPECTS);
    }
}
