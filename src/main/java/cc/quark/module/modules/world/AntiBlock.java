package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;

/**
 * AntiBlock - Prevents servers from cancelling block placements by intercepting
 * block update packets that revert player-placed blocks.
 */
public class AntiBlock extends Module {

    private final BoolSetting override = register(new BoolSetting(
            "Override", "Cancel server block revert packets", true));

    public AntiBlock() {
        super("AntiBlock", "Prevents block placement from being cancelled", Category.WORLD);
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (!override.isEnabled()) return;
        if (mc.player == null || mc.world == null) return;

        // If the server sends a BlockUpdateS2CPacket for a block the player
        // just placed (i.e., the server wants to set it to AIR), cancel it.
        if (event.getPacket() instanceof BlockUpdateS2CPacket pkt) {
            var serverState = pkt.getState();
            var localState  = mc.world.getBlockState(pkt.getPos());

            // If local world has a non-air block but server wants air → cancel
            if (!localState.isAir() && serverState.isAir()) {
                event.cancel();
            }
        }
    }
}
