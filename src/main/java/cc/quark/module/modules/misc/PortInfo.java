package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;

public class PortInfo extends Module {

    private final BoolSetting showMOTD = register(new BoolSetting(
            "ShowMOTD", "Also display the server MOTD in chat on join", false));

    private boolean shown = false;

    public PortInfo() {
        super("PortInfo", "Shows server IP and port in chat when joining a server", Category.MISC);
    }

    @Override
    public void onEnable() {
        shown = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.getCurrentServerEntry() == null) {
            shown = false;
            return;
        }
        if (shown) return;
        shown = true;
        ServerInfo info = mc.getCurrentServerEntry();
        String address = info.address;
        mc.player.sendMessage(Text.literal("§7[PortInfo] §aServer: §f" + address), false);
        if (showMOTD.isEnabled()) {
            String motd = info.label != null ? info.label.getString() : "N/A";
            mc.player.sendMessage(Text.literal("§7[PortInfo] §aMOTD: §f" + motd), false);
        }
    }
}
