package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;

public class AutoQueue extends Module {

    private final IntSetting    delay        = register(new IntSetting("Delay",         "Seconds to wait before re-joining",   15, 5, 60));
    private final StringSetting targetServer = register(new StringSetting("Target Server", "Server IP to rejoin (leave blank for last)", ""));

    private final TimerUtil timer       = new TimerUtil();
    private boolean         disconnected = false;

    public AutoQueue() {
        super("AutoQueue", "Automatically re-queues into servers (2b2t-style queue)", Category.MISC);
    }

    @Override
    public void onEnable() {
        disconnected = false;
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.currentScreen instanceof DisconnectedScreen && !disconnected) {
            disconnected = true;
            timer.reset();
            ChatUtil.info("Disconnected - will rejoin in " + delay.get() + "s");
        }
        if (disconnected && timer.hasReached((long) delay.get() * 1000L)) {
            disconnected = false;
            String server = targetServer.get();
            if (server.isBlank() && mc.getCurrentServerEntry() != null) {
                server = mc.getCurrentServerEntry().address;
            }
            if (!server.isBlank()) {
                final String finalServer = server;
                mc.execute(() -> mc.connect(
                    new net.minecraft.client.network.ServerAddress(finalServer.split(":")[0],
                        finalServer.contains(":") ? Integer.parseInt(finalServer.split(":")[1]) : 25565),
                    new net.minecraft.client.network.ServerInfo("AutoQueue", finalServer, net.minecraft.client.network.ServerInfo.ServerType.OTHER),
                    new TitleScreen()));
            }
        }
    }
}
