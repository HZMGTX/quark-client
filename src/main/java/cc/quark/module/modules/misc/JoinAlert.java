package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class JoinAlert extends Module {

    private final BoolSetting alertJoin  = register(new BoolSetting("Alert Join",  "Notify when a player joins", true));
    private final BoolSetting alertLeave = register(new BoolSetting("Alert Leave", "Notify when a player leaves", true));
    private final BoolSetting ignoreSelf = register(new BoolSetting("Ignore Self", "Don't alert for yourself", true));

    private final Set<UUID> knownPlayers = new HashSet<>();

    public JoinAlert() {
        super("JoinAlert", "Alerts via chat when a player joins or leaves the server", Category.MISC);
    }

    @Override
    public void onEnable() {
        knownPlayers.clear();
        if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().getPlayerList().forEach(e -> knownPlayers.add(e.getProfile().getId()));
        }
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (!(event.getPacket() instanceof PlayerListS2CPacket pkt)) return;

        mc.execute(() -> {
            for (PlayerListS2CPacket.Entry entry : pkt.getPlayerAdditionEntries()) {
                UUID id = entry.profileId();
                if (knownPlayers.add(id)) {
                    if (!alertJoin.isEnabled()) continue;
                    if (ignoreSelf.isEnabled() && mc.player != null && mc.player.getUuid().equals(id)) continue;
                    String name = entry.profile() != null ? entry.profile().getName() : id.toString();
                    ChatUtil.info("§a+ §f" + name + " §7joined");
                }
            }
            for (PlayerListS2CPacket.Entry entry : pkt.getPlayerRemovalEntries()) {
                UUID id = entry.profileId();
                if (knownPlayers.remove(id)) {
                    if (!alertLeave.isEnabled()) continue;
                    if (ignoreSelf.isEnabled() && mc.player != null && mc.player.getUuid().equals(id)) continue;
                    String name = entry.profile() != null ? entry.profile().getName() : id.toString();
                    ChatUtil.info("§c- §f" + name + " §7left");
                }
            }
        });
    }
}
