package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class VanishSpy extends Module {

    private final BoolSetting alert = register(new BoolSetting(
            "Alert", "Send a chat alert when vanish is detected", true));

    private final Map<UUID, String> knownPlayers = new HashMap<>();

    public VanishSpy() {
        super("VanishSpy", "Detects when a player disappears from the tab list without a leave message", Category.MISC);
    }

    @Override
    public void onEnable() {
        knownPlayers.clear();
        if (mc.getNetworkHandler() == null) return;
        for (PlayerListEntry entry : mc.getNetworkHandler().getPlayerList()) {
            knownPlayers.put(entry.getProfile().getId(), entry.getProfile().getName());
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.getNetworkHandler() == null || mc.player == null) return;

        Set<UUID> currentIds = new HashSet<>();
        for (PlayerListEntry entry : mc.getNetworkHandler().getPlayerList()) {
            currentIds.add(entry.getProfile().getId());
        }

        for (Map.Entry<UUID, String> e : knownPlayers.entrySet()) {
            if (!currentIds.contains(e.getKey())) {
                if (alert.isEnabled()) {
                    mc.player.sendMessage(Text.literal("§7[VanishSpy] §e" + e.getValue() + " §cmay have vanished!"), false);
                }
            }
        }

        knownPlayers.clear();
        for (PlayerListEntry entry : mc.getNetworkHandler().getPlayerList()) {
            knownPlayers.put(entry.getProfile().getId(), entry.getProfile().getName());
        }
    }
}
