package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;

public class AutoUpdate extends Module {

    private final BoolSetting notify = register(new BoolSetting(
            "Notify", "Display update check result in chat", true));

    private final IntSetting checkInterval = register(new IntSetting(
            "Check Interval", "Seconds between update checks", 300, 30, 3600));

    private final TimerUtil timer = new TimerUtil();
    private boolean hasChecked = false;

    private static final String CURRENT_VERSION = "1.0.0";

    public AutoUpdate() {
        super("AutoUpdate", "Checks for client updates", Category.MISC);
    }

    @Override
    public void onEnable() {
        timer.reset();
        hasChecked = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!timer.hasReached(checkInterval.get() * 1000L)) return;
        timer.reset();

        // Stub: actual HTTP check would be done asynchronously
        checkForUpdate();
    }

    private void checkForUpdate() {
        // Run async to avoid blocking the game thread
        Thread checkThread = new Thread(() -> {
            try {
                // Stub update check - in a real implementation this would
                // fetch from a version endpoint. Here we simulate "up to date".
                Thread.sleep(100);
                boolean upToDate = true; // stub result

                if (notify.isEnabled()) {
                    mc.execute(() -> {
                        if (mc.player != null) {
                            if (upToDate) {
                                ChatUtil.addMessage("§aQuark is up to date (v" + CURRENT_VERSION + ")");
                            } else {
                                ChatUtil.addMessage("§eQuark update available! Current: v" + CURRENT_VERSION);
                            }
                        }
                    });
                }
            } catch (Exception ignored) {}
        }, "AutoUpdate-Check");
        checkThread.setDaemon(true);
        checkThread.start();
    }
}
