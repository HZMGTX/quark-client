package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPreMotion;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.BoolSetting;

public class TeleportFly extends Module {
    private final DoubleSetting speed = register(new DoubleSetting("Speed", "Teleport speed", 1.0, 0.1, 5.0));
    private final BoolSetting vertical = register(new BoolSetting("Vertical", "Enable vertical movement", true));
    public TeleportFly() { super("TeleportFly", "Flies using position teleportation", Category.MOVEMENT); }
    @EventHandler
    public void onPreMotion(EventPreMotion event) {
        if (mc.player == null) return;
        double spd = speed.getValue();
        double dx = 0, dz = 0, dy = 0;
        float yaw = (float) Math.toRadians(mc.player.getYaw());
        if (mc.options.forwardKey.isPressed()) { dx -= Math.sin(yaw) * spd; dz += Math.cos(yaw) * spd; }
        if (mc.options.backKey.isPressed()) { dx += Math.sin(yaw) * spd; dz -= Math.cos(yaw) * spd; }
        if (vertical.getValue()) {
            if (mc.options.jumpKey.isPressed()) dy += spd;
            if (mc.options.sneakKey.isPressed()) dy -= spd;
        }
        if (dx != 0 || dz != 0 || dy != 0) {
            event.setX(event.getX() + dx);
            event.setY(event.getY() + dy);
            event.setZ(event.getZ() + dz);
        }
    }
}
