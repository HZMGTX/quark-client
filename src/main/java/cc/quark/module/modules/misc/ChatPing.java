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
 * ChatPing - Plays a sound and optionally highlights the chat message when
 * your own username (or custom keywords) appears in incoming chat.
 */
public class ChatPing extends Module {

    private final BoolSetting pingOnName = register(new BoolSetting(
            "Ping On Name", "Alert when your username is mentioned", true));

    private final StringSetting keywords = register(new StringSetting(
            "Keywords", "Extra comma-separated words to alert on (leave blank to disable)", ""));

    private final BoolSetting sound = register(new BoolSetting(
            "Sound", "Play a notification sound on match", true));

    private final DoubleSetting volume = register(new DoubleSetting(
            "Volume", "Notification sound volume", 1.0, 0.1, 3.0));

    private final DoubleSetting pitch = register(new DoubleSetting(
            "Pitch", "Notification sound pitch", 1.5, 0.5, 2.0));

    private final BoolSetting printHighlight = register(new BoolSetting(
            "Highlight", "Print a highlighted copy of the message in chat", true));

    public ChatPing() {
        super("ChatPing", "Plays a sound and highlights chat when your name is mentioned", Category.MISC);
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (!event.isIncoming()) return;
        if (mc.player == null) return;

        String msg = event.getMessage();
        if (msg == null || msg.isEmpty()) return;

        String lower = msg.toLowerCase();
        boolean matched = false;

        if (pingOnName.isEnabled()) {
            String name = mc.player.getName().getString().toLowerCase();
            if (lower.contains(name)) matched = true;
        }

        if (!matched) {
            String kw = keywords.get().trim();
            if (!kw.isEmpty()) {
                for (String word : kw.split(",")) {
                    word = word.trim().toLowerCase();
                    if (!word.isEmpty() && lower.contains(word)) {
                        matched = true;
                        break;
                    }
                }
            }
        }

        if (!matched) return;

        if (sound.isEnabled()) {
            mc.player.playSound(
                    SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
                    (float) volume.get(),
                    (float) pitch.get());
        }

        if (printHighlight.isEnabled() && mc.player.networkHandler != null) {
            // Add a highlighted notice in local chat (not sent to server)
            mc.inGameHud.getChatHud().addMessage(
                    net.minecraft.text.Text.literal("§e[ChatPing] §fMentioned: §a" + msg));
        }
    }
}
