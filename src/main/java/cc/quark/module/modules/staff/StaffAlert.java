package cc.quark.module.modules.staff;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.StringSetting;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;

public class StaffAlert extends Module {

    private final BoolSetting playerJoin     = register(new BoolSetting("PlayerJoin", "PlayerJoin", true));
    private final BoolSetting playerLeave    = register(new BoolSetting("PlayerLeave", "PlayerLeave", true));
    private final BoolSetting commandUse     = register(new BoolSetting("CommandUse", "CommandUse", true));
    private final BoolSetting lowHealthAlert = register(new BoolSetting("LowHealth", "LowHealth", false));
    private final StringSetting alertPrefix  = register(new StringSetting("Prefix", "Prefix", "[ALERT]"));

    private final Map<String, Boolean> seenPlayers = new HashMap<>();

    public StaffAlert() {
        super("StaffAlert", "Alerts staff to notable server events in chat", Category.STAFF);
    }


    @EventHandler
    public void onChat(EventChat event) {
        if (!event.isIncoming()) return;
        
        if (mc == null || mc.player == null) return;
        String msg = event.getMessage();
        if (msg == null) return;

        if (playerJoin.isEnabled() && msg.contains("joined the game")) {
            alert("Player joined: " + extractName(msg));
        }
        if (playerLeave.isEnabled() && msg.contains("left the game")) {
            alert("Player left: " + extractName(msg));
        }
        if (commandUse.isEnabled() && msg.startsWith("/")) {
            alert("Command: " + msg.substring(0, Math.min(60, msg.length())));
        }
    }

    private void alert(String msg) {
        
        if (mc == null || mc.player == null) return;
        mc.player.sendMessage(Text.literal("§c" + alertPrefix.get() + " §f" + msg), false);
    }

    private String extractName(String msg) {
        int idx = msg.indexOf(' ');
        return idx > 0 ? msg.substring(0, idx) : msg;
    }
}
