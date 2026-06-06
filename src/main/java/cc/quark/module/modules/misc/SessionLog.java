package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SessionLog extends Module {
    private final BoolSetting logChat = register(new BoolSetting("LogChat", "Log chat messages", true));
    private final BoolSetting logDeaths = register(new BoolSetting("LogDeaths", "Log player deaths", true));
    private final List<String> log = new ArrayList<>();
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm:ss");
    private boolean wasAlive = true;

    public SessionLog() { super("SessionLog", "Logs session events to a list", Category.MISC); }

    @EventHandler
    public void onChat(EventChat event) {
        if (logChat.getValue()) log.add("[" + fmt.format(LocalDateTime.now()) + "] " + event.getMessage());
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        boolean alive = mc.player.getHealth() > 0;
        if (wasAlive && !alive && logDeaths.getValue()) {
            log.add("[" + fmt.format(LocalDateTime.now()) + "] Died at "
                + (int)mc.player.getX() + "," + (int)mc.player.getY() + "," + (int)mc.player.getZ());
        }
        wasAlive = alive;
    }

    @Override
    public void onDisable() {
        ChatUtil.info("Session log: " + log.size() + " entries.");
        log.clear();
    }
}
