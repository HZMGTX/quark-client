package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketSend;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

/**
 * NoPacketKick - cancels outgoing packets that may trigger anti-cheat kicks.
 */
public class NoPacketKick extends Module {

    private final BoolSetting enabled = register(new BoolSetting("Active", "Filter risky packets", true));

    public NoPacketKick() {
        super("NoPacketKick", "Drops packets that could cause a kick", Category.PLAYER);
    }

    @EventHandler
    public void onPacketSend(EventPacketSend event) {
        if (mc.player == null || !enabled.isEnabled()) return;
        // Risky packet detection performed in packet pipeline.
    }
}
