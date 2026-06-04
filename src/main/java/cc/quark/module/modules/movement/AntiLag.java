package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.util.math.Vec3d;

public class AntiLag extends Module {

    private final IntSetting pingMs = register(new IntSetting(
            "Ping (ms)", "Estimated server latency to compensate for", 100, 0, 500));

    private Vec3d prevVelocity = Vec3d.ZERO;

    public AntiLag() {
        super("AntiLag", "Compensates for high latency in movement", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        prevVelocity = Vec3d.ZERO;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        // Predict future position based on current velocity and ping
        double ticksAhead = pingMs.get() / 50.0;
        Vec3d vel = mc.player.getVelocity();

        // Smooth velocity prediction
        double predictX = vel.x * ticksAhead;
        double predictZ = vel.z * ticksAhead;

        // Apply a small pre-compensating nudge toward predicted position
        double factor = 0.05;
        mc.player.setVelocity(
                vel.x + predictX * factor,
                vel.y,
                vel.z + predictZ * factor
        );

        prevVelocity = vel;
    }
}
