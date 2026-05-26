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
 * CrystalAura2 - attacks the closest non-living entity (crystals) in range.
 */
public class CrystalAura2 extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Break range", 4.0, 1.0, 8.0));

    public CrystalAura2() {
        super("CrystalAura2", "Breaks nearby non-living combat entities", Category.COMBAT);
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
