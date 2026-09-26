package me.moonscenty.alchemia.item;

import java.util.List;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModDataComponents;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModWandParts;
import me.moonscenty.alchemia.wand.WandCap;
import me.moonscenty.alchemia.wand.WandRod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

/**
 * A wand: a rod with a metal cap at each end, holding vis drawn out of the air around its bearer.
 * <p>
 * Vis is kept in hundredths rather than whole points. Caps change what a thing costs by a fraction — an alchemium
 * cap pays nine tenths of the asking price — and a price rounded to whole vis every time would either round that
 * discount away or hand it out twice over.
 */
public class WandItem extends Item implements VisHolder {
    /** How many hundredths make one point of vis. */
    public static final int FINE = 100;
    /** How often a wand pulls at the aura, in ticks. */
    private static final int DRAWS_EVERY = 5;
    /** A trickling rod fills itself only this far, as a share of what the rod holds. */
    private static final float TRICKLES_TO = 0.5F;

    public WandItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    // --- what it is made of -------------------------------------------------

    public static WandRod rod(ItemStack stack) {
        Holder<WandRod> rod = stack.get(ModDataComponents.WAND_ROD.get());
        return rod == null ? ModWandParts.WOOD.get() : rod.value();
    }

    public static WandCap cap(ItemStack stack) {
        Holder<WandCap> cap = stack.get(ModDataComponents.WAND_CAP.get());
        return cap == null ? ModWandParts.IRON.get() : cap.value();
    }

    public static ItemStack of(Holder<WandRod> rod, Holder<WandCap> cap) {
        ItemStack wand = new ItemStack(ModItems.WAND.get());
        wand.set(ModDataComponents.WAND_ROD.get(), rod);
        wand.set(ModDataComponents.WAND_CAP.get(), cap);
        return wand;
    }

    /** How much of one primal the wand holds at most, in whole vis. */
    public static int capacity(ItemStack stack) {
        return rod(stack).capacity();
    }

    /** What is in the wand, in hundredths. */
    private static AspectList fine(ItemStack stack) {
        AspectList stored = stack.get(ModDataComponents.VIS.get());
        return stored == null ? AspectList.EMPTY : stored;
    }

    // --- VisHolder ----------------------------------------------------------

    @Override
    public int held(ItemStack stack, Holder<Aspect> aspect) {
        return fine(stack).get(aspect) / FINE;
    }

    /**
     * Pays a price out of the wand.
     * <p>
     * The cap is applied here rather than where the price was quoted, so that what the workbench shows is the price
     * of the work and what the wand loses is what this particular wand charges for doing it.
     */
    @Override
    public void take(ItemStack stack, AspectList cost, Player player) {
        float rate = cap(stack).discount();
        AspectList left = fine(stack);
        for (Holder<Aspect> aspect : cost.sortedByName()) {
            int asked = Math.round(cost.get(aspect) * FINE * rate);
            left = left.withAmount(aspect, Math.max(0, left.get(aspect) - asked));
        }
        stack.set(ModDataComponents.VIS.get(), left);
    }

    /**
     * Whether the whole price could be paid. The cap is counted in, so an iron cap can leave a wand a point short
     * of work a brass one would manage with the same vis in it.
     */
    @Override
    public boolean holds(ItemStack stack, AspectList cost) {
        float rate = cap(stack).discount();
        AspectList have = fine(stack);
        for (Holder<Aspect> aspect : cost.sortedByName()) {
            if (have.get(aspect) < Math.round(cost.get(aspect) * FINE * rate)) {
                return false;
            }
        }
        return true;
    }

    /** Puts vis in, in hundredths, and says how much would not fit. */
    private static int put(ItemStack stack, Holder<Aspect> aspect, int amount) {
        if (!aspect.value().isPrimal()) {
            return amount;
        }
        int room = capacity(stack) * FINE;
        AspectList stored = fine(stack);
        int after = Math.min(room, stored.get(aspect) + amount);
        stack.set(ModDataComponents.VIS.get(), stored.withAmount(aspect, after));
        return Math.max(0, stored.get(aspect) + amount - room);
    }

    // --- filling up ---------------------------------------------------------

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity holder, int slot, boolean selected) {
        if (level.isClientSide || !(holder instanceof Player player) || player.tickCount % DRAWS_EVERY != 0) {
            return;
        }
        trickle(stack, player);
        int rate = drawRate(selected, slot);
        if (rate > 0 && level instanceof ServerLevel served) {
            charge(stack, served, player.blockPosition(), rate);
        }
    }

    /**
     * How hard a wand pulls at the aura around it.
     * <p>
     * Only the wand actually in hand draws anything for now. The original let a researcher tap the aura from the
     * belt once they had worked out how; that research does not exist yet, so the belt is left at nothing.
     */
    private static int drawRate(boolean selected, int slot) {
        return selected ? 1 : 0;
    }

    /**
     * Draws vis out of the chunk into the wand, and says whether any came.
     * <p>
     * The cap's own bonus is added here rather than by whoever asked, so a wand held in a hand and one sitting
     * under a charger are both worth as much more as the cap on them is.
     */
    public static boolean charge(ItemStack stack, ServerLevel level, BlockPos at, int amount) {
        amount += cap(stack).chargeBonus();
        int room = capacity(stack);
        boolean any = false;

        for (Holder<Aspect> aspect : ModAspects.primals()) {
            if (amount <= 0) {
                break;
            }
            int space = room - fine(stack).get(aspect) / FINE;
            if (space <= 0 || AuraHandler.shouldSpare(level, at, aspect)) {
                continue;
            }
            int drawn = AuraHandler.drainAvailable(level, at, aspect, Math.min(amount, space));
            if (drawn > 0) {
                put(stack, aspect, drawn * FINE);
                amount -= drawn;
                any = true;
            }
        }
        return any;
    }

    /** A rod that makes its own vis tops itself up, to halfway and no further. */
    private static void trickle(ItemStack stack, Player player) {
        rod(stack).trickle().ifPresent(aspect -> {
            if (fine(stack).get(aspect) < capacity(stack) * FINE * TRICKLES_TO) {
                put(stack, aspect, FINE);
            }
        });
    }

    // --- what it can be pointed at ------------------------------------------

    /**
     * A shelf of books and a wand make an Alchemonomicon. The shelf is used up doing it, which is the whole cost of
     * starting down this road.
     */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!level.getBlockState(pos).is(Blocks.BOOKSHELF)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            level.removeBlock(pos, false);
            ItemEntity book = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.3, pos.getZ() + 0.5,
                    new ItemStack(ModItems.ALCHEMONOMICON.get()));
            book.setDeltaMovement(0, 0, 0);
            level.addFreshEntity(book);
            level.playSound(null, pos, SoundEvents.ILLUSIONER_CAST_SPELL, SoundSource.BLOCKS, 0.7F, 1.4F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    // --- what it says about itself ------------------------------------------

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.alchemia.wand.named",
                cap(stack).displayName(), rod(stack).displayName());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        int room = capacity(stack);
        for (Holder<Aspect> aspect : ModAspects.primals()) {
            int have = held(stack, aspect);
            if (have > 0) {
                lines.add(Component.translatable("item.alchemia.wand.vis",
                                aspect.value().displayName(), have, room)
                        .withStyle(ChatFormatting.GRAY));
            }
        }
        if (cap(stack).chargeBonus() > 0) {
            lines.add(Component.translatable("item.alchemia.wand.charge", cap(stack).chargeBonus())
                    .withStyle(ChatFormatting.AQUA));
        }
        if (cap(stack).discount() != 1.0F) {
            lines.add(Component.translatable("item.alchemia.wand.discount",
                            Math.round((cap(stack).discount() - 1.0F) * 100))
                    .withStyle(cap(stack).discount() < 1.0F ? ChatFormatting.GREEN : ChatFormatting.RED));
        }
    }

    /** The bar under the icon shows the primal the wand is fullest of, so an empty wand reads at a glance. */
    @Override
    public boolean isBarVisible(ItemStack stack) {
        return !fine(stack).isEmpty();
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int room = capacity(stack) * FINE;
        int most = 0;
        for (Holder<Aspect> aspect : ModAspects.primals()) {
            most = Math.max(most, fine(stack).get(aspect));
        }
        return Math.round(13.0F * most / room);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        Holder<Aspect> fullest = ModAspects.primals().get(0);
        for (Holder<Aspect> aspect : ModAspects.primals()) {
            if (fine(stack).get(aspect) > fine(stack).get(fullest)) {
                fullest = aspect;
            }
        }
        return fullest.value().color();
    }
}
