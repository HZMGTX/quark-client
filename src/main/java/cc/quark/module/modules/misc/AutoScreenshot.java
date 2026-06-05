package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.client.util.ScreenshotRecorder;

public class AutoScreenshot extends Module {
    private final IntSetting interval = register(new IntSetting("Interval", "Seconds between auto-screenshots", 60, 10, 3600));
    private final BoolSetting onDeath = register(new BoolSetting("On Death", "Screenshot on death", false));
    private final TimerUtil timer = new TimerUtil();

    public AutoScreenshot() {
        super("Auto Screenshot", "Automatically takes screenshots at intervals", Category.MISC, 0);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (timer.hasReached(interval.get() * 1000L)) {
            ScreenshotRecorder.saveScreenshot(mc.runDirectory, mc.getFramebuffer(), msg -> {});
            timer.reset();
        }
    }
}
