package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.util.math.Vec3d;

public class BunnyFly extends Module {

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Delay between jump and fly phase in ms", 100, 50, 500));

    private final TimerUtil timer = new TimerUtil();
    private boolean jumpPhase = true;

    public BunnyFly() {
        super("BunnyFly", "Alternates between jumping and flying for speed in creative fly", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        jumpPhase = true;
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.getAbilities().flying) return;

        boolean moving = mc.player.input.movementForward != 0 || mc.player.input.movementSideways != 0;
        if (!moving) return;

        if (timer.hasReached(delay.get())) {
            jumpPhase = !jumpPhase;
            timer.reset();
        }

        Vec3d vel = mc.player.getVelocity();
        float yaw = (float) Math.toRadians(mc.player.getYaw());
        float fwd = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        double len = Math.max(1.0, Math.sqrt(fwd * fwd + side * side));
        double vx = (-Math.sin(yaw) * (fwd / len) + Math.cos(yaw) * (side / len)) * 0.35;
        double vz = (Math.cos(yaw) * (fwd / len) + Math.sin(yaw) * (side / len)) * 0.35;

        // Alternate between slightly up and slightly down
        double vy = jumpPhase ? 0.1 : -0.05;
        mc.player.setVelocity(vx, vy, vz);
    }
}
