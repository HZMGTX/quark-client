package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventKey;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class BoatJump extends Module {

    private final DoubleSetting force = register(new DoubleSetting("Force", "Upward velocity applied after ejecting", 3.0, 1.0, 5.0));

    public BoatJump() {
        super("BoatJump", "Eject from boat and apply powerful upward boost on space press", Category.MOVEMENT);
    }

    @EventHandler
    public void onKey(EventKey event) {
        if (mc.player == null) return;
        if (event.getKeyCode() != GLFW.GLFW_KEY_SPACE) return;

        Entity vehicle = mc.player.getVehicle();
        if (!(vehicle instanceof BoatEntity)) return;

        mc.player.stopRiding();

        Vec3d vel = mc.player.getVelocity();
        mc.player.setVelocity(vel.x, force.get(), vel.z);
        mc.player.fallDistance = 0;
    }
}
