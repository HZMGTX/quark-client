package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPreMotion;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;

public class HeadRoll extends Module {

    private final DoubleSetting rollAmount = register(new DoubleSetting(
            "RollAmount", "Maximum roll angle in degrees based on strafing", 15.0, 1.0, 45.0));

    private float currentRoll = 0f;

    public HeadRoll() {
        super("HeadRoll", "Rolls player head based on movement direction for visual effect", Category.PLAYER);
    }

    @EventHandler
    public void onPreMotion(EventPreMotion event) {
        if (mc.player == null) return;

        float strafe = mc.player.sidewaysSpeed;
        float target = (float) (-strafe * rollAmount.get());
        currentRoll = currentRoll + (target - currentRoll) * 0.15f;
    }

    public float getCurrentRoll() {
        return currentRoll;
    }

    @Override
    public void onDisable() {
        currentRoll = 0f;
    }
}
