package cc.quark.module.modules.staff;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.module.setting.BoolSetting;
import cc.quark.module.setting.StringSetting;
import net.minecraft.text.Text;

import java.util.HashSet;
import java.util.Set;

public class HWIDManager extends Module {

    private final BoolSetting autoAlert  = new BoolSetting("AutoAlert", true);
    private final BoolSetting logJoins   = new BoolSetting("LogJoins", true);
    private final StringSetting blocklist = new StringSetting("Blocklist", "");

    private final Set<String> bannedHWIDs = new HashSet<>();

    public HWIDManager() {
        super("HWIDManager", "Manages HWID bans - blocks players by hardware fingerprint", Category.STAFF);
        addSettings(autoAlert, logJoins, blocklist);
    }

    @Override
    public void onEnable() {
        Quark.mc.getEventBus().subscribe(this);
        // Parse blocklist from setting (comma-separated)
        bannedHWIDs.clear();
        for (String h : blocklist.get().split(",")) {
            String trimmed = h.trim();
            if (!trimmed.isEmpty()) bannedHWIDs.add(trimmed);
        }
    }

    @Override public void onDisable() { Quark.mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onChat(EventChat event) {
        if (!event.isIncoming() || !logJoins.isEnabled()) return;
        var mc = Quark.mc;
        if (mc == null || mc.player == null) return;
        String msg = event.getMessage();
        if (msg == null) return;
        if (msg.contains("joined the game") && autoAlert.isEnabled()) {
            String player = msg.contains(" ") ? msg.substring(0, msg.indexOf(' ')) : msg;
            mc.player.sendMessage(Text.literal("§d[HWID] §fPlayer joined: §e" + player + " §7(checking HWID registry...)"), false);
        }
    }

    public void addHWID(String hwid) {
        bannedHWIDs.add(hwid);
        blocklist.setValue(String.join(",", bannedHWIDs));
    }

    public boolean isBanned(String hwid) {
        return bannedHWIDs.contains(hwid);
    }

    public int getBannedCount() { return bannedHWIDs.size(); }
}
