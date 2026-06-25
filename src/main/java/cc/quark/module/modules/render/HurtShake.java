package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;

/**
 * HurtShake - Amplifies the camera shake when the player takes damage.
 * Achieved by boosting the hurtTime field to prolong the built-in screen wobble.
 */
public class HurtShake extends Module {

    private final DoubleSetting intensity = register(new DoubleSetting(
            "Intensity", "Shake intensity multiplier", 0.5, 0.1, 2.0));

    private float lastHealth = 20.0f;

    public HurtShake() {
        super("HurtShake", "Camera shakes when taking damage", Category.RENDER);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) lastHealth = mc.player.getHealth();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        float current = mc.player.getHealth();
        if (current < lastHealth) {
            // Amplify vanilla hurt wobble
            int extraHurt = (int) (10 * intensity.get());
            mc.player.hurtTime = Math.max(mc.player.hurtTime, extraHurt);
        }
        lastHealth = current;
    }
}
