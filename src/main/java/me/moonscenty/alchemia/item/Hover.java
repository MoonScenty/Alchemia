package me.moonscenty.alchemia.item;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.registry.ModDataComponents;
import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Flying on the thaumostatic harness, the original's {@code Hover}.
 * <p>
 * Switched on, the harness lets its wearer fly as a creative player does, burning a tick of fuel each tick (three
 * while sprinting) and slowing them across the ground and through the air to a little over four fifths of their
 * speed -- more with haste on it, all of it with a thaumostatic girdle in the belt slot. Run dry, it switches off. Switched off, it still
 * takes a quarter off a fall every tick it is worn. It does nothing for a creative player, who flies anyway.
 */
@EventBusSubscriber(modid = Alchemia.MODID)
public final class Hover {
    /** Marks a player the harness has let fly, so the flight can be taken back when it comes off. */
    private static final String LIFTED = Alchemia.MODID + ":hover_lifted";
    /** How much of their speed a flier keeps, and what the girdle gives back. */
    private static final float DRAG = 0.825F;
    private static final float GIRDLE = 0.175F;
    private static final float HASTE = 0.075F;

    private Hover() {
    }

    /** The harness the player has on, or nothing. */
    public static ItemStack worn(Player player) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        return chest.getItem() instanceof ThaumostaticHarnessItem ? chest : ItemStack.EMPTY;
    }

    /** H: on, if there is anything to fly on, or off. */
    public static void toggle(Player player) {
        ItemStack harness = worn(player);
        if (harness.isEmpty() || player.getAbilities().instabuild) {
            return;
        }
        boolean on = !ThaumostaticHarnessItem.hovering(harness);
        if (on && ThaumostaticHarnessItem.fuel(harness) <= 0) {
            return;
        }
        harness.set(ModDataComponents.HOVERING.get(), on);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                on ? ModSounds.HH_ON.get() : ModSounds.HH_OFF.get(), SoundSource.PLAYERS, 0.33F, 1.0F);
    }

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.getAbilities().instabuild || player.isSpectator()) {
            return;
        }
        ItemStack harness = worn(player);
        if (player.level().isClientSide) {
            if (player.isLocalPlayer() && !harness.isEmpty() && ThaumostaticHarnessItem.hovering(harness)
                    && ThaumostaticHarnessItem.fuel(harness) > 0) {
                drag(player);
            }
            return;
        }
        boolean lifted = player.getPersistentData().getBoolean(LIFTED);
        boolean hover = !harness.isEmpty() && ThaumostaticHarnessItem.hovering(harness);
        if (hover && ThaumostaticHarnessItem.fuel(harness) <= 0) {
            harness.set(ModDataComponents.HOVERING.get(), false);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.HH_OFF.get(),
                    SoundSource.PLAYERS, 0.33F, 1.0F);
            hover = false;
        }
        if (hover) {
            ThaumostaticHarnessItem.setFuel(harness,
                    ThaumostaticHarnessItem.fuel(harness) - (player.isSprinting() ? 3 : 1));
            player.resetFallDistance();
            lift(player, true);
        } else {
            if (!harness.isEmpty()) {
                player.fallDistance *= 0.75F;
            }
            if (lifted) {
                lift(player, false);
            }
        }
    }

    /** Lets the player fly, or takes it back, and tells their client. */
    private static void lift(Player player, boolean up) {
        player.getPersistentData().putBoolean(LIFTED, up);
        if (player.getAbilities().mayfly == up) {
            return;
        }
        player.getAbilities().mayfly = up;
        if (!up) {
            player.getAbilities().flying = false;
        }
        player.onUpdateAbilities();
    }

    /** Slower on the harness than off it, unless the girdle makes it up; and a hum as it works. */
    private static void drag(Player player) {
        // each level of haste on the harness takes some of the drag off
        float keep = DRAG + HASTE * me.moonscenty.alchemia.enchantment.ModEnchantments.haste(player, worn(player));
        if (wearsGirdle(player)) {
            keep += GIRDLE;
        }
        keep = Math.min(1.0F, keep);
        Vec3 motion = player.getDeltaMovement();
        player.setDeltaMovement(motion.x * keep, motion.y, motion.z * keep);
        long now = player.level().getGameTime();
        if (now >= player.getPersistentData().getLong(LIFTED + "_hum")) {
            boolean running = player.isSprinting();
            player.getPersistentData().putLong(LIFTED + "_hum", now + (running ? 20 : 24));
            player.level().playLocalSound(player.getX(), player.getY(), player.getZ(), ModSounds.JACOBS.get(),
                    SoundSource.PLAYERS, running ? 0.1F : 0.05F,
                    1.0F + player.getRandom().nextFloat() * 0.05F + (running ? 0.1F : 0.0F), false);
        }
    }

    private static boolean wearsGirdle(Player player) {
        return top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player)
                .flatMap(curios -> curios.findFirstCurio(stack ->
                        stack.getItem() instanceof me.moonscenty.alchemia.item.bauble.HoverGirdleItem))
                .isPresent();
    }
}
