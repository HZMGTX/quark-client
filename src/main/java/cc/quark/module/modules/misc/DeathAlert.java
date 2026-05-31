package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.network.packet.s2c.play.DeathMessageS2CPacket;

public class DeathAlert extends Module {

    private final BoolSetting showCoords = register(new BoolSetting("ShowCoords", "Show location when a death is detected", false));

    public DeathAlert() {
        super("DeathAlert", "Notifies in chat when any player death message is received", Category.MISC);
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (!(event.getPacket() instanceof DeathMessageS2CPacket pkt)) return;
        String msg = pkt.message().getString();
        String display = "§cDeath: §f" + msg;
        if (showCoords.isEnabled() && mc.player != null) {
            display += String.format(" §7(%.0f, %.0f, %.0f)", mc.player.getX(), mc.player.getY(), mc.player.getZ());
        }
        mc.execute(() -> ChatUtil.addMessage(display));
    }
}
