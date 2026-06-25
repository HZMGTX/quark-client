package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class BackPedal extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to detect enemies and back away", 3.0, 1.0, 8.0));

    private final BoolSetting sprint = register(new BoolSetting(
            "Sprint", "Sprint while back-pedaling", true));

    public BackPedal() {
        super("BackPedal", "Auto-moves backward from enemies", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        double r = range.get();
        PlayerEntity nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof PlayerEntity p)) continue;
            if (p == mc.player) continue;
            double dist = mc.player.distanceTo(p);
            if (dist <= r && dist < nearestDist) {
                nearestDist = dist;
                nearest = p;
            }
        }

        if (nearest != null) {
            Vec3d dir = mc.player.getPos().subtract(nearest.getPos()).normalize();
            mc.player.setVelocity(dir.x * 0.25, mc.player.getVelocity().y, dir.z * 0.25);
            if (sprint.isEnabled()) mc.player.setSprinting(true);
        }
    }
}
