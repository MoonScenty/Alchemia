package me.moonscenty.alchemia.datagen;

import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.Optional;
import java.util.Map;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.crafting.ArcaneShapedRecipe;
import me.moonscenty.alchemia.crafting.ArcaneShapelessRecipe;
import me.moonscenty.alchemia.crafting.InfusionRecipe;
import me.moonscenty.alchemia.enchantment.InfusionEnchantment;
import me.moonscenty.alchemia.crafting.LabelRecipe;
import me.moonscenty.alchemia.crafting.CrucibleRecipe;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.crafting.ArcaneWandRecipe;
import me.moonscenty.alchemia.block.CrystalType;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.item.HelmFitting;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModTags;
import me.moonscenty.alchemia.registry.StoneSet;
import me.moonscenty.alchemia.registry.WoodSet;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;

public class ModRecipeProvider extends RecipeProvider {
    /** How badly an altar takes to sewing void metal onto cloth. The original's number for all three. */
    private static final int VOID_ROBE_UNSTABLE = 6;
    /** How badly an altar takes to rebuilding a finished suit into plate. The original's number. */
    private static final int FORTRESS_UNSTABLE = 3;
    /** How badly an altar takes to setting lenses into a visor. The original's number. */
    private static final int HELM_GOGGLES_UNSTABLE = 5;
    /** And to beating a face onto one, which it likes a good deal less. The original's number. */
    private static final int MASK_UNSTABLE = 8;
    /** How badly an altar takes to binding a primal into a tool, and to beating four tools into one. */
    private static final int ELEMENTAL_UNSTABLE = 1;
    private static final int CRUSHER_UNSTABLE = 6;
    /** How badly an altar takes to an aura totem. The original's number. */
    private static final int TOTEM_UNSTABLE = 5;

    /** A totem pole: silverwood above and below, greatwood banding it, a shard in the middle. */
    private void pole(RecipeOutput output, String name, String research,
            net.neoforged.neoforge.registries.DeferredBlock<?> pole, Ingredient shard, AspectList cost) {
        arcane(output, name, research, new ItemStack(pole.get()), cost,
                Map.of('N', shard,
                        'W', Ingredient.of(ModBlocks.SILVERWOOD.planks()),
                        'G', Ingredient.of(ModBlocks.GREATWOOD.planks())),
                "WGW", "WNW", "WGW");
    }

    /** How badly an altar takes to the recharge pedestal. The original's number. */
    private static final int PEDESTAL_UNSTABLE = 3;
    /** How badly an altar takes to the two working lamps, and to the jug. The original's numbers. */
    private static final int LAMP_UNSTABLE = 4;
    private static final int JUG_UNSTABLE = 1;
    /** How badly an altar takes to growing a brain into a clockwork mind. The original's number. */
    private static final int BIOALCHEMIC_UNSTABLE = 4;
    /** How badly an altar takes to giving a focus a mind of its own. The original's number. */
    private static final int AUTOCASTER_UNSTABLE = 5;

    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        baubles(output);
        cook(output, "amber_from_ore", Ingredient.of(ModTags.Items.ORES_AMBER), has(ModTags.Items.ORES_AMBER), new ItemStack(ModItems.AMBER.get()));
        cook(output, "quicksilver_from_ore", Ingredient.of(ModTags.Items.ORES_CINNABAR), has(ModTags.Items.ORES_CINNABAR), new ItemStack(ModItems.QUICKSILVER.get()));
        cook(output, "quicksilver_from_raw_cinnabar", Ingredient.of(ModItems.RAW_CINNABAR), has(ModItems.RAW_CINNABAR), new ItemStack(ModItems.QUICKSILVER.get()));

        compress(output, ModItems.ALCHEMIUM_NUGGET, ModTags.Items.NUGGETS_ALCHEMIUM, ModItems.ALCHEMIUM_INGOT, ModTags.Items.INGOTS_ALCHEMIUM);
        compress(output, ModItems.ALCHEMIUM_INGOT, ModTags.Items.INGOTS_ALCHEMIUM, ModBlocks.ALCHEMIUM_BLOCK, ModTags.Items.STORAGE_BLOCKS_ALCHEMIUM);
        compress(output, ModItems.BRASS_NUGGET, ModTags.Items.NUGGETS_BRASS, ModItems.BRASS_INGOT, ModTags.Items.INGOTS_BRASS);
        compress(output, ModItems.BRASS_INGOT, ModTags.Items.INGOTS_BRASS, ModBlocks.BRASS_BLOCK, ModTags.Items.STORAGE_BLOCKS_BRASS);
        compress(output, ModItems.QUICKSILVER_DROP, ModTags.Items.NUGGETS_QUICKSILVER, ModItems.QUICKSILVER, ModTags.Items.GEMS_QUICKSILVER);

        plate(output, ModItems.ALCHEMIUM_PLATE, ModTags.Items.INGOTS_ALCHEMIUM);
        plate(output, ModItems.BRASS_PLATE, ModTags.Items.INGOTS_BRASS);
        plate(output, ModItems.IRON_PLATE, Tags.Items.INGOTS_IRON);
        plate(output, ModItems.VOID_PLATE, ModTags.Items.INGOTS_VOID);
        gear(output, ModItems.ALCHEMIUM_GEAR, ModTags.Items.NUGGETS_ALCHEMIUM);
        gear(output, ModItems.BRASS_GEAR, ModTags.Items.NUGGETS_BRASS);
        gear(output, ModItems.VOID_GEAR, ModTags.Items.NUGGETS_VOID);
        // a stone table, which a golem press is built round
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.STONE_TABLE.get())
                .pattern("SSS").pattern("W W")
                .define('S', Items.STONE_SLAB)
                .define('W', Tags.Items.STONES)
                .unlockedBy("has_input", has(Items.STONE_SLAB))
                .save(output);

        // a balanced shard put in a furnace comes out as salis mundus, exactly as in the original
        cook(output, "salis_mundus", Ingredient.of(ModItems.BALANCED_SHARD), has(ModItems.BALANCED_SHARD),
                new ItemStack(ModItems.SALIS_MUNDUS.get()));

        wand(output);
        arcane(output);
        crucible(output);
        metalGear(output);
        distillery(output);
        voidMetal(output);
        infusion(output);
        enchantments(output);

        // arcane stone itself is shaped on the arcane workbench; these are what it is worked into afterwards
        quadrupleFrom(output, ModBlocks.ARCANE_STONE_BRICKS.block(), ModBlocks.ARCANE_STONE.block());
        for (StoneSet set : ModBlocks.STONE_SETS) {
            stairBuilder(set.stairs(), Ingredient.of(set.block())).unlockedBy("has_input", has(set.block())).save(output);
            slab(output, RecipeCategory.BUILDING_BLOCKS, set.slab(), set.block());
            stonecutting(output, set.stairs(), set.block(), 1);
            stonecutting(output, set.slab(), set.block(), 2);
        }
        stonecutting(output, ModBlocks.ARCANE_STONE_BRICKS.block(), ModBlocks.ARCANE_STONE.block(), 1);

        nodeStabilizer(output);

        // Amber blocks and bricks are cut back and forth freely, as in the original
        compress(output, ModItems.AMBER, ModTags.Items.GEMS_AMBER, ModBlocks.AMBER_BLOCK, ModTags.Items.STORAGE_BLOCKS_AMBER);
        quadrupleFrom(output, ModBlocks.AMBER_BRICKS, ModBlocks.AMBER_BLOCK);
        quadrupleFrom(output, ModBlocks.AMBER_BLOCK, ModBlocks.AMBER_BRICKS);

        // Both blooms are steeped down into the essence they carry
        distill(output, ModBlocks.SHIMMERLEAF, ModItems.QUICKSILVER);
        distill(output, ModBlocks.CINDERPEARL, Items.BLAZE_POWDER);

        for (WoodSet wood : ModBlocks.WOODS) {
            planksFromLogs(output, wood.planks(), wood.logsItemTag(), 4);
            stairBuilder(wood.stairs(), Ingredient.of(wood.planks())).group("wooden_stairs")
                    .unlockedBy("has_planks", has(wood.planks())).save(output);
            slab(output, RecipeCategory.BUILDING_BLOCKS, wood.slab(), wood.planks());
        }

        cluster(output, "iron_ingot", ModItems.IRON_CLUSTER, Items.IRON_INGOT);
        cluster(output, "gold_ingot", ModItems.GOLD_CLUSTER, Items.GOLD_INGOT);
        cluster(output, "copper_ingot", ModItems.COPPER_CLUSTER, Items.COPPER_INGOT);
        cluster(output, "quicksilver", ModItems.CINNABAR_CLUSTER, ModItems.QUICKSILVER);
    }

    /**
     * The vanilla helper saves under the minecraft namespace, so this one spells the id out.
     */
    private static void stonecutting(RecipeOutput output, ItemLike result, ItemLike material, int count) {
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(material), RecipeCategory.BUILDING_BLOCKS, result, count)
                .unlockedBy("has_input", has(material))
                .save(output, Alchemia.id(getConversionRecipeName(result, material) + "_stonecutting"));
    }

    /** Four blocks in a square become four of another, so nothing is lost either way. */
    private static void quadrupleFrom(RecipeOutput output, ItemLike result, ItemLike input) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, result, 4)
                .pattern("##").pattern("##")
                .define('#', input)
                .unlockedBy("has_input", has(input))
                .save(output, Alchemia.id(BuiltInRegistries.ITEM.getKey(result.asItem()).getPath()
                        + "_from_" + BuiltInRegistries.ITEM.getKey(input.asItem()).getPath()));
    }

    /** One plant yields one of whatever it is steeped with. */
    private static void distill(RecipeOutput output, ItemLike plant, ItemLike result) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, result)
                .requires(plant)
                .unlockedBy("has_input", has(plant))
                .save(output, Alchemia.id(BuiltInRegistries.ITEM.getKey(result.asItem()).getPath()
                        + "_from_" + BuiltInRegistries.ITEM.getKey(plant.asItem()).getPath()));
    }

    /**
     * Arcane stone around a balanced shard, braced with alchemium. What holds a node still is mostly a matter of
     * having something steady to hold it against.
     */
    private static void nodeStabilizer(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.NODE_STABILIZER.get())
                .pattern(" P ")
                .pattern("PSP")
                .pattern("BBB")
                .define('P', ModItems.ALCHEMIUM_PLATE.get())
                .define('S', ModItems.BALANCED_SHARD.get())
                .define('B', ModBlocks.ARCANE_STONE_BRICKS.block().get())
                .unlockedBy("has_shard", has(ModItems.BALANCED_SHARD.get()))
                .save(output, Alchemia.id("node_stabilizer"));
    }

    /** Nine small items pack into one big item and back. */
    private static void compress(RecipeOutput output, ItemLike small, TagKey<Item> smallTag, ItemLike big, TagKey<Item> bigTag) {
        String smallName = BuiltInRegistries.ITEM.getKey(small.asItem()).getPath();
        String bigName = BuiltInRegistries.ITEM.getKey(big.asItem()).getPath();
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, big)
                .pattern("###").pattern("###").pattern("###")
                .define('#', smallTag)
                .unlockedBy("has_input", has(smallTag))
                .save(output, Alchemia.id(bigName + "_from_" + smallName));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, small, 9)
                .requires(bigTag)
                .unlockedBy("has_input", has(bigTag))
                .save(output, Alchemia.id(smallName + "_from_" + bigName));
    }

    private static void plate(RecipeOutput output, ItemLike plate, TagKey<Item> ingot) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, plate, 3)
                .pattern("###")
                .define('#', ingot)
                .unlockedBy("has_input", has(ingot))
                .save(output);
    }

    private static void gear(RecipeOutput output, ItemLike gear, TagKey<Item> nugget) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, gear)
                .pattern("###").pattern("#I#").pattern("###")
                .define('#', nugget)
                .define('I', Tags.Items.INGOTS_IRON)
                .unlockedBy("has_input", has(nugget))
                .save(output);
    }

    /** Clusters are purified ore: they smelt into twice the usual amount. */
    private static void cluster(RecipeOutput output, String resultName, ItemLike cluster, ItemLike result) {
        cook(output, resultName + "_from_cluster", Ingredient.of(cluster), has(cluster), new ItemStack(result, 2));
    }

    /** Registers both the furnace and the blast furnace version of a recipe. */
    private static void cook(RecipeOutput output, String name, Ingredient input, Criterion<?> unlockedBy, ItemStack result) {
        SimpleCookingRecipeBuilder.smelting(input, RecipeCategory.MISC, result, 1.0F, 200)
                .unlockedBy("has_input", unlockedBy)
                .save(output, Alchemia.id(name + "_smelting"));
        SimpleCookingRecipeBuilder.blasting(input, RecipeCategory.MISC, result, 1.0F, 100)
                .unlockedBy("has_input", unlockedBy)
                .save(output, Alchemia.id(name + "_blasting"));
    }

    /**
     * The first wand, and the caps it is made with.
     * <p>
     * Both are worked at a plain bench, as they were in the original, and they have to be: an arcane workbench takes
     * its price out of a wand, so the first one cannot be made at one. Every other pairing of rod and cap is put
     * together at the arcane workbench by {@code alchemia:arcane_wand}, which is written in code because nine rods
     * against five caps is forty-five results and none of them is worth a file.
     * <p>
     * A wand with nothing written on it is a wooden rod with iron caps, so this recipe needs no components: leaving
     * them off says the same thing and keeps the item stacking with one built at the bench.
     */
    private void wand(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.WAND_CAPS.get("iron").get())
                .pattern("NNN")
                .pattern("N N")
                .define('N', Tags.Items.NUGGETS_IRON)
                .unlockedBy("has_nugget", has(Tags.Items.NUGGETS_IRON))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.WAND.get())
                .pattern("  C")
                .pattern(" S ")
                .pattern("C  ")
                .define('C', ModItems.WAND_CAPS.get("iron").get())
                .define('S', Tags.Items.RODS_WOODEN)
                .unlockedBy("has_cap", has(ModItems.WAND_CAPS.get("iron").get()))
                .save(output);

        SpecialRecipeBuilder.special(category -> ArcaneWandRecipe.INSTANCE)
                .save(output, Alchemia.id("arcane_wand").toString());
    }

    /**
     * What is shaped on the arcane workbench, and what it costs in vis.
     * <p>
     * The prices are the original's: eight times what the piece is reckoned to be worth, split across the primals
     * that go into the work. A cap is order, fire and air; a rod is entropy alone, since cutting a wand out of a
     * log is mostly undoing what the tree made.
     */
    private void arcane(RecipeOutput output) {
        arcane(output, "wand_cap_gold", new ItemStack(ModItems.WAND_CAPS.get("gold").get()),
                AspectList.of(ModAspects.ORDER, 24).add(ModAspects.FIRE, 24).add(ModAspects.AIR, 24),
                Map.of('N', Ingredient.of(Tags.Items.NUGGETS_GOLD)), "NNN", "N N");

        arcane(output, "wand_cap_brass", new ItemStack(ModItems.WAND_CAPS.get("brass").get()),
                AspectList.of(ModAspects.ORDER, 24).add(ModAspects.FIRE, 24).add(ModAspects.AIR, 24),
                Map.of('N', Ingredient.of(ModTags.Items.NUGGETS_BRASS)), "NNN", "N N");

        // what the workbench makes of these two is a casting, not a cap; the altar finishes them
        arcane(output, "wand_cap_alchemium_inert", new ItemStack(ModItems.INERT_CAPS.get("alchemium").get()),
                AspectList.of(ModAspects.ORDER, 48).add(ModAspects.FIRE, 48).add(ModAspects.AIR, 48),
                Map.of('N', Ingredient.of(ModTags.Items.NUGGETS_ALCHEMIUM)), "NNN", "N N");

        arcane(output, "wand_rod_greatwood", new ItemStack(ModItems.WAND_RODS.get("greatwood").get()),
                AspectList.of(ModAspects.ENTROPY, 24),
                Map.of('G', Ingredient.of(ModBlocks.GREATWOOD.log())), " G", "G ");

        arcane(output, "arcane_stone", new ItemStack(ModBlocks.ARCANE_STONE.block(), 9),
                AspectList.of(ModAspects.EARTH, 5).add(ModAspects.FIRE, 5),
                Map.of('S', Ingredient.of(Tags.Items.STONES), 'C', Ingredient.of(ModTags.Items.SHARDS)),
                "SSS", "SCS", "SSS");

        // the cloth everything soft is cut from: string round wool, and all six primals to work it
        arcane(output, "enchanted_fabric", "enchanted_fabric", new ItemStack(ModItems.ENCHANTED_FABRIC.get()),
                AspectList.of(ModAspects.AIR, 5).add(ModAspects.FIRE, 5).add(ModAspects.WATER, 5)
                        .add(ModAspects.EARTH, 5).add(ModAspects.ORDER, 5).add(ModAspects.ENTROPY, 5),
                Map.of('S', Ingredient.of(Items.STRING), 'C', Ingredient.of(ItemTags.WOOL)),
                " S ", "SCS", " S ");

        // the cloth robe. No hood: the original had none, and the void robe is where a hood comes from
        arcane(output, "cloth_chest", "robes", new ItemStack(ModItems.ROBES.get("cloth_chest").get()),
                AspectList.of(ModAspects.AIR, 30),
                Map.of('I', Ingredient.of(ModItems.ENCHANTED_FABRIC)), "I I", "III", "III");
        arcane(output, "cloth_legs", "robes", new ItemStack(ModItems.ROBES.get("cloth_legs").get()),
                AspectList.of(ModAspects.WATER, 25),
                Map.of('I', Ingredient.of(ModItems.ENCHANTED_FABRIC)), "III", "I I", "I I");
        arcane(output, "cloth_boots", "robes", new ItemStack(ModItems.ROBES.get("cloth_boots").get()),
                AspectList.of(ModAspects.EARTH, 20),
                Map.of('I', Ingredient.of(ModItems.ENCHANTED_FABRIC)), "I I", "I I");

        // the pouch: a leather bag with a brass buckle and a gold clasp. The original held a mundane girdle in
        // the middle, which is a plain belt
        arcane(output, "focus_pouch", new ItemStack(ModItems.FOCUS_POUCH.get()),
                AspectList.of(ModAspects.EARTH, 25).add(ModAspects.ORDER, 25).add(ModAspects.ENTROPY, 25),
                Map.of('L', Ingredient.of(Items.LEATHER),
                        'G', Ingredient.of(Items.GOLD_INGOT),
                        'B', Ingredient.of(ModItems.MUNDANE_BELT)),
                "LGL", "LBL", "LLL");

        // goggles: two alchemometers looking through brass, held on with leather
        arcane(output, "goggles_of_revealing", "goggles",
                new ItemStack(ModItems.GOGGLES.get()),
                AspectList.of(ModAspects.AIR, 25).add(ModAspects.FIRE, 25).add(ModAspects.WATER, 25)
                        .add(ModAspects.EARTH, 25).add(ModAspects.ENTROPY, 25).add(ModAspects.ORDER, 25),
                Map.of('T', Ingredient.of(ModItems.ALCHEMOMETER),
                        'G', Ingredient.of(ModTags.Items.INGOTS_BRASS),
                        'L', Ingredient.of(Items.LEATHER)),
                "LGL", "L L", "TGT");

        // the bench a focus is worked on. The original's shape and its hundred and fifty of each primal, with
        // the charm of all six at its heart; worked stone stands for the stone table the original set into the
        // bottom, which is a piece of furniture this mod does not have
        arcane(output, "focal_manipulator", "focal_manipulator",
                new ItemStack(ModBlocks.FOCAL_MANIPULATOR.get()),
                AspectList.of(ModAspects.FIRE, 150).add(ModAspects.AIR, 150).add(ModAspects.ENTROPY, 150)
                        .add(ModAspects.EARTH, 150).add(ModAspects.WATER, 150).add(ModAspects.ORDER, 150),
                Map.of('I', Ingredient.of(Items.IRON_INGOT),
                        'Q', Ingredient.of(ModBlocks.ARCANE_STONE.slab()),
                        'S', Ingredient.of(ModBlocks.ARCANE_STONE.block()),
                        'T', Ingredient.of(ModBlocks.ARCANE_STONE_BRICKS.block()),
                        'G', Ingredient.of(Items.GOLD_INGOT),
                        'P', Ingredient.of(ModItems.PRIMAL_CHARM)),
                "IQI", "SPS", "GTG");

        // the parts devices are built from. All the original's, recipe for recipe
        shapeless(output, "primal_charm", "primal_charm", new ItemStack(ModItems.PRIMAL_CHARM.get()),
                AspectList.of(ModAspects.EARTH, 25).add(ModAspects.FIRE, 25).add(ModAspects.AIR, 25)
                        .add(ModAspects.WATER, 25).add(ModAspects.ORDER, 25).add(ModAspects.ENTROPY, 25),
                Ingredient.of(Items.GOLD_INGOT), Ingredient.of(ModItems.BALANCED_SHARD),
                Ingredient.of(ModItems.SHARDS.get(CrystalType.AIR)), Ingredient.of(ModItems.SHARDS.get(CrystalType.FIRE)),
                Ingredient.of(ModItems.SHARDS.get(CrystalType.WATER)),
                Ingredient.of(ModItems.SHARDS.get(CrystalType.EARTH)),
                Ingredient.of(ModItems.SHARDS.get(CrystalType.ORDER)),
                Ingredient.of(ModItems.SHARDS.get(CrystalType.ENTROPY)));
        shapeless(output, "mirrored_glass", "primal_charm", new ItemStack(ModItems.MIRRORED_GLASS.get()),
                AspectList.of(ModAspects.FIRE, 25).add(ModAspects.EARTH, 25),
                Ingredient.of(ModItems.QUICKSILVER), Ingredient.of(net.neoforged.neoforge.common.Tags.Items.GLASS_PANES));
        arcane(output, "morphic_resonator", "crucible", new ItemStack(ModItems.MORPHIC_RESONATOR.get()),
                AspectList.of(ModAspects.ORDER, 50).add(ModAspects.ENTROPY, 50),
                Map.of('G', Ingredient.of(net.neoforged.neoforge.common.Tags.Items.GLASS_PANES),
                        'B', Ingredient.of(ModItems.BRASS_PLATE),
                        'F', Ingredient.of(ModItems.SHARDS.get(CrystalType.FLUX))),
                " G ", "BFB", " G ");
        arcane(output, "clockwork_mind", "clockwork_mind", new ItemStack(ModItems.CLOCKWORK_MIND.get()),
                AspectList.of(ModAspects.FIRE, 10).add(ModAspects.WATER, 10).add(ModAspects.ORDER, 10),
                Map.of('P', Ingredient.of(net.neoforged.neoforge.common.Tags.Items.GLASS_PANES),
                        'G', Ingredient.of(ModItems.BRASS_GEAR),
                        'B', Ingredient.of(ModItems.BRASS_PLATE),
                        'C', Ingredient.of(Items.COMPARATOR)),
                "PPP", "PGP", "BCB");
        // golemancy: eyes for a golem, and the bell that tells golems what to do
        arcane(output, "vision_module", "golem_vision", new ItemStack(ModItems.VISION_MODULE.get()),
                AspectList.of(ModAspects.WATER, 25).add(ModAspects.AIR, 25),
                Map.of('B', Ingredient.of(Items.GLASS_BOTTLE),
                        'E', Ingredient.of(Items.FERMENTED_SPIDER_EYE),
                        'P', Ingredient.of(ModItems.BRASS_PLATE),
                        'G', Ingredient.of(ModItems.BRASS_GEAR)),
                "B B", "E E", "PGP");
        arcane(output, "golem_bell", "control_seals", new ItemStack(ModItems.GOLEM_BELL.get()),
                AspectList.of(ModAspects.ORDER, 10).add(ModAspects.AIR, 10),
                Map.of('S', Ingredient.of(Tags.Items.RODS_WOODEN),
                        'Q', Ingredient.of(Tags.Items.GEMS_QUARTZ)),
                " QQ", " QQ", "S  ");
        // what makes a golem want to fight: blaze powder behind glass, on brass
        arcane(output, "aggression_module", "seal_guard", new ItemStack(ModItems.AGGRESSION_MODULE.get()),
                AspectList.of(ModAspects.FIRE, 25).add(ModAspects.ENTROPY, 25),
                Map.of('R', Ingredient.of(net.neoforged.neoforge.common.Tags.Items.GLASS_PANES),
                        'T', Ingredient.of(Items.BLAZE_POWDER),
                        'P', Ingredient.of(ModItems.BRASS_PLATE),
                        'G', Ingredient.of(ModItems.BRASS_GEAR)),
                " R ", "RTR", "PGP");
        // blank seals, three at a time: clay, tallow, a red dye and a nitor
        shapeless(output, "seal_blank", "control_seals", new ItemStack(ModItems.SEAL_BLANK.get(), 3),
                AspectList.of(ModAspects.FIRE, 10).add(ModAspects.ORDER, 10),
                Ingredient.of(Items.CLAY_BALL), Ingredient.of(ModItems.TALLOW),
                Ingredient.of(net.neoforged.neoforge.common.Tags.Items.DYES_RED), Ingredient.of(ModTags.Items.NITOR));
        // the small devices, all the original's
        arcane(output, "arcane_lamp", "arcane_lamp", new ItemStack(ModBlocks.ARCANE_LAMP.get()),
                AspectList.of(ModAspects.FIRE, 25).add(ModAspects.AIR, 25),
                Map.of('S', Ingredient.of(Items.DAYLIGHT_DETECTOR),
                        'I', Ingredient.of(Items.IRON_INGOT),
                        'A', Ingredient.of(ModBlocks.AMBER_BLOCK.get()),
                        'N', Ingredient.of(ModTags.Items.NITOR)),
                " S ", "IAI", " N ");
        arcane(output, "hungry_chest", "hungry_chest", new ItemStack(ModBlocks.HUNGRY_CHEST.get()),
                AspectList.of(ModAspects.AIR, 15).add(ModAspects.ORDER, 15).add(ModAspects.ENTROPY, 15),
                Map.of('W', Ingredient.of(ModBlocks.GREATWOOD.planks()),
                        'T', Ingredient.of(net.minecraft.tags.ItemTags.WOODEN_TRAPDOORS)),
                "WTW", "W W", "WWW");

        // the levitator, the relay and the ear, all the original's; a rotten brain stands in for the zombie's
        arcane(output, "levitator", "levitator", new ItemStack(ModBlocks.LEVITATOR.get()),
                AspectList.of(ModAspects.AIR, 20).add(ModAspects.EARTH, 15),
                Map.of('W', Ingredient.of(net.minecraft.tags.ItemTags.PLANKS),
                        'E', Ingredient.of(ModItems.SHARDS.get(CrystalType.AIR)),
                        'B', Ingredient.of(Items.IRON_INGOT),
                        'N', Ingredient.of(ModTags.Items.NITOR),
                        'G', Ingredient.of(ModItems.BRASS_GEAR)),
                "WEW", "BNB", "WGW");
        arcane(output, "redstone_relay", "redstone_relay", new ItemStack(ModBlocks.REDSTONE_RELAY.get()),
                AspectList.of(ModAspects.FIRE, 5).add(ModAspects.ORDER, 5),
                Map.of('T', Ingredient.of(Items.REDSTONE_TORCH),
                        'G', Ingredient.of(ModItems.BRASS_GEAR),
                        'C', Ingredient.of(ModItems.SHARDS.get(CrystalType.ORDER)),
                        'S', Ingredient.of(Items.SMOOTH_STONE_SLAB)),
                "TGC", "SSS");
        arcane(output, "arcane_ear", "arcane_ear", new ItemStack(ModBlocks.ARCANE_EAR.get()),
                AspectList.of(ModAspects.AIR, 10).add(ModAspects.ORDER, 5),
                Map.of('P', Ingredient.of(ModItems.BRASS_PLATE),
                        'B', Ingredient.of(ModItems.ZOMBIE_BRAIN),
                        'W', Ingredient.of(net.minecraft.tags.ItemTags.WOODEN_SLABS),
                        'R', Ingredient.of(Items.REDSTONE)),
                "PBP", " P ", "WRW");

        // the totem poles: silverwood banded with greatwood round a shard -- earth for the inner, air for the outer,
        // and balance for the stabilising one
        pole(output, "aura_totem_pole_inner", "aura_totem", ModBlocks.AURA_TOTEM_POLE_INNER,
                Ingredient.of(ModItems.SHARDS.get(CrystalType.EARTH)),
                AspectList.of(ModAspects.AIR, 50).add(ModAspects.EARTH, 125).add(ModAspects.ORDER, 75));
        pole(output, "aura_totem_pole_outer", "aura_totem", ModBlocks.AURA_TOTEM_POLE_OUTER,
                Ingredient.of(ModItems.SHARDS.get(CrystalType.AIR)),
                AspectList.of(ModAspects.AIR, 125).add(ModAspects.EARTH, 50).add(ModAspects.ORDER, 125));
        pole(output, "aura_totem_pole_pure", "aura_totem_pole_pure", ModBlocks.AURA_TOTEM_POLE_PURE,
                Ingredient.of(ModItems.BALANCED_SHARD),
                AspectList.of(ModAspects.AIR, 125).add(ModAspects.EARTH, 125).add(ModAspects.ORDER, 250));

        // the dioptra: a pair of goggles looking up through arcane stone, a slab of it for the crystal to sit in
        arcane(output, "dioptra", "dioptra", new ItemStack(ModBlocks.DIOPTRA.get()),
                AspectList.of(ModAspects.AIR, 25).add(ModAspects.EARTH, 25).add(ModAspects.ORDER, 25),
                Map.of('A', Ingredient.of(ModBlocks.ARCANE_STONE.block()),
                        'P', Ingredient.of(ModBlocks.ARCANE_STONE.slab()),
                        'I', Ingredient.of(Items.IRON_INGOT),
                        'G', Ingredient.of(ModItems.GOGGLES)),
                "APA", "IGI", "AAA");

        // the spa: a jar and a gear in arcane stone, a quartz rim, and bars over the inlet
        arcane(output, "spa", "spa", new ItemStack(ModBlocks.SPA.get()),
                AspectList.of(ModAspects.WATER, 25).add(ModAspects.ORDER, 15).add(ModAspects.EARTH, 15),
                Map.of('Q', Ingredient.of(Items.QUARTZ_BLOCK),
                        'I', Ingredient.of(Items.IRON_BARS),
                        'S', Ingredient.of(ModBlocks.ARCANE_STONE.block()),
                        'J', Ingredient.of(ModBlocks.JAR),
                        'P', Ingredient.of(ModItems.BRASS_GEAR)),
                "QIQ", "SJS", "SPS");

        // bellows: two boards a side, the hide between them, a shard of air to blow with and an iron nozzle
        arcane(output, "bellows", "bellows", new ItemStack(ModBlocks.BELLOWS.get()),
                AspectList.of(ModAspects.AIR, 25).add(ModAspects.ORDER, 10),
                Map.of('W', Ingredient.of(net.minecraft.tags.ItemTags.PLANKS),
                        'L', Ingredient.of(Items.LEATHER),
                        'C', Ingredient.of(ModItems.SHARDS.get(CrystalType.AIR)),
                        'I', Ingredient.of(Items.IRON_INGOT)),
                "WW ", "LCI", "WW ");

        // the two altar stones: the same eight stones round a block of gold, with light or with fuel packed in
        arcane(output, "infusion_speed_stone", new ItemStack(ModBlocks.INFUSION_SPEED_STONE.get()),
                AspectList.of(ModAspects.AIR, 250).add(ModAspects.ORDER, 250).add(ModAspects.ENTROPY, 250),
                Map.of('S', Ingredient.of(ModBlocks.ARCANE_STONE.block()),
                        'N', Ingredient.of(ModTags.Items.NITOR),
                        'G', Ingredient.of(Blocks.GOLD_BLOCK)),
                "SNS", "NGN", "SNS");
        arcane(output, "infusion_cost_stone", new ItemStack(ModBlocks.INFUSION_COST_STONE.get()),
                AspectList.of(ModAspects.WATER, 250).add(ModAspects.ORDER, 250).add(ModAspects.ENTROPY, 250),
                Map.of('S', Ingredient.of(ModBlocks.ARCANE_STONE.block()),
                        'A', Ingredient.of(ModItems.ALUMENTUM),
                        'G', Ingredient.of(Blocks.GOLD_BLOCK)),
                "SAS", "AGA", "SAS");
    }

    /** A crystal of one aspect's essence, as a ring ingredient. */
    private static Ingredient essence(net.minecraft.core.Holder<me.moonscenty.alchemia.aspect.Aspect> aspect) {
        return net.neoforged.neoforge.common.crafting.DataComponentIngredient.of(false,
                me.moonscenty.alchemia.item.CrystallizedEssenceItem.of(aspect));
    }

    /** A potion of exactly this kind, as a ring ingredient. */
    private static Ingredient potion(net.minecraft.world.item.Item bottle,
            net.minecraft.core.Holder<net.minecraft.world.item.alchemy.Potion> potion) {
        return net.neoforged.neoforge.common.crafting.DataComponentIngredient.of(false,
                net.minecraft.world.item.alchemy.PotionContents.createItemStack(bottle, potion));
    }

    /**
     * The baubles: the plain and fancy ones at a crafting table, the rest at the altar, and runic augmentation,
     * which hardens whatever runic gear it is given.
     */
    private void baubles(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MUNDANE_AMULET.get())
                .pattern(" S ").pattern("S S").pattern(" I ")
                .define('S', Items.STRING).define('I', ModTags.Items.INGOTS_BRASS)
                .unlockedBy("has_input", has(ModTags.Items.INGOTS_BRASS)).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MUNDANE_RING.get())
                .pattern("NNN").pattern("N N").pattern("NNN")
                .define('N', ModTags.Items.NUGGETS_BRASS)
                .unlockedBy("has_input", has(ModTags.Items.NUGGETS_BRASS)).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MUNDANE_BELT.get())
                .pattern(" L ").pattern("L L").pattern(" I ")
                .define('L', Items.LEATHER).define('I', ModTags.Items.INGOTS_BRASS)
                .unlockedBy("has_input", has(ModTags.Items.INGOTS_BRASS)).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FANCY_AMULET.get())
                .pattern(" S ").pattern("SGS").pattern(" I ")
                .define('S', Items.STRING).define('G', Items.DIAMOND).define('I', Items.GOLD_INGOT)
                .unlockedBy("has_input", has(Items.DIAMOND)).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FANCY_RING.get())
                .pattern("NGN").pattern("N N").pattern("NNN")
                .define('N', Items.GOLD_NUGGET).define('G', Items.DIAMOND)
                .unlockedBy("has_input", has(Items.DIAMOND)).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FANCY_BELT.get())
                .pattern(" L ").pattern("LGL").pattern(" I ")
                .define('L', Items.LEATHER).define('G', Items.DIAMOND).define('I', Items.GOLD_INGOT)
                .unlockedBy("has_input", has(Items.DIAMOND)).save(output);
        // the original made its treat of three kinds of meat chunk, which this mod does not have; three cooked
        // meats stand for them
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, ModItems.TRIPLE_MEAT_TREAT.get())
                .requires(Items.SUGAR).requires(Items.COOKED_BEEF).requires(Items.COOKED_PORKCHOP)
                .requires(Items.COOKED_CHICKEN)
                .unlockedBy("has_input", has(Items.SUGAR)).save(output);

        Ingredient charm = Ingredient.of(ModItems.PRIMAL_CHARM);
        Ingredient amber = Ingredient.of(ModItems.AMBER);
        Ingredient fabric = Ingredient.of(ModItems.ENCHANTED_FABRIC);
        Ingredient nitor = Ingredient.of(ModTags.Items.NITOR);
        Ingredient scribing = Ingredient.of(ModItems.SCRIBING_TOOLS);
        Ingredient balanced = Ingredient.of(ModItems.BALANCED_SHARD);
        Ingredient air = Ingredient.of(ModItems.SHARDS.get(me.moonscenty.alchemia.block.CrystalType.AIR).get());
        Ingredient earth = Ingredient.of(ModItems.SHARDS.get(me.moonscenty.alchemia.block.CrystalType.EARTH).get());
        Ingredient fire = Ingredient.of(ModItems.SHARDS.get(me.moonscenty.alchemia.block.CrystalType.FIRE).get());
        Ingredient water = Ingredient.of(ModItems.SHARDS.get(me.moonscenty.alchemia.block.CrystalType.WATER).get());

        infusion(output, "thaumostatic_girdle", Ingredient.of(ModItems.MUNDANE_BELT),
                List.of(air, Ingredient.of(Items.FEATHER), Ingredient.of(Items.GOLD_INGOT), earth,
                        Ingredient.of(Items.FEATHER), Ingredient.of(Items.GOLD_INGOT)),
                new ItemStack(ModItems.THAUMOSTATIC_GIRDLE.get()), AspectList.of(ModAspects.FLIGHT, 16)
                        .add(ModAspects.ENERGY, 32).add(ModAspects.AIR, 32).add(ModAspects.MOTION, 16),
                8, "thaumostatic_girdle");
        infusion(output, "vis_amulet", Ingredient.of(ModItems.MUNDANE_AMULET),
                List.of(charm, air, fire, charm, water, earth),
                new ItemStack(ModItems.VIS_AMULET.get()), AspectList.of(ModAspects.AURA, 24)
                        .add(ModAspects.ENERGY, 128).add(ModAspects.VOID, 24),
                6, "vis_amulet");
        infusion(output, "runic_amulet", Ingredient.of(ModItems.MUNDANE_AMULET),
                List.of(charm, amber, fabric, nitor, nitor, scribing),
                new ItemStack(ModItems.RUNIC_AMULET.get()), AspectList.of(ModAspects.PROTECT, 30)
                        .add(ModAspects.ENERGY, 50), 4, "runic_shielding");
        infusion(output, "runic_ring", Ingredient.of(ModItems.MUNDANE_RING),
                List.of(charm, amber, fabric, nitor, scribing),
                new ItemStack(ModItems.RUNIC_RING.get()), AspectList.of(ModAspects.PROTECT, 25)
                        .add(ModAspects.ENERGY, 45), 3, "runic_shielding");
        infusion(output, "runic_girdle", Ingredient.of(ModItems.MUNDANE_BELT),
                List.of(charm, amber, fabric, nitor, nitor, nitor, scribing),
                new ItemStack(ModItems.RUNIC_GIRDLE.get()), AspectList.of(ModAspects.PROTECT, 40)
                        .add(ModAspects.ENERGY, 64), 4, "runic_shielding");
        infusion(output, "emergency_runic_amulet", Ingredient.of(ModItems.RUNIC_AMULET),
                List.of(balanced, earth, earth, potion(Items.POTION, net.minecraft.world.item.alchemy.Potions.STRONG_STRENGTH),
                        earth, earth),
                new ItemStack(ModItems.EMERGENCY_RUNIC_AMULET.get()), AspectList.of(ModAspects.PROTECT, 30)
                        .add(ModAspects.EARTH, 50).add(ModAspects.VOID, 32), 7, "runic_special");
        infusion(output, "charged_runic_ring", Ingredient.of(ModItems.RUNIC_RING),
                List.of(balanced, fire, fire, potion(Items.POTION, net.minecraft.world.item.alchemy.Potions.STRONG_SWIFTNESS),
                        fire, fire),
                new ItemStack(ModItems.CHARGED_RUNIC_RING.get()), AspectList.of(ModAspects.PROTECT, 25)
                        .add(ModAspects.ENERGY, 64), 6, "runic_special");
        infusion(output, "revitalizing_runic_ring", Ingredient.of(ModItems.RUNIC_RING),
                List.of(balanced, water, water, potion(Items.POTION, net.minecraft.world.item.alchemy.Potions.LONG_REGENERATION),
                        water, water),
                new ItemStack(ModItems.REVITALIZING_RUNIC_RING.get()), AspectList.of(ModAspects.PROTECT, 25)
                        .add(ModAspects.WATER, 32).add(ModAspects.LIFE, 32), 6, "runic_special");
        infusion(output, "kinetic_runic_girdle", Ingredient.of(ModItems.RUNIC_GIRDLE),
                List.of(balanced, air, air, potion(Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.STRONG_HARMING),
                        air, air),
                new ItemStack(ModItems.KINETIC_RUNIC_GIRDLE.get()), AspectList.of(ModAspects.PROTECT, 40)
                        .add(ModAspects.AIR, 64), 7, "runic_special");
        infusion(output, "verdant_heart", Ingredient.of(ModItems.FANCY_RING),
                List.of(Ingredient.of(ModItems.PRIMORDIAL_PEARL), essence(ModAspects.LIFE),
                        Ingredient.of(Items.MILK_BUCKET), essence(ModAspects.PLANT)),
                new ItemStack(ModItems.VERDANT_HEART.get()), AspectList.of(ModAspects.LIFE, 48)
                        .add(ModAspects.ORDER, 24).add(ModAspects.PLANT, 48), 5, "verdant_heart");
        infusion(output, "verdant_heart_lifegiver", Ingredient.of(ModItems.VERDANT_HEART),
                List.of(Ingredient.of(Items.GOLDEN_APPLE), essence(ModAspects.LIFE),
                        potion(Items.POTION, net.minecraft.world.item.alchemy.Potions.REGENERATION), essence(ModAspects.MAN)),
                me.moonscenty.alchemia.item.bauble.VerdantHeartItem.of(new ItemStack(ModItems.VERDANT_HEART.get()),
                        me.moonscenty.alchemia.item.bauble.VerdantHeartItem.LIFEGIVER),
                AspectList.of(ModAspects.LIFE, 64).add(ModAspects.MAN, 64), 5, "verdant_heart_lifegiver");
        infusion(output, "verdant_heart_sustainer", Ingredient.of(ModItems.VERDANT_HEART),
                List.of(Ingredient.of(ModItems.TRIPLE_MEAT_TREAT), essence(ModAspects.DESIRE),
                        potion(Items.POTION, net.minecraft.world.item.alchemy.Potions.WATER_BREATHING), essence(ModAspects.AIR)),
                me.moonscenty.alchemia.item.bauble.VerdantHeartItem.of(new ItemStack(ModItems.VERDANT_HEART.get()),
                        me.moonscenty.alchemia.item.bauble.VerdantHeartItem.SUSTAINER),
                AspectList.of(ModAspects.DESIRE, 64).add(ModAspects.AIR, 64), 5, "verdant_heart_lifegiver");
        // what the altar is given for this is worked out from what is on it; the ring and essentia here are the
        // first time's, for the book to show
        output.accept(Alchemia.id("runic_augmentation"), new InfusionRecipe(
                Ingredient.of(ModTags.Items.RUNIC_AUGMENTABLE),
                List.of(Ingredient.of(Items.DIAMOND), Ingredient.of(ModItems.SALIS_MUNDUS)), ItemStack.EMPTY,
                AspectList.of(ModAspects.PROTECT, 16).add(ModAspects.CRYSTAL, 16).add(ModAspects.ENERGY, 32), 5,
                Optional.of(Alchemia.id("runic_augmentation")), Optional.empty(), Optional.empty(), true), null);
    }

    private void arcane(RecipeOutput output, String name, ItemStack result, AspectList cost,
            Map<Character, Ingredient> key, String... pattern) {
        arcane(output, name, null, result, cost, key, pattern);
    }

    /** The same, for the ones that are only shown once the research behind them is done. */
    private void arcane(RecipeOutput output, String name, String research, ItemStack result, AspectList cost,
            Map<Character, Ingredient> key, String... pattern) {
        output.accept(Alchemia.id(name), new ArcaneShapedRecipe("", ShapedRecipePattern.of(key, pattern), result,
                cost, Optional.ofNullable(research).map(Alchemia::id)), null);
    }

    /** Worked at the arcane workbench, but laid out anyhow: most of the tube fittings are one thing plus another. */
    private void shapeless(RecipeOutput output, String name, String research, ItemStack result, AspectList cost,
            Ingredient... ingredients) {
        NonNullList<Ingredient> laid = NonNullList.of(Ingredient.EMPTY, ingredients);
        output.accept(Alchemia.id(name), new ArcaneShapelessRecipe("", laid, result, cost,
                Optional.ofNullable(research).map(Alchemia::id)), null);
    }

    /**
     * The distillery: what catches essentia, what carries it, and what it is kept in.
     * <p>
     * The prices and the shapes are the original's. The one substitution is the tube's quicksilver nugget, which
     * the original spelled as the fifth of its nine nuggets.
     */
    private void distillery(RecipeOutput output) {
        arcane(output, "filter", "distillation", new ItemStack(ModItems.FILTER.get(), 2),
                AspectList.of(ModAspects.ORDER, 15).add(ModAspects.WATER, 15),
                Map.of('G', Ingredient.of(Tags.Items.INGOTS_GOLD),
                        'W', Ingredient.of(ModBlocks.SILVERWOOD.planks())), "GWG");

        arcane(output, "essentia_smelter", "distillation", new ItemStack(ModBlocks.ESSENTIA_SMELTER.get()),
                AspectList.of(ModAspects.FIRE, 25).add(ModAspects.WATER, 25),
                Map.of('B', Ingredient.of(ModItems.BRASS_PLATE),
                        'C', Ingredient.of(ModBlocks.CRUCIBLE),
                        'F', Ingredient.of(Items.FURNACE),
                        'S', Ingredient.of(Tags.Items.COBBLESTONES)),
                "BCB", "SFS", "SSS");

        // the better smelters, and what goes against a smelter's sides. The original built the first two round an
        // alchemical construct, which there is none of here; the brass block it was drawn as stands in for it, and
        // for the advanced construct (a construct worked with a primordial pearl), a primal charm
        arcane(output, "alchemium_smelter", "alchemium_smelter", new ItemStack(ModBlocks.ALCHEMIUM_SMELTER.get()),
                AspectList.of(ModAspects.FIRE, 350).add(ModAspects.WATER, 350),
                Map.of('B', Ingredient.of(ModItems.BRASS_PLATE),
                        'F', Ingredient.of(ModBlocks.ESSENTIA_SMELTER),
                        'G', Ingredient.of(ModBlocks.BRASS_BLOCK),
                        'I', Ingredient.of(ModItems.ALCHEMIUM_INGOT)),
                "BFB", "IGI", "III");
        arcane(output, "void_smelter", "void_smelter", new ItemStack(ModBlocks.VOID_SMELTER.get()),
                AspectList.of(ModAspects.FIRE, 750).add(ModAspects.WATER, 750),
                Map.of('B', Ingredient.of(ModItems.BRASS_PLATE),
                        'F', Ingredient.of(ModBlocks.ALCHEMIUM_SMELTER),
                        'G', Ingredient.of(ModItems.PRIMORDIAL_PEARL),
                        'I', Ingredient.of(ModItems.VOID_INGOT)),
                "BFB", "IGI", "III");
        arcane(output, "arcane_bore_base", "arcane_bore", new ItemStack(ModBlocks.ARCANE_BORE_BASE.get()),
                AspectList.of(ModAspects.AIR, 100).add(ModAspects.ORDER, 100),
                Map.of('W', Ingredient.of(ModBlocks.GREATWOOD.planks()),
                        'I', Ingredient.of(Tags.Items.INGOTS_IRON),
                        'D', Ingredient.of(Items.DISPENSER),
                        'G', Ingredient.of(ModItems.BRASS_GEAR)),
                "WIW", "IDI", "WGW");
        arcane(output, "mnemonic_matrix", "thaumatorium", new ItemStack(ModBlocks.MNEMONIC_MATRIX.get()),
                AspectList.of(ModAspects.FIRE, 50).add(ModAspects.WATER, 50).add(ModAspects.ORDER, 50),
                Map.of('I', Ingredient.of(Tags.Items.INGOTS_IRON),
                        'A', Ingredient.of(ModItems.AMBER),
                        'B', Ingredient.of(ModItems.CLOCKWORK_MIND)),
                "IAI", "ABA", "IAI");
        arcane(output, "centrifuge", "centrifuge", new ItemStack(ModBlocks.CENTRIFUGE.get()),
                AspectList.of(ModAspects.WATER, 100).add(ModAspects.ORDER, 100).add(ModAspects.ENTROPY, 100),
                Map.of('T', Ingredient.of(ModBlocks.TUBE),
                        'R', Ingredient.of(ModItems.MORPHIC_RESONATOR),
                        'C', Ingredient.of(ModBlocks.BRASS_BLOCK),
                        'P', Ingredient.of(ModItems.BRASS_GEAR)),
                " T ", "RCP", " T ");
        arcane(output, "crystallizer", "crystallizer", new ItemStack(ModBlocks.CRYSTALLIZER.get()),
                AspectList.of(ModAspects.WATER, 100).add(ModAspects.EARTH, 250).add(ModAspects.ORDER, 100),
                Map.of('I', Ingredient.of(Tags.Items.INGOTS_IRON),
                        'D', Ingredient.of(ModItems.BRASS_GEAR),
                        'Q', Ingredient.of(ModItems.BALANCED_SHARD),
                        'C', Ingredient.of(ModBlocks.BRASS_BLOCK),
                        'W', Ingredient.of(net.minecraft.tags.ItemTags.PLANKS),
                        'T', Ingredient.of(ModBlocks.TUBE)),
                "IDI", "QCQ", "WTW");
        arcane(output, "smelter_aux", "improved_distillation", new ItemStack(ModBlocks.SMELTER_AUX.get()),
                AspectList.of(ModAspects.AIR, 50).add(ModAspects.WATER, 50).add(ModAspects.EARTH, 25),
                Map.of('W', Ingredient.of(ModBlocks.GREATWOOD.planks()),
                        'T', Ingredient.of(ModBlocks.TUBE_FILTER),
                        'R', Ingredient.of(ModItems.BRASS_PLATE),
                        'G', Ingredient.of(ModBlocks.BRASS_BLOCK),
                        'I', Ingredient.of(Tags.Items.INGOTS_IRON),
                        'B', Ingredient.of(ModBlocks.BELLOWS)),
                "WTW", "RGR", "IBI");
        arcane(output, "smelter_vent", "improved_distillation", new ItemStack(ModBlocks.SMELTER_VENT.get()),
                AspectList.of(ModAspects.AIR, 125).add(ModAspects.WATER, 125).add(ModAspects.ORDER, 100),
                Map.of('I', Ingredient.of(Tags.Items.INGOTS_IRON),
                        'B', Ingredient.of(ModItems.BRASS_INGOT),
                        'M', Ingredient.of(ModItems.MORPHIC_RESONATOR),
                        'G', Ingredient.of(ModBlocks.BRASS_BLOCK),
                        'F', Ingredient.of(ModItems.FILTER)),
                "IBI", "MGF", "IBI");

        arcane(output, "alembic", "distillation", new ItemStack(ModBlocks.ALEMBIC.get()),
                AspectList.of(ModAspects.AIR, 15).add(ModAspects.WATER, 25),
                Map.of('W', Ingredient.of(ModBlocks.GREATWOOD.planks()),
                        'F', Ingredient.of(ModItems.FILTER),
                        'S', Ingredient.of(ModItems.BRASS_PLATE),
                        'B', Ingredient.of(Items.BUCKET)),
                "WFW", "SBS", "WFW");

        // the only one of the lot worked at a plain bench: glass and a lump of clay for the stopper
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.PHIAL.get(), 8)
                .pattern(" C ").pattern("G G").pattern(" G ")
                .define('G', Tags.Items.GLASS_BLOCKS)
                .define('C', Items.CLAY_BALL)
                .unlockedBy("has_glass", has(Tags.Items.GLASS_BLOCKS))
                .save(output);

        arcane(output, "jar", "jar_label", new ItemStack(ModBlocks.JAR.get()),
                AspectList.of(ModAspects.WATER, 5),
                Map.of('G', Ingredient.of(Tags.Items.GLASS_PANES),
                        'W', Ingredient.of(ItemTags.WOODEN_SLABS)),
                "GWG", "G G", "GGG");

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.JAR_LABEL.get(), 4)
                .requires(Tags.Items.DYES_BLACK)
                .requires(Tags.Items.SLIME_BALLS)
                .requires(Items.PAPER, 4)
                .unlockedBy("has_paper", has(Items.PAPER))
                .save(output);

        // writing an aspect on a label and rubbing it out again are both in code: an aspect is not a shape
        SpecialRecipeBuilder.special(LabelRecipe::new).save(output, Alchemia.id("jar_label_writing").toString());

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.JAR_BRACE.get(), 2)
                .pattern("NSN").pattern("S S").pattern("NSN")
                .define('N', ModTags.Items.NUGGETS_BRASS)
                .define('S', Tags.Items.RODS_WOODEN)
                .unlockedBy("has_nugget", has(ModTags.Items.NUGGETS_BRASS))
                .save(output);

        tubes(output);
    }

    /** The pipework: one recipe for the plain length, and one apiece for what is done to it afterwards. */
    private void tubes(RecipeOutput output) {
        arcane(output, "tube", "tubes", new ItemStack(ModBlocks.TUBE.get(), 8),
                AspectList.of(ModAspects.WATER, 5).add(ModAspects.ORDER, 5),
                Map.of('I', Ingredient.of(Tags.Items.INGOTS_IRON),
                        'B', Ingredient.of(Tags.Items.NUGGETS_GOLD),
                        'G', Ingredient.of(Tags.Items.GLASS_BLOCKS),
                        'Q', Ingredient.of(ModTags.Items.NUGGETS_QUICKSILVER)),
                " Q ", "IGI", " B ");

        shapeless(output, "tube_valve", "tubes", new ItemStack(ModBlocks.TUBE_VALVE.get()),
                AspectList.of(ModAspects.WATER, 5).add(ModAspects.ORDER, 5),
                Ingredient.of(ModBlocks.TUBE), Ingredient.of(Items.LEVER));

        shapeless(output, "tube_filter", "tube_filter", new ItemStack(ModBlocks.TUBE_FILTER.get()),
                AspectList.of(ModAspects.WATER, 5).add(ModAspects.ORDER, 10),
                Ingredient.of(ModBlocks.TUBE), Ingredient.of(ModItems.FILTER));

        shapeless(output, "tube_restrict", "tube_filter", new ItemStack(ModBlocks.TUBE_RESTRICT.get()),
                AspectList.of(ModAspects.WATER, 5).add(ModAspects.EARTH, 10),
                Ingredient.of(ModBlocks.TUBE), Ingredient.of(Tags.Items.STONES));

        shapeless(output, "tube_oneway", "tube_filter", new ItemStack(ModBlocks.TUBE_ONEWAY.get()),
                AspectList.of(ModAspects.WATER, 5).add(ModAspects.ORDER, 10).add(ModAspects.ENTROPY, 10),
                Ingredient.of(ModBlocks.TUBE), Ingredient.of(Tags.Items.DYES_BLUE));

        arcane(output, "tube_buffer", "tube_filter", new ItemStack(ModBlocks.TUBE_BUFFER.get()),
                AspectList.of(ModAspects.WATER, 25).add(ModAspects.ORDER, 25),
                Map.of('P', Ingredient.of(ModItems.PHIAL),
                        'V', Ingredient.of(ModBlocks.TUBE_VALVE),
                        'R', Ingredient.of(ModBlocks.TUBE_RESTRICT),
                        'T', Ingredient.of(ModBlocks.TUBE)),
                "PVP", "T T", "PRP");
    }

    /**
     * The altar, and the seven rods that can only be made on one.
     * <p>
     * The rod prices are the original's: twice the rod is reckoned to be worth in whatever it is chiefly made of,
     * and once each in a couple of aspects that say what it is for. Silverwood pays once in everything instead,
     * which is the whole point of silverwood.
     */
    private void infusion(RecipeOutput output) {
        arcane(output, "infusion_matrix", "infusion", new ItemStack(ModBlocks.INFUSION_MATRIX.get()),
                AspectList.of(ModAspects.ORDER, 100),
                Map.of('S', Ingredient.of(ModBlocks.ARCANE_STONE_BRICKS.block()),
                        'B', Ingredient.of(ModTags.Items.SHARDS),
                        'N', Ingredient.of(ModItems.BALANCED_SHARD)),
                "SBS", "BNB", "SBS");

        // the pedestals of the two older stones: the original's patterns and prices
        arcane(output, "ancient_pedestal", "infusion", new ItemStack(ModBlocks.ANCIENT_PEDESTAL.get()),
                AspectList.of(ModAspects.AIR, 25).add(ModAspects.ENTROPY, 25).add(ModAspects.ORDER, 150),
                Map.of('S', Ingredient.of(ModBlocks.ANCIENT_STONE.slab()),
                        'B', Ingredient.of(ModBlocks.ANCIENT_STONE.block())),
                "SSS", " B ", "SSS");
        arcane(output, "eldritch_pedestal", "infusion", new ItemStack(ModBlocks.ELDRITCH_PEDESTAL.get()),
                AspectList.of(ModAspects.AIR, 25).add(ModAspects.ENTROPY, 150).add(ModAspects.ORDER, 25),
                Map.of('S', Ingredient.of(ModBlocks.ELDRITCH_STONE.slab()),
                        'B', Ingredient.of(ModBlocks.ELDRITCH_STONE.block())),
                "SSS", " B ", "SSS");
        // banners: a stick, a slab and wool of the colour
        for (net.minecraft.world.item.DyeColor dye : net.minecraft.world.item.DyeColor.values()) {
            arcane(output, "banner_" + dye.getName(), "banners",
                    me.moonscenty.alchemia.block.BannerItem.of(dye, null),
                    AspectList.of(ModAspects.WATER, 5).add(ModAspects.EARTH, 5),
                    Map.of('W', Ingredient.of(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(
                                    net.minecraft.resources.ResourceLocation.withDefaultNamespace(dye.getName() + "_wool"))),
                            'S', Ingredient.of(Items.STICK),
                            'B', Ingredient.of(net.minecraft.tags.ItemTags.WOODEN_SLABS)),
                    "WS", "WS", "WB");
        }
        // the eye that opens a stone ring: an ender eye with a void seed and gold about it
        infusion(output, "eldritch_eye", Ingredient.of(Items.ENDER_EYE),
                List.of(Ingredient.of(ModItems.VOID_SEED), Ingredient.of(Items.GOLD_INGOT)),
                new ItemStack(ModItems.ELDRITCH_EYE.get()),
                AspectList.of(ModAspects.ELDRITCH, 64).add(ModAspects.VOID, 16).add(ModAspects.DARKNESS, 16)
                        .add(ModAspects.MOTION, 16), 5, "oculus");
        arcane(output, "arcane_pedestal", "infusion", new ItemStack(ModBlocks.ARCANE_PEDESTAL.get()),
                AspectList.of(ModAspects.AIR, 5),
                Map.of('S', Ingredient.of(ModBlocks.ARCANE_STONE.slab()),
                        'B', Ingredient.of(ModBlocks.ARCANE_STONE.block())),
                "SSS", " B ", "SSS");

        // the rods: what it is made of in the middle, a balanced shard and its own element around it
        rod(output, "obsidian", Ingredient.of(Items.OBSIDIAN), 3,
                AspectList.of(ModAspects.EARTH, 12).add(ModAspects.ENERGY, 6).add(ModAspects.DARKNESS, 6),
                CrystalType.EARTH);
        rod(output, "ice", Ingredient.of(Items.ICE), 3,
                AspectList.of(ModAspects.WATER, 12).add(ModAspects.ENERGY, 6).add(ModAspects.COLD, 6),
                CrystalType.WATER);
        rod(output, "quartz", Ingredient.of(Items.QUARTZ_BLOCK), 3,
                AspectList.of(ModAspects.ORDER, 12).add(ModAspects.ENERGY, 6).add(ModAspects.CRYSTAL, 6),
                CrystalType.ORDER);
        rod(output, "reed", Ingredient.of(Items.SUGAR_CANE), 3,
                AspectList.of(ModAspects.AIR, 12).add(ModAspects.ENERGY, 6).add(ModAspects.MOTION, 6),
                CrystalType.AIR);
        rod(output, "blaze", Ingredient.of(Items.BLAZE_ROD), 3,
                AspectList.of(ModAspects.FIRE, 12).add(ModAspects.ENERGY, 6).add(ModAspects.BEAST, 6),
                CrystalType.FIRE);
        rod(output, "bone", Ingredient.of(Items.BONE), 3,
                AspectList.of(ModAspects.ENTROPY, 12).add(ModAspects.ENERGY, 6).add(ModAspects.UNDEAD, 6),
                CrystalType.ENTROPY);

        // silverwood is the exception: a little of everything, and every primal shard laid out around it
        List<Ingredient> around = new java.util.ArrayList<>();
        around.add(Ingredient.of(ModItems.BALANCED_SHARD));
        AspectList evenly = AspectList.of(ModAspects.ENERGY, 9);
        for (CrystalType type : CrystalType.values()) {
            if (type.generatesNaturally()) {
                around.add(Ingredient.of(ModItems.SHARDS.get(type)));
                evenly = evenly.add(type.aspect(), 9);
            }
        }
        infusion(output, "wand_rod_silverwood", Ingredient.of(ModBlocks.SILVERWOOD.log()), around,
                new ItemStack(ModItems.WAND_RODS.get("silverwood").get()), evenly, 5, "wand_rods");

        // boots for walking, made of ordinary boots and the things that go fast
        infusion(output, "traveller_boots", Ingredient.of(Items.LEATHER_BOOTS),
                List.of(Ingredient.of(ModItems.SHARDS.get(CrystalType.AIR)),
                        Ingredient.of(ModItems.SHARDS.get(CrystalType.AIR)),
                        Ingredient.of(ModItems.ENCHANTED_FABRIC), Ingredient.of(ModItems.ENCHANTED_FABRIC),
                        Ingredient.of(Items.FEATHER), Ingredient.of(Items.COD)),
                new ItemStack(ModItems.TRAVELLER_BOOTS.get()),
                AspectList.of(ModAspects.FLIGHT, 25).add(ModAspects.MOTION, 25), 1, "traveller_boots");

        // the void robes: a suit of void metal taken apart and sewn back together round cloth
        voidRobe(output, "void_robe_helm", "void_helmet",
                List.of(Ingredient.of(ModItems.GOGGLES), Ingredient.of(ModItems.ENCHANTED_FABRIC),
                        Ingredient.of(ModItems.ENCHANTED_FABRIC), Ingredient.of(ModItems.SALIS_MUNDUS),
                        Ingredient.of(ModItems.ENCHANTED_FABRIC), Ingredient.of(ModItems.ENCHANTED_FABRIC)),
                AspectList.of(ModAspects.METAL, 16).add(ModAspects.SENSES, 16).add(ModAspects.PROTECT, 16)
                        .add(ModAspects.ENERGY, 16).add(ModAspects.ELDRITCH, 16).add(ModAspects.VOID, 16));
        voidRobe(output, "void_robe_chest", "void_chestplate",
                List.of(Ingredient.of(ModItems.ROBES.get("cloth_chest")), Ingredient.of(ModItems.VOID_PLATE),
                        Ingredient.of(ModItems.VOID_PLATE), Ingredient.of(ModItems.SALIS_MUNDUS),
                        Ingredient.of(ModItems.ENCHANTED_FABRIC), Ingredient.of(Items.LEATHER)),
                AspectList.of(ModAspects.METAL, 24).add(ModAspects.PROTECT, 24).add(ModAspects.ENERGY, 16)
                        .add(ModAspects.ELDRITCH, 16).add(ModAspects.VOID, 24));
        voidRobe(output, "void_robe_legs", "void_leggings",
                List.of(Ingredient.of(ModItems.ROBES.get("cloth_legs")), Ingredient.of(ModItems.VOID_PLATE),
                        Ingredient.of(ModItems.VOID_PLATE), Ingredient.of(ModItems.SALIS_MUNDUS),
                        Ingredient.of(ModItems.ENCHANTED_FABRIC), Ingredient.of(Items.LEATHER)),
                AspectList.of(ModAspects.METAL, 20).add(ModAspects.PROTECT, 20).add(ModAspects.ENERGY, 16)
                        .add(ModAspects.ELDRITCH, 16).add(ModAspects.VOID, 20));

        // fortress armour: a finished suit of alchemium pulled apart on the altar and built up into plate.
        // The original's numbers, piece for piece
        fortress(output, "fortress_helm", "alchemium_helmet", 2,
                List.of(Ingredient.of(Items.GOLD_INGOT), Ingredient.of(Items.GOLD_INGOT),
                        Ingredient.of(Items.EMERALD)),
                AspectList.of(ModAspects.METAL, 24).add(ModAspects.PROTECT, 16).add(ModAspects.ENERGY, 16));
        fortress(output, "fortress_chest", "alchemium_chestplate", 4,
                List.of(Ingredient.of(Items.GOLD_INGOT), Ingredient.of(Items.LEATHER)),
                AspectList.of(ModAspects.METAL, 24).add(ModAspects.PROTECT, 24).add(ModAspects.ENERGY, 16));
        fortress(output, "fortress_legs", "alchemium_leggings", 3,
                List.of(Ingredient.of(Items.GOLD_INGOT), Ingredient.of(Items.LEATHER)),
                AspectList.of(ModAspects.METAL, 24).add(ModAspects.PROTECT, 20).add(ModAspects.ENERGY, 16));

        // goggles set into a fortress helm. The helm that goes under the matrix is the helm that comes back,
        // so whatever was enchanted onto it or worn off it stays as it was
        output.accept(Alchemia.id("helm_goggles"), new InfusionRecipe(
                Ingredient.of(ModItems.FORTRESS.get("fortress_helm")),
                List.of(Ingredient.of(Items.SLIME_BALL), Ingredient.of(ModItems.GOGGLES)),
                ItemStack.EMPTY,
                AspectList.of(ModAspects.SENSES, 32).add(ModAspects.AURA, 16).add(ModAspects.PROTECT, 16),
                HELM_GOGGLES_UNSTABLE, Optional.of(Alchemia.id("fortress_armour")),
                Optional.empty(), Optional.of(HelmFitting.GOGGLES)), null);

        // the three masks. Each is a dye, two irons and a hide round the one thing that gives it its face.
        // The original's numbers.
        mask(output, "mask_grinning_devil", HelmFitting.GRINNING_DEVIL,
                List.of(Ingredient.of(Items.INK_SAC), Ingredient.of(ModBlocks.SHIMMERLEAF.get()),
                        Ingredient.of(ModItems.ZOMBIE_BRAIN)),
                AspectList.of(ModAspects.MIND, 64).add(ModAspects.LIFE, 64).add(ModAspects.PROTECT, 16));
        mask(output, "mask_angry_ghost", HelmFitting.ANGRY_GHOST,
                List.of(Ingredient.of(Items.BONE_MEAL), Ingredient.of(Items.POISONOUS_POTATO),
                        Ingredient.of(Items.WITHER_SKELETON_SKULL)),
                AspectList.of(ModAspects.ENTROPY, 64).add(ModAspects.DEATH, 64).add(ModAspects.PROTECT, 16));
        mask(output, "mask_sipping_fiend", HelmFitting.SIPPING_FIEND,
                List.of(Ingredient.of(Items.RED_DYE), Ingredient.of(Items.GHAST_TEAR),
                        Ingredient.of(Items.MILK_BUCKET)),
                AspectList.of(ModAspects.UNDEAD, 64).add(ModAspects.LIFE, 64).add(ModAspects.PROTECT, 16));

        // the bioalchemic mind: the clockwork one with a zombie's brain grown into it and a clock to keep it to time
        // the seals that need an altar: harvesting, butchering and breaking
        infusion(output, "seal_harvest", Ingredient.of(ModItems.SEAL_BLANK),
                List.of(Ingredient.of(Items.WHEAT_SEEDS), Ingredient.of(Items.PUMPKIN_SEEDS),
                        Ingredient.of(Items.MELON_SEEDS), Ingredient.of(Items.COCOA_BEANS),
                        Ingredient.of(Items.SUGAR_CANE), Ingredient.of(Items.CACTUS)),
                new ItemStack(ModItems.SEALS.get("harvest").get()),
                AspectList.of(ModAspects.PLANT, 4).add(ModAspects.SENSES, 4).add(ModAspects.MAN, 4), 0,
                "seal_harvest");
        infusion(output, "seal_butcher", Ingredient.of(ModItems.SEALS.get("guard").get()),
                List.of(Ingredient.of(Items.LEATHER), Ingredient.of(net.minecraft.tags.ItemTags.WOOL),
                        Ingredient.of(Items.RABBIT_HIDE), Ingredient.of(Items.PORKCHOP),
                        Ingredient.of(Items.MUTTON), Ingredient.of(Items.BEEF)),
                new ItemStack(ModItems.SEALS.get("butcher").get()),
                AspectList.of(ModAspects.BEAST, 4).add(ModAspects.SENSES, 4).add(ModAspects.MAN, 4), 0,
                "seal_butcher");
        infusion(output, "seal_breaker", Ingredient.of(ModItems.SEAL_BLANK),
                List.of(Ingredient.of(Items.GOLDEN_PICKAXE), Ingredient.of(Items.GOLDEN_AXE),
                        Ingredient.of(Items.GOLDEN_SHOVEL)),
                new ItemStack(ModItems.SEALS.get("breaker").get()),
                AspectList.of(ModAspects.TOOL, 4).add(ModAspects.ENTROPY, 4).add(ModAspects.MAN, 4), 1,
                "seal_break");
        infusion(output, "bioalchemic_mind", Ingredient.of(ModItems.CLOCKWORK_MIND),
                List.of(Ingredient.of(ModItems.ZOMBIE_BRAIN), Ingredient.of(Items.CLOCK)),
                new ItemStack(ModItems.BIOALCHEMIC_MIND.get()),
                AspectList.of(ModAspects.MIND, 16).add(ModAspects.MECHANISM, 16), BIOALCHEMIC_UNSTABLE,
                "bioalchemic_mind");

        // the totems: a block of silverwood worked on with greatwood, a chest, a balanced shard, salis mundus and a
        // wisp's essence of any kind -- with amber and a lure to draw vis in, or quicksilver and a piston to push it
        // out
        infusion(output, "aura_totem_pull", Ingredient.of(ModBlocks.SILVERWOOD.planks()),
                List.of(Ingredient.of(ModBlocks.GREATWOOD.planks()), Ingredient.of(Items.CHEST),
                        Ingredient.of(ModItems.BALANCED_SHARD), Ingredient.of(ModItems.SALIS_MUNDUS),
                        Ingredient.of(ModItems.AMBER), Ingredient.of(Items.CARROT_ON_A_STICK),
                        Ingredient.of(ModItems.WISPY_ESSENCE)),
                new ItemStack(ModBlocks.AURA_TOTEM_PULL.get()),
                AspectList.of(ModAspects.ENERGY, 25).add(ModAspects.AURA, 25).add(ModAspects.MOTION, 25)
                        .add(ModAspects.DESIRE, 25), TOTEM_UNSTABLE, "aura_totem_pull");
        infusion(output, "aura_totem_push", Ingredient.of(ModBlocks.SILVERWOOD.planks()),
                List.of(Ingredient.of(ModBlocks.GREATWOOD.planks()), Ingredient.of(Items.CHEST),
                        Ingredient.of(ModItems.BALANCED_SHARD), Ingredient.of(ModItems.SALIS_MUNDUS),
                        Ingredient.of(ModItems.QUICKSILVER), Ingredient.of(Items.PISTON),
                        Ingredient.of(ModItems.WISPY_ESSENCE)),
                new ItemStack(ModBlocks.AURA_TOTEM_PUSH.get()),
                AspectList.of(ModAspects.ENERGY, 25).add(ModAspects.AURA, 25).add(ModAspects.MOTION, 25)
                        .add(ModAspects.AVERSION, 25), TOTEM_UNSTABLE, "aura_totem");

        // the recharge pedestal: a pedestal worked on with gold, diamonds and a primal charm
        // the mirrors: mirrored glass framed in gold or iron, with an ender pearl; and a mirror on a handle
        infusion(output, "mirror", Ingredient.of(ModItems.MIRRORED_GLASS),
                List.of(Ingredient.of(Items.GOLD_INGOT), Ingredient.of(Items.GOLD_INGOT), Ingredient.of(Items.GOLD_INGOT),
                        Ingredient.of(Items.ENDER_PEARL)),
                new ItemStack(ModBlocks.MIRROR.get()),
                AspectList.of(ModAspects.MOTION, 8).add(ModAspects.DARKNESS, 8).add(ModAspects.EXCHANGE, 8), 1, "mirror");
        infusion(output, "hand_mirror", Ingredient.of(ModBlocks.MIRROR),
                List.of(Ingredient.of(Items.STICK), Ingredient.of(Items.COMPASS), Ingredient.of(Items.MAP)),
                new ItemStack(ModItems.HAND_MIRROR.get()),
                AspectList.of(ModAspects.TOOL, 16).add(ModAspects.MOTION, 16), 5, "mirror_hand");
        infusion(output, "mirror_essentia", Ingredient.of(ModItems.MIRRORED_GLASS),
                List.of(Ingredient.of(Tags.Items.INGOTS_IRON), Ingredient.of(Tags.Items.INGOTS_IRON),
                        Ingredient.of(Tags.Items.INGOTS_IRON), Ingredient.of(Items.ENDER_PEARL)),
                new ItemStack(ModBlocks.MIRROR_ESSENTIA.get()),
                AspectList.of(ModAspects.MOTION, 8).add(ModAspects.WATER, 8).add(ModAspects.EXCHANGE, 8), 2,
                "mirror_essentia");

        // the bore: a clockwork mind given the pick, the shovel and the earth to dig with
        infusion(output, "arcane_bore", Ingredient.of(ModItems.CLOCKWORK_MIND),
                List.of(Ingredient.of(ModBlocks.GREATWOOD.planks()), Ingredient.of(ModBlocks.GREATWOOD.planks()),
                        Ingredient.of(ModItems.BRASS_GEAR), Ingredient.of(ModItems.BRASS_PLATE),
                        Ingredient.of(Items.DIAMOND_PICKAXE), Ingredient.of(Items.DIAMOND_SHOVEL),
                        Ingredient.of(ModItems.SHARDS.get(me.moonscenty.alchemia.block.CrystalType.AIR)),
                        Ingredient.of(ModItems.SHARDS.get(me.moonscenty.alchemia.block.CrystalType.EARTH))),
                new ItemStack(ModBlocks.ARCANE_BORE.get()),
                AspectList.of(ModAspects.ENERGY, 16).add(ModAspects.EARTH, 32).add(ModAspects.MECHANISM, 32)
                        .add(ModAspects.VOID, 16).add(ModAspects.MOTION, 16),
                4, "arcane_bore");

        // a warded jar with a zombie's brain in it
        infusion(output, "brain_jar", Ingredient.of(ModBlocks.JAR),
                List.of(Ingredient.of(ModItems.ZOMBIE_BRAIN), Ingredient.of(Items.SPIDER_EYE),
                        Ingredient.of(Items.WATER_BUCKET), Ingredient.of(Items.SPIDER_EYE)),
                new ItemStack(ModBlocks.BRAIN_JAR.get()),
                AspectList.of(ModAspects.MIND, 10).add(ModAspects.SENSES, 10).add(ModAspects.UNDEAD, 20),
                4, "brain_jar");

        infusion(output, "recharge_pedestal", Ingredient.of(ModBlocks.ARCANE_PEDESTAL),
                List.of(Ingredient.of(Items.GOLD_INGOT), Ingredient.of(Items.DIAMOND),
                        Ingredient.of(ModItems.PRIMAL_CHARM), Ingredient.of(Items.DIAMOND)),
                new ItemStack(ModBlocks.RECHARGE_PEDESTAL.get()),
                AspectList.of(ModAspects.AURA, 10).add(ModAspects.ENERGY, 15).add(ModAspects.EXCHANGE, 15),
                PEDESTAL_UNSTABLE, "recharge_pedestal");

        // the two lamps that burn essentia: an arcane lamp worked on with gold and what feeds what they tend
        infusion(output, "growth_lamp", Ingredient.of(ModBlocks.ARCANE_LAMP.get()),
                List.of(Ingredient.of(Items.GOLD_INGOT), Ingredient.of(Items.BONE_MEAL),
                        Ingredient.of(ModItems.SHARDS.get(CrystalType.EARTH)), Ingredient.of(Items.GOLD_INGOT),
                        Ingredient.of(Items.BONE_MEAL), Ingredient.of(ModItems.SHARDS.get(CrystalType.EARTH))),
                new ItemStack(ModBlocks.GROWTH_LAMP.get()),
                AspectList.of(ModAspects.PLANT, 16).add(ModAspects.LIGHT, 8).add(ModAspects.LIFE, 8)
                        .add(ModAspects.TOOL, 8), LAMP_UNSTABLE, "growth_lamp");
        infusion(output, "fertility_lamp", Ingredient.of(ModBlocks.ARCANE_LAMP.get()),
                List.of(Ingredient.of(Items.GOLD_INGOT), Ingredient.of(Items.WHEAT),
                        Ingredient.of(ModItems.SHARDS.get(CrystalType.FIRE)), Ingredient.of(Items.GOLD_INGOT),
                        Ingredient.of(Items.CARROT), Ingredient.of(ModItems.SHARDS.get(CrystalType.FIRE))),
                new ItemStack(ModBlocks.FERTILITY_LAMP.get()),
                AspectList.of(ModAspects.BEAST, 16).add(ModAspects.LIGHT, 8).add(ModAspects.LIFE, 8)
                        .add(ModAspects.DESIRE, 8), LAMP_UNSTABLE, "fertility_lamp");
        // a jug of baked clay, two buckets' worth of water and a shard of it worked in
        infusion(output, "water_jug", Ingredient.of(Items.TERRACOTTA),
                List.of(Ingredient.of(Items.WATER_BUCKET), Ingredient.of(Items.WATER_BUCKET),
                        Ingredient.of(ModItems.SHARDS.get(CrystalType.WATER)), Ingredient.of(ModItems.SALIS_MUNDUS)),
                new ItemStack(ModBlocks.WATER_JUG.get()),
                AspectList.of(ModAspects.WATER, 16).add(ModAspects.CRAFT, 8).add(ModAspects.EARTH, 8),
                JUG_UNSTABLE, "water_jug");

        // the autocaster: seven things round a made mind, as the original had it
        infusion(output, "autocaster", Ingredient.of(ModItems.BIOALCHEMIC_MIND),
                List.of(Ingredient.of(ModItems.BALANCED_SHARD), Ingredient.of(ModItems.MORPHIC_RESONATOR),
                        Ingredient.of(ModBlocks.GREATWOOD.planks()), Ingredient.of(ModBlocks.GREATWOOD.planks()),
                        Ingredient.of(ModItems.BRASS_GEAR), Ingredient.of(ModItems.BRASS_PLATE),
                        Ingredient.of(ModItems.WAND)),
                new ItemStack(ModItems.AUTOCASTER.get()),
                AspectList.of(ModAspects.AURA, 16).add(ModAspects.MECHANISM, 32).add(ModAspects.MIND, 16)
                        .add(ModAspects.SENSES, 16).add(ModAspects.AVERSION, 16),
                AUTOCASTER_UNSTABLE, "autocaster");

        // the elemental tools: an alchemium tool on the matrix, two shards of its element, a diamond and a
        // board of greatwood. Instability one and the essentia are the original's, tool by tool
        elemental(output, "elemental_axe", "alchemium_axe", CrystalType.WATER, CrystalType.WATER,
                AspectList.of(ModAspects.WATER, 16).add(ModAspects.PLANT, 8));
        elemental(output, "elemental_pickaxe", "alchemium_pickaxe", CrystalType.FIRE, CrystalType.FIRE,
                AspectList.of(ModAspects.FIRE, 8).add(ModAspects.METAL, 8).add(ModAspects.SENSES, 8));
        elemental(output, "elemental_sword", "alchemium_sword", CrystalType.AIR, CrystalType.AIR,
                AspectList.of(ModAspects.AIR, 8).add(ModAspects.MOTION, 8).add(ModAspects.AVERSION, 8));
        elemental(output, "elemental_shovel", "alchemium_shovel", CrystalType.EARTH, CrystalType.EARTH,
                AspectList.of(ModAspects.EARTH, 16).add(ModAspects.CRAFT, 8));
        elemental(output, "elemental_hoe", "alchemium_hoe", CrystalType.ORDER, CrystalType.ENTROPY,
                AspectList.of(ModAspects.ORDER, 8).add(ModAspects.PLANT, 8).add(ModAspects.ENTROPY, 8));

        // the crusher: the four diggers and two primal charms round a primordial pearl
        infusion(output, "primal_crusher", Ingredient.of(ModItems.PRIMORDIAL_PEARL),
                List.of(Ingredient.of(ModItems.PRIMAL_CHARM),
                        Ingredient.of(ModItems.METAL_TOOLS.get("void_pickaxe")),
                        Ingredient.of(ModItems.METAL_TOOLS.get("void_shovel")),
                        Ingredient.of(ModItems.PRIMAL_CHARM),
                        Ingredient.of(ModItems.ELEMENTAL_TOOLS.get("elemental_pickaxe")),
                        Ingredient.of(ModItems.ELEMENTAL_TOOLS.get("elemental_shovel"))),
                new ItemStack(ModItems.PRIMAL_CRUSHER.get()),
                AspectList.of(ModAspects.EARTH, 24).add(ModAspects.TOOL, 24).add(ModAspects.ENTROPY, 16)
                        .add(ModAspects.VOID, 16).add(ModAspects.AVERSION, 16).add(ModAspects.ELDRITCH, 16)
                        .add(ModAspects.DESIRE, 16),
                CRUSHER_UNSTABLE, "primal_crusher");

        // the two caps a workbench can only cast. Salis mundus round them, and the altar does the rest
        infusion(output, "wand_cap_alchemium", Ingredient.of(ModItems.INERT_CAPS.get("alchemium")),
                salis(3), new ItemStack(ModItems.WAND_CAPS.get("alchemium").get()),
                AspectList.of(ModAspects.ENERGY, 12).add(ModAspects.AURA, 6), 5, "wand_cap_alchemium");
        infusion(output, "wand_cap_void", Ingredient.of(ModItems.INERT_CAPS.get("void")),
                salis(4), new ItemStack(ModItems.WAND_CAPS.get("void").get()),
                AspectList.of(ModAspects.ENERGY, 18).add(ModAspects.VOID, 18)
                        .add(ModAspects.ELDRITCH, 18).add(ModAspects.AURA, 18), 8, "void_metal");
    }

    /** One of the five: the alchemium tool of the same shape, two shards, a diamond and a board of greatwood. */
    private void elemental(RecipeOutput output, String made, String from, CrystalType one, CrystalType two,
            AspectList essentia) {
        infusion(output, made, Ingredient.of(ModItems.METAL_TOOLS.get(from)),
                List.of(Ingredient.of(ModItems.SHARDS.get(one)), Ingredient.of(ModItems.SHARDS.get(two)),
                        Ingredient.of(Items.DIAMOND), Ingredient.of(ModBlocks.GREATWOOD.planks())),
                new ItemStack(ModItems.ELEMENTAL_TOOLS.get(made).get()), essentia, ELEMENTAL_UNSTABLE,
                "elemental_tools");
    }

    /**
     * What an altar can put on a tool somebody already owns.
     * <p>
     * The knowledge fragment the original laid on the ring is eldritch loot and there is none yet, so a written
     * research note stands in its place: the same reading, come by the long way rather than found.
     */
    private void enchantments(RecipeOutput output) {
        enchantment(output, InfusionEnchantment.COLLECTOR,
                AspectList.of(ModAspects.DESIRE, 16).add(ModAspects.WATER, 24),
                List.of(Ingredient.of(ModItems.RESEARCH_NOTES),
                        Ingredient.of(Items.ENCHANTED_BOOK), Ingredient.of(Items.LEAD)));
        enchantment(output, InfusionEnchantment.DESTRUCTIVE,
                AspectList.of(ModAspects.AVERSION, 48).add(ModAspects.ENTROPY, 64),
                List.of(Ingredient.of(ModItems.RESEARCH_NOTES),
                        Ingredient.of(Items.ENCHANTED_BOOK), Ingredient.of(Items.TNT)));
        enchantment(output, InfusionEnchantment.BURROWING,
                AspectList.of(ModAspects.SENSES, 24).add(ModAspects.EARTH, 32),
                List.of(Ingredient.of(ModItems.RESEARCH_NOTES),
                        Ingredient.of(Items.ENCHANTED_BOOK), Ingredient.of(Items.RABBIT_FOOT)));
        enchantment(output, InfusionEnchantment.REFINING,
                AspectList.of(ModAspects.ORDER, 16).add(ModAspects.EXCHANGE, 12),
                List.of(Ingredient.of(ModItems.RESEARCH_NOTES),
                        Ingredient.of(Items.ENCHANTED_BOOK), Ingredient.of(ModItems.SALIS_MUNDUS)));
        enchantment(output, InfusionEnchantment.SOUNDING,
                AspectList.of(ModAspects.SENSES, 8).add(ModAspects.FIRE, 12),
                List.of(Ingredient.of(ModItems.RESEARCH_NOTES),
                        Ingredient.of(Items.ENCHANTED_BOOK), Ingredient.of(Items.MAP)));
        enchantment(output, InfusionEnchantment.ARCING,
                AspectList.of(ModAspects.ENERGY, 8).add(ModAspects.AIR, 12),
                List.of(Ingredient.of(ModItems.RESEARCH_NOTES),
                        Ingredient.of(Items.ENCHANTED_BOOK), Ingredient.of(Items.REDSTONE_BLOCK)));
        enchantment(output, InfusionEnchantment.ESSENCE,
                AspectList.of(ModAspects.BEAST, 8).add(ModAspects.FLUX, 12),
                List.of(Ingredient.of(ModItems.RESEARCH_NOTES),
                        Ingredient.of(Items.ENCHANTED_BOOK), Ingredient.of(ModItems.CRYSTALLIZED_ESSENCE)));
    }

    /** One working that puts something on a tool: the tools it will take, and what is laid round them. */
    private void enchantment(RecipeOutput output, InfusionEnchantment which, AspectList essentia,
            List<Ingredient> ring) {
        String name = "infusion_enchantment_" + which.getSerializedName();
        // named as tags rather than as the items in them: the tags are read when the recipe is used, so a tool
        // from anywhere else is covered without this having to know it exists
        Ingredient takes = net.neoforged.neoforge.common.crafting.CompoundIngredient.of(
                which.goesOn().stream().map(Ingredient::of).toArray(Ingredient[]::new));
        output.accept(Alchemia.id(name), new InfusionRecipe(takes, ring, ItemStack.EMPTY, essentia,
                INFUSION_ENCHANTMENT_INSTABILITY, Optional.of(Alchemia.id("infusion_enchantment")),
                Optional.of(which)), null);
    }

    /** What any of these costs in steadiness, which the original set once for all of them. */
    private static final int INFUSION_ENCHANTMENT_INSTABILITY = 4;

    /** A ring of salis mundus, which is what every finishing of a cast thing is laid out with. */
    private static List<Ingredient> salis(int many) {
        return java.util.Collections.nCopies(many, Ingredient.of(ModItems.SALIS_MUNDUS));
    }

    /** One wand rod: the stuff it is cut from, with a balanced shard and a shard of its own element beside it. */
    private void rod(RecipeOutput output, String name, Ingredient from, int instability, AspectList cost,
            CrystalType element) {
        infusion(output, "wand_rod_" + name, from,
                List.of(Ingredient.of(ModItems.BALANCED_SHARD), Ingredient.of(ModItems.SHARDS.get(element))),
                new ItemStack(ModItems.WAND_RODS.get(name).get()), cost, instability, "wand_rods");
    }

    private void infusion(RecipeOutput output, String name, Ingredient central, List<Ingredient> ring,
            ItemStack result, AspectList essentia, int instability, String research) {
        output.accept(Alchemia.id(name), new InfusionRecipe(central, ring, result, essentia, instability,
                Optional.of(Alchemia.id(research))), null);
    }

    /**
     * One of the three masks, beaten onto a fortress helm.
     * <p>
     * Every mask is two irons and a hide; what tells them apart is the dye and the two things that give the face
     * its character, which is what the caller hands in.
     */
    private void mask(RecipeOutput output, String name, HelmFitting face, List<Ingredient> own,
            AspectList essentia) {
        List<Ingredient> ring = new java.util.ArrayList<>(own);
        ring.add(Ingredient.of(Items.IRON_INGOT));
        ring.add(Ingredient.of(Items.IRON_INGOT));
        ring.add(Ingredient.of(Items.LEATHER));
        output.accept(Alchemia.id(name), new InfusionRecipe(
                Ingredient.of(ModItems.FORTRESS.get("fortress_helm")), ring, ItemStack.EMPTY, essentia,
                MASK_UNSTABLE, Optional.of(Alchemia.id("fortress_armour")),
                Optional.empty(), Optional.of(face)), null);
    }

    /**
     * One piece of fortress armour: the matching piece of alchemium at the centre, so many plates of the same
     * metal round it, and whatever else that piece wants.
     *
     * @param plates how many plates of alchemium go round it, which is how much of the piece is being rebuilt
     */
    private void fortress(RecipeOutput output, String name, String from, int plates, List<Ingredient> rest,
            AspectList essentia) {
        List<Ingredient> ring = new java.util.ArrayList<>();
        for (int beaten = 0; beaten < plates; beaten++) {
            ring.add(Ingredient.of(ModItems.ALCHEMIUM_PLATE));
        }
        ring.addAll(rest);
        infusion(output, name, Ingredient.of(ModItems.METAL_ARMOUR.get(from)), ring,
                new ItemStack(ModItems.FORTRESS.get(name).get()), essentia, FORTRESS_UNSTABLE, "fortress_armour");
    }

    /** One of the three void robes: the matching piece of void armour at the centre, unstable to the same degree. */
    private void voidRobe(RecipeOutput output, String name, String from, List<Ingredient> ring,
            AspectList essentia) {
        infusion(output, name, Ingredient.of(ModItems.METAL_ARMOUR.get(from)), ring,
                new ItemStack(ModItems.ROBES.get(name).get()), essentia, VOID_ROBE_UNSTABLE, "void_robes");
    }

    /** The same, for a working nothing has been written up yet. An altar will do it for anybody. */
    private void infusion(RecipeOutput output, String name, Ingredient central, List<Ingredient> ring,
            ItemStack result, AspectList essentia, int instability) {
        output.accept(Alchemia.id(name), new InfusionRecipe(central, ring, result, essentia, instability,
                Optional.empty()), null);
    }

    /**
     * What is boiled out of a crucible.
     * <p>
     * A crucible recipe is not a shape but a state: what has to be dissolved in the water already, and the one
     * thing thrown in afterwards to finish it. The thing thrown in is used up either way, so an iron ingot dropped
     * into a pot that is not ready simply comes apart into what iron is made of.
     */
    /**
     * The tools and armour of our two metals, in the shapes everyone already knows.
     * <p>
     * A plain crafting table and the vanilla patterns, exactly as the original had them. Neither metal is worth
     * making these out of until it has been made at all, and making it is the hard part; asking a player to
     * relearn where the sticks go on top of that would be a second cost for nothing.
     */
    private void metalGear(RecipeOutput output) {
        forge(output, "alchemium", ModTags.Items.INGOTS_ALCHEMIUM);
        forge(output, "void", ModTags.Items.INGOTS_VOID);
    }

    private void forge(RecipeOutput output, String metal, net.minecraft.tags.TagKey<Item> of) {
        Ingredient bar = Ingredient.of(of);
        Ingredient haft = Ingredient.of(Items.STICK);
        shape(output, metal + "_pickaxe", bar, haft, "III", " S ", " S ");
        shape(output, metal + "_axe", bar, haft, "II", "SI", "S ");
        shape(output, metal + "_shovel", bar, haft, "I", "S", "S");
        shape(output, metal + "_sword", bar, haft, "I", "I", "S");
        shape(output, metal + "_hoe", bar, haft, "II", "S ", "S ");
        shape(output, metal + "_helmet", bar, haft, "III", "I I");
        shape(output, metal + "_chestplate", bar, haft, "I I", "III", "III");
        shape(output, metal + "_leggings", bar, haft, "III", "I I", "I I");
        shape(output, metal + "_boots", bar, haft, "I I", "I I");
    }

    private void shape(RecipeOutput output, String name, Ingredient bar, Ingredient haft, String... rows) {
        Item made = ModItems.METAL_TOOLS.containsKey(name)
                ? ModItems.METAL_TOOLS.get(name).get() : ModItems.METAL_ARMOUR.get(name).get();
        var builder = ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, made)
                .define('I', bar)
                .unlockedBy("has_ingot", has(bar.getItems()[0].getItem()));
        if (String.join("", rows).indexOf('S') >= 0) {
            builder.define('S', haft);
        }
        for (String row : rows) {
            builder.pattern(row);
        }
        builder.save(output, Alchemia.id(name));
    }

    private void crucible(RecipeOutput output) {
        crucible(output, "alchemium_ingot_from_iron", Ingredient.of(Tags.Items.INGOTS_IRON),
                AspectList.of(ModAspects.EARTH, 2).add(ModAspects.ORDER, 2),
                new ItemStack(ModItems.ALCHEMIUM_INGOT.get()), "metallurgy");
        crucible(output, "brass_ingot_from_iron", Ingredient.of(Tags.Items.INGOTS_IRON),
                AspectList.of(ModAspects.ENERGY, 1).add(ModAspects.WATER, 1),
                new ItemStack(ModItems.BRASS_INGOT.get()), "metallurgy");

        // flesh rendered down to tallow, and a blank seal steeped into each kind of control seal
        crucible(output, "tallow", Ingredient.of(Items.ROTTEN_FLESH), AspectList.of(ModAspects.FIRE, 1),
                new ItemStack(ModItems.TALLOW.get()), "tallow");
        seal(output, "pickup", ModItems.SEAL_BLANK.get(), AspectList.of(ModAspects.DESIRE, 4), "seal_collect");
        seal(output, "pickup_advanced", ModItems.SEALS.get("pickup").get(),
                AspectList.of(ModAspects.SENSES, 4).add(ModAspects.MIND, 4), "seal_collect");
        seal(output, "fill", ModItems.SEAL_BLANK.get(), AspectList.of(ModAspects.AVERSION, 4), "seal_store");
        seal(output, "fill_advanced", ModItems.SEALS.get("fill").get(),
                AspectList.of(ModAspects.SENSES, 4).add(ModAspects.MIND, 4), "seal_store");
        seal(output, "empty", ModItems.SEAL_BLANK.get(), AspectList.of(ModAspects.VOID, 4), "seal_empty");
        seal(output, "empty_advanced", ModItems.SEALS.get("empty").get(),
                AspectList.of(ModAspects.SENSES, 4).add(ModAspects.MIND, 4), "seal_empty");
        seal(output, "provider", ModItems.SEALS.get("empty_advanced").get(),
                AspectList.of(ModAspects.EXCHANGE, 4).add(ModAspects.DESIRE, 4), "seal_provide");
        seal(output, "guard", ModItems.SEAL_BLANK.get(),
                AspectList.of(ModAspects.AVERSION, 8).add(ModAspects.PROTECT, 8), "seal_guard");
        seal(output, "guard_advanced", ModItems.SEALS.get("guard").get(),
                AspectList.of(ModAspects.SENSES, 8).add(ModAspects.MIND, 8), "seal_guard");
        seal(output, "lumber", ModItems.SEALS.get("breaker").get(),
                AspectList.of(ModAspects.PLANT, 16).add(ModAspects.SENSES, 8), "seal_lumber");
        seal(output, "use", ModItems.SEAL_BLANK.get(),
                AspectList.of(ModAspects.CRAFT, 8).add(ModAspects.SENSES, 4).add(ModAspects.MIND, 4), "seal_use");

        // coal boiled until it is four coals, and glowstone boiled until it is light with nothing under it
        crucible(output, "bath_salts", Ingredient.of(ModItems.SALIS_MUNDUS),
                AspectList.of(ModAspects.MIND, 6).add(ModAspects.AIR, 6).add(ModAspects.ORDER, 6)
                        .add(ModAspects.LIFE, 6),
                new ItemStack(ModItems.BATH_SALTS.get()), "bath_salts");
        crucible(output, "alumentum", Ingredient.of(ItemTags.COALS),
                AspectList.of(ModAspects.ENERGY, 3).add(ModAspects.FIRE, 3).add(ModAspects.ENTROPY, 3),
                new ItemStack(ModItems.ALUMENTUM.get()), "alumentum");
        crucible(output, "nitor", Ingredient.of(Items.GLOWSTONE_DUST),
                AspectList.of(ModAspects.ENERGY, 3).add(ModAspects.FIRE, 3).add(ModAspects.LIGHT, 3),
                new ItemStack(ModBlocks.NITOR.get(DyeColor.WHITE).get(), NITOR_AT_A_TIME), "nitor");
        nitorDyes(output);
        leftovers(output);
    }

    /** How many flames one boiling of glowstone makes. */
    private static final int NITOR_AT_A_TIME = 4;

    /**
     * A flame takes a dye like wool does.
     * <p>
     * Any of the sixteen goes in and the dyed one comes out, so a flame can be changed its mind about rather than
     * boiled again. The white one is no more the original than the rest -- it is only the one the crucible makes.
     */
    private Ingredient shard(me.moonscenty.alchemia.block.CrystalType type) {
        return Ingredient.of(ModItems.SHARDS.get(type));
    }

    /**
     * The odds and ends of step 14C, all the original's: flesh packed into a block and soap made of it, tallow
     * candles and their dyeing, the pattern crafter, and the sinister lodestone.
     */
    private void leftovers(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.FLESH_BLOCK.get())
                .define('F', Items.ROTTEN_FLESH).pattern("FFF").pattern("FFF").pattern("FFF")
                .unlockedBy("has_flesh", has(Items.ROTTEN_FLESH)).save(output, Alchemia.id("flesh_block"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.ROTTEN_FLESH, 9)
                .requires(ModBlocks.FLESH_BLOCK.get())
                .unlockedBy("has_flesh_block", has(ModBlocks.FLESH_BLOCK.get()))
                .save(output, Alchemia.id("rotten_flesh_from_block"));
        crucible(output, "sanity_soap", Ingredient.of(ModBlocks.FLESH_BLOCK.get()),
                AspectList.of(ModAspects.MIND, 16).add(ModAspects.ELDRITCH, 16).add(ModAspects.ORDER, 16)
                        .add(ModAspects.LIFE, 16),
                new ItemStack(ModItems.SANITY_SOAP.get()), "sanity_soap");

        // a string wick in two of tallow makes three white candles; a dye colours one, and bone meal bleaches any
        var white = ModBlocks.TALLOW_CANDLES.get(DyeColor.WHITE).get();
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, white, 3)
                .define('S', Items.STRING).define('T', ModItems.TALLOW.get()).pattern("S").pattern("T").pattern("T")
                .unlockedBy("has_tallow", has(ModItems.TALLOW.get())).save(output, Alchemia.id("tallow_candle"));
        for (DyeColor colour : DyeColor.values()) {
            if (colour == DyeColor.WHITE) {
                continue;
            }
            ShapelessRecipeBuilder.shapeless(RecipeCategory.DECORATIONS, ModBlocks.TALLOW_CANDLES.get(colour).get())
                    .requires(white).requires(colour.getTag())
                    .unlockedBy("has_candle", has(white))
                    .save(output, Alchemia.id(colour.getSerializedName() + "_tallow_candle"));
        }
        ShapelessRecipeBuilder.shapeless(RecipeCategory.DECORATIONS, white)
                .requires(Ingredient.of(ModBlocks.TALLOW_CANDLES.values().stream()
                        .map(candle -> new ItemStack(candle.get())))).requires(Items.BONE_MEAL)
                .unlockedBy("has_candle", has(white)).save(output, Alchemia.id("white_tallow_candle_bleached"));

        arcane(output, "pattern_crafter", "pattern_crafter", new ItemStack(ModBlocks.PATTERN_CRAFTER.get()),
                AspectList.of(ModAspects.ORDER, 100).add(ModAspects.FIRE, 100).add(ModAspects.ENTROPY, 100),
                Map.of('H', Ingredient.of(Items.HOPPER), 'G', Ingredient.of(ModItems.BRASS_GEAR),
                        'C', Ingredient.of(Items.CRAFTING_TABLE), 'W', Ingredient.of(ModBlocks.GREATWOOD.planks())),
                " H ", "GCG", " W ");

        infusion(output, "sinister_stone", Ingredient.of(Items.FLINT),
                List.of(Ingredient.of(ModTags.Items.NITOR), shard(me.moonscenty.alchemia.block.CrystalType.ORDER),
                        Ingredient.of(ModItems.KNOWLEDGE_FRAGMENT),
                        shard(me.moonscenty.alchemia.block.CrystalType.ENTROPY)),
                new ItemStack(ModItems.SINISTER_STONE.get()),
                AspectList.of(ModAspects.SENSES, 8).add(ModAspects.DARKNESS, 8).add(ModAspects.ELDRITCH, 8)
                        .add(ModAspects.AURA, 8), 5, "sinister_stone");
    }

    private void nitorDyes(RecipeOutput output) {
        for (DyeColor colour : DyeColor.values()) {
            ShapelessRecipeBuilder.shapeless(RecipeCategory.DECORATIONS, ModBlocks.NITOR.get(colour).get())
                    .requires(ModTags.Items.NITOR)
                    .requires(DyeItem.byColor(colour))
                    .unlockedBy("has_nitor", has(ModTags.Items.NITOR))
                    .save(output, Alchemia.id(ModBlocks.name(colour) + "_from_dye"));
        }
    }

    /**
     * Void metal, which is boiled rather than mined.
     * <p>
     * A wheat seed steeped in darkness stops being a seed; the thing it becomes, steeped again in metal, comes out
     * as an ingot. The prices are the original's, and so is the oddity that the second boiling wants a point of
     * flux in the water -- the metal will not set without something wrong in it.
     */
    private void voidMetal(RecipeOutput output) {
        crucible(output, "void_seed", Ingredient.of(Items.WHEAT_SEEDS),
                AspectList.of(ModAspects.DARKNESS, 8).add(ModAspects.VOID, 8).add(ModAspects.ELDRITCH, 2),
                new ItemStack(ModItems.VOID_SEED.get()), "void_metal");
        crucible(output, "void_ingot", Ingredient.of(ModItems.VOID_SEED),
                AspectList.of(ModAspects.METAL, 7).add(ModAspects.FLUX, 1),
                new ItemStack(ModItems.VOID_INGOT.get()), "void_metal");

        compress(output, ModItems.VOID_NUGGET, ModTags.Items.NUGGETS_VOID,
                ModItems.VOID_INGOT, ModTags.Items.INGOTS_VOID);

        // the last of the caps, and the dearest: nine times what an iron one costs, across all four of the primals
        arcane(output, "wand_cap_void_inert", "void_metal",
                new ItemStack(ModItems.INERT_CAPS.get("void").get()),
                AspectList.of(ModAspects.ENTROPY, 72).add(ModAspects.ORDER, 72)
                        .add(ModAspects.FIRE, 72).add(ModAspects.AIR, 72),
                Map.of('N', Ingredient.of(ModTags.Items.NUGGETS_VOID)), "NNN", "N N");
    }

    /** A control seal steeped in a crucible out of a blank, or out of a lesser seal of its kind. */
    private void seal(RecipeOutput output, String kind, net.minecraft.world.level.ItemLike from, AspectList aspects,
            String research) {
        crucible(output, "seal_" + kind, Ingredient.of(from), aspects, new ItemStack(ModItems.SEALS.get(kind).get()),
                research);
    }

    private void crucible(RecipeOutput output, String name, Ingredient catalyst, AspectList aspects,
            ItemStack result, String research) {
        output.accept(Alchemia.id(name),
                new CrucibleRecipe(catalyst, aspects, result, Optional.of(Alchemia.id(research))), null);
    }
}
