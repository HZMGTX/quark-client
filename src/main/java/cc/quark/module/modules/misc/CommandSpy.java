package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.text.Text;

public class CommandSpy extends Module {

    private final BoolSetting hideFromServer = register(new BoolSetting(
            "HideFromServer", "Cancel the command packet so server never receives it (outgoing only)", false));

    public CommandSpy() {
        super("CommandSpy", "Logs all commands sent to server in local chat", Category.MISC);
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (event.isIncoming()) return;
        String msg = event.getMessage();
        if (!msg.startsWith("/")) return;
        if (mc.player != null) {
            mc.player.sendMessage(Text.literal("§7[CommandSpy] §e" + msg), false);
        }
        if (hideFromServer.isEnabled()) {
            event.cancel();
        }
    }
}
