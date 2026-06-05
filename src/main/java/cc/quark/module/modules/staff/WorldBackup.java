package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;

public class WorldBackup extends Module {

    private final StringSetting saveCommand = register(new StringSetting(
            "Save Command", "Command to trigger world save", "save-all"));
    private final BoolSetting flushFirst = register(new BoolSetting(
            "Flush", "Append 'flush' to force immediate disk write", true));
    private final BoolSetting notifyOnComplete = register(new BoolSetting(
            "Notify On Complete", "Watch chat for save-complete message and alert", true));
    private final StringSetting completePattern = register(new StringSetting(
            "Complete Pattern", "Text to watch for in chat indicating save is done", "Saved the game"));
    private final IntSetting timeoutTicks = register(new IntSetting(
            "Timeout Ticks", "Ticks to wait before assuming save failed (0 = no timeout)", 600, 0, 6000));
    private final BoolSetting autoDisable = register(new BoolSetting(
            "Auto Disable", "Disable module after save completes or times out", true));

    private boolean commandSent = false;
    private boolean saveComplete = false;
    private int ticksElapsed = 0;

    public WorldBackup() {
        super("WorldBackup", "Sends world save command and notifies when complete", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) { disable(); return; }
        mc.getEventBus().subscribe(this);
        commandSent = false;
        saveComplete = false;
        ticksElapsed = 0;
        ChatUtil.info("§6[WorldBackup] §fInitiating world save...");
    }

    @Override
    public void onDisable() {
        mc.getEventBus().unsubscribe(this);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        // Send the save command on the very first tick
        if (!commandSent) {
            String cmd = saveCommand.get().trim();
            if (flushFirst.isEnabled() && !cmd.endsWith("flush")) {
                cmd = cmd + " flush";
            }
            mc.player.networkHandler.sendChatCommand(cmd);
            ChatUtil.info("§6[WorldBackup] §fSent: §e/" + cmd);
            commandSent = true;
            return;
        }

        if (saveComplete) return;

        ticksElapsed++;
        int timeout = timeoutTicks.get();
        if (timeout > 0 && ticksElapsed >= timeout) {
            ChatUtil.warn("§6[WorldBackup] §eTimeout reached — save status unknown.");
            if (autoDisable.isEnabled()) disable();
        }
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (!commandSent || saveComplete) return;
        if (!event.isIncoming()) return;
        if (!notifyOnComplete.isEnabled()) return;

        String msg = event.getMessage();
        if (msg == null) return;

        String clean = msg.replaceAll("§[0-9a-fklmnorA-FK-OR]", "").trim();
        String pattern = completePattern.get().trim();

        if (!pattern.isEmpty() && clean.toLowerCase().contains(pattern.toLowerCase())) {
            saveComplete = true;
            ChatUtil.info("§a[WorldBackup] §fWorld save §acomplete§f! Server confirmed: §7" + clean);
            if (autoDisable.isEnabled()) disable();
        }
    }
}
