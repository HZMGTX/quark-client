package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPreMotion;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.util.math.Vec3d;

public class AirWalk extends Module {

    private final BoolSetting sneakToDescend = register(new BoolSetting(
            "SneakToDescend", "Hold sneak to descend slowly", true));

    public AirWalk() {
        super("AirWalk", "Simulates walking in the air by canceling gravity", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.isOnGround()) return;

        Vec3d vel = mc.player.getVelocity();
        double newY;

        if (sneakToDescend.isEnabled() && mc.player.isSneaking()) {
            newY = -0.1;
        } else {
            newY = 0.0;
        }

        mc.player.setVelocity(vel.x, newY, vel.z);
        mc.player.fallDistance = 0;
    }

    @EventHandler
    public void onPreMotion(EventPreMotion event) {
        if (mc.player == null) return;
        if (mc.player.isOnGround()) return;
        event.setOnGround(true);
    }
}
