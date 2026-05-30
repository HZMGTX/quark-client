package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.module.modules.render.NotificationOverlay;
import cc.quark.setting.ModeSetting;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;

import java.util.Locale;

public class Notifications2 extends Module {

    private final ModeSetting filter = register(new ModeSetting(
            "Filter", "How to handle matched messages", "None", "None", "Block", "Alert"));

    // Keywords to match — can be expanded
    private static final String[] KEYWORDS = {"ban", "kick", "warning", "alert", "afk", "muted"};

    public Notifications2() {
        super("Notifications2", "Filters or alerts on server messages containing keywords", Category.MISC);
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        String mode = filter.get();
        if (mode.equals("None")) return;

        String content = null;

        if (event.getPacket() instanceof GameMessageS2CPacket packet) {
            content = packet.content().getString();
        }

        if (content == null || content.isEmpty()) return;

        String lower = content.toLowerCase(Locale.ROOT);
        boolean matched = false;
        for (String kw : KEYWORDS) {
            if (lower.contains(kw)) {
                matched = true;
                break;
            }
        }

        if (!matched) return;

        if (mode.equals("Block")) {
            event.cancel();
        } else if (mode.equals("Alert")) {
            NotificationOverlay.send("Notifications2", "Keyword match: " + content, NotificationOverlay.NotifType.WARNING);
        }
    }
}
