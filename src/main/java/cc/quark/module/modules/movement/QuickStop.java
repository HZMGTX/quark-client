package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.util.math.Vec3d;

/**
 * QuickStop - instantly zero horizontal velocity when no movement keys are
 * pressed. Air option controls whether it also applies when airborne.
 */
public class QuickStop extends Module {

    private final BoolSetting air = register(new BoolSetting(
            "Air", "Also stop instantly while airborne", false));

    public QuickStop() {
        super("QuickStop", "Instantly halt horizontal motion when idle", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        boolean moving = mc.player.input.movementForward != 0
                      || mc.player.input.movementSideways != 0;
        if (moving) return;

        if (!air.isEnabled() && !mc.player.isOnGround()) return;

        Vec3d v = mc.player.getVelocity();
        if (Math.abs(v.x) < 0.001 && Math.abs(v.z) < 0.001) return;

        mc.player.setVelocity(0.0, v.y, 0.0);
    }
}
