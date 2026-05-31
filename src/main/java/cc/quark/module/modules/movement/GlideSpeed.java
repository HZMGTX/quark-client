package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

public class GlideSpeed extends Module {

    private final DoubleSetting fallSpeed = register(new DoubleSetting(
            "FallSpeed", "Downward speed while shift-gliding", 0.15, 0.05, 1.0));

    public GlideSpeed() {
        super("GlideSpeed", "Increases falling speed while holding shift for a gliding effect", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.isOnGround()) return;
        if (!mc.player.isSneaking()) return;

        Vec3d vel = mc.player.getVelocity();
        // Force a controlled downward glide speed
        double targetY = -fallSpeed.get();
        mc.player.setVelocity(vel.x, targetY, vel.z);
        mc.player.fallDistance = 0;
    }
}
