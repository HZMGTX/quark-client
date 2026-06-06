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

    private final BoolSetting autoName   = register(new BoolSetting("Auto Name",   "Organise screenshots into date sub-folders", true));
    private final BoolSetting openFolder = register(new BoolSetting("Open Folder", "Open screenshots folder after capture",       false));

    public ScreenshotHelper() {
        super("ScreenshotHelper", "Auto-labels and saves screenshots to organised folders", Category.MISC);
    }

    @EventHandler
    public void onKey(EventKey e) {
        if (e.getKeyCode() != GLFW.GLFW_KEY_F2) return;
        if (mc.player == null) return;

        File base = new File(mc.runDirectory, "screenshots");
        File dir  = base;

        if (autoName.isEnabled()) {
            String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            dir = new File(base, today);
        }

        if (!dir.exists()) dir.mkdirs();
        final File targetDir = dir;

        ScreenshotRecorder.saveScreenshot(targetDir, mc.getFramebuffer(), msg ->
            mc.execute(() -> {
                ChatUtil.info("Screenshot saved to " + targetDir.getName());
                if (openFolder.isEnabled()) {
                    try { java.awt.Desktop.getDesktop().open(targetDir); } catch (Exception ignored) {}
                }
            }));
    }
}
