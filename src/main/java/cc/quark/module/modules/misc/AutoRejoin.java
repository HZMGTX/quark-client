package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

public class AutoRejoin extends Module {

    private final IntSetting  delay       = register(new IntSetting("Delay", "Seconds to wait before rejoining", 5, 1, 60));
    private final IntSetting  maxAttempts = register(new IntSetting("Max Attempts", "Max reconnect attempts (0 = infinite)", 0, 0, 20));
    private final BoolSetting notify      = register(new BoolSetting("Notify", "Show chat notification on reconnect", true));

    private final TimerUtil timer = new TimerUtil();
    private int attempts = 0;
    private String lastAddress = "";
    private String lastName = "Server";

    public AutoRejoin() {
        super("AutoRejoin", "Auto-rejoins the last server after a disconnect", Category.MISC);
    }

    @Override
    public void onEnable() {
        attempts = 0;
        timer.reset();
        // Cache current server info while connected
        if (mc.getCurrentServerEntry() != null) {
            lastAddress = mc.getCurrentServerEntry().address;
            lastName = mc.getCurrentServerEntry().name;
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        // While connected, keep updating last known server
        if (mc.player != null && mc.getCurrentServerEntry() != null) {
            lastAddress = mc.getCurrentServerEntry().address;
            lastName = mc.getCurrentServerEntry().name;
            return;
        }

        // Only act when on a disconnect screen
        if (!(mc.currentScreen instanceof DisconnectedScreen)) return;
        if (lastAddress.isEmpty()) return;
        if (!timer.hasReached(delay.get() * 1000L)) return;

        int max = maxAttempts.get();
        if (max > 0 && attempts >= max) return;

        attempts++;
        timer.reset();

        // Reconnect
        ServerInfo info = new ServerInfo(lastName, lastAddress, ServerInfo.ServerType.OTHER);
        ServerAddress addr = ServerAddress.parse(lastAddress);
        mc.connect(addr, info, mc.getProfileKeys());

        if (notify.isEnabled() && mc.inGameHud != null) {
            mc.inGameHud.getChatHud().addMessage(
                    net.minecraft.text.Text.literal(
                            "§e[AutoRejoin] §fReconnecting to §b" + lastAddress +
                            " §7(attempt " + attempts + ")"));
        }
    }
}
