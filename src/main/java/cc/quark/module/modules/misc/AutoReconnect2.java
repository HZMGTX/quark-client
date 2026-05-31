package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;

public class AutoReconnect2 extends Module {

    private final IntSetting delaySeconds = register(new IntSetting(
            "Delay", "Seconds to wait before reconnecting", 5, 1, 60));

    private final IntSetting maxAttempts = register(new IntSetting(
            "MaxAttempts", "Maximum reconnect attempts (0 = unlimited)", 0, 0, 20));

    private final BoolSetting announceReason = register(new BoolSetting(
            "AnnounceReason", "Print disconnect reason when kicked", true));

    private final TimerUtil timer = new TimerUtil();
    private boolean disconnected = false;
    private int attempts = 0;
    private String lastServer = null;
    private int lastPort = 25565;

    public AutoReconnect2() {
        super("AutoReconnect2", "Automatically reconnects to the server on disconnect", Category.MISC);
    }

    @Override
    public void onEnable() {
        disconnected = false;
        attempts = 0;
        // Save current server info if connected
        if (mc.getCurrentServerEntry() != null) {
            lastServer = mc.getCurrentServerEntry().address;
            lastPort = 25565;
        }
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (!(event.getPacket() instanceof DisconnectS2CPacket pkt)) return;

        // Save server info before disconnect
        if (mc.getCurrentServerEntry() != null) {
            lastServer = mc.getCurrentServerEntry().address;
        }

        mc.execute(() -> {
            if (announceReason.isEnabled()) {
                ChatUtil.warn("AutoReconnect2: Disconnected — " + pkt.getReason().getString());
            }
            disconnected = true;
            timer.reset();
        });
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (!disconnected || lastServer == null) return;
        if (!timer.hasReached(delaySeconds.get() * 1000L)) return;

        int max = maxAttempts.get();
        if (max > 0 && attempts >= max) {
            ChatUtil.warn("AutoReconnect2: Max attempts reached (" + attempts + ").");
            disconnected = false;
            return;
        }

        // Check if we are on the disconnect screen or title screen
        if (mc.currentScreen instanceof DisconnectedScreen || mc.currentScreen instanceof TitleScreen
                || mc.currentScreen instanceof MultiplayerScreen) {
            attempts++;
            ChatUtil.info("AutoReconnect2: Reconnecting (attempt " + attempts + ")...");

            String server = lastServer;
            mc.execute(() -> {
                try {
                    ServerAddress addr = ServerAddress.parse(server);
                    ServerInfo info = new ServerInfo("AutoReconnect", server, ServerInfo.ServerType.OTHER);
                    ConnectScreen.connect(mc.currentScreen, mc, addr, info, false, null);
                } catch (Exception e) {
                    ChatUtil.warn("AutoReconnect2: Failed to connect — " + e.getMessage());
                }
            });

            disconnected = false;
            timer.reset();
        }
    }
}
