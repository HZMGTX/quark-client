package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.network.packet.s2c.login.LoginDisconnectS2CPacket;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;

public class AntiCombatLog extends Module {

    private final BoolSetting reconnect = register(new BoolSetting("Reconnect", "Attempt to reconnect after combat log detection", false));
    private final IntSetting delayMs = register(new IntSetting("Delay Ms", "Milliseconds before reconnecting", 500, 100, 5000));

    private boolean combatLogDetected = false;
    private final TimerUtil timer = new TimerUtil();

    public AntiCombatLog() {
        super("AntiCombatLog", "Freezes packets on combat logout attempt detection", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        combatLogDetected = false;
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (mc.player == null) return;

        // Detect server-side disconnect packet during active combat
        if (event.getPacket() instanceof DisconnectS2CPacket
                || event.getPacket() instanceof LoginDisconnectS2CPacket) {

            // Check if we're in combat (recently attacked or took damage)
            if (mc.player.hurtTime > 0 || mc.player.getAttackCooldownProgress(0f) < 0.9f) {
                combatLogDetected = true;
                timer.reset();
                // Cancel the disconnect packet to try to freeze state
                event.cancel();
            }
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (!combatLogDetected) return;
        if (!reconnect.isEnabled()) {
            combatLogDetected = false;
            return;
        }

        if (timer.hasReached(delayMs.get())) {
            combatLogDetected = false;
            // Reconnect logic: re-connect to the last server
            if (mc.getCurrentServerEntry() != null) {
                // ConnectScreen.connect
            }
        }
    }

    public boolean isCombatLogDetected() {
        return combatLogDetected;
    }
}
