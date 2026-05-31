package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;

public class XZFly extends Module {

    private final DoubleSetting speedH = register(new DoubleSetting(
            "Speed H", "Horizontal fly speed (blocks/tick)", 0.4, 0.05, 3.0));

    private final DoubleSetting speedV = register(new DoubleSetting(
            "Speed V", "Vertical fly speed (blocks/tick)", 0.3, 0.05, 3.0));

    public XZFly() {
        super("XZFly", "Fly mode with separated horizontal and vertical speed controls",
                Category.MOVEMENT);
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.getAbilities().flying = false;
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        mc.player.getAbilities().flying = false;
        mc.player.fallDistance = 0;

        float yaw = mc.player.getYaw();
        double yawRad = Math.toRadians(yaw);
        float fwd = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;

        double len = (fwd == 0 && side == 0) ? 0.0 : Math.sqrt(fwd * fwd + side * side);
        double dx = 0, dz = 0;
        if (len > 0) {
            double nFwd = fwd / len;
            double nSide = side / len;
            double sh = speedH.get();
            dx = (-Math.sin(yawRad) * nFwd + Math.cos(yawRad) * nSide) * sh;
            dz = (Math.cos(yawRad) * nFwd + Math.sin(yawRad) * nSide) * sh;
        }

        double dy = 0;
        if (mc.options.jumpKey.isPressed()) dy = speedV.get();
        else if (mc.options.sneakKey.isPressed()) dy = -speedV.get();

        mc.player.setVelocity(dx, dy, dz);
    }
}
