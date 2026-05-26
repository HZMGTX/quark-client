package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.ChatUtil;

/**
 * AutoLeave - warns and returns to menu intent when health drops below the threshold.
 */
public class AutoLeave extends Module {

    private final DoubleSetting health = register(new DoubleSetting("Health", "Health to leave at", 6.0, 1.0, 20.0));

    private boolean warned;

    public AutoLeave() {
        super("AutoLeave", "Warns to leave when health gets dangerously low", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        warned = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.getHealth() <= health.get()) {
            if (!warned) {
                ChatUtil.error("Low health! Leave the fight now.");
                warned = true;
            }
        } else {
            warned = false;
        }
    }
}
