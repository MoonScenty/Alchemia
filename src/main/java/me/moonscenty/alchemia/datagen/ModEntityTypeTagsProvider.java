package me.moonscenty.alchemia.datagen;

import java.util.concurrent.CompletableFuture;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.registry.ModEntities;
import me.moonscenty.alchemia.registry.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModEntityTypeTagsProvider extends EntityTypeTagsProvider {
    public ModEntityTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
            ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, Alchemia.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // the original's list went by class, so every kind of zombie, spider and skeleton was on it
        tag(ModTags.Entities.CHAMPION_CANDIDATES)
                .add(EntityType.ZOMBIE, EntityType.HUSK, EntityType.DROWNED, EntityType.ZOMBIE_VILLAGER,
                        EntityType.ZOMBIFIED_PIGLIN, EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.BLAZE,
                        EntityType.ENDERMAN, EntityType.SKELETON, EntityType.STRAY, EntityType.WITHER_SKELETON,
                        EntityType.BOGGED, EntityType.WITCH)
                .add(ModEntities.CULTIST_KNIGHT.get(), ModEntities.CULTIST_CLERIC.get())
                .add(ModEntities.ANGRY_ZOMBIE.get(), ModEntities.FURIOUS_ZOMBIE.get(), ModEntities.TAINTACLE.get());

        // a crawler is a grub, and bane of arthropods knows it
        tag(EntityTypeTags.ARTHROPOD).add(ModEntities.TAINT_CRAWLER.get());
        tag(EntityTypeTags.UNDEAD).add(ModEntities.ANGRY_ZOMBIE.get(), ModEntities.FURIOUS_ZOMBIE.get());
        tag(EntityTypeTags.ZOMBIES).add(ModEntities.ANGRY_ZOMBIE.get(), ModEntities.FURIOUS_ZOMBIE.get());
    }
}
