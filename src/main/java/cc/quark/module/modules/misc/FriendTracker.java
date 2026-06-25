package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.client.network.PlayerListEntry;

import java.util.*;

public class FriendTracker extends Module {
    private final StringSetting friends = register(new StringSetting("Friends", "Comma-separated friend names", ""));
    private final BoolSetting joinAlert = register(new BoolSetting("Join Alert", "Alert when friend joins server", true));
    private final BoolSetting leaveAlert = register(new BoolSetting("Leave Alert", "Alert when friend leaves", true));
    private final BoolSetting highlightInList = register(new BoolSetting("Highlight", "Highlight friends in tab list", true));

    private final Set<String> lastOnline = new HashSet<>();

    public FriendTracker() {
        super("Friend Tracker", "Track friends joining/leaving the server", Category.MISC, 0);
    }

    @Override
    public void onEnable() { lastOnline.clear(); }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        Set<String> friendSet = getFriends();
        if (friendSet.isEmpty()) return;

        Set<String> currentOnline = new HashSet<>();
        for (PlayerListEntry entry : mc.getNetworkHandler().getPlayerList()) {
            String name = entry.getProfile().getName();
            if (friendSet.contains(name)) currentOnline.add(name);
        }

        if (joinAlert.isEnabled()) {
            for (String f : currentOnline) {
                if (!lastOnline.contains(f)) {
                    ChatUtil.info("§a[Friends] §f" + f + " §7joined the server!");
                }
            }
        }
        if (leaveAlert.isEnabled()) {
            for (String f : lastOnline) {
                if (!currentOnline.contains(f)) {
                    ChatUtil.info("§c[Friends] §f" + f + " §7left the server.");
                }
            }
        }

        lastOnline.clear();
        lastOnline.addAll(currentOnline);
    }

    private Set<String> getFriends() {
        String list = friends.get().trim();
        if (list.isEmpty()) return new HashSet<>();
        return new HashSet<>(Arrays.asList(list.split(",")));
    }

    public boolean isFriend(String name) {
        return getFriends().contains(name);
    }
}
