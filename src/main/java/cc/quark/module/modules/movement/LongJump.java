package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventJump;
import cc.quark.event.events.EventMove;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;

public class LongJump extends Module {

    private final DoubleSetting power = register(new DoubleSetting(
            "Power", "Horizontal boost power applied at jump", 2.0, 1.5, 3.0));

    private boolean inJump = false;
    private double savedVx = 0;
    private double savedVz = 0;

    public LongJump() {
        super("LongJump", "Jump farther horizontally by boosting horizontal velocity on jump", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        inJump = false;
    }

    @EventHandler
    public void onJump(EventJump event) {
        if (mc == null || mc.player == null) return;

        float yaw = (float) Math.toRadians(mc.player.getYaw());
        double boost = 0.18 * power.get();

        savedVx = -Math.sin(yaw) * boost;
        savedVz =  Math.cos(yaw) * boost;

        mc.player.setVelocity(savedVx, mc.player.getVelocity().y, savedVz);
        inJump = true;
    }

    @EventHandler
    public void onMove(EventMove event) {
        if (mc == null || mc.player == null) return;
        if (!inJump) return;

        if (mc.player.isOnGround()) {
            inJump = false;
            return;
        }

        event.setX(savedVx);
        event.setZ(savedVz);
    }
}
