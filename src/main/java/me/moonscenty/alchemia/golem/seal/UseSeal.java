package me.moonscenty.alchemia.golem.seal;

import java.util.Arrays;
import java.util.List;

import me.moonscenty.alchemia.golem.Golem;
import me.moonscenty.alchemia.golem.GolemTrait;
import me.moonscenty.alchemia.golem.task.Task;
import me.moonscenty.alchemia.golem.task.Tasks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;

/**
 * Use: a deft, smart golem carrying something its filter lets by uses it on the block the seal is on, through the
 * face it is on, as a player would: a right click, or a left click if set to. It can be set to use an empty hand,
 * to click as if crouching, to work only where the block has gone, and to ask providing seals for its first
 * whitelisted thing when the golem has none. Whatever the click leaves in the hand comes back to the golem.
 */
public class UseSeal extends Seal {
    private static final int LEFT = 4;
    private static final int EMPTY = 5;
    private static final int EMPTY_HAND = 6;
    private static final int SNEAK = 7;
    private static final int PROVISION = 8;

    private int delay;
    private int watched = Integer.MIN_VALUE;

    public UseSeal() {
        super("use", 1, new Toggle("meta", true), new Toggle("nbt", true), new Toggle("ore", false),
                new Toggle("mod", false), new Toggle("left", false), new Toggle("empty", false),
                new Toggle("emptyhand", false), new Toggle("sneak", false), new Toggle("provision.wl", false));
        this.delay = java.util.concurrent.ThreadLocalRandom.current().nextInt(49);
    }

    /** A quarter second at a time, one task open at the block, if the block is there (or gone, if set so). */
    @Override
    public void tick(ServerLevel level, SealEntity seal) {
        if (delay++ % 5 != 0) {
            return;
        }
        Task old = Tasks.get(level, watched);
        if (old == null || old.suspended() || old.completed()) {
            if (toggle(EMPTY) != level.isEmptyBlock(seal.pos().pos())) {
                return;
            }
            Task task = new Task(seal.pos(), seal.pos().pos());
            task.setPriority(seal.priority());
            Tasks.add(level, task);
            watched = task.id();
        }
    }

    @Override
    public boolean onTaskCompletion(ServerLevel level, Golem golem, Task task) {
        SealPos at = task.seal();
        if (toggle(EMPTY) == level.isEmptyBlock(task.pos())) {
            ItemStack click = golem.carrying()[0];
            if (!filter().get(0).isEmpty()) {
                click = Matching.firstAllowed(filter(), blacklist(), Arrays.asList(golem.carrying()), rule()).stack();
            }
            if (!click.isEmpty() || toggle(EMPTY_HAND)) {
                FakePlayer hands = Fieldwork.hands(level, golem);
                ItemStack held = click.copy();
                if (!click.isEmpty()) {
                    golem.drop(click.copy());
                }
                hands.setItemInHand(InteractionHand.MAIN_HAND, held);
                hands.setShiftKeyDown(toggle(SNEAK));
                if (toggle(LEFT)) {
                    hands.gameMode.handleBlockBreakAction(task.pos(),
                            ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, at.face(),
                            level.getMaxBuildHeight(), 0);
                } else {
                    if (held.getItem() instanceof BlockItem item && !room(level, item, task.pos())) {
                        // out of the way of what it is about to put down, as the original stepped aside
                        golem.setPos(golem.getX() + at.face().getStepX(), golem.getY() + at.face().getStepY(),
                                golem.getZ() + at.face().getStepZ());
                    }
                    hands.gameMode.useItemOn(hands, level, held, InteractionHand.MAIN_HAND,
                            new BlockHitResult(Vec3.atCenterOf(task.pos()), at.face(), task.pos(), false));
                }
                golem.addRankXp(1);
                giveBack(hands, golem);
                golem.swing(InteractionHand.MAIN_HAND);
            }
        }
        task.setSuspended(true);
        return true;
    }

    /** Whether nothing stands where a block would go. */
    private static boolean room(Level level, BlockItem item, BlockPos pos) {
        AABB box = item.getBlock().defaultBlockState().getCollisionShape(level, pos).isEmpty() ? null
                : item.getBlock().defaultBlockState().getCollisionShape(level, pos).bounds().move(pos);
        return box == null || level.getEntities(null, box).isEmpty();
    }

    /** Everything the stand-in player is left holding goes back to the golem, or falls at its feet. */
    private static void giveBack(FakePlayer hands, Golem golem) {
        for (int slot = 0; slot < hands.getInventory().getContainerSize(); slot++) {
            ItemStack stack = hands.getInventory().getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            ItemStack left = golem.canCarry(stack) ? golem.hold(stack) : stack;
            if (!left.isEmpty()) {
                golem.spawnAtLocation(left, 0.25F);
            }
            hands.getInventory().setItem(slot, ItemStack.EMPTY);
        }
        hands.setShiftKeyDown(false);
    }

    /** A golem must carry something to use, unless an empty hand will do; set to, it asks for some. */
    @Override
    public boolean canGolemPerform(Golem golem, Task task) {
        if (toggle(EMPTY_HAND)) {
            return true;
        }
        boolean found = Matching.firstAllowed(filter(), blacklist(), Arrays.asList(golem.carrying()), rule())
                .found();
        if (!found && toggle(PROVISION) && !blacklist() && !filter().get(0).isEmpty()) {
            SealEntity seal = Seals.get(golem.level(), task.seal());
            if (seal != null) {
                Provisioning.request(golem.level(), seal, filter().get(0));
            }
        }
        return found;
    }

    @Override
    public boolean canPlaceAt(Level level, BlockPos pos, Direction face) {
        return true;
    }

    @Override
    public int[] categories() {
        return new int[] {FILTER, OPTIONS, PRIORITY, TRAITS};
    }

    @Override
    public List<GolemTrait> required() {
        return List.of(GolemTrait.DEFT, GolemTrait.SMART);
    }
}
