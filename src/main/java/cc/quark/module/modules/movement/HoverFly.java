package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;

public class HoverFly extends Module {

    private final DoubleSetting hoverHeight = register(new DoubleSetting(
            "Hover Height", "Y offset from activation point to hover at", 0.0, -5.0, 10.0));

    private double targetY = Double.MIN_VALUE;

    public HoverFly() {
        super("HoverFly", "Hover in place while holding fly keybind", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        targetY = Double.MIN_VALUE;
    }

    @Override
    public void onDisable() {
        targetY = Double.MIN_VALUE;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        // Set target Y on first tick
        if (targetY == Double.MIN_VALUE) {
            targetY = mc.player.getY() + hoverHeight.get();
        }

        double currentY = mc.player.getY();
        double dy = targetY - currentY;

        // Proportional correction to stay at hover height
        double velY = Math.max(-0.5, Math.min(0.5, dy * 0.4));

        // Allow manual vertical adjustment while holding keys
        if (mc.options.jumpKey.isPressed()) {
            velY = 0.1;
            targetY = currentY + 0.1;
        } else if (mc.options.sneakKey.isPressed()) {
            velY = -0.1;
            targetY = currentY - 0.1;
        }

        mc.player.setVelocity(mc.player.getVelocity().x, velY, mc.player.getVelocity().z);
        mc.player.fallDistance = 0.0f;
    }
}
