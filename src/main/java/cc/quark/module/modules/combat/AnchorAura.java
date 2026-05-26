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
 * AnchorAura - rapidly attacks nearby non-living entities (anchors/crystals).
 */
public class AnchorAura extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Attack range", 5.0, 1.0, 8.0));
    private final IntSetting delay = register(new IntSetting("Delay", "Ticks between hits", 2, 0, 20));

    private int ticks;

    public AnchorAura() {
        super("AnchorAura", "Targets nearby anchors and crystals", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        ticks++;
        if (ticks < delay.get()) return;

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player || entity instanceof LivingEntity) continue;
            if (mc.player.distanceTo(entity) > range.get()) continue;
            mc.interactionManager.attackEntity(mc.player, entity);
            mc.player.swingHand(Hand.MAIN_HAND);
            ticks = 0;
            return;
        }
    }
}
