package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * GroundSpeed - boosts movement speed only while the player is on the ground.
 */
public class GroundSpeed extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Ground speed multiplier", 1.4, 1.0, 3.0));

    public GroundSpeed() {
        super("GroundSpeed", "Speed boost on the ground", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isOnGround()) return;
        if (mc.player.input.movementForward == 0 && mc.player.input.movementSideways == 0) return;
        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(v.x * speed.get(), v.y, v.z * speed.get());
    }
}
