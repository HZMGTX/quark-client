package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.module.modules.render.NotificationOverlay;
import cc.quark.setting.DoubleSetting;

public class HealthAlert extends Module {

    private final DoubleSetting lowHp   = register(new DoubleSetting("Low HP",    "Warn when HP drops below this (hearts)", 6.0, 1.0, 18.0));
    private final DoubleSetting critHp  = register(new DoubleSetting("Crit HP",   "Critical alert threshold (hearts)",      3.0, 0.5, 10.0));

    private boolean wasCritical = false;
    private boolean wasLow      = false;

    public HealthAlert() {
        super("HealthAlert", "Sends HUD notifications when your health reaches warning thresholds", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        float hp = mc.player.getHealth() / 2f;

        boolean isCritical = hp <= critHp.get();
        boolean isLow      = hp <= lowHp.get();

        if (isCritical && !wasCritical) {
            NotificationOverlay.send("Health", "CRITICAL: " + String.format("%.1f", hp) + " hearts!", NotificationOverlay.NotifType.ERROR);
        } else if (isLow && !wasLow) {
            NotificationOverlay.send("Health", "Low HP: " + String.format("%.1f", hp) + " hearts", NotificationOverlay.NotifType.WARNING);
        }

        wasCritical = isCritical;
        wasLow      = isLow;
    }
}
