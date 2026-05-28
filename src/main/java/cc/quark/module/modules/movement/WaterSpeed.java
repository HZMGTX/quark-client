package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventMove;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

public class WaterSpeed extends Module {

    private final DoubleSetting multiplier = register(new DoubleSetting(
            "Multiplier", "Water speed multiplier", 1.5, 1.0, 3.0));

    private final BoolSetting dolphinJump = register(new BoolSetting(
            "Dolphin Jump", "Jump when surfacing water", true));

    public WaterSpeed() {
        super("WaterSpeed", "Faster swimming", Category.MOVEMENT);
    }

    @EventHandler
    public void onMove(EventMove event) {
        if (mc.player == null) return;
        if (!mc.player.isTouchingWater()) return;

        Vec3d vel = mc.player.getVelocity();
        double hLen = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
        if (hLen > 0) {
            double boosted = hLen * multiplier.get();
            double scale = boosted / hLen;
            mc.player.setVelocity(vel.x * scale, vel.y, vel.z * scale);
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!dolphinJump.isEnabled()) return;
        if (!mc.player.isTouchingWater()) return;

        Vec3d vel = mc.player.getVelocity();
        boolean surfacing = vel.y > 0 && !mc.player.isSubmergedInWater();
        if (surfacing && mc.options.jumpKey.isPressed()) {
            mc.player.setVelocity(vel.x, 0.42, vel.z);
        }
    }
}
