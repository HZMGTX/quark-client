package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventKey;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.ChatUtil;

public class QuickMessages extends Module {

    // GLFW key codes: F1=290, F2=291, F3=292, F4=293, F5=294
    private static final int[] DEFAULT_KEYS = { 290, 291, 292, 293, 294 };

    private final IntSetting key1 = register(new IntSetting("Key 1", "GLFW key for message 1 (F1=290)", DEFAULT_KEYS[0], -1, 400));
    private final ModeSetting msg1 = register(new ModeSetting("Message 1", "Message to send for key 1",
            "GG!", "GG!", "GL HF", "gg ez", "nice try", "wp", "none"));

    private final IntSetting key2 = register(new IntSetting("Key 2", "GLFW key for message 2 (F2=291)", DEFAULT_KEYS[1], -1, 400));
    private final ModeSetting msg2 = register(new ModeSetting("Message 2", "Message to send for key 2",
            "GL HF", "GG!", "GL HF", "gg ez", "nice try", "wp", "none"));

    private final IntSetting key3 = register(new IntSetting("Key 3", "GLFW key for message 3 (F3=292)", DEFAULT_KEYS[2], -1, 400));
    private final ModeSetting msg3 = register(new ModeSetting("Message 3", "Message to send for key 3",
            "gg ez", "GG!", "GL HF", "gg ez", "nice try", "wp", "none"));

    private final IntSetting key4 = register(new IntSetting("Key 4", "GLFW key for message 4 (F4=293)", DEFAULT_KEYS[3], -1, 400));
    private final ModeSetting msg4 = register(new ModeSetting("Message 4", "Message to send for key 4",
            "nice try", "GG!", "GL HF", "gg ez", "nice try", "wp", "none"));

    private final IntSetting key5 = register(new IntSetting("Key 5", "GLFW key for message 5 (F5=294)", DEFAULT_KEYS[4], -1, 400));
    private final ModeSetting msg5 = register(new ModeSetting("Message 5", "Message to send for key 5",
            "wp", "GG!", "GL HF", "gg ez", "nice try", "wp", "none"));

    public QuickMessages() {
        super("QuickMessages", "Send preset messages with configurable key bindings", Category.MISC);
    }

    @EventHandler
    public void onKey(EventKey event) {
        if (mc.player == null) return;
        int k = event.getKeyCode();
        if (k <= 0) return;

        checkSlot(k, key1.get(), msg1.get());
        checkSlot(k, key2.get(), msg2.get());
        checkSlot(k, key3.get(), msg3.get());
        checkSlot(k, key4.get(), msg4.get());
        checkSlot(k, key5.get(), msg5.get());
    }

    private void checkSlot(int pressed, int bound, String message) {
        if (bound <= 0 || pressed != bound) return;
        if (message.equals("none") || message.isEmpty()) return;
        mc.player.networkHandler.sendChatMessage(message);
        ChatUtil.info("Sent: " + message);
    }
}
