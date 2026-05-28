package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventMove;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;

public class Acceleration extends Module {

    private final IntSetting ticks = register(new IntSetting("Ticks", "Ticks to ramp up to max speed", 20, 5, 40));
    private final DoubleSetting maxSpeed = register(new DoubleSetting("Max Speed", "Maximum speed multiplier", 1.5, 1.0, 3.0));

    private int currentTick = 0;

    public Acceleration() {
        super("Acceleration", "Gradually accelerate to max speed over configurable ticks", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        currentTick = 0;
    }

    @EventHandler
    public void onMove(EventMove event) {
        if (mc.player == null) return;

        float fwd = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;

        if (fwd == 0 && side == 0) {
            currentTick = 0;
            return;
        }

        int maxTicks = ticks.get();
        if (currentTick < maxTicks) currentTick++;

        double progress = (double) currentTick / maxTicks;
        double multiplier = 1.0 + (maxSpeed.get() - 1.0) * progress;

        event.setX(event.getX() * multiplier);
        event.setZ(event.getZ() * multiplier);
    }
}
