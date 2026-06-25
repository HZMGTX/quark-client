package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

public class OffhandAttack extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Attack range in blocks", 3.5, 1.0, 6.0));

    private final IntSetting delayMs = register(new IntSetting(
            "Delay Ms", "Milliseconds between offhand attacks", 100, 20, 1000));

    private final TimerUtil timer = new TimerUtil();

    public OffhandAttack() {
        super("OffhandAttack", "Attacks with offhand item simultaneously", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delayMs.get())) return;

        // Only act if offhand has an attackable item
        if (mc.player.getOffHandStack().isEmpty()) return;

        LivingEntity target = null;
        double minDist = Double.MAX_VALUE;

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof PlayerEntity)) continue;
            LivingEntity living = (LivingEntity) entity;
            if (living.isRemoved() || living.getHealth() <= 0f) continue;

            double dist = mc.player.distanceTo(entity);
            if (dist > range.get()) continue;
            if (dist < minDist) {
                minDist = dist;
                target = living;
            }
        }

        if (target != null) {
            // Attack with mainhand and swing offhand
            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(Hand.OFF_HAND);
            timer.reset();
        }
    }
}
