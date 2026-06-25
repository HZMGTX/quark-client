package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventMove;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;

public class StrafeBoost extends Module {

    private final DoubleSetting amount = register(new DoubleSetting(
            "Amount", "Strafe speed boost amount", 0.05, 0.01, 0.5));

    private final BoolSetting onlySprinting = register(new BoolSetting(
            "OnlySprinting", "Only boost when sprinting", true));

    public StrafeBoost() {
        super("StrafeBoost", "Increases strafe speed", Category.MOVEMENT);
    }

    @EventHandler
    public void onMove(EventMove event) {
        if (mc.player == null) return;
        if (onlySprinting.isEnabled() && !mc.player.isSprinting()) return;

        float side = mc.player.input.movementSideways;
        if (side == 0) return;

        float yaw = (float) Math.toRadians(mc.player.getYaw());
        double boost = amount.get() * Math.signum(side);

        double addX = Math.cos(yaw) * boost;
        double addZ = Math.sin(yaw) * boost;

        event.setX(event.getX() + addX);
        event.setZ(event.getZ() + addZ);
    }
}
