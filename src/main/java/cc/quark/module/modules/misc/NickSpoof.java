package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.StringSetting;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

import java.util.UUID;

public class NickSpoof extends Module {

    private final StringSetting fakeNick = register(new StringSetting(
            "FakeNick", "Display name shown in local tab list", "Player"));

    public NickSpoof() {
        super("NickSpoof", "Spoofs the local display name shown in the tab list for the current player", Category.MISC);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        String nick = fakeNick.get();
        if (nick.isEmpty()) return;
        UUID selfId = mc.player.getUuid();
        PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(selfId);
        if (entry != null) {
            entry.setDisplayName(Text.literal(nick));
        }
    }

    @Override
    public void onDisable() {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        UUID selfId = mc.player.getUuid();
        PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(selfId);
        if (entry != null) {
            entry.setDisplayName(null);
        }
    }
}
