package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;

import java.util.ArrayList;
import java.util.List;

public class FriendManager extends Module {

    private final BoolSetting notifyJoin = register(new BoolSetting(
            "NotifyJoin", "Alert when a friend joins the server", true));

    private final BoolSetting notifyLeave = register(new BoolSetting(
            "NotifyLeave", "Alert when a friend leaves the server", false));

    // Managed friend list — pre-seeded with examples
    private final List<String> friends = new ArrayList<>();

    // Track who was online last tick to detect join/leave
    private final List<String> lastOnline = new ArrayList<>();

    public FriendManager() {
        super("FriendManager", "Manages a list of trusted players and alerts on their join/leave", Category.MISC);
        // Default friends list
        friends.add("Notch");
        friends.add("jeb_");
    }

    @Override
    public void onEnable() {
        lastOnline.clear();
        if (mc.player != null && mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().getPlayerList().forEach(p -> lastOnline.add(p.getProfile().getName()));
        }
        ChatUtil.info("FriendManager: Tracking " + friends.size() + " friend(s).");
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        List<String> currentOnline = new ArrayList<>();
        mc.getNetworkHandler().getPlayerList().forEach(p -> currentOnline.add(p.getProfile().getName()));

        if (notifyJoin.isEnabled()) {
            for (String name : currentOnline) {
                if (friends.contains(name) && !lastOnline.contains(name)) {
                    ChatUtil.info("Friend joined: " + name);
                }
            }
        }

        if (notifyLeave.isEnabled()) {
            for (String name : lastOnline) {
                if (friends.contains(name) && !currentOnline.contains(name)) {
                    ChatUtil.info("Friend left: " + name);
                }
            }
        }

        lastOnline.clear();
        lastOnline.addAll(currentOnline);
    }

    public void addFriend(String name) {
        if (!friends.contains(name)) {
            friends.add(name);
            ChatUtil.info("FriendManager: Added " + name);
        } else {
            ChatUtil.warn("FriendManager: " + name + " is already a friend.");
        }
    }

    public void removeFriend(String name) {
        if (friends.remove(name)) {
            ChatUtil.info("FriendManager: Removed " + name);
        } else {
            ChatUtil.warn("FriendManager: " + name + " is not in your friend list.");
        }
    }

    public boolean isFriend(String name) {
        return friends.contains(name);
    }

    public List<String> getFriends() {
        return friends;
    }

    @Override
    public String getSuffix() {
        return friends.size() + " friends";
    }
}
