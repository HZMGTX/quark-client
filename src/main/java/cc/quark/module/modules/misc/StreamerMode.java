package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.regex.Pattern;

public class StreamerMode extends Module {

    private static final Pattern IP_PATTERN = Pattern.compile(
            "\\b(?:\\d{1,3}\\.){3}\\d{1,3}(?::\\d+)?\\b|(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}(?::\\d+)?\\b");
    private static final Pattern COORDS_PATTERN = Pattern.compile(
            "(?i)[xXzZ]\\s*[=:]?\\s*-?\\d+");

    private final BoolSetting blurCoords  = register(new BoolSetting("Blur Coords", "Overlay coordinates with [HIDDEN]", true));
    private final BoolSetting blurServer  = register(new BoolSetting("Blur Server", "Hide server IP/address in HUD", true));
    private final BoolSetting hideIP      = register(new BoolSetting("Hide IP", "Replace IPs in chat with [REDACTED]", true));
    private final BoolSetting hideName    = register(new BoolSetting("Hide Name", "Replace own username in chat with 'You'", true));
    private final BoolSetting hideCoords  = register(new BoolSetting("Hide Coords", "Replace coord patterns in chat with [REDACTED]", true));
    private final ModeSetting blurLevel   = register(new ModeSetting("Blur Level", "How aggressively to obscure info", "Medium", "Light", "Medium", "Heavy"));

    public StreamerMode() {
        super("StreamerMode", "Hides sensitive info for streaming: coords, server IP, usernames", Category.MISC);
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

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.getWindow() == null) return;

        DrawContext ctx = event.getDrawContext();
        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();

        int blurAlpha = blurLevel.is("Light") ? 0x66 : blurLevel.is("Medium") ? 0xAA : 0xDD;

        if (blurCoords.isEnabled() && mc.player != null) {
            String hidden = "[HIDDEN]";
            int tw = mc.textRenderer.getWidth(hidden);
            int x = 4, y = 4;
            ctx.fill(x - 1, y - 1, x + tw + 5, y + mc.textRenderer.fontHeight * 3 + 5, blurAlpha << 24);
            ctx.drawTextWithShadow(mc.textRenderer, hidden, x + 2, y + 2, 0xFFFFAA00);
        }

        if (blurServer.isEnabled()) {
            net.minecraft.client.network.ServerInfo si = mc.getCurrentServerEntry();
            if (si != null) {
                int y = sh - 12;
                int w = mc.textRenderer.getWidth("Server: [HIDDEN]") + 6;
                ctx.fill(2, y - 1, 2 + w, y + mc.textRenderer.fontHeight + 1, blurAlpha << 24);
                ctx.drawTextWithShadow(mc.textRenderer, "Server: [HIDDEN]", 4, y, 0xFFFFAA00);
            }
        }
    }
}
