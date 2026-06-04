package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

public class StunAura extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Attack range", 3.5, 1.0, 6.0));

    private final IntSetting cps = register(new IntSetting(
            "CPS", "Clicks per second to stun enemies", 8, 1, 20));

    private long lastHit = 0;

    public StunAura() {
        super("StunAura", "Rapidly hits to interrupt enemy actions", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        long delay = 1000L / cps.get();
        if (System.currentTimeMillis() - lastHit < delay) return;

        double r = range.get();
        LivingEntity closest = null;
        double closestDist = Double.MAX_VALUE;

        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (living == mc.player) continue;
            if (living.isDead()) continue;
            double dist = mc.player.distanceTo(living);
            if (dist <= r && dist < closestDist) {
                closestDist = dist;
                closest = living;
            }
        }

        if (closest != null) {
            mc.interactionManager.attackEntity(mc.player, closest);
            mc.player.swingHand(Hand.MAIN_HAND);
            lastHit = System.currentTimeMillis();
        }
    }
}
