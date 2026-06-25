package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.client.gui.screen.GameMenuScreen;

public class AutoPause extends Module {

    private final IntSetting afkSeconds = register(new IntSetting(
            "AfkTimeout", "Seconds of inactivity before opening game menu", 120, 10, 600));

    private final BoolSetting resumeOnInput = register(new BoolSetting(
            "ResumeOnInput", "Automatically close the menu when input is detected", true));

    private final BoolSetting notifyBeforePause = register(new BoolSetting(
            "Notify", "Show a warning message 5 seconds before pausing", true));

    private final TimerUtil idleTimer = new TimerUtil();
    private double lastX, lastY, lastZ;
    private float lastYaw, lastPitch;
    private boolean notified = false;
    private boolean paused = false;

    public AutoPause() {
        super("AutoPause", "Opens the game menu when AFK timeout triggers to prevent idle kicks", Category.MISC);
    }

    @Override
    public void onEnable() {
        idleTimer.reset();
        notified = false;
        paused = false;
        if (mc.player != null) {
            lastX = mc.player.getX();
            lastY = mc.player.getY();
            lastZ = mc.player.getZ();
            lastYaw = mc.player.getYaw();
            lastPitch = mc.player.getPitch();
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        double cx = mc.player.getX();
        double cy = mc.player.getY();
        double cz = mc.player.getZ();
        float cyaw = mc.player.getYaw();
        float cpitch = mc.player.getPitch();

        boolean moved = Math.abs(cx - lastX) > 0.001
                || Math.abs(cy - lastY) > 0.001
                || Math.abs(cz - lastZ) > 0.001
                || Math.abs(cyaw - lastYaw) > 0.1f
                || Math.abs(cpitch - lastPitch) > 0.1f;

        if (moved) {
            lastX = cx; lastY = cy; lastZ = cz;
            lastYaw = cyaw; lastPitch = cpitch;
            idleTimer.reset();
            notified = false;

            // Resume from pause if input detected
            if (resumeOnInput.isEnabled() && paused && mc.currentScreen instanceof GameMenuScreen) {
                mc.setScreen(null);
                paused = false;
                ChatUtil.info("AutoPause: Resumed — movement detected.");
            }
            return;
        }

        long timeoutMs = afkSeconds.get() * 1000L;

        // Warn 5 seconds before
        if (!notified && idleTimer.hasReached(timeoutMs - 5000L)) {
            if (notifyBeforePause.isEnabled()) {
                ChatUtil.warn("AutoPause: Pausing in 5 seconds due to AFK...");
            }
            notified = true;
        }

        // Open game menu when timeout is reached
        if (!paused && idleTimer.hasReached(timeoutMs)) {
            if (mc.currentScreen == null) {
                mc.setScreen(new GameMenuScreen(true));
                paused = true;
                ChatUtil.info("AutoPause: AFK pause activated.");
            }
        }
    }
}
