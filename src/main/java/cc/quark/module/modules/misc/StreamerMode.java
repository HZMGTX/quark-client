package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.client.MinecraftClient;

import java.util.regex.Pattern;

public class StreamerMode extends Module {

    private static final Pattern IP_PATTERN =
            Pattern.compile("\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}(:\\d+)?\\b");

    private static final Pattern COORDS_PATTERN =
            Pattern.compile("(?i)[xXzZ]\\s*[=:]?\\s*-?\\d+");

    private final BoolSetting hideIP = register(new BoolSetting(
            "Hide IP", "Replace IP addresses in chat with [REDACTED]", true));

    private final BoolSetting hideName = register(new BoolSetting(
            "Hide Name", "Replace own username in chat with 'You'", true));

    private final BoolSetting hideCoords = register(new BoolSetting(
            "Hide Coords", "Replace coordinate patterns in chat with [REDACTED]", false));

    public StreamerMode() {
        super("StreamerMode", "Redacts personal info from incoming chat messages", Category.MISC);
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (!event.isIncoming()) return;

        String msg = event.getMessage();

        if (hideIP.isEnabled()) {
            msg = IP_PATTERN.matcher(msg).replaceAll("[REDACTED]");
        }

        if (hideName.isEnabled()) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player != null) {
                String name = mc.player.getGameProfile().getName();
                if (name != null && !name.isEmpty()) {
                    msg = msg.replace(name, "You");
                }
            }
        }

        if (hideCoords.isEnabled()) {
            msg = COORDS_PATTERN.matcher(msg).replaceAll("[REDACTED]");
        }

        event.setMessage(msg);
    }
}
