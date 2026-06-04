package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ScreenRecorder extends Module {

    private final IntSetting interval = register(new IntSetting(
            "Interval", "Seconds between automatic screenshots", 60, 1, 600));

    private final BoolSetting enabled = register(new BoolSetting(
            "Enabled", "Enable automatic screenshot capture", true));

    private final TimerUtil timer = new TimerUtil();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private int screenshotCount = 0;

    public ScreenRecorder() {
        super("ScreenRecorder", "Records screenshots at intervals", Category.MISC);
    }

    @Override
    public void onEnable() {
        timer.reset();
        screenshotCount = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (!enabled.isEnabled()) return;
        if (mc.player == null || mc.world == null) return;
        if (!timer.hasReached(interval.get() * 1000L)) return;
        timer.reset();

        takeScreenshot();
    }

    private void takeScreenshot() {
        try {
            Path screenshotDir = mc.runDirectory.toPath().resolve("screenshots").resolve("quark-recorder");
            Files.createDirectories(screenshotDir);
            String filename = "shot_" + LocalDateTime.now().format(FMT) + ".png";

            // Use Minecraft's built-in screenshot mechanism
            net.minecraft.client.util.ScreenshotRecorder.saveScreenshot(
                    screenshotDir.toFile(),
                    filename,
                    mc.getFramebuffer(),
                    msg -> {}
            );
            screenshotCount++;
        } catch (Exception ignored) {}
    }

    @Override
    public String getSuffix() {
        return screenshotCount + " shots";
    }
}
