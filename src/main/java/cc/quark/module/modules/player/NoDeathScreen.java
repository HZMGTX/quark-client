package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;

public class NoDeathScreen extends Module {

    private final BoolSetting autoRespawn = register(new BoolSetting(
            "Auto Respawn", "Automatically respawn instead of showing death screen", true));
    private final BoolSetting closeInstant = register(new BoolSetting(
            "Close Instant", "Close death screen instantly each tick if it somehow shows", true));

    public NoDeathScreen() {
        super("NoDeathScreen", "Suppresses the death screen and optionally auto-respawns immediately", Category.PLAYER);
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (!(event.getPacket() instanceof GameStateChangeS2CPacket pkt)) return;
        if (mc.player == null) return;

        if (pkt.getReason() == GameStateChangeS2CPacket.WIN_GAME) {
            event.cancel();
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!(mc.currentScreen instanceof DeathScreen)) return;

        if (autoRespawn.isEnabled()) {
            if (mc.getNetworkHandler() != null) {
                mc.getNetworkHandler().sendPacket(
                        new ClientStatusC2SPacket(ClientStatusC2SPacket.Mode.PERFORM_RESPAWN));
                mc.player.requestRespawn();
            }
            if (closeInstant.isEnabled()) {
                mc.setScreen(null);
            }
        } else if (closeInstant.isEnabled()) {
            mc.setScreen(null);
        }
    }
}
