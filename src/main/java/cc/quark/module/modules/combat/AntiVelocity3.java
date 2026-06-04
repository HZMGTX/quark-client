package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventMove;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.util.math.Vec3d;

public class AntiVelocity3 extends Module {

    private final ModeSetting   mode   = register(new ModeSetting("Mode",   "Velocity handling mode",
            "Cancel", "Cancel", "Reduce", "Redirect"));
    private final DoubleSetting reduce = register(new DoubleSetting("Reduce", "Velocity reduction factor (Reduce mode)", 0.5, 0.0, 1.0));

    public AntiVelocity3() {
        super("AntiVelocity3", "Enhanced velocity cancel with modes", Category.COMBAT);
    }

    @EventHandler
    public void onMove(EventMove event) {
        if (mc.player == null) return;
        if (mc.player.hurtTime <= 0) return;

        switch (mode.get()) {
            case "Cancel" -> {
                event.setX(0.0);
                event.setZ(0.0);
            }
            case "Reduce" -> {
                double r = reduce.get();
                event.setX(event.getX() * (1.0 - r));
                event.setZ(event.getZ() * (1.0 - r));
            }
            case "Redirect" -> {
                // Redirect knockback in the direction we are moving
                float yaw = (float) Math.toRadians(mc.player.getYaw());
                double speed = Math.sqrt(event.getX() * event.getX() + event.getZ() * event.getZ());
                event.setX(-Math.sin(yaw) * speed);
                event.setZ( Math.cos(yaw) * speed);
            }
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.hurtTime <= 0) return;

        Vec3d vel = mc.player.getVelocity();

        switch (mode.get()) {
            case "Cancel" -> mc.player.setVelocity(0, vel.y, 0);
            case "Reduce" -> {
                double r = reduce.get();
                mc.player.setVelocity(vel.x * (1.0 - r), vel.y, vel.z * (1.0 - r));
            }
            case "Redirect" -> {
                float yaw   = (float) Math.toRadians(mc.player.getYaw());
                double spd  = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
                mc.player.setVelocity(-Math.sin(yaw) * spd, vel.y, Math.cos(yaw) * spd);
            }
        }
    }

    @Override
    public String getSuffix() {
        return mode.get();
    }
}
