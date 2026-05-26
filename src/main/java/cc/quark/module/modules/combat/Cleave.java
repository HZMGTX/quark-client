package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;

/**
 * Cleave - attacks multiple targets in a sweep each cycle, up to a limit.
 */
public class Cleave extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Attack range", 3.5, 1.0, 6.0));
    private final IntSetting max = register(new IntSetting("Max Targets", "Max targets per cycle", 3, 1, 8));

    public Cleave() {
        super("Cleave", "Sweeps multiple targets each cycle", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        int hit = 0;
        for (Entity entity : mc.world.getEntities()) {
            if (hit >= max.get()) break;
            if (entity == mc.player || !(entity instanceof LivingEntity living) || living.isDead()) continue;
            if (mc.player.distanceTo(entity) > range.get()) continue;
            mc.interactionManager.attackEntity(mc.player, living);
            hit++;
        }
        if (hit > 0) {
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }
}
