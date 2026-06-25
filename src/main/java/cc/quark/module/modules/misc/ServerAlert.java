package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.StringSetting;
import net.minecraft.sound.SoundEvents;

/**
 * ServerAlert - Monitors incoming chat for user-defined trigger phrases and
 * plays a sound / prints a local notification when one is detected.
 *
 * Useful for alerting when a queue position is reached, a player joins, a
 * minigame starts, or any other server message appears.
 */
public class ServerAlert extends Module {

    private final StringSetting triggers = register(new StringSetting(
            "Triggers", "Comma-separated phrases to watch for", "Queue complete,game starting,has joined"));

    private final BoolSetting   caseSensitive = register(new BoolSetting(
            "Case Sensitive", "Match case exactly", false));

    private final BoolSetting   sound = register(new BoolSetting(
            "Sound", "Play alert sound on match", true));

    private final DoubleSetting volume = register(new DoubleSetting(
            "Volume", "Alert sound volume", 1.0, 0.1, 3.0));

    private final DoubleSetting pitch = register(new DoubleSetting(
            "Pitch", "Alert sound pitch", 1.0, 0.5, 2.0));

    private final BoolSetting   printAlert = register(new BoolSetting(
            "Print Alert", "Print a highlighted notice in chat", true));

    private final BoolSetting   cancelOriginal = register(new BoolSetting(
            "Cancel Msg", "Suppress the original message (only show the alert)", false));

    public ServerAlert() {
        super("ServerAlert", "Alerts when specific server messages are received", Category.MISC);
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (!event.isIncoming()) return;
        if (mc.player == null) return;

        String msg = event.getMessage();
        if (msg == null || msg.isEmpty()) return;

        String compare = caseSensitive.isEnabled() ? msg : msg.toLowerCase();

        String triggerList = triggers.get().trim();
        if (triggerList.isEmpty()) return;

        String matched = null;
        for (String phrase : triggerList.split(",")) {
            phrase = phrase.trim();
            if (phrase.isEmpty()) continue;
            String cmp = caseSensitive.isEnabled() ? phrase : phrase.toLowerCase();
            if (compare.contains(cmp)) {
                matched = phrase;
                break;
            }
        }

        if (matched == null) return;

        if (sound.isEnabled()) {
            mc.player.playSound(
                    SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(),
                    (float) volume.get(),
                    (float) pitch.get());
        }

        if (printAlert.isEnabled()) {
            mc.inGameHud.getChatHud().addMessage(
                    net.minecraft.text.Text.literal("§c[ServerAlert] §fMatched: §e" + matched));
        }

        if (cancelOriginal.isEnabled()) {
            event.cancel();
        }
    }
}
