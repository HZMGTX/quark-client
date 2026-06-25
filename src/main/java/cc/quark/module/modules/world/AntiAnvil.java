package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;

/**
 * AntiAnvil - Prevents items from being destroyed by intercepting the anvil
 * "too expensive" slot update packet and cancelling it when enabled.
 */
public class AntiAnvil extends Module {

    private final BoolSetting cancel = register(new BoolSetting(
            "Cancel", "Cancel the packet that would destroy the item", true));

    public AntiAnvil() {
        super("AntiAnvil", "Prevents items from being destroyed on anvil overuse", Category.WORLD);
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (!cancel.isEnabled()) return;
        if (mc.player == null) return;

        // Cancel slot update packets for the anvil output slot (slot 2) when
        // the handler is an AnvilScreenHandler - the server sends an empty stack
        // to destroy the output item when repair cost is too high.
        if (event.getPacket() instanceof ScreenHandlerSlotUpdateS2CPacket pkt) {
            var handler = mc.player.currentScreenHandler;
            if (handler instanceof net.minecraft.screen.AnvilScreenHandler) {
                // Slot 2 is the anvil output
                if (pkt.getSlot() == 2) {
                    event.cancel();
                }
            }
        }
    }
}
