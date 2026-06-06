package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketSend;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;

public class NoSwing2 extends Module {

    private final BoolSetting hideLocal = register(new BoolSetting("HideLocal", "Hide swing animation locally", true));
    private final BoolSetting hideNetwork = register(new BoolSetting("HideNetwork", "Cancel swing packets", true));
    private final BoolSetting onlyOffhand = register(new BoolSetting("OnlyOffhand", "Only suppress offhand swings", false));

    public NoSwing2() {
        super("NoSwing2", "Advanced swing suppression for local and network", Category.PLAYER);
    }

    @EventHandler
    public void onPacketSend(EventPacketSend event) {
        if (!hideNetwork.getValue()) return;
        if (!(event.getPacket() instanceof HandSwingC2SPacket packet)) return;
        if (onlyOffhand.getValue() && packet.getHand() != net.minecraft.util.Hand.OFF_HAND) return;
        event.cancel();
    }
}
