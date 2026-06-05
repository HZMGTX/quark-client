package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketSend;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.Direction;

/**
 * AntiItemDrop - intercepts outgoing item-drop action packets to prevent
 * accidentally dropping items with Q (or inventory drag-drops).
 *
 * <p>In Minecraft, pressing Q sends a {@code PlayerActionC2SPacket} with action
 * {@code DROP_ITEM} or {@code DROP_ALL_ITEMS}.  Cancelling those packets stops
 * the server from ever removing the item.
 */
public class AntiItemDrop extends Module {

    private final BoolSetting onlyInCombat = register(new BoolSetting(
            "Only In Combat", "Only prevent drops when another player is within 10 blocks", false));

    public AntiItemDrop() {
        super("Anti Item Drop", "Prevents accidentally dropping items with Q key", Category.PLAYER);
    }

    @EventHandler
    public void onPacketSend(EventPacketSend event) {
        if (!(event.getPacket() instanceof PlayerActionC2SPacket pkt)) return;

        PlayerActionC2SPacket.Action action = pkt.getAction();
        if (action != PlayerActionC2SPacket.Action.DROP_ITEM
                && action != PlayerActionC2SPacket.Action.DROP_ALL_ITEMS) return;

        if (!onlyInCombat.isEnabled() || isNearPlayer()) {
            event.cancel();
        }
    }

    private boolean isNearPlayer() {
        if (mc.player == null || mc.world == null) return false;
        for (var p : mc.world.getPlayers()) {
            if (p != mc.player && p.distanceTo(mc.player) < 10) return true;
        }
        return false;
    }
}
