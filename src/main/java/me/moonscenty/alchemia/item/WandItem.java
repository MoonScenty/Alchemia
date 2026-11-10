package me.moonscenty.alchemia.item;

import net.minecraft.world.level.block.state.BlockState;
import java.util.List;

import javax.annotation.Nullable;

import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.aspect.AspectList;
import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModDataComponents;
import me.moonscenty.alchemia.registry.ModItems;
import me.moonscenty.alchemia.registry.ModSounds;
import me.moonscenty.alchemia.registry.ModWandParts;
import me.moonscenty.alchemia.wand.WandCap;
import me.moonscenty.alchemia.wand.WandRod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import me.moonscenty.alchemia.wand.Casting;
import me.moonscenty.alchemia.wand.Focus;
import me.moonscenty.alchemia.wand.FocusUpgrade;
import me.moonscenty.alchemia.wand.FocusUpgrades;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Blocks;

public class WandItem extends Item implements VisHolder, Rechargeable {
    // vis is stored in hundredths so fractional cap discounts are not rounded away
    public static final int FINE = 100;
    private static final int DRAWS_EVERY = 5;
    // per rank of frugal, and for sceptre/staff; original's value
    private static final float THRIFT = 0.1F;
    // original's value
    private static final double STAFF_HITS = 6.0;

    private static final float TRICKLES_TO = 0.5F;
    private static final int HELD_FOREVER = 72000;

    public WandItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static WandRod rod(ItemStack stack) {
        Holder<WandRod> rod = stack.get(ModDataComponents.WAND_ROD.get());
        return rod == null ? ModWandParts.WOOD.get() : rod.value();
    }

    public static boolean isStaff(ItemStack stack) {
        return stack.getItem() instanceof WandItem && rod(stack).staff();
    }

    public static WandCap cap(ItemStack stack) {
        Holder<WandCap> cap = stack.get(ModDataComponents.WAND_CAP.get());
        return cap == null ? ModWandParts.IRON.get() : cap.value();
    }

    // returns a copy; the component's stack must not be mutated
    public static ItemStack focus(ItemStack stack) {
        ItemContainerContents fitted = stack.get(ModDataComponents.WAND_FOCUS.get());
        return fitted == null ? ItemStack.EMPTY : fitted.copyOne();
    }

    public static void setFocus(ItemStack stack, ItemStack focus) {
        if (focus.isEmpty()) {
            stack.remove(ModDataComponents.WAND_FOCUS.get());
        } else {
            stack.set(ModDataComponents.WAND_FOCUS.get(),
                    ItemContainerContents.fromItems(java.util.List.of(focus.copy())));
        }
    }

    public static boolean isSceptre(ItemStack stack) {
        return stack.getItem() instanceof WandItem && stack.has(ModDataComponents.SCEPTRE.get());
    }

    public static ItemStack sceptre(Holder<WandRod> rod, Holder<WandCap> cap) {
        ItemStack sceptre = of(rod, cap);
        sceptre.set(ModDataComponents.SCEPTRE.get(), net.minecraft.util.Unit.INSTANCE);
        return sceptre;
    }

    public static ItemStack of(Holder<WandRod> rod, Holder<WandCap> cap) {
        ItemStack wand = new ItemStack(ModItems.WAND.get());
        wand.set(ModDataComponents.WAND_ROD.get(), rod);
        wand.set(ModDataComponents.WAND_CAP.get(), cap);
        return wand;
    }

    public static int capacity(ItemStack stack) {
        return isSceptre(stack) ? rod(stack).capacity() * 3 / 2 : rod(stack).capacity();
    }

    private static AspectList fine(ItemStack stack) {
        AspectList stored = stack.get(ModDataComponents.VIS.get());
        return stored == null ? AspectList.EMPTY : stored;
    }

    @Override
    public int held(ItemStack stack, Holder<Aspect> aspect) {
        return fine(stack).get(aspect) / FINE;
    }

    // discounts are applied at payment, so quoted prices stay the base cost
    @Override
    public void take(ItemStack stack, AspectList cost, Player player, Spending why) {
        AspectList left = fine(stack);
        for (Holder<Aspect> aspect : cost.sortedByName()) {
            int asked = Math.round(cost.get(aspect) * FINE * rate(stack, player, why, aspect));
            left = left.withAmount(aspect, Math.max(0, left.get(aspect) - asked));
        }
        stack.set(ModDataComponents.VIS.get(), left);
    }

    @Override
    public boolean holds(ItemStack stack, AspectList cost, Player player, Spending why) {
        AspectList have = fine(stack);
        for (Holder<Aspect> aspect : cost.sortedByName()) {
            if (have.get(aspect) < Math.round(cost.get(aspect) * FINE * rate(stack, player, why, aspect))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public float priceRate(ItemStack stack, Player player, Spending why, Holder<Aspect> aspect) {
        return rate(stack, player, why, aspect);
    }

    private static float rate(ItemStack stack, Player player, Spending why, Holder<Aspect> aspect) {
        float rate = VisDiscount.rate(cap(stack).discount(), player, aspect);
        if (isSceptre(stack)) {
            rate -= THRIFT;
        }
        if (why == Spending.WORKING) {
            rate -= FocusUpgrades.level(focus(stack), FocusUpgrade.FRUGAL) * THRIFT;
            if (rod(stack).staff()) {
                rate -= THRIFT;
            }
        }
        return Math.max(VisDiscount.LEAST, rate);
    }

    public static ItemStack brimming(ItemStack stack) {
        AspectList full = AspectList.EMPTY;
        for (Holder<Aspect> aspect : ModAspects.primals()) {
            full = full.add(aspect, capacity(stack) * FINE);
        }
        stack.set(ModDataComponents.VIS.get(), full);
        return stack;
    }

    public static void addVis(ItemStack stack, Holder<Aspect> aspect, int points) {
        put(stack, aspect, points * FINE);
    }

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

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity holder, int slot, boolean selected) {
        if (level.isClientSide || !(holder instanceof Player player) || player.tickCount % DRAWS_EVERY != 0) {
            return;
        }
        trickle(stack, player);
        int rate = drawRate(player, stack, selected, slot);
        if (rate > 0 && level instanceof ServerLevel served) {
            chargeFrom(stack, served, player.blockPosition(), rate, AuraHandler.spares(player));
        }
    }

    // original's: the held wand draws, and with aura tapping research every wand on the hotbar, faster
    private static int drawRate(Player player, ItemStack stack, boolean selected, int slot) {
        var knowledge = me.moonscenty.alchemia.player.PlayerKnowledge.of(player);
        boolean hotbar = slot < 9 && player.getInventory().items.get(slot) == stack;
        if (hotbar && knowledge.hasResearch(me.moonscenty.alchemia.Alchemia.id("node_tapper2"))) {
            return 3;
        }
        if (hotbar && knowledge.hasResearch(me.moonscenty.alchemia.Alchemia.id("node_tapper1"))) {
            return 2;
        }
        return selected ? 1 : 0;
    }

    @Override
    public boolean charge(ItemStack stack, ServerLevel level, BlockPos at, int amount) {
        return chargeFrom(stack, level, at, amount, true) != null;
    }

    public record Charged(Holder<Aspect> last, boolean full) {
    }

    // the cap bonus is added here so hand-held and charger charging both get it;
    // devices draw even from a chunk that is nearly spent, a carried wand spares it
    @Nullable
    public static Charged chargeFrom(ItemStack stack, ServerLevel level, BlockPos at, int amount, boolean spare) {
        amount += cap(stack).chargeBonus();
        int room = capacity(stack);
        Holder<Aspect> last = null;

        java.util.List<Holder<Aspect>> roomiest = new java.util.ArrayList<>(ModAspects.primals());
        roomiest.sort(java.util.Comparator.comparingInt((Holder<Aspect> aspect) -> fine(stack).get(aspect)));
        for (Holder<Aspect> aspect : roomiest) {
            if (amount <= 0) {
                break;
            }
            int space = room - fine(stack).get(aspect) / FINE;
            if (space <= 0) {
                continue;
            }
            int wanted = Math.min(amount, space);
            int drawn = spare ? AuraHandler.drainAvailableUnlessSpared(level, at, aspect, wanted)
                    : AuraHandler.drainAvailable(level, at, aspect, wanted);
            if (drawn > 0) {
                put(stack, aspect, drawn * FINE);
                amount -= drawn;
                last = aspect;
            }
        }
        return last == null ? null : new Charged(last, amount <= 0);
    }

    private static void trickle(ItemStack stack, Player player) {
        java.util.List<Holder<Aspect>> low = rod(stack).trickles().stream()
                .filter(aspect -> fine(stack).get(aspect) < capacity(stack) * FINE * TRICKLES_TO).toList();
        if (!low.isEmpty()) {
            put(stack, low.get(player.getRandom().nextInt(low.size())), FINE);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack wand = player.getItemInHand(hand);
        Focus focus = Casting.fitted(wand);
        if (focus == null || player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.pass(wand);
        }
        if (focus.perTick()) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(wand);
        }
        ServerLevel served = Casting.served(level);
        boolean went = served != null && Casting.cast(served, player, wand, 0);
        if (went) {
            player.swing(hand, true);
        }
        // consume even when nothing fired, so the use doesn't fall through to block placement
        return InteractionResultHolder.sidedSuccess(wand, level.isClientSide);
    }

    @Override
    public boolean onEntitySwing(ItemStack wand, LivingEntity swinger, net.minecraft.world.InteractionHand hand) {
        if (swinger.level() instanceof ServerLevel served && swinger instanceof Player player
                && focus(wand).is(ModItems.FOCI.get("trade").get())) {
            me.moonscenty.alchemia.wand.spell.Swapping.swing(served, player, wand);
        }
        return false;
    }

    @Override
    public void onUseTick(Level level, LivingEntity using, ItemStack wand, int remaining) {
        if (!(using instanceof Player caster)) {
            return;
        }
        ServerLevel served = Casting.served(level);
        if (served == null) {
            return;
        }
        Focus focus = Casting.fitted(wand);
        if (focus == null || !VisHolder.canPay(wand, focus.cost(), caster, Spending.WORKING)) {
            caster.stopUsingItem();
            return;
        }
        Casting.cast(served, caster, wand, getUseDuration(wand, using) - remaining);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity using) {
        Focus focus = Casting.fitted(stack);
        return focus != null && focus.perTick() ? HELD_FOREVER : 0;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        if (level.getBlockState(pos).is(Blocks.BOOKSHELF) && focus(context.getItemInHand()).isEmpty()) {
            if (!level.isClientSide) {
                level.removeBlock(pos, false);
                drop(level, pos, new ItemStack(ModItems.ALCHEMONOMICON.get()));
                level.playSound(null, pos, ModSounds.WAND.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                level.playSound(null, pos, ModSounds.POOF.get(), SoundSource.BLOCKS, 0.4F,
                        1.0F + (float) level.random.nextGaussian() * 0.05F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (level.getBlockEntity(pos) instanceof me.moonscenty.alchemia.block.entity.ArcaneBoreBlockEntity bore) {
            if (!level.isClientSide) {
                bore.aim(context.getClickedFace());
                level.playSound(null, pos, ModSounds.TOOL.get(), SoundSource.BLOCKS, 0.5F,
                        0.9F + level.random.nextFloat() * 0.2F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (level.getBlockState(pos).getBlock() instanceof me.moonscenty.alchemia.block.ArcaneBoreBaseBlock
                && context.getClickedFace().getAxis().isHorizontal()) {
            if (!level.isClientSide) {
                level.setBlockAndUpdate(pos, level.getBlockState(pos).setValue(
                        me.moonscenty.alchemia.block.ArcaneBoreBaseBlock.FACING, context.getClickedFace()));
                level.playSound(null, pos, ModSounds.TOOL.get(), SoundSource.BLOCKS, 0.3F,
                        1.9F + level.random.nextFloat() * 0.2F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        BlockState clicked = level.getBlockState(pos);
        if (context.getPlayer() != null && clicked.is(ModBlocks.ELDRITCH_ALTAR.get())
                && me.moonscenty.alchemia.block.eldritch.Oculus.open(level, pos, context.getPlayer(),
                        context.getItemInHand())) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (context.getPlayer() != null
                && (clicked.is(Blocks.OBSIDIAN) || clicked.is(Blocks.NETHER_BRICKS) || clicked.is(Blocks.IRON_BARS))
                && me.moonscenty.alchemia.block.InfernalFurnaceBlock.form(level, pos, context.getPlayer(),
                        context.getItemInHand())) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (context.getPlayer() != null
                && (clicked.is(Blocks.PISTON) || clicked.is(Blocks.IRON_BARS) || clicked.is(Blocks.ANVIL)
                        || clicked.is(Blocks.CAULDRON) || clicked.is(me.moonscenty.alchemia.registry.ModBlocks
                                .STONE_TABLE.get()))
                && me.moonscenty.alchemia.block.GolemPressBlock.form(level, pos, context.getPlayer(),
                        context.getItemInHand())) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (context.getPlayer() != null && me.moonscenty.alchemia.block.ThaumatoriumBlock.form(level, pos,
                context.getClickedFace(), context.getPlayer(), context.getItemInHand())) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        // any cauldron, full or empty; its contents are discarded
        if (level.getBlockState(pos).getBlock() instanceof AbstractCauldronBlock) {
            if (!level.isClientSide) {
                level.setBlockAndUpdate(pos, ModBlocks.CRUCIBLE.get().defaultBlockState());
                level.playSound(null, pos, ModSounds.WAND.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                level.playSound(null, pos, ModSounds.POOF.get(), SoundSource.BLOCKS, 0.4F,
                        1.0F + (float) level.random.nextGaussian() * 0.05F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    private static void drop(Level level, BlockPos pos, ItemStack stack) {
        ItemEntity item = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.3, pos.getZ() + 0.5, stack);
        item.setDeltaMovement(0, 0, 0);
        level.addFreshEntity(item);
    }

    @Override
    public Component getName(ItemStack stack) {
        String kind = isStaff(stack) ? "item.alchemia.staff.named"
                : isSceptre(stack) ? "item.alchemia.sceptre.named" : "item.alchemia.wand.named";
        return Component.translatable(kind,
                cap(stack).displayName(), rod(stack).displayName());
    }

    @Override
    public net.minecraft.world.item.component.ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        if (!isStaff(stack)) {
            return super.getDefaultAttributeModifiers(stack);
        }
        return net.minecraft.world.item.component.ItemAttributeModifiers.builder()
                .add(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE,
                        new net.minecraft.world.entity.ai.attributes.AttributeModifier(BASE_ATTACK_DAMAGE_ID,
                                STAFF_HITS, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation
                                        .ADD_VALUE),
                        net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND)
                .build();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        ItemStack fitted = focus(stack);
        if (!fitted.isEmpty()) {
            lines.add(Component.translatable("item.alchemia.wand.focus", fitted.getHoverName())
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }
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
