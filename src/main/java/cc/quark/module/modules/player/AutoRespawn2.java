package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;

/**
 * AutoRespawn2 — clicks the respawn button instantly on death with zero delay.
 * Enhanced version with immediate-respawn mode and optional death-log notification.
 */
public class AutoRespawn2 extends Module {

    private final BoolSetting instant = register(new BoolSetting(
            "Instant", "Respawn immediately with no delay", true));
    private final IntSetting delay = register(new IntSetting(
            "Delay", "Ticks to wait before respawning when Instant is off (20=1s)", 0, 0, 100));
    private final BoolSetting closeScreen = register(new BoolSetting(
            "Close Screen", "Close death screen after respawning", true));
    private final BoolSetting sendDeathMsg = register(new BoolSetting(
            "Death Message", "Print death coordinates to chat", true));

    private boolean dead = false;
    private int waited = 0;
    private boolean msgSent = false;

    public AutoRespawn2() {
        super("AutoRespawn2", "Clicks respawn button instantly on death", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        dead = false;
        waited = 0;
        msgSent = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        boolean isDead = mc.player.getHealth() <= 0f
                || mc.player.isRemoved()
                || mc.currentScreen instanceof DeathScreen;

        if (isDead && !dead) {
            dead = true;
            waited = 0;
            msgSent = false;

            if (sendDeathMsg.isEnabled()) {
                double x = mc.player.getX();
                double y = mc.player.getY();
                double z = mc.player.getZ();
                mc.player.sendMessage(
                        net.minecraft.text.Text.literal(
                                "[AutoRespawn2] Died at X:" + (int)x + " Y:" + (int)y + " Z:" + (int)z),
                        false);
                msgSent = true;
            }
        }

        if (!dead) return;

        if (!instant.isEnabled()) {
            waited++;
            if (waited < delay.get()) return;
        }

        if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendPacket(
                    new ClientStatusC2SPacket(ClientStatusC2SPacket.Mode.PERFORM_RESPAWN));
        }
        mc.player.requestRespawn();

        if (closeScreen.isEnabled() && mc.currentScreen instanceof DeathScreen) {
            mc.setScreen(null);
        }

        dead = false;
        waited = 0;
    }
}
