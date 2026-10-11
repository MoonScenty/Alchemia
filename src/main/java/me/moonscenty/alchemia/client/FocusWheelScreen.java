package me.moonscenty.alchemia.client;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.item.WandItem;
import me.moonscenty.alchemia.network.ChooseFocus;
import me.moonscenty.alchemia.wand.Foci;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Every focus the player is carrying, laid out in a ring, with the one on the wand in the middle.
 *
 * <p>It opens while the key is held and closes when it is let go; whatever the cursor is over at that moment is
 * what gets fitted, and the middle is where nothing is. A ring is worth having over a cycle once somebody is
 * carrying more than three or four, which a pouch makes easy.
 *
 * <p>The ring is drawn from the client's own copy of the bag and the choice is sent by name, so nothing here has
 * to agree with the server about where anything is lying.
 */
public class FocusWheelScreen extends Screen {
    /** The two rings the original turned behind the foci, one each way. */
    private static final ResourceLocation RING = Alchemia.id("textures/gui/focus_wheel.png");
    private static final ResourceLocation RING_BACK = Alchemia.id("textures/gui/focus_wheel_back.png");

    /** How wide the ring is: a little for its own sake, and a little more for every focus on it. */
    private static final float ROOM = 32.0F;
    private static final float ROOM_EACH = 5.0F;
    /** How big the rings are drawn against that, and how fast they turn, in degrees a tick. */
    private static final float OUTER = 2.75F;
    private static final float INNER = 2.55F;
    private static final float TURNS = 0.5F;

    /** Half an item, since everything here is placed by its middle. */
    private static final int HALF = 8;
    /** How close the cursor has to be to count as over one. */
    private static final double OVER = 10.0;
    /** How much the one under the cursor grows, and how fast it gets there. */
    private static final float GROWS = 1.3F;
    private static final float GROWS_BY = 0.05F;
    /** How long the ring takes to open, as a share of the way a frame. */
    private static final float OPENS_BY = 0.15F;

    private final List<ItemStack> carried;
    private final ItemStack fitted;
    /** Which key opened it, so that letting that key go is what closes it. */
    private final int key;

    private float open;
    private float grown;
    private int under = -1;
    /** What was picked, which is sent once and only once. */
    private boolean chosen;

    public FocusWheelScreen(List<ItemStack> carried, ItemStack fitted, int key) {
        super(CommonComponents.EMPTY);
        this.carried = List.copyOf(carried);
        this.fitted = fitted;
        this.key = key;
    }

    /** It is held open for a moment in the middle of a fight. Pausing the world would be the wrong answer. */
    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        // whoever opened it may have put the wand away, or had it taken off them
        if (minecraft == null || minecraft.player == null
                || !(minecraft.player.getMainHandItem().getItem() instanceof WandItem)) {
            onClose();
            return;
        }
        // a second way out, for a key whose letting go never reaches a screen -- a mouse button bound to this
        if (!WandKeys.CHANGE_FOCUS.isDown()) {
            choose();
            onClose();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        open = Math.min(1.0F, open + OPENS_BY);
        int middleX = width / 2;
        int middleY = height / 2;
        float room = (ROOM + carried.size() * ROOM_EACH) * open;
        float age = minecraft == null || minecraft.player == null ? 0.0F
                : minecraft.player.tickCount + partial;

        rings(graphics, middleX, middleY, room, age);
        if (!fitted.isEmpty()) {
            graphics.renderItem(fitted, middleX - HALF, middleY - HALF);
        }

        under = -1;
        for (int which = 0; which < carried.size(); which++) {
            int[] at = place(which, middleX, middleY, room);
            if (Mth.square(mouseX - at[0]) + Mth.square(mouseY - at[1]) <= Mth.square(OVER)) {
                under = which;
            }
        }
        grown = under < 0 ? 1.0F : Math.min(GROWS, grown + GROWS_BY);

        for (int which = 0; which < carried.size(); which++) {
            int[] at = place(which, middleX, middleY, room);
            graphics.pose().pushPose();
            graphics.pose().translate(at[0], at[1], 0.0F);
            if (which == under) {
                graphics.pose().scale(grown, grown, 1.0F);
            }
            graphics.pose().translate(-HALF, -HALF, 0.0F);
            graphics.renderItem(carried.get(which), 0, 0);
            graphics.pose().popPose();
        }

        if (under >= 0) {
            graphics.renderTooltip(font, carried.get(under), mouseX, mouseY);
        }
    }

    /** Where one focus sits on the ring: from the top, and round. */
    private int[] place(int which, int middleX, int middleY, float room) {
        float round = -90.0F + 360.0F * which / Math.max(1, carried.size());
        return new int[] {
                middleX + (int) (Mth.cos(round * Mth.DEG_TO_RAD) * room),
                middleY + (int) (Mth.sin(round * Mth.DEG_TO_RAD) * room)};
    }

    /** The two rings behind it, turning against one another as the original had them. */
    private void rings(GuiGraphics graphics, int middleX, int middleY, float room, float age) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        ring(graphics, RING, middleX, middleY, room * OUTER, age * TURNS);
        ring(graphics, RING_BACK, middleX, middleY, room * INNER, -age * TURNS);
    }

    private void ring(GuiGraphics graphics, ResourceLocation sheet, int middleX, int middleY, float size,
            float turn) {
        graphics.pose().pushPose();
        graphics.pose().translate(middleX, middleY, 0.0F);
        graphics.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(turn));
        graphics.pose().translate(-size / 2.0F, -size / 2.0F, 0.0F);
        graphics.blit(sheet, 0, 0, 0.0F, 0.0F, (int) size, (int) size, (int) size, (int) size);
        graphics.pose().popPose();
    }

    /** Letting the key go is the choosing. Anything else closes it with nothing picked. */
    @Override
    public boolean keyReleased(int pressed, int scancode, int modifiers) {
        if (pressed == key) {
            choose();
            onClose();
            return true;
        }
        return super.keyReleased(pressed, scancode, modifiers);
    }

    /** Clicking one works too, for anybody who would rather not hold a key down. */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (under >= 0) {
            choose();
            onClose();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void choose() {
        if (chosen || under < 0) {
            return;
        }
        chosen = true;
        PacketDistributor.sendToServer(new ChooseFocus(Foci.name(carried.get(under))));
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(null);
        }
    }
}
