package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.util.math.Vec3d;

/**
 * Boost - applies an immediate velocity kick in the configured axis when
 * the forward key is held.
 *
 * <ul>
 *   <li><b>Horizontal</b> - boost in the look-direction on the XZ plane.</li>
 *   <li><b>Vertical</b>   - boost upward.</li>
 *   <li><b>Both</b>       - boost in the 3-D look vector.</li>
 * </ul>
 */
public class Boost extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "Axis to boost", "Horizontal", "Horizontal", "Vertical", "Both"));
    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Velocity added per tick in the chosen direction", 0.15, 0.01, 2.0));
    private final IntSetting triggerKey = register(new IntSetting(
            "Trigger Key (GLFW)", "GLFW key code; -1 = always active", -1, -1, 400));

    public Boost() {
        super("Boost", "Velocity boost in a configurable direction", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        // Check trigger key (GLFW_PRESS = 1, GLFW_RELEASE = 0)
        int key = triggerKey.get();
        if (key != -1) {
            int keyState = org.lwjgl.glfw.GLFW.glfwGetKey(mc.getWindow().getHandle(), key);
            if (keyState != org.lwjgl.glfw.GLFW.GLFW_PRESS) return;
        }

        boolean fwdHeld  = mc.player.input.movementForward != 0;
        boolean sideHeld = mc.player.input.movementSideways != 0;
        if (!fwdHeld && !sideHeld) return;

        double spd    = speed.get();
        double yawRad = Math.toRadians(mc.player.getYaw());
        double pitchRad = Math.toRadians(mc.player.getPitch());

        double fwd  = mc.player.input.movementForward;
        double side = mc.player.input.movementSideways;
        double dx   = (-Math.sin(yawRad) * fwd + Math.cos(yawRad) * side);
        double dz   = ( Math.cos(yawRad) * fwd + Math.sin(yawRad) * side);
        double hLen = Math.sqrt(dx * dx + dz * dz);
        if (hLen > 0) { dx /= hLen; dz /= hLen; }

        Vec3d v = mc.player.getVelocity();

        switch (mode.get()) {
            case "Horizontal" -> mc.player.setVelocity(v.x + dx * spd, v.y, v.z + dz * spd);
            case "Vertical"   -> mc.player.setVelocity(v.x, v.y + spd, v.z);
            case "Both" -> {
                double lookX = -Math.sin(yawRad) * Math.cos(pitchRad);
                double lookY = -Math.sin(pitchRad);
                double lookZ =  Math.cos(yawRad) * Math.cos(pitchRad);
                mc.player.setVelocity(v.x + lookX * spd, v.y + lookY * spd, v.z + lookZ * spd);
            }
        }
    }
}
