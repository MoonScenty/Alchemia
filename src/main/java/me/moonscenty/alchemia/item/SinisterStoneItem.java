package me.moonscenty.alchemia.item;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;

// only portals the client has loaded count, as in the original
public class SinisterStoneItem extends Item {
    public static final Set<GlobalPos> PORTALS = ConcurrentHashMap.newKeySet();
    // reach along the line of sight, and cone half-angle in radians
    public static final double REACH = 256.0;
    public static final double CONE = 0.33;

    public SinisterStoneItem(Properties properties) {
        super(properties);
    }

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

    public static boolean aimedAt(Vec3 eye, Vec3 look, Vec3 point) {
        Vec3 to = point.subtract(eye);
        double along = to.dot(look);
        if (along <= 0.0 || along > REACH) {
            return false;
        }
        double length = to.length();
        return length > 0.0 && Math.acos(Math.min(1.0, along / length)) < CONE;
    }
}
