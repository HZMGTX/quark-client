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

public class ForceField extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Entities within this radius are attacked", 3.5, 1.0, 6.0));
    private final IntSetting    delay = register(new IntSetting("Delay",    "Ticks between field pulses",                4, 1, 20));

    private int ticker = 0;

    public ForceField() {
        super("ForceField", "Attacks all living entities within radius every pulse — 360° aura", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (++ticker < delay.get()) return;
        ticker = 0;

        boolean hit = false;
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof LivingEntity living)) continue;
            if (living.isDead() || living.getHealth() <= 0) continue;
            if (mc.player.distanceTo(entity) > range.get()) continue;
            mc.interactionManager.attackEntity(mc.player, entity);
            hit = true;
        }
        if (hit) mc.player.swingHand(Hand.MAIN_HAND);
    }
}
