package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

/**
 * SmartCrit - hops a tiny amount near a target to enable a critical hit.
 */
public class SmartCrit extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Trigger range", 3.5, 1.0, 6.0));

    public SmartCrit() {
        super("SmartCrit", "Mini-jumps near targets to enable crits", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!mc.player.isOnGround()) return;
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player || !(entity instanceof LivingEntity living) || living.isDead()) continue;
            if (mc.player.distanceTo(entity) <= range.get()) {
                mc.player.setVelocity(mc.player.getVelocity().x, 0.42, mc.player.getVelocity().z);
                return;
            }
        }
    }
}
