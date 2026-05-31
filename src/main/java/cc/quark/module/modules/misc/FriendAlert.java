package cc.quark.module.modules.misc;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.friend.FriendManager;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FriendAlert extends Module {

    private final BoolSetting alertJoin  = register(new BoolSetting("AlertJoin",  "Alert when a friend joins", true));
    private final BoolSetting alertLeave = register(new BoolSetting("AlertLeave", "Alert when a friend leaves", true));

    private final Set<UUID> knownPlayers = new HashSet<>();

    public FriendAlert() {
        super("FriendAlert", "Pings in chat when a friend from your friend list joins or leaves", Category.MISC);
    }

    @Override
    public void onEnable() {
        knownPlayers.clear();
        if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().getPlayerList().forEach(e -> knownPlayers.add(e.getProfile().getId()));
        }
    }

    private FriendManager fm() {
        return Quark.getInstance().getFriendManager();
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (!(event.getPacket() instanceof PlayerListS2CPacket pkt)) return;
        FriendManager fm = fm();
        if (fm == null) return;

        mc.execute(() -> {
            for (PlayerListS2CPacket.Entry entry : pkt.getPlayerAdditionEntries()) {
                UUID id = entry.profileId();
                if (!knownPlayers.add(id)) continue;
                if (!alertJoin.isEnabled()) continue;
                String name = entry.profile() != null ? entry.profile().getName() : "";
                if (fm.isFriend(name)) {
                    ChatUtil.addMessage("§b[FriendAlert] §a+ §f" + name + " §7(friend) joined!");
                }
            }
            for (PlayerListS2CPacket.Entry entry : pkt.getPlayerRemovalEntries()) {
                UUID id = entry.profileId();
                if (!knownPlayers.remove(id)) continue;
                if (!alertLeave.isEnabled()) continue;
                String name = entry.profile() != null ? entry.profile().getName() : "";
                if (fm.isFriend(name)) {
                    ChatUtil.addMessage("§b[FriendAlert] §c- §f" + name + " §7(friend) left");
                }
            }
        });
    }
}
