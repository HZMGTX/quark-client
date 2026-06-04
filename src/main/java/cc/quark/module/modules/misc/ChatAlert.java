package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.StringSetting;

import java.util.Arrays;

public class ChatAlert extends Module {

    private final StringSetting keywords = register(new StringSetting("Keywords", "Comma-separated words to alert on", ""));
    private final BoolSetting   sound    = register(new BoolSetting  ("Sound",    "Play notification sound on match", true));
    private final BoolSetting   username = register(new BoolSetting  ("Username", "Alert when own username is mentioned", true));

    public ChatAlert() {
        super("ChatAlert", "Alerts when keywords or your username appear in chat", Category.MISC);
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (!event.isIncoming()) return;
        String msg = event.getMessage().toLowerCase();

        if (username.isEnabled() && mc.player != null) {
            String name = mc.player.getName().getString().toLowerCase();
            if (msg.contains(name)) {
                alert();
                return;
            }
        }

        String kw = keywords.get().trim();
        if (kw.isEmpty()) return;
        for (String word : kw.split(",")) {
            word = word.trim().toLowerCase();
            if (!word.isEmpty() && msg.contains(word)) {
                alert();
                return;
            }
        }
    }

    private void alert() {
        if (sound.isEnabled() && mc.player != null) {
            mc.player.playSound(net.minecraft.sound.SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
                    1.0f, 1.2f);
        }
    }
}
