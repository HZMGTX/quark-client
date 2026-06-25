package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.ChatUtil;

/**
 * AutoLeave - Automatically disconnects from the server when the player's
 * health drops below a configurable threshold. Useful as a last-resort safety
 * net when other protection modules (AutoTotem, AutoHeal) are unavailable.
 */
public class AutoLeave extends Module {

    private final DoubleSetting threshold = register(new DoubleSetting(
            "Threshold", "Health (half-hearts) to trigger disconnect", 4.0, 1.0, 18.0));
    private final BoolSetting ignoreLocal = register(new BoolSetting(
            "Ignore Local", "Do nothing in single-player / LAN worlds", true));
    private final BoolSetting notify = register(new BoolSetting(
            "Notify", "Print a message in chat before disconnecting", true));
    private final BoolSetting oncePerSession = register(new BoolSetting(
            "Once Per Session", "Only trigger once until module is re-enabled", true));

    private boolean triggered = false;

    public AutoLeave() {
        super("AutoLeave", "Auto-disconnects when HP drops below a threshold", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        triggered = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (triggered && oncePerSession.isEnabled()) return;

        // Optionally skip single-player / LAN
        if (ignoreLocal.isEnabled() && mc.getServer() != null) return;

        float health = mc.player.getHealth();
        if (health > (float) threshold.get()) return;

        triggered = true;

        if (notify.isEnabled()) {
            ChatUtil.warn("AutoLeave: disconnecting (HP " + String.format("%.1f", health / 2f) + " hearts)");
        }

        mc.execute(() -> {
            if (mc.world != null) mc.world.disconnect();
            mc.disconnect();
        });
    }
}
