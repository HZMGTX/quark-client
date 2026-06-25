package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

public class JetpackFly extends Module {

    private final DoubleSetting thrust = register(new DoubleSetting(
            "Thrust", "Vertical thrust per tick while holding jump", 0.15, 0.05, 0.5));

    public JetpackFly() {
        super("JetpackFly", "Jetpack-style vertical flight", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        Vec3d vel = mc.player.getVelocity();

        if (mc.options.jumpKey.isPressed()) {
            mc.player.setVelocity(vel.x, thrust.get(), vel.z);
        } else if (mc.options.sneakKey.isPressed()) {
            mc.player.setVelocity(vel.x, -thrust.get(), vel.z);
        } else {
            // Hover
            mc.player.setVelocity(vel.x, 0, vel.z);
        }

        mc.player.setOnGround(false);
    }
}
