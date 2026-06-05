package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Mute extends Module {

    private final StringSetting mutedPlayers = register(new StringSetting(
            "Muted Players", "Comma-separated player names to mute", ""));
    private final BoolSetting hideMessages = register(new BoolSetting(
            "Hide Messages", "Cancel chat messages from muted players", true));
    private final BoolSetting useCommand = register(new BoolSetting(
            "Use /mute", "Also issue /mute <player> command when enabled", false));

    public Mute() {
        super("Mute", "Client-side mute: hides/cancels chat from specified players", Category.STAFF, 0);
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (!event.isIncoming() || !hideMessages.isEnabled()) return;

        String msg = event.getMessage();
        if (msg == null) return;

        Set<String> muted = getMuted();
        if (muted.isEmpty()) return;

        String clean = msg.replaceAll("§[0-9a-fklmnor]", "").trim();

        for (String name : muted) {
            // Match "Name: message" or "<Name> message" patterns
            if (clean.startsWith(name + ":") || clean.startsWith("<" + name + ">")
                    || clean.startsWith("[" + name + "]")) {
                event.cancel();
                return;
            }
        }
    }

    public void mutePlayer(String name) {
        String current = mutedPlayers.get().trim();
        Set<String> set = getMuted();
        if (set.contains(name)) return;
        mutedPlayers.setValue(current.isEmpty() ? name : current + "," + name);
        ChatUtil.info("[Mute] Muted: " + name);

        if (useCommand.isEnabled() && mc.player != null) {
            mc.player.networkHandler.sendChatCommand("mute " + name);
        }
    }

    private Set<String> getMuted() {
        String list = mutedPlayers.get().trim();
        if (list.isEmpty()) return new HashSet<>();
        return new HashSet<>(Arrays.asList(list.split(",")));
    }
}
