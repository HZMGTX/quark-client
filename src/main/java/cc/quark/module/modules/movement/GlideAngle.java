package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPreMotion;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;

public class GlideAngle extends Module {

    private final DoubleSetting angle = register(new DoubleSetting(
            "Angle", "Target pitch angle for optimal elytra glide", -20.0, -90.0, 90.0));

    private final BoolSetting autoAdjust = register(new BoolSetting(
            "Auto Adjust", "Automatically adjust pitch to maintain glide angle", true));

    public GlideAngle() {
        super("GlideAngle", "Optimizes elytra glide angle for max range", Category.MOVEMENT);
    }

    @EventHandler
    public void onPreMotion(EventPreMotion event) {
        if (mc.player == null) return;

        // Only apply when gliding with elytra
        if (!mc.player.isFallFlying()) return;

        if (autoAdjust.isEnabled()) {
            // Smooth pitch adjustment toward target angle
            float currentPitch = event.getPitch();
            float targetPitch = (float) angle.get();
            float diff = targetPitch - currentPitch;
            float step = Math.max(-3.0f, Math.min(3.0f, diff));
            event.setPitch(currentPitch + step);
        } else {
            event.setPitch((float) angle.get());
        }
    }
}
