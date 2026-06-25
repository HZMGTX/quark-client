package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

public class OverclockSprint extends Module {

    private final BoolSetting foodSave = register(new BoolSetting(
            "FoodSave", "Only maintain sprint when food level is sufficient", false));

    public OverclockSprint() {
        super("OverclockSprint", "Removes sprint fatigue and maintains sprint through turns and damage",
                Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        boolean moving = mc.player.input.movementForward != 0
                      || mc.player.input.movementSideways != 0;
        if (!moving) return;

        if (foodSave.isEnabled() && mc.player.getHungerManager().getFoodLevel() < 6) return;

        // Force sprint regardless of hurt time, turn angle, or food level
        mc.player.setSprinting(true);

        // Clear sprint cooldown by keeping speed up during knockback
        if (mc.player.hurtTime > 0) {
            var vel = mc.player.getVelocity();
            double hSpeed = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
            if (hSpeed < 0.15) {
                float yaw = mc.player.getYaw();
                double yawRad = Math.toRadians(yaw);
                mc.player.setVelocity(
                        -Math.sin(yawRad) * 0.15,
                        vel.y,
                        Math.cos(yawRad) * 0.15
                );
            }
        }
    }
}
