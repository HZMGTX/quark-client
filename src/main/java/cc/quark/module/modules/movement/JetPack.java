package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class JetPack extends Module {

    private final DoubleSetting thrust = register(new DoubleSetting(
            "Thrust", "Upward acceleration per tick", 0.15, 0.05, 0.6));
    private final DoubleSetting maxVelocity = register(new DoubleSetting(
            "Max Velocity", "Maximum vertical speed", 0.6, 0.1, 2.0));
    private final BoolSetting sneakDown = register(new BoolSetting(
            "Sneak Descend", "Hold sneak to fly downward", true));
    private final BoolSetting noFallDmg = register(new BoolSetting(
            "No Fall Damage", "Reset fall distance while active", true));

    public JetPack() {
        super("JetPack", "Hold Space to fly upward; Shift to descend", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        long window = mc.getWindow().getHandle();
        Vec3d vel = mc.player.getVelocity();
        double vy = vel.y;

        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS) {
            vy = Math.min(maxVelocity.get(), vy + thrust.get());
        } else if (sneakDown.isEnabled()
                && GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS) {
            vy = Math.max(-maxVelocity.get(), vy - thrust.get());
        }

        mc.player.setVelocity(vel.x, vy, vel.z);
        if (noFallDmg.isEnabled()) mc.player.fallDistance = 0;
    }
}
