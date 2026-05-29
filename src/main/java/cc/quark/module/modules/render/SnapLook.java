package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPreMotion;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;

public class SnapLook extends Module {

    private final IntSetting  snapAngle = register(new IntSetting(
            "Snap Angle", "Degrees per snap step (e.g. 45 = 8 directions)", 45, 5, 90));
    private final BoolSetting snapPitch = register(new BoolSetting(
            "Snap Pitch", "Also snap pitch to nearest step", true));

    public SnapLook() {
        super("SnapLook", "Snaps look direction to nearest angle increment when enabled", Category.RENDER);
    }

    @EventHandler
    public void onPreMotion(EventPreMotion event) {
        if (mc.player == null) return;
        int step = snapAngle.get();
        if (step <= 0) return;

        float yaw = snap(event.getYaw(), step);
        event.setYaw(yaw);

        if (snapPitch.isEnabled()) {
            float pitch = snap(event.getPitch(), step);
            event.setPitch(Math.max(-90f, Math.min(90f, pitch)));
        }

        mc.player.setYaw(yaw);
        if (snapPitch.isEnabled()) {
            mc.player.setPitch(Math.max(-90f, Math.min(90f, snap(mc.player.getPitch(), step))));
        }
    }

    private float snap(float angle, int step) {
        return Math.round(angle / step) * step;
    }
}
