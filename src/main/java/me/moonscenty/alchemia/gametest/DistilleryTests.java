package me.moonscenty.alchemia.gametest;

import java.util.List;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.block.JarBlock;
import me.moonscenty.alchemia.block.entity.JarBlockEntity;
import me.moonscenty.alchemia.crafting.LabelRecipe;
import me.moonscenty.alchemia.item.PhialItem;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModDataComponents;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * The fittings of the distillery: what carries essentia by hand, and what a jar is told to do.
 * <p>
 * The pipes have tests of their own. These are about the things a person picks up and uses on a vessel, which is
 * the other half of how essentia moves about.
 */
@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class DistilleryTests {
    private static final String TEMPLATE = "empty_32x40x32";
    private static final BlockPos START = new BlockPos(2, 1, 2);

    private static JarBlockEntity jar(GameTestHelper helper, BlockPos at) {
        helper.setBlock(at, ModBlocks.JAR.get());
        return (JarBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(at));
    }

    /** Puts the stack in a hand and uses it on the top of that block, the way a person would. */
    private static void useOn(GameTestHelper helper, BlockPos at, ItemStack held) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, held);
        BlockPos where = helper.absolutePos(at);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(where), Direction.UP, where, false);
        held.getItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
    }

    private static ItemStack phialOf(net.minecraft.core.Holder<me.moonscenty.alchemia.aspect.Aspect> aspect) {
        return PhialItem.filled(new ItemStack(ModItems.PHIAL.get()), aspect);
    }

    /** A phial draws a whole draught out of a jar and pours the same draught back in. */
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

    /** Nothing happens by halves: a jar with less than a draught in it will not fill a phial. */
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

    /** A labelled jar takes the one thing it is labelled with, whether or not there is any of it in there. */
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

    /** A braced jar is filled and never emptied, whatever asks. */
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

    /** A blank label held up to a full phial takes its name, and the phial comes back empty rather than used up. */
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

    /** A written label laid down on its own is rubbed out. */
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
}
