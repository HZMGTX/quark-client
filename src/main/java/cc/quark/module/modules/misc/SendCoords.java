package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventKey;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class SendCoords extends Module {

    private final ModeSetting format = register(new ModeSetting(
            "Format", "Message format", "Full", "Full", "Short", "Nether"));
    private final BoolSetting announce = register(new BoolSetting(
            "Announce", "Send coordinates to chat", false));

    public SendCoords() {
        super("SendCoords", "Sends your current coordinates to chat on key press", Category.MISC);
    }

    @Override
    public String getSuffix() {
        if (mc.player == null) return "";
        return String.format("%.0f %.0f %.0f",
            mc.player.getX(), mc.player.getY(), mc.player.getZ());
    }

    @EventHandler
    public void onKey(EventKey event) {
        if (mc.player == null || event.getKeyCode() != GLFW.GLFW_KEY_F9) return;
        sendCoords();
    }

    private void sendCoords() {
        if (mc.player == null) return;
        double x = mc.player.getX(), y = mc.player.getY(), z = mc.player.getZ();
        String msg;
        switch (format.get()) {
            case "Short" -> msg = String.format("%.0f %.0f %.0f", x, y, z);
            case "Nether" -> msg = String.format("Nether: %.0f %.0f %.0f | Overworld: %.0f %.0f %.0f",
                x / 8, y, z / 8, x * 8, y, z * 8);
            default -> msg = String.format("X: %.0f Y: %.0f Z: %.0f", x, y, z);
        }

        if (announce.isEnabled()) {
            mc.player.networkHandler.sendChatMessage(msg);
        } else {
            mc.player.sendMessage(Text.literal("§7[Coords] §f" + msg), false);
        }
    }
}
