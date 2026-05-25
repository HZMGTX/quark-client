package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;

public class StraightLine extends Module {

    private final IntSetting  snapAngle = register(new IntSetting("Snap Angle", "Snap yaw to multiples of this", 45, 1, 90));
    private final BoolSetting lockPitch = register(new BoolSetting("Lock Pitch", "Keep pitch at 0 (flat look)",  false));

    public StraightLine() {
        super("StraightLine", "Snaps your yaw to the nearest angle for perfectly straight movement", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        int snap = snapAngle.get();
        if (snap <= 0) return;

        float yaw = mc.player.getYaw();
        float snapped = Math.round(yaw / (float) snap) * (float) snap;
        mc.player.setYaw(snapped);

        if (lockPitch.isEnabled()) mc.player.setPitch(0);
    }
}
