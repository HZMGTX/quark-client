package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.BoolSetting;

public class WaterFly extends Module {
    private final DoubleSetting speed = register(new DoubleSetting("Speed", "Water fly speed", 0.5, 0.1, 2.0));
    private final BoolSetting onlyInWater = register(new BoolSetting("OnlyInWater", "Only work when submerged", true));
    public WaterFly() { super("WaterFly", "Fly freely while in water", Category.MOVEMENT); }
    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (onlyInWater.getValue() && !mc.player.isTouchingWater()) return;
        double spd = speed.getValue();
        float yaw = (float) Math.toRadians(mc.player.getYaw());
        double dx = 0, dy = 0, dz = 0;
        if (mc.options.forwardKey.isPressed()) { dx -= Math.sin(yaw) * spd; dz += Math.cos(yaw) * spd; }
        if (mc.options.backKey.isPressed()) { dx += Math.sin(yaw) * spd; dz -= Math.cos(yaw) * spd; }
        if (mc.options.jumpKey.isPressed()) dy += spd;
        if (mc.options.sneakKey.isPressed()) dy -= spd;
        mc.player.setVelocity(dx, dy, dz);
    }
}
