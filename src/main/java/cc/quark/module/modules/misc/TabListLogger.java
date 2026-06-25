package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.client.network.PlayerListEntry;

import java.util.HashSet;
import java.util.Set;

public class TabListLogger extends Module {
    private final BoolSetting joinAlert = register(new BoolSetting("JoinAlert", "Alert when players join", true));
    private final BoolSetting leaveAlert = register(new BoolSetting("LeaveAlert", "Alert when players leave", true));
    private final Set<String> lastPlayers = new HashSet<>();

    public TabListLogger() { super("TabListLogger", "Logs player joins and leaves from tab list", Category.MISC); }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        Set<String> current = new HashSet<>();
        for (PlayerListEntry e : mc.getNetworkHandler().getPlayerList()) {
            current.add(e.getProfile().getName());
        }
        if (joinAlert.getValue()) {
            for (String p : current) {
                if (!lastPlayers.contains(p) && !p.equals(mc.player.getName().getString())) {
                    ChatUtil.info("[+] " + p + " joined");
                }
            }
        }
        if (leaveAlert.getValue()) {
            for (String p : lastPlayers) {
                if (!current.contains(p)) ChatUtil.info("[-] " + p + " left");
            }
        }
        lastPlayers.clear();
        lastPlayers.addAll(current);
    }
}
