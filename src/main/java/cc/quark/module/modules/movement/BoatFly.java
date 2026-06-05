package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.Vec3d;

public class BoatFly extends Module {
    private final DoubleSetting speed = register(new DoubleSetting("Speed", "Boat fly speed", 1.0, 0.1, 5.0));
    private final DoubleSetting vSpeed = register(new DoubleSetting("V Speed", "Vertical speed", 0.5, 0.1, 3.0));

    public BoatFly() { super("BoatFly", "Fly while sitting in a boat", Category.MOVEMENT); }
    @Override public void onEnable() { mc.getEventBus().subscribe(this); }
    @Override public void onDisable() { mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null || !(mc.player.getVehicle() instanceof BoatEntity boat)) return;
        double yaw = Math.toRadians(mc.player.getYaw());
        double x = 0, y = 0, z = 0;
        double s = speed.get();
        if (mc.options.forwardKey.isPressed()) { x -= Math.sin(yaw) * s; z += Math.cos(yaw) * s; }
        if (mc.options.backKey.isPressed()) { x += Math.sin(yaw) * s; z -= Math.cos(yaw) * s; }
        if (mc.options.jumpKey.isPressed()) y = vSpeed.get();
        if (mc.options.sneakKey.isPressed()) y = -vSpeed.get();
        boat.setVelocity(x, y, z);
        boat.noGravity = true;
    }
}
