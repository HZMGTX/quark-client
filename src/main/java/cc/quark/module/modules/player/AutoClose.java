package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.TimerUtil;

public class AutoClose extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "When to close the screen",
            "Delay", "Instant", "Delay"));
    private final IntSetting delay = register(new IntSetting(
            "Delay ms", "Milliseconds before closing (Delay mode)", 1500, 100, 10000));

    private final TimerUtil timer = new TimerUtil();
    private boolean screenWasOpen = false;

    public AutoClose() {
        super("AutoClose", "Auto-closes open inventory screens after a configurable delay", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        screenWasOpen = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        boolean screenOpen = mc.currentScreen != null;

        if (screenOpen && !screenWasOpen) {
            // Screen just opened
            screenWasOpen = true;
            timer.reset();

            if (mode.is("Instant")) {
                mc.player.closeHandledScreen();
                screenWasOpen = false;
            }
        } else if (!screenOpen) {
            screenWasOpen = false;
        }

        // Delay mode: close after timer
        if (screenOpen && mode.is("Delay") && timer.hasReached(delay.get())) {
            mc.player.closeHandledScreen();
            screenWasOpen = false;
        }
    }
}
