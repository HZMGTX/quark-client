package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.StringSetting;
import cc.quark.util.TimerUtil;

public class AutoCommand extends Module {

    private final StringSetting onJoin  = register(new StringSetting("On Join",  "Command to run on world join",  "/spawn"));
    private final StringSetting onDeath = register(new StringSetting("On Death", "Command to run on player death", ""));

    private final TimerUtil joinTimer  = new TimerUtil();
    private boolean pendingJoin  = false;
    private boolean pendingDeath = false;

    public AutoCommand() {
        super("AutoCommand", "Executes configured commands on specific triggers", Category.MISC);
    }

    @Override
    public void onEnable() {
        pendingJoin = true;
        joinTimer.reset();
    }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null) return;
        if (pendingJoin && joinTimer.hasReached(2000)) {
            pendingJoin = false;
            String cmd = onJoin.get();
            if (!cmd.isBlank()) mc.player.networkHandler.sendChatMessage(cmd);
        }
        if (pendingDeath) {
            pendingDeath = false;
            String cmd = onDeath.get();
            if (!cmd.isBlank()) mc.player.networkHandler.sendChatMessage(cmd);
        }
    }

    @EventHandler
    public void onChat(EventChat e) {
        if (!e.isIncoming()) return;
        String msg = e.getMessage().toLowerCase();
        if (msg.contains(mc.player != null ? mc.player.getName().getString().toLowerCase() : "") &&
            (msg.contains("was slain") || msg.contains("died") || msg.contains("fell"))) {
            pendingDeath = true;
        }
    }
}
