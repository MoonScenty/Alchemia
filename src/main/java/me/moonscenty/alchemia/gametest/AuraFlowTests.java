package me.moonscenty.alchemia.gametest;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.aura.AuraChunk;
import me.moonscenty.alchemia.aura.AuraGeneration;
import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.aura.AuraTicker;
import me.moonscenty.alchemia.aura.BiomePaint;
import me.moonscenty.alchemia.aura.FluxBolt;
import me.moonscenty.alchemia.aura.FluxEvents;
import me.moonscenty.alchemia.aura.ModAuraAttachment;
import me.moonscenty.alchemia.aura.node.AuraNode;
import me.moonscenty.alchemia.aura.node.NodeType;
import me.moonscenty.alchemia.block.entity.RechargePedestalBlockEntity;
import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModWandParts;
import me.moonscenty.alchemia.worldgen.ModBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Alchemia.MODID)
@PrefixGameTestTemplate(false)
public class AuraFlowTests {
    private static final String TEMPLATE = "empty_32x40x32";
    private static final int BASE = AuraGeneration.BASE;
    private static final BlockPos HERE = new BlockPos(3, 2, 3);

    private static void air(GameTestHelper helper, int air) {
        helper.getLevel().getChunkAt(helper.absolutePos(HERE)).setData(ModAuraAttachment.AURA,
                new AuraChunk(BASE, AspectList.EMPTY.add(ModAspects.AIR, air)));
    }

    private static ChunkPos chunk(GameTestHelper helper) {
        return new ChunkPos(helper.absolutePos(HERE));
    }

    @GameTest(template = TEMPLATE)
    public static void fedAuraArrivesWhenTheChunkSettles(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        air(helper, 0);
        AuraHandler.recharge(level, helper.absolutePos(HERE), ModAspects.AIR, 50, level.getRandom());
        helper.assertValueEqual(AuraHandler.get(level, helper.absolutePos(HERE), ModAspects.AIR), 0, "not yet");
        helper.assertValueEqual(AuraTicker.owed(level, chunk(helper), ModAspects.AIR), 50, "but owed");
        AuraTicker.settle(level, chunk(helper));
        helper.assertValueEqual(AuraHandler.get(level, helper.absolutePos(HERE), ModAspects.AIR), 50,
                "paid in ten portions of five");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void anOverfullChunkSendsItsFeedFurtherOff(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        air(helper, BASE * 2);
        AuraHandler.recharge(level, helper.absolutePos(HERE), ModAspects.AIR, 100, level.getRandom());
        AuraTicker.settle(level, chunk(helper));
        helper.assertTrue(AuraHandler.get(level, helper.absolutePos(HERE), ModAspects.AIR) <= BASE * 2,
                "nothing more went into the full chunk");
        ChunkPos at = chunk(helper);
        int sent = 0;
        for (Direction side : Direction.Plane.HORIZONTAL) {
            for (int far = 1; far <= 3; far++) {
                sent += AuraTicker.owed(level, new ChunkPos(at.x + side.getStepX() * far, at.z + side.getStepZ() * far),
                        ModAspects.AIR);
            }
        }
        helper.assertTrue(sent > 0 && sent <= 100, "some went one to three chunks off, " + sent);
        helper.assertValueEqual(sent % 10, 0, "in portions of ten");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aTaintedNodeFeedsItsOwnAspectToo(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        air(helper, BASE);
        AuraNode node = new AuraNode(level);
        BlockPos at = helper.absolutePos(HERE.above(3));
        node.moveTo(at.getX() + 0.5, at.getY() + 0.5, at.getZ() + 0.5, 0.0F, 0.0F);
        node.drawUp(RandomSource.create(4L));
        node.setType(NodeType.TAINTED);
        node.setAspect(ModAspects.AIR);
        for (int i = 0; i < NodeType.PERIOD * 2 + 2; i++) {
            node.tick();
        }
        helper.assertTrue(AuraTicker.owed(level, chunk(helper), ModAspects.AIR) > 0, "aer is owed to the land");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aHeldNodeIsNotSwallowed(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos at = helper.absolutePos(HERE.above(5));
        AuraNode big = new AuraNode(level);
        big.moveTo(at.getX() + 0.5, at.getY() + 0.5, at.getZ() + 0.5, 0.0F, 0.0F);
        big.drawUp(RandomSource.create(5L));
        big.setSize(40);
        AuraNode held = new AuraNode(level);
        held.moveTo(at.getX() + 0.5, at.getY() + 0.5, at.getZ() + 0.5, 0.0F, 0.0F);
        held.drawUp(RandomSource.create(6L));
        held.setSize(5);
        held.setHeld(true);
        level.addFreshEntity(big);
        level.addFreshEntity(held);
        for (int i = 0; i < 5; i++) {
            big.tick();
        }
        helper.assertTrue(held.isAlive(), "the held node is still there");
        helper.assertValueEqual(big.getSize(), 40, "and the big one has not grown on it");
        big.discard();
        held.discard();
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aPedestalDrawsFromASpentChunk(GameTestHelper helper) {
        air(helper, BASE / 20);
        helper.setBlock(HERE, ModBlocks.RECHARGE_PEDESTAL.get());
        RechargePedestalBlockEntity pedestal = (RechargePedestalBlockEntity) helper.getBlockEntity(HERE);
        ItemStack wand = WandItem.of(ModWandParts.SILVERWOOD, ModWandParts.GOLD);
        pedestal.setItem(0, wand);
        BlockPos where = helper.absolutePos(HERE);
        RechargePedestalBlockEntity.tick(helper.getLevel(), where, helper.getLevel().getBlockState(where), pedestal);
        ItemStack charged = pedestal.getItem(0);
        helper.assertTrue(((WandItem) charged.getItem()).held(charged, ModAspects.AIR) > 0,
                "a pedestal does not spare a chunk below a tenth");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void fluxLightningStrikesOneToThreeTimes(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int i = 0; i < 30; i++) {
            FluxBolt bolt = new FluxBolt(level, Vec3.atBottomCenterOf(helper.absolutePos(HERE)));
            helper.assertTrue(bolt.strikesLeft() >= 1 && bolt.strikesLeft() <= 3, "strikes " + bolt.strikesLeft());
            bolt.discard();
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "aura_taint_biome")
    public static void noFluxWeatherNextToTaintedLand(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos at = helper.absolutePos(HERE);
        helper.assertTrue(!FluxEvents.nearTaint(level, at), "clear land at first");
        var taint = level.registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(ModBiomes.TAINTED_LAND);
        BiomePaint.paint(level, at.relative(Direction.EAST, 16), taint);
        helper.assertTrue(FluxEvents.nearTaint(level, at), "tainted land sixteen off keeps the weather away");
        helper.succeed();
    }
}
