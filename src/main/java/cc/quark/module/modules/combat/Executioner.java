package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

/**
 * Executioner - targets the lowest-health player and announces the kill attempt.
 */
public class Executioner extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Attack range", 4.0, 1.0, 6.0));

    public Executioner() {
        super("Executioner", "Targets the lowest-health player", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        LivingEntity best = null;
        float bestHealth = Float.MAX_VALUE;
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player || !(entity instanceof PlayerEntity player) || player.isDead()) continue;
            if (mc.player.distanceTo(entity) > range.get()) continue;
            if (player.getHealth() < bestHealth) { bestHealth = player.getHealth(); best = player; }
        }
        if (best != null) {
            mc.interactionManager.attackEntity(mc.player, best);
            mc.player.swingHand(Hand.MAIN_HAND);
            if (bestHealth <= 2.0f) {
                ChatUtil.info("Executing low-health target");
            }
        }
    }
}
