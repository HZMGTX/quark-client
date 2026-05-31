package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventKey;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.util.math.Vec3d;

public class AirDash extends Module {

    private final IntSetting dashCooldown = register(new IntSetting("DashCooldown", "Cooldown between dashes (ms)", 1500, 300, 5000));

    private final TimerUtil timer = new TimerUtil();
    private long lastKeyTime = 0;
    private int lastKey = -1;
    private int doubleTapCount = 0;

    public AirDash() {
        super("AirDash", "Applies horizontal dash burst on double-tap direction", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        timer.reset();
        lastKey = -1;
        doubleTapCount = 0;
    }

    @EventHandler
    public void onKey(EventKey event) {
        if (mc.player == null) return;
        int key = event.getKeyCode();

        long now = System.currentTimeMillis();
        if (key == lastKey && (now - lastKeyTime) < 300) {
            doubleTapCount++;
            if (doubleTapCount >= 1 && timer.hasReached(dashCooldown.get())) {
                performDash();
                timer.reset();
                doubleTapCount = 0;
            }
        } else {
            doubleTapCount = 0;
        }
        lastKey = key;
        lastKeyTime = now;
    }

    @EventHandler
    public void onTick(EventTick event) {
    }

    private void performDash() {
        if (mc.player == null) return;
        float yaw = mc.player.getYaw();
        double yawRad = Math.toRadians(yaw);
        float fwd = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;

        if (fwd == 0 && side == 0) {
            fwd = 1;
        }

        double len = Math.sqrt(fwd * fwd + side * side);
        double nx = fwd / len;
        double nz = side / len;

        double dashStrength = 1.2;
        double vx = (-Math.sin(yawRad) * nx + Math.cos(yawRad) * nz) * dashStrength;
        double vz = ( Math.cos(yawRad) * nx + Math.sin(yawRad) * nz) * dashStrength;

        Vec3d vel = mc.player.getVelocity();
        mc.player.setVelocity(vx, vel.y, vz);
    }
}
