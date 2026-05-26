package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;

/**
 * KeepInventory - warns the player to bank their items when health gets dangerously low.
 */
public class KeepInventory extends Module {

    private final IntSetting health = register(new IntSetting("Health", "Warn below this health", 6, 1, 19));
    private boolean warned = false;

    public KeepInventory() {
        super("KeepInventory", "Warns to protect inventory at low health", Category.PLAYER);
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
                ChatUtil.error("Low health! Protect your inventory.");
                warned = true;
            }
        } else {
            warned = false;
        }
    }
}
