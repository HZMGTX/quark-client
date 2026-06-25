package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;

public class HighFly extends Module {
    private final IntSetting targetY = register(new IntSetting("Target Y", "Y level to fly to", 320, 100, 500));
    private final DoubleSetting speed = register(new DoubleSetting("Speed", "Ascent speed", 1.0, 0.1, 5.0));

    public HighFly() { super("HighFly", "Rapidly ascends to a high Y level", Category.MOVEMENT); }
    @Override public void onDisable() { if (mc.player != null) mc.player.noGravity = false; }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null) return;
        if (mc.player.getY() < targetY.get()) {
            mc.player.noGravity = true;
            mc.player.setVelocity(mc.player.getVelocity().x, speed.get(), mc.player.getVelocity().z);
        } else {
            mc.player.noGravity = false;
        }
    }
}
