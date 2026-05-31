package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.event.events.EventKey;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.Deque;

public class MessageHistory extends Module {

    private final IntSetting maxMessages = register(new IntSetting("MaxMessages", "Maximum private messages to remember", 20, 5, 100));
    private final IntSetting printKey    = register(new IntSetting("PrintKey",    "GLFW key code to print history",       GLFW.GLFW_KEY_H, 0, 400));

    private final Deque<String> history = new ArrayDeque<>();

    public MessageHistory() {
        super("MessageHistory", "Stores recent private messages and prints them on a key press", Category.MISC);
    }

    @Override
    public void onEnable() {
        history.clear();
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (!event.isIncoming()) return;
        String msg = event.getMessage();
        if (msg == null) return;

        boolean isWhisper = msg.contains("whispers") || msg.contains("-> You") || msg.contains("[MSG]");
        if (!isWhisper) return;

        history.addLast(msg);
        while (history.size() > maxMessages.get()) {
            history.pollFirst();
        }
    }

    @EventHandler
    public void onKey(EventKey event) {
        if (event.getKeyCode() != printKey.get()) return;
        if (history.isEmpty()) {
            ChatUtil.info("[MsgHistory] No private messages recorded.");
            return;
        }
        ChatUtil.info("[MsgHistory] Last " + history.size() + " private message(s):");
        for (String msg : history) {
            ChatUtil.addMessage("  §7" + msg);
        }
    }
}
