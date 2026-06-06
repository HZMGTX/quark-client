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

public class ScreenshotHelper extends Module {

    private final BoolSetting autoName    = register(new BoolSetting("Auto Name",    "Name screenshots by date/time/coords", true));
    private final BoolSetting openFolder  = register(new BoolSetting("Open Folder",  "Open screenshots folder after capture", false));

    public ScreenshotHelper() {
        super("ScreenshotHelper", "Auto-labels and saves screenshots to organised folders", Category.MISC);
    }

    @EventHandler
    public void onKey(EventKey e) {
        if (e.getKeyCode() != GLFW.GLFW_KEY_F2) return;
        if (mc.player == null || mc.world == null) return;

        File dir = new File(mc.runDirectory, "screenshots");
        if (!dir.exists()) dir.mkdirs();

        if (autoName.isEnabled()) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            String coords = String.format("%.0f_%.0f_%.0f",
                mc.player.getX(), mc.player.getY(), mc.player.getZ());
            String name = timestamp + "_" + coords;
            File sub = new File(dir, new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            if (!sub.exists()) sub.mkdirs();
            ScreenshotRecorder.saveScreenshot(sub, name + ".png", mc.getFramebuffer(), msg ->
                mc.execute(() -> ChatUtil.info("Screenshot saved: " + name)));
        }

        if (openFolder.isEnabled()) {
            try {
                java.awt.Desktop.getDesktop().open(dir);
            } catch (Exception ignored) {}
        }
    }
}
