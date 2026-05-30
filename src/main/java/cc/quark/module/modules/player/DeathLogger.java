package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.network.packet.s2c.play.DeathMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DeathLogger extends Module {

    private final BoolSetting chatLog = register(new BoolSetting(
            "ChatLog", "Log death info to chat", true));
    private final BoolSetting fileLog = register(new BoolSetting(
            "FileLog", "Log death info to file (not yet implemented)", false));

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public DeathLogger() {
        super("DeathLogger", "Logs player death information including coordinates and cause", Category.PLAYER);
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (mc.player == null) return;

        if (event.getPacket() instanceof DeathMessageS2CPacket deathPacket) {
            if (!chatLog.isEnabled()) return;
            String time = LocalTime.now().format(TIME_FMT);
            String cause = deathPacket.getMessage().getString();
            double x = mc.player.getX();
            double y = mc.player.getY();
            double z = mc.player.getZ();
            ChatUtil.warn("[DeathLogger] " + time + " | Died at X:" +
                    String.format("%.1f", x) + " Y:" + String.format("%.1f", y) + " Z:" + String.format("%.1f", z) +
                    " | Cause: " + cause);
        } else if (event.getPacket() instanceof PlayerRespawnS2CPacket) {
            if (!chatLog.isEnabled()) return;
            String time = LocalTime.now().format(TIME_FMT);
            double x = mc.player.getX();
            double y = mc.player.getY();
            double z = mc.player.getZ();
            ChatUtil.info("[DeathLogger] " + time + " | Respawned (was at X:" +
                    String.format("%.1f", x) + " Y:" + String.format("%.1f", y) + " Z:" + String.format("%.1f", z) + ")");
        }
    }
}
