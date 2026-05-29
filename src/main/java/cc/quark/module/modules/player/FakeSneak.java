package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ModeSetting;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;

public class FakeSneak extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "When to send sneak packet",
            "Always", "Always", "Toggle"));

    private boolean sneakSent = false;
    private boolean toggled = false;

    public FakeSneak() {
        super("FakeSneak", "Sends sneak packets to appear sneaking to other players without actually sneaking", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        sneakSent = false;
        toggled = false;
        sendSneak(true);
        sneakSent = true;
    }

    @Override
    public void onDisable() {
        if (sneakSent) {
            sendSneak(false);
            sneakSent = false;
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        if (mode.is("Always")) {
            if (!sneakSent) {
                sendSneak(true);
                sneakSent = true;
            }
        } else {
            // Toggle mode: sneak while not actually sneaking
            boolean actualSneak = mc.player.isSneaking();
            if (!actualSneak && !sneakSent) {
                sendSneak(true);
                sneakSent = true;
            } else if (actualSneak && sneakSent) {
                sendSneak(false);
                sneakSent = false;
            }
        }
    }

    private void sendSneak(boolean press) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        ClientCommandC2SPacket.Mode pktMode = press
                ? ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY
                : ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY;
        mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, pktMode));
    }
}
