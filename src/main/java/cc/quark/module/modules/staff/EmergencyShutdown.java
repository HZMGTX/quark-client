package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;

public class EmergencyShutdown extends Module {

    private final IntSetting delay = register(new IntSetting(
            "Delay (Seconds)", "Seconds to wait before executing the shutdown command", 30, 0, 300));
    private final IntSetting warnInterval = register(new IntSetting(
            "Warn Interval (Seconds)", "How often (in seconds) to broadcast countdown warnings", 30, 30, 120));
    private final StringSetting shutdownCmd = register(new StringSetting(
            "Shutdown Command", "Server command to execute for shutdown", "stop"));

    private int ticksElapsed = 0;
    private int nextWarnAt = 0;   // ticks until next warning broadcast

    public EmergencyShutdown() {
        super("EmergencyShutdown", "Broadcasts countdown warnings then issues a configurable shutdown command", Category.STAFF);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) { disable(); return; }
        ticksElapsed = 0;
        nextWarnAt = 0;

        int d = delay.get();
        if (d == 0) {
            executeShutdown();
            return;
        }

        // Broadcast initial warning
        mc.player.networkHandler.sendChatCommand(
                "say [SHUTDOWN] Server shutting down in " + d + " second(s). Save your progress!");
        ChatUtil.warn("§6[Shutdown] §fShutdown initiated. §e" + d + "s §fremaining.");
    }

    @Override
    public void onDisable() {
        ChatUtil.info("§6[Shutdown] §fShutdown sequence cancelled.");
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        ticksElapsed++;

        int totalTicks = delay.get() * 20;
        if (ticksElapsed >= totalTicks) {
            executeShutdown();
            return;
        }

        // Periodic warnings
        if (ticksElapsed >= nextWarnAt) {
            int remaining = (totalTicks - ticksElapsed) / 20;
            if (remaining > 0) {
                mc.player.networkHandler.sendChatCommand(
                        "say [SHUTDOWN] Server shutting down in " + remaining + " second(s)!");
                ChatUtil.warn("§6[Shutdown] §e" + remaining + "s §fremaining.");
            }
            nextWarnAt = ticksElapsed + warnInterval.get() * 20;
        }
    }

    private void executeShutdown() {
        if (mc.player != null) {
            mc.player.networkHandler.sendChatCommand("say [SHUTDOWN] Server is shutting down NOW. Goodbye!");
            mc.player.networkHandler.sendChatCommand(shutdownCmd.get());
            ChatUtil.warn("§6[Shutdown] §cShutdown command sent: §e/" + shutdownCmd.get());
        }
        disable();
    }
}
