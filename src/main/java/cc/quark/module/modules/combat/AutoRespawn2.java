package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;

public class AutoRespawn2 extends Module {

    private final IntSetting delayMs = register(new IntSetting("Delay Ms", "Milliseconds before auto-respawning", 200, 0, 2000));

    private final TimerUtil timer = new TimerUtil();
    private boolean deathScreenSeen = false;

    public AutoRespawn2() {
        super("AutoRespawn2", "Auto respawns on death screen", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        deathScreenSeen = false;
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        boolean onDeathScreen = mc.currentScreen instanceof DeathScreen;

        if (onDeathScreen) {
            if (!deathScreenSeen) {
                deathScreenSeen = true;
                timer.reset();
            }
            if (timer.hasReached(delayMs.get())) {
                mc.player.requestRespawn();
                mc.getNetworkHandler().sendPacket(
                        new ClientStatusC2SPacket(ClientStatusC2SPacket.Mode.PERFORM_RESPAWN));
                deathScreenSeen = false;
                timer.reset();
            }
        } else {
            deathScreenSeen = false;
        }
    }
}
