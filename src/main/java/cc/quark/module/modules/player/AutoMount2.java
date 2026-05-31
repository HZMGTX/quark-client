package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.CamelEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.StriderEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.Hand;

/**
 * AutoMount2 — auto-mounts the nearest rideable entity with priority
 * selection (prefer horse > boat > pig > strider > minecart).
 */
public class AutoMount2 extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Search radius for rideable entities", 4.0, 1.0, 8.0));

    private final BoolSetting preferHorse = register(new BoolSetting(
            "Prefer Horse", "Prefer horses and camels over other mounts", true));

    private final BoolSetting notify = register(new BoolSetting(
            "Notify", "Show chat notification when mounting", false));

    private final TimerUtil timer = new TimerUtil();

    public AutoMount2() {
        super("AutoMount2", "Auto-mounts nearest rideable entity with priority", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.player.getVehicle() != null) return;
        if (!timer.hasReached(600)) return;
        timer.reset();

        Entity best = null;
        int bestPriority = Integer.MAX_VALUE;
        double bestDist = Double.MAX_VALUE;

        for (Entity entity : mc.world.getEntities()) {
            double dist = mc.player.distanceTo(entity);
            if (dist > range.get()) continue;

            int priority = getPriority(entity);
            if (priority < 0) continue; // not rideable

            // Pick closest within priority tier
            if (priority < bestPriority || (priority == bestPriority && dist < bestDist)) {
                best = entity;
                bestPriority = priority;
                bestDist = dist;
            }
        }

        if (best == null) return;

        mc.interactionManager.interactEntity(mc.player, best, Hand.MAIN_HAND);

        if (notify.isEnabled()) {
            ChatUtil.info("AutoMount2: mounted " + best.getType().getName().getString());
        }
    }

    /**
     * Returns a priority value for the entity (lower = higher priority).
     * Returns -1 if not rideable.
     */
    private int getPriority(Entity e) {
        if (preferHorse.isEnabled()) {
            if (e instanceof AbstractHorseEntity || e instanceof CamelEntity) return 0;
        }
        if (e instanceof BoatEntity) return 1;
        if (e instanceof PigEntity) return 2;
        if (e instanceof LlamaEntity) return 3;
        if (e instanceof StriderEntity) return 4;
        if (e instanceof AbstractMinecartEntity) return 5;
        if (!preferHorse.isEnabled() && (e instanceof AbstractHorseEntity || e instanceof CamelEntity)) return 6;
        return -1;
    }
}
