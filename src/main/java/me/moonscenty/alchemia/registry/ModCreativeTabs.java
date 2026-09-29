package me.moonscenty.alchemia.registry;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.item.PhialItem;
import me.moonscenty.alchemia.item.CrystallizedEssenceItem;
import me.moonscenty.alchemia.item.WandItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Alchemia.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = CREATIVE_MODE_TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.alchemia"))
            .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
            .icon(() -> ModItems.BALANCED_SHARD.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                ModItems.ITEMS.getEntries().forEach(item -> output.accept(item.get()));
                // a wand nobody has to charge, for trying out what a charged one is for
                output.accept(WandItem.brimming(WandItem.of(ModWandParts.SILVERWOOD, ModWandParts.VOID)));
                // a phial of every essentia. Filling one by hand means a smelter, a vessel and a working furnace
                // first, which is a long way round for somebody only wanting to see what the colours look like
                ModAspects.REGISTRY.holders().forEach(aspect -> {
                    output.accept(PhialItem.filled(ModItems.PHIAL.get().getDefaultInstance(), aspect));
                    output.accept(CrystallizedEssenceItem.of(aspect));
                });
            })
            .build());
}
