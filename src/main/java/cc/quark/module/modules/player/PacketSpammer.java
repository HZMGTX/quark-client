package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Hand;

/**
 * PacketSpammer - sends extra swing packets each tick.
 */
public class PacketSpammer extends Module {

    private final IntSetting amount = register(new IntSetting("Amount", "Packets per tick", 5, 1, 50));

    public PacketSpammer() {
        super("PacketSpammer", "Spams swing packets to the server", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        for (int i = 0; i < amount.get(); i++) {
            mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        }
    }
}
