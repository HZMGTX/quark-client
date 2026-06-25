package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

/**
 * BurstClick - simulates burst-fire clicking patterns common in PvP.
 * Sends a rapid burst of attacks against the nearest entity, then
 * pauses for a configurable cooldown before the next burst.
 */
public class BurstClick extends Module {

    private final IntSetting burstSize = register(new IntSetting(
            "Burst Size", "Attacks per burst", 3, 1, 8));

    private final IntSetting burstDelay = register(new IntSetting(
            "Burst Delay", "Ticks between bursts", 8, 2, 20));

    private final IntSetting attackDelay = register(new IntSetting(
            "Attack Delay", "Ticks between individual hits in burst", 1, 0, 4));

    private final DoubleSetting range = register(new cc.quark.setting.DoubleSetting(
            "Range", "Attack range", 3.5, 1.0, 6.0));

    private final BoolSetting playersOnly = register(new BoolSetting(
            "Players Only", "Only target players", true));

    private int burstTicks = 0;
    private int cooldownTicks = 0;
    private int currentBurst = 0;

    public BurstClick() {
        super("BurstClick", "Simulates burst-fire clicking patterns for PvP", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        burstTicks = 0;
        cooldownTicks = 0;
        currentBurst = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        if (cooldownTicks > 0) {
            cooldownTicks--;
            return;
        }

        if (burstTicks > 0) {
            burstTicks--;
            return;
        }

        LivingEntity target = findTarget();
        if (target == null) return;

        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(net.minecraft.util.Hand.MAIN_HAND);
        currentBurst++;

        if (currentBurst >= burstSize.get()) {
            currentBurst = 0;
            cooldownTicks = burstDelay.get();
        } else {
            burstTicks = attackDelay.get();
        }
    }

    private LivingEntity findTarget() {
        LivingEntity nearest = null;
        double bestDist = range.get();

        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            if (!(e instanceof LivingEntity living)) continue;
            if (playersOnly.isEnabled() && !(e instanceof PlayerEntity)) continue;
            double d = mc.player.distanceTo(e);
            if (d < bestDist) {
                bestDist = d;
                nearest = living;
            }
        }
        return nearest;
    }
}
