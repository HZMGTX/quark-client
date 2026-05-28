package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

public class Hover2 extends Module {

    private final DoubleSetting boost = register(new DoubleSetting("Boost", "Upward velocity when space held", 0.05, 0.0, 0.5));

    public Hover2() {
        super("Hover2", "Hover mid-air; hold space for slow upward movement", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.isOnGround()) return;

        Vec3d vel = mc.player.getVelocity();
        double vy = 0.0;

        if (mc.options.jumpKey.isPressed()) {
            vy = boost.get();
        }

        mc.player.setVelocity(vel.x, vy, vel.z);
        mc.player.fallDistance = 0;
    }
}
