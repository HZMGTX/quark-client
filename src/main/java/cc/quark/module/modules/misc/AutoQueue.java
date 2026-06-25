package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

public class AutoQueue extends Module {

    private final IntSetting    delay        = register(new IntSetting("Delay",          "Seconds to wait before re-joining",             15, 5, 60));
    private final StringSetting targetServer = register(new StringSetting("Target Server", "Server IP to rejoin (blank = last server used)", ""));

    private int         countdown  = -1;
    private ServerInfo  lastServer = null;

    public AutoQueue() {
        super("AutoQueue", "Automatically re-queues into servers (2b2t-style queue)", Category.MISC);
    }

    @Override
    public void onEnable() {
        countdown = -1;
    }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.currentScreen instanceof DisconnectedScreen) {
            if (mc.getCurrentServerEntry() != null) lastServer = mc.getCurrentServerEntry();

            if (countdown < 0) {
                countdown = delay.get() * 20;
                ChatUtil.info("AutoQueue: rejoining in " + delay.get() + "s...");
            } else if (--countdown <= 0) {
                countdown = -1;
                String ip = targetServer.get();
                ServerInfo target = null;
                if (!ip.isBlank()) {
                    target = new ServerInfo("AutoQueue", ip, ServerInfo.ServerType.OTHER);
                } else if (lastServer != null) {
                    target = lastServer;
                }
                if (target != null) {
                    ServerAddress addr = ServerAddress.parse(target.address);
                    ConnectScreen.connect(mc, addr, target, null);
                }
            }
        } else {
            countdown = -1;
        }
    }
}
