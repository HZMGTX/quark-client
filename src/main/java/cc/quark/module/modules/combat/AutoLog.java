package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.text.Text;

/**
 * AutoLog — disconnects when health drops below a threshold (EventTick).
 * Also monitors chat for PvP kill/death messages so it can alert the user
 * before the next respawn attempt.
 */
public class AutoLog extends Module {

    private final DoubleSetting health      = register(new DoubleSetting("Health",      "Disconnect at or below this HP value",   4.0,  1.0, 20.0));
    private final BoolSetting   chatMonitor = register(new BoolSetting  ("Chat Monitor","Detect kill/death messages in chat",     true));
    private final BoolSetting   logOnDeath  = register(new BoolSetting  ("Log On Death","Disconnect when a death message is seen", false));

    // Common death-message fragments (server-agnostic)
    private static final String[] DEATH_PATTERNS = {
        " was slain by ", " was killed by ", " died", " fell ", " drowned", " burned",
        " was shot by ", " blew up", " suffocated"
    };

    public AutoLog() {
        super("AutoLog", "Disconnects when HP is critical; monitors chat for kill/death events", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        if (mc.player.getHealth() <= (float) health.get()) {
            disconnect("HP " + String.format("%.1f", mc.player.getHealth()));
        }
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (!chatMonitor.isEnabled()) return;
        String msg = event.getMessage().toLowerCase();

        // Check if the message contains a player name matching our own
        String name = mc.player != null ? mc.player.getGameProfile().getName().toLowerCase() : "";

        for (String pattern : DEATH_PATTERNS) {
            if (msg.contains(name) && msg.contains(pattern.toLowerCase())) {
                ChatUtil.warn("[AutoLog] Death detected: " + event.getMessage());
                if (logOnDeath.isEnabled()) {
                    disconnect("death detected in chat");
                }
                break;
            }
        }
    }

    private void disconnect(String reason) {
        if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().getConnection().disconnect(
                    Text.literal("[AutoLog] " + reason));
        }
    }
}
