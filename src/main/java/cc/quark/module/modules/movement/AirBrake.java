package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.util.math.Vec3d;

/**
 * AirBrake - instantly zeroes all horizontal momentum while airborne when the
 * trigger key (sneak or sprint) is held.
 */
public class AirBrake extends Module {

    private final BoolSetting onSneak = register(new BoolSetting(
            "On Sneak", "Trigger air brake with the sneak key (off = sprint key)", true));
    private final BoolSetting cancelVertical = register(new BoolSetting(
            "Cancel Vertical", "Also zero vertical velocity when braking", false));

    public AirBrake() {
        super("AirBrake", "Instantly stop horizontal momentum in the air", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.isOnGround()) return;

        boolean triggered = onSneak.isEnabled()
                ? mc.options.sneakKey.isPressed()
                : mc.options.sprintKey.isPressed();

        if (!triggered) return;

        Vec3d vel = mc.player.getVelocity();
        double newY = cancelVertical.isEnabled() ? 0.0 : vel.y;
        mc.player.setVelocity(0.0, newY, 0.0);
    }
}
