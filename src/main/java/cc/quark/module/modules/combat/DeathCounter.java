package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.text.Text;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DeathCounter extends Module {

    private int deaths = 0;
    private final List<String> timestamps = new ArrayList<>();

    public DeathCounter() {
        super("DeathCounter", "Tracks deaths this session with timestamps; displays count in action bar", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        deaths = 0;
        timestamps.clear();
    }

    @Override
    public String getSuffix() {
        return String.valueOf(deaths);
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (mc.player == null || mc.world == null) return;
        if (event.getPacket() instanceof EntityStatusS2CPacket pkt) {
            mc.execute(() -> {
                // Status 35 = death for living entities; check if it's our player
                if (pkt.getStatus() == 35 && pkt.getEntity(mc.world) == mc.player) {
                    deaths++;
                    timestamps.add(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                }
            });
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        mc.player.sendMessage(Text.literal("Deaths: " + deaths), true);
    }
}
