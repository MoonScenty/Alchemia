package me.moonscenty.alchemia.gametest;

import java.util.List;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.JarBlock;
import me.moonscenty.alchemia.block.entity.AlembicBlockEntity;
import me.moonscenty.alchemia.block.entity.JarBlockEntity;
import me.moonscenty.alchemia.crafting.LabelRecipe;
import me.moonscenty.alchemia.item.PhialItem;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.registry.ModDataComponents;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class DistilleryTests {
    private static final String TEMPLATE = "empty_32x40x32";
    private static final BlockPos START = new BlockPos(2, 1, 2);

    private static JarBlockEntity jar(GameTestHelper helper, BlockPos at) {
        helper.setBlock(at, ModBlocks.JAR.get());
        return (JarBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(at));
    }

    private static void useOn(GameTestHelper helper, BlockPos at, ItemStack held) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, held);
        BlockPos where = helper.absolutePos(at);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(where), Direction.UP, where, false);
        BlockState state = helper.getLevel().getBlockState(where);

        ItemInteractionResult first = state.useItemOn(player.getMainHandItem(), helper.getLevel(), player,
                InteractionHand.MAIN_HAND, hit);
        if (first.consumesAction()) {
            return;
        }
        if (first == ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
                && state.useWithoutItem(helper.getLevel(), player, hit).consumesAction()) {
            return;
        }
        player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
    }

    private static ItemStack phialOf(net.minecraft.core.Holder<me.moonscenty.alchemia.aspect.Aspect> aspect) {
        return PhialItem.filled(new ItemStack(ModItems.PHIAL.get()), aspect);
    }

    @GameTest(template = TEMPLATE)
    public static void aPhialCarriesADraught(GameTestHelper helper) {
        JarBlockEntity jar = jar(helper, START);
        for (int one = 0; one < PhialItem.DRAUGHT; one++) {
            jar.accept(ModAspects.FIRE);
        }

        useOn(helper, START, new ItemStack(ModItems.PHIAL.get()));
        helper.assertValueEqual(jar.amount(), 0, "the phial took the lot");

        useOn(helper, START, phialOf(ModAspects.FIRE));
        helper.assertValueEqual(jar.amount(), PhialItem.DRAUGHT, "and poured it back");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aVesselDoesNotTalkOverThePhial(GameTestHelper helper) {
        JarBlockEntity jar = jar(helper, START);
        for (int one = 0; one < PhialItem.DRAUGHT; one++) {
            jar.accept(ModAspects.FIRE);
        }

        useOn(helper, START, new ItemStack(ModItems.PHIAL.get()));
        helper.assertValueEqual(jar.amount(), 0, "the phial got at it through the jar being chatty");

        helper.setBlock(START.above(), ModBlocks.ALEMBIC.get());
        AlembicBlockEntity alembic = (AlembicBlockEntity) helper.getLevel()
                .getBlockEntity(helper.absolutePos(START.above()));
        for (int one = 0; one < PhialItem.DRAUGHT; one++) {
            alembic.accept(ModAspects.WATER);
        }
        useOn(helper, START.above(), new ItemStack(ModItems.PHIAL.get()));
        helper.assertValueEqual(alembic.amount(), 0, "and at the alembic too");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aPhialWillNotTakeHalfADraught(GameTestHelper helper) {
        JarBlockEntity jar = jar(helper, START);
        for (int one = 0; one < PhialItem.DRAUGHT - 1; one++) {
            jar.accept(ModAspects.FIRE);
        }

        useOn(helper, START, new ItemStack(ModItems.PHIAL.get()));
        helper.assertValueEqual(jar.amount(), PhialItem.DRAUGHT - 1, "it was left where it was");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aLabelledJarTakesOneThing(GameTestHelper helper) {
        JarBlockEntity jar = jar(helper, START);
        jar.label(ModAspects.FIRE);

        helper.assertTrue(jar.wants(ModAspects.FIRE), "an empty labelled jar still wants what it says");
        helper.assertTrue(!jar.wants(ModAspects.METAL), "and will not take anything else");
        helper.assertTrue(!jar.accept(ModAspects.METAL), "not even when it is handed some");

        jar.accept(ModAspects.FIRE);
        jar.release(ModAspects.FIRE);
        helper.assertValueEqual(jar.amount(), 0, "emptied again");
        helper.assertTrue(jar.label().isPresent() && jar.holding().isPresent(),
                "and it still says what belongs in it");
        helper.assertTrue(helper.getBlockState(START).getValue(JarBlock.LABELLED), "which the model is told about");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aBracedJarGivesNothingUp(GameTestHelper helper) {
        JarBlockEntity jar = jar(helper, START);
        for (int one = 0; one < PhialItem.DRAUGHT; one++) {
            jar.accept(ModAspects.FIRE);
        }
        jar.brace(true);

        helper.assertTrue(!jar.release(ModAspects.FIRE), "a tube gets nothing out of it");
        helper.assertTrue(jar.accept(ModAspects.FIRE), "but it still takes more");

        useOn(helper, START, new ItemStack(ModItems.PHIAL.get()));
        helper.assertValueEqual(jar.amount(), PhialItem.DRAUGHT + 1, "and a phial gets nothing out of it either");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aBrokenJarKeepsWhatIsInIt(GameTestHelper helper) {
        JarBlockEntity jar = jar(helper, START);
        for (int one = 0; one < 20; one++) {
            jar.accept(ModAspects.FIRE);
        }
        jar.label(ModAspects.FIRE);

        List<ItemStack> dropped = Block.getDrops(helper.getBlockState(START), helper.getLevel(),
                helper.absolutePos(START), jar);
        helper.assertValueEqual(dropped.size(), 1, "one jar came back");

        ItemStack picked = dropped.getFirst();
        AspectList inside = picked.get(ModDataComponents.CONTENTS.get());
        helper.assertTrue(inside != null && inside.get(ModAspects.FIRE) == 20,
                "with the fire still in it, was " + inside);
        helper.assertTrue(picked.get(ModDataComponents.ESSENTIA.get()) != null, "and its label still on it");

        helper.setBlock(START.east(), ModBlocks.JAR.get());
        JarBlockEntity back = (JarBlockEntity) helper.getLevel()
                .getBlockEntity(helper.absolutePos(START.east()));
        back.applyComponentsFromItemStack(picked);
        helper.assertValueEqual(back.amount(), 20, "the essentia came back with it");
        helper.assertTrue(back.label().isPresent(), "and so did the label");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aLabelIsWrittenFromAPhial(GameTestHelper helper) {
        LabelRecipe recipe = new LabelRecipe(CraftingBookCategory.MISC);
        CraftingInput bench = CraftingInput.of(2, 1,
                List.of(new ItemStack(ModItems.JAR_LABEL.get()), phialOf(ModAspects.FIRE)));

        helper.assertTrue(recipe.matches(bench, helper.getLevel()), "a label and a full phial are a recipe");
        ItemStack written = recipe.assemble(bench, helper.getLevel().registryAccess());
        helper.assertTrue(written.is(ModItems.JAR_LABEL.get()), "what comes out is a label");
        helper.assertValueEqual(written.get(ModDataComponents.ESSENTIA.get()).value(), ModAspects.FIRE.value(),
                "with the name of what was in the phial on it");

        ItemStack back = recipe.getRemainingItems(bench).get(1);
        helper.assertTrue(back.is(ModItems.PHIAL.get()) && PhialItem.inside(back).isEmpty(),
                "and the phial is handed back empty");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aLabelIsRubbedOut(GameTestHelper helper) {
        LabelRecipe recipe = new LabelRecipe(CraftingBookCategory.MISC);
        CraftingInput bench = CraftingInput.of(1, 1, List.of(JarBlock.labelFor(ModAspects.FIRE)));

        helper.assertTrue(recipe.matches(bench, helper.getLevel()), "a written label on its own is a recipe");
        helper.assertTrue(recipe.assemble(bench, helper.getLevel().registryAccess())
                .get(ModDataComponents.ESSENTIA.get()) == null, "and what comes out is blank");

        CraftingInput blank = CraftingInput.of(1, 1, List.of(new ItemStack(ModItems.JAR_LABEL.get())));
        helper.assertTrue(!recipe.matches(blank, helper.getLevel()), "a blank one is nothing to work on");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void crouchingTipsAnAlembicOut(GameTestHelper helper) {
        helper.setBlock(START, ModBlocks.ALEMBIC.get());
        AlembicBlockEntity alembic = (AlembicBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(START));
        for (int one = 0; one < 5; one++) {
            alembic.accept(ModAspects.WATER);
        }
        net.minecraft.core.BlockPos here = helper.absolutePos(START);
        int before = me.moonscenty.alchemia.aura.AuraHandler.get(helper.getLevel(), here, ModAspects.FLUX);
        net.minecraft.world.entity.player.Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        helper.useBlock(START, player);
        helper.assertValueEqual(alembic.amount(), 5, "a plain click only reads it");
        player.setShiftKeyDown(true);
        helper.useBlock(START, player);
        helper.assertTrue(alembic.isEmpty(), "crouching tips it out");
        helper.assertValueEqual(me.moonscenty.alchemia.aura.AuraHandler.get(helper.getLevel(), here, ModAspects.FLUX),
                before + 5, "into the air as flux");
        helper.succeed();
    }
}
