package me.moonscenty.alchemia.item;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * The sinister lodestone, the original's {@code ItemSinisterStone}: infused flint that lights up while whoever holds
 * it looks roughly towards an eldritch portal, open or shut, within two hundred and fifty-six blocks. It is not a
 * compass and does not point; it only says yes or no to where you are looking. It sees through walls.
 * <p>
 * Only portals the client has loaded count, as in the original: a portal's block entity puts itself on the list
 * when it is loaded on the client and takes itself off when it goes.
 */
public class SinisterStoneItem extends Item implements WarpingGear {
    /** Every portal the client has loaded, with the world it is in. Empty on a server. */
    public static final Set<GlobalPos> PORTALS = ConcurrentHashMap.newKeySet();
    /** How far a portal is felt along the line of sight, and how far off that line, in radians. */
    public static final double REACH = 256.0;
    public static final double CONE = 0.33;

    public SinisterStoneItem(Properties properties) {
        super(properties);
    }

    /** Whether something is looking near enough at a portal the client knows of for the stone to light. */
    public static boolean feels(Entity looker) {
        if (looker == null) {
            return false;
        }
        Vec3 eye = looker.getEyePosition();
        Vec3 look = looker.getLookAngle();
        for (GlobalPos portal : PORTALS) {
            if (portal.dimension() != looker.level().dimension()) {
                continue;
            }
            if (aimedAt(eye, look, Vec3.atCenterOf(portal.pos()))) {
                return true;
            }
        }
        return false;
    }

    /** Within the cone round the line of sight, and not past its reach along it. */
    public static boolean aimedAt(Vec3 eye, Vec3 look, Vec3 point) {
        Vec3 to = point.subtract(eye);
        double along = to.dot(look);
        if (along <= 0.0 || along > REACH) {
            return false;
        }
        double length = to.length();
        return length > 0.0 && Math.acos(Math.min(1.0, along / length)) < CONE;
    }

    /** A point of warp while it is held, as the original laid on it. */
    @Override
    public int warp(ItemStack stack, Player holder) {
        return 1;
    }
}
