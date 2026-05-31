package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketSend;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;

public class ThrowFilter extends Module {

    private final BoolSetting onlyInCombat = register(new BoolSetting(
            "OnlyInCombat", "Only block drops when a player is nearby", false));

    public ThrowFilter() {
        super("ThrowFilter", "Prevents accidentally throwing items by pressing Q", Category.PLAYER);
    }

    @EventHandler
    public void onPacketSend(EventPacketSend event) {
        if (!(event.getPacket() instanceof PlayerActionC2SPacket pkt)) return;
        if (pkt.getAction() != PlayerActionC2SPacket.Action.DROP_ITEM
                && pkt.getAction() != PlayerActionC2SPacket.Action.DROP_ALL_ITEMS) return;

        if (onlyInCombat.isEnabled()) {
            if (mc.player == null || mc.world == null) return;
            boolean nearPlayer = mc.world.getPlayers().stream()
                    .filter(p -> p != mc.player)
                    .anyMatch(p -> p.squaredDistanceTo(mc.player) < 100);
            if (!nearPlayer) return;
        }

        event.cancel();
    }
}
