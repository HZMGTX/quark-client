package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

public class NoGravity extends Module {

    private final BoolSetting  hover     = register(new BoolSetting("Hover",    "Hover in place (cancel fall)", true));
    private final DoubleSetting hoverY   = register(new DoubleSetting("Descend","Slow descent speed",           0.05, 0.0, 0.5));
    private final BoolSetting  ascend    = register(new BoolSetting("Ascend",   "Hold jump to rise",            true));

    public NoGravity() {
        super("NoGravity", "Disables gravity — hover, float, or slowly descend", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.isOnGround() || mc.player.isTouchingWater() || mc.player.isInLava()) return;

        Vec3d vel = mc.player.getVelocity();

        if (hover.isEnabled()) {
            double targetY = vel.y < 0 ? -hoverY.get() : vel.y;

            if (ascend.isEnabled() && mc.options.jumpKey.isPressed()) {
                targetY = 0.2;
            }

            mc.player.setVelocity(vel.x, targetY, vel.z);
            mc.player.fallDistance = 0;
        }
    }
}
