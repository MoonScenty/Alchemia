package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.research.ModResearch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The original's crucible families and the crystal farmer: their recipes and research are all there. */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class AlchemyFamiliesTests {
    private static final String TEMPLATE = "empty_32x40x32";

    @GameTest(template = TEMPLATE)
    public static void theFamiliesAreAllThere(GameTestHelper helper) {
        var recipes = helper.getLevel().getRecipeManager();
        for (String id : new String[] {"gunpowder_duplication", "prismarine_shard_duplication", "clay_ball_from_dirt",
                "lava_bucket_from_bucket", "bone_meal_from_bone", "string_from_wool", "iron_cluster_from_ore",
                "cinnabar_cluster_from_ore", "gold_nugget_transmutation", "flux_shard_from_shard",
                "air_crystal_from_shard", "flux_crystal_from_shard"}) {
            helper.assertTrue(recipes.byKey(Alchemia.id(id)).isPresent(), "no recipe " + id);
        }
        var research = helper.getLevel().registryAccess().registryOrThrow(ModResearch.ENTRY_KEY);
        for (String id : new String[] {"alchemical_duplication", "alchemical_manufacture", "entropic_processing",
                "taint_shard", "pure_metal", "trans_metal", "crystal_farmer"}) {
            helper.assertTrue(research.containsKey(ResourceKey.create(ModResearch.ENTRY_KEY, Alchemia.id(id))),
                    "no research " + id);
        }
        helper.succeed();
    }
}
