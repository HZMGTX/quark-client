package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;

/**
 * AutoClose - closes the currently open screen after a configurable timeout.
 */
public class AutoClose extends Module {

    private final IntSetting delay = register(new IntSetting("Delay", "Ticks before closing", 60, 5, 600));
    private int timer = 0;

    public AutoClose() {
        super("AutoClose", "Closes open GUIs after a delay", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.currentScreen == null) {
            timer = 0;
            return;
        }
        if (++timer >= delay.get()) {
            timer = 0;
            mc.player.closeHandledScreen();
        }
    }
}
