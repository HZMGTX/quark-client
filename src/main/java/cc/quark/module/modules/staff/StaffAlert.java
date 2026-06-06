package cc.quark.module.modules.staff;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.module.setting.BoolSetting;
import cc.quark.module.setting.StringSetting;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;

public class StaffAlert extends Module {

    private final BoolSetting playerJoin     = new BoolSetting("PlayerJoin", true);
    private final BoolSetting playerLeave    = new BoolSetting("PlayerLeave", true);
    private final BoolSetting commandUse     = new BoolSetting("CommandUse", true);
    private final BoolSetting lowHealthAlert = new BoolSetting("LowHealth", false);
    private final StringSetting alertPrefix  = new StringSetting("Prefix", "[ALERT]");

    private final Map<String, Boolean> seenPlayers = new HashMap<>();

    public StaffAlert() {
        super("StaffAlert", "Alerts staff to notable server events in chat", Category.STAFF);
        addSettings(playerJoin, playerLeave, commandUse, lowHealthAlert, alertPrefix);
    }

    @Override public void onEnable()  { Quark.mc.getEventBus().subscribe(this); seenPlayers.clear(); }
    @Override public void onDisable() { Quark.mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onChat(EventChat event) {
        if (!event.isIncoming()) return;
        var mc = Quark.mc;
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
        var mc = Quark.mc;
        if (mc == null || mc.player == null) return;
        mc.player.sendMessage(Text.literal("§c" + alertPrefix.get() + " §f" + msg), false);
    }

    private String extractName(String msg) {
        int idx = msg.indexOf(' ');
        return idx > 0 ? msg.substring(0, idx) : msg;
    }
}
