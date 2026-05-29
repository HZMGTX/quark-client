package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;

public class AutoRespawn extends Module {

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Ticks to wait before respawning (20 = 1 second)", 20, 0, 100));
    private final BoolSetting closeScreen = register(new BoolSetting(
            "Close Screen", "Close death screen after respawning", true));

    private boolean isDead = false;
    private int ticksWaited = 0;

    public AutoRespawn() {
        super("AutoRespawn", "Automatically respawns after death", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        isDead = false;
        ticksWaited = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        boolean dead = mc.player.isDead()
                || mc.player.getHealth() <= 0f
                || mc.currentScreen instanceof DeathScreen;

        if (dead && !isDead) {
            isDead = true;
            ticksWaited = 0;
        }

        if (!isDead) return;

        if (ticksWaited < delay.get()) {
            ticksWaited++;
            return;
        }

        // Send respawn packet
        if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendPacket(new ClientStatusC2SPacket(ClientStatusC2SPacket.Mode.PERFORM_RESPAWN));
        }
        mc.player.requestRespawn();

        if (closeScreen.isEnabled() && mc.currentScreen instanceof DeathScreen) {
            mc.setScreen(null);
        }

        isDead = false;
        ticksWaited = 0;
    }
}
