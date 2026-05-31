package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventKey;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.client.util.ScreenshotRecorder;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScreenCapture extends Module {

    private final BoolSetting autoName = register(new BoolSetting(
            "AutoName", "Use timestamp as screenshot filename", true));

    public ScreenCapture() {
        super("ScreenCapture", "Takes a screenshot when pressing F6; supports timestamp filenames", Category.MISC);
    }

    @EventHandler
    public void onKey(EventKey event) {
        if (event.getKeyCode() != GLFW.GLFW_KEY_F6) return;
        if (mc.getFramebuffer() == null) return;

        File screenshotsDir = new File(mc.runDirectory, "screenshots");
        if (!screenshotsDir.exists()) screenshotsDir.mkdirs();

        String name = autoName.isEnabled()
                ? new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + ".png"
                : null;

        ScreenshotRecorder.saveScreenshot(
                screenshotsDir,
                name,
                mc.getFramebuffer(),
                text -> mc.execute(() -> ChatUtil.info("[ScreenCapture] " + text.getString()))
        );
    }
}
