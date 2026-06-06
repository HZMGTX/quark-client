package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.client.util.Clipboard;

public class ClipboardShare extends Module {
    private final BoolSetting autoCopy = register(new BoolSetting("Auto Copy", "Auto-copy coords to clipboard", false));
    private String lastClip = "";

    public ClipboardShare() { super("ClipboardShare", "Shares clipboard coords and URLs in chat", Category.MISC); }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null || mc.getWindow() == null) return;
        if (autoCopy.isEnabled()) {
            double x = mc.player.getX(), y = mc.player.getY(), z = mc.player.getZ();
            String coords = String.format("%.0f %.0f %.0f", x, y, z);
            if (!coords.equals(lastClip)) {
                mc.keyboard.setClipboard(coords);
                lastClip = coords;
            }
        }
    }
}
