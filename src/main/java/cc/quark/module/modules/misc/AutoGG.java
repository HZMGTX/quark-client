package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.TimerUtil;

public class AutoGG extends Module {

    private final StringSetting message  = register(new StringSetting("Message",   "Message to send after a match/death",   "gg"));
    private final IntSetting    delay    = register(new IntSetting   ("Delay",     "Delay in ms before sending GG",         1500, 0, 10000));
    private final BoolSetting   onDeath  = register(new BoolSetting  ("On Death",  "Send GG when the player dies",          true));
    private final BoolSetting   onGame   = register(new BoolSetting  ("On Game End","Send GG on 'game over' style messages", true));

    private final TimerUtil cooldown = new TimerUtil();
    private boolean pendingGG = false;

    /** Keywords in incoming chat that indicate a round/match ended. */
    private static final String[] TRIGGERS = {
        "game over", "round over", "match ended", "you died", "you were killed",
        "has won", "wins the game", "victory", "the game has ended", "game end"
    };

    public AutoGG() {
        super("AutoGG", "Auto-sends a GG message after a match ends or the player dies", Category.MISC);
    }

    @Override
    public void onEnable() {
        pendingGG = false;
        cooldown.reset();
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (!event.isIncoming()) return;
        if (mc.player == null) return;

        String msg = event.getMessage().toLowerCase();

        // Death detection: server sends "You died" style messages
        if (onDeath.isEnabled() && (msg.contains("you died") || msg.contains("you were killed")
                || msg.contains("respawn"))) {
            scheduleGG();
            return;
        }

        // Game-over keyword scan
        if (onGame.isEnabled()) {
            for (String trigger : TRIGGERS) {
                if (msg.contains(trigger)) {
                    scheduleGG();
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onTick(cc.quark.event.events.EventTick event) {
        if (!pendingGG) return;
        if (!cooldown.hasReached(delay.get())) return;
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        String gg = message.get().trim();
        if (!gg.isEmpty()) {
            mc.getNetworkHandler().sendChatMessage(gg);
        }
        pendingGG = false;
    }

    private void scheduleGG() {
        // Ignore if we already sent one recently (5 second window)
        if (!cooldown.hasReached(5000) && !pendingGG) return;
        pendingGG = true;
        cooldown.reset();
    }
}
