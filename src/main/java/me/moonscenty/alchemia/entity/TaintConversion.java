package me.moonscenty.alchemia.entity;

import me.moonscenty.alchemia.Alchemia;
import me.moonscenty.alchemia.player.effect.ModEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@EventBusSubscriber(modid = Alchemia.MODID)
public final class TaintConversion {
    private TaintConversion() {
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        LivingEntity dead = event.getEntity();
        if (!(dead.level() instanceof ServerLevel level) || dead instanceof TaintedMob || dead instanceof AbstractGolem
                || dead instanceof Autocaster || !dead.hasEffect(ModEffects.TAINT_POISON)) {
            return;
        }
        Mob made = madeOver(level, dead);
        if (made == null) {
            if (dead.getType().is(EntityTypeTags.ARTHROPOD) || dead instanceof Animal) {
                int crawlers = (int) Math.max(1.0, Math.sqrt(dead.getMaxHealth() + 2.0F));
                for (int crawler = 0; crawler < crawlers; crawler++) {
                    TaintCrawler.spawnAt(level,
                            dead.getX() + (level.random.nextFloat() - level.random.nextFloat()) * dead.getBbWidth(),
                            dead.getY() + level.random.nextFloat() * dead.getBbHeight(),
                            dead.getZ() + (level.random.nextFloat() - level.random.nextFloat()) * dead.getBbWidth());
                }
                takeOver(dead, event);
                return;
            }
            ThaumicSlime slime = new ThaumicSlime(level);
            slime.setSize((int) (1.0F + Math.min(dead.getMaxHealth() / 10.0F, 6.0F)), true);
            made = slime;
        }
        made.moveTo(dead.getX(), dead.getY(), dead.getZ(), dead.getYRot(), 0.0F);
        level.addFreshEntity(made);
        if (!(dead instanceof Player)) {
            takeOver(dead, event);
        }
    }

    // unlike the original, rabbits are checked before the generic animal case, so a rabbit can become a tainted rabbit
    private static Mob madeOver(ServerLevel level, LivingEntity dead) {
        if (dead instanceof Creeper) {
            return new TaintedCreeper(level);
        }
        if (dead instanceof Sheep) {
            return new TaintedSheep(level);
        }
        if (dead instanceof Cow) {
            return new TaintedCow(level);
        }
        if (dead instanceof Pig) {
            return new TaintedPig(level);
        }
        if (dead instanceof Chicken) {
            return new TaintedChicken(level);
        }
        if (dead instanceof Villager) {
            return new TaintedVillager(level);
        }
        if (dead instanceof Rabbit rabbit) {
            TaintedRabbit tainted = new TaintedRabbit(level);
            tainted.setVariant(rabbit.getVariant());
            return tainted;
        }
        return null;
    }

    private static void takeOver(Entity dead, LivingDeathEvent event) {
        dead.discard();
        event.setCanceled(true);
    }
}
