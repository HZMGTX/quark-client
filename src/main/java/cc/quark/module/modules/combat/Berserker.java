package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;

/**
 * Berserker - relentlessly attacks every in-range target while sprinting.
 */
public class Berserker extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Attack range", 4.0, 1.0, 6.0));

    public Berserker() {
        super("Berserker", "Relentlessly attacks all targets while sprinting", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        boolean attacked = false;
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player || !(entity instanceof LivingEntity living) || living.isDead()) continue;
            if (mc.player.distanceTo(entity) > range.get()) continue;
            mc.interactionManager.attackEntity(mc.player, living);
            attacked = true;
        }
        if (attacked) {
            mc.player.setSprinting(true);
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }
}
