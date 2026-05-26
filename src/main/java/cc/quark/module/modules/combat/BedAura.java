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
 * BedAura - attacks nearby non-living entities used for bed bombing.
 */
public class BedAura extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Attack range", 4.5, 1.0, 8.0));

    public BedAura() {
        super("BedAura", "Detonates nearby bed-bomb entities", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player || entity instanceof LivingEntity) continue;
            if (mc.player.distanceTo(entity) > range.get()) continue;
            mc.interactionManager.attackEntity(mc.player, entity);
            mc.player.swingHand(Hand.MAIN_HAND);
            return;
        }
    }
}
