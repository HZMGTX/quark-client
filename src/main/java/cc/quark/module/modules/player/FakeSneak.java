package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;

public class FakeSneak extends Module {

    private boolean sentSneak = false;

    public FakeSneak() {
        super("FakeSneak", "Sends sneak packets to the server without actually sneaking visually", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        sentSneak = false;
        sendSneak(true);
    }

    @Override
    public void onDisable() {
        sendSneak(false);
        sentSneak = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!sentSneak) {
            sendSneak(true);
            sentSneak = true;
        }
    }

    private void sendSneak(boolean press) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        ClientCommandC2SPacket.Mode mode = press
                ? ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY
                : ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY;
        mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, mode));
    }
}
