package me.moonscenty.alchemia.datagen;

import java.util.concurrent.CompletableFuture;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.registry.ModDamageTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/** What the primal arrows' kinds of harm are: projectiles all, two of them past armour and magic, one fire. */
public class ModDamageTypeTagsProvider extends TagsProvider<DamageType> {
    public ModDamageTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
            ExistingFileHelper existingFileHelper) {
        super(output, Registries.DAMAGE_TYPE, lookupProvider, Alchemia.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(DamageTypeTags.IS_PROJECTILE).add(ModDamageTypes.AIR_ARROW, ModDamageTypes.FIRE_ARROW,
                ModDamageTypes.ORDER_ARROW);
        tag(DamageTypeTags.BYPASSES_ARMOR).add(ModDamageTypes.AIR_ARROW, ModDamageTypes.ORDER_ARROW);
        tag(DamageTypeTags.WITCH_RESISTANT_TO).add(ModDamageTypes.AIR_ARROW, ModDamageTypes.ORDER_ARROW);
        tag(DamageTypeTags.IS_FIRE).add(ModDamageTypes.FIRE_ARROW);
    }
}
