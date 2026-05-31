package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.StringSetting;
import net.minecraft.network.packet.s2c.play.PickupItemAnimationS2CPacket;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PickupFilter extends Module {

    private final BoolSetting whitelistMode = register(new BoolSetting(
            "WhitelistMode", "Only allow picking up items on the list (false = blacklist)", false));

    private final StringSetting itemList = register(new StringSetting(
            "Items", "Comma-separated item names (e.g. dirt,gravel,sand)", "dirt,gravel,sand"));

    public PickupFilter() {
        super("PickupFilter", "Filters which items get picked up from the ground", Category.PLAYER);
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (!(event.getPacket() instanceof PickupItemAnimationS2CPacket pkt)) return;

        mc.execute(() -> {
            if (mc.world == null || mc.player == null) return;

            var entity = mc.world.getEntityById(pkt.getEntityId());
            if (entity == null) return;

            String entityName = entity.getType().getName().getString().toLowerCase();
            Set<String> list = parseList();

            boolean inList = list.contains(entityName);

            if (whitelistMode.isEnabled() && !inList) {
                event.cancel();
            } else if (!whitelistMode.isEnabled() && inList) {
                event.cancel();
            }
        });
    }

    private Set<String> parseList() {
        Set<String> result = new HashSet<>();
        for (String s : itemList.get().split(",")) {
            result.add(s.trim().toLowerCase());
        }
        return result;
    }
}
