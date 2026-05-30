package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ChatHistory extends Module {

    private final IntSetting maxLines = register(new IntSetting(
            "MaxLines", "Maximum number of chat messages to store", 50, 10, 200));
    private final BoolSetting timestamps = register(new BoolSetting(
            "Timestamps", "Prepend timestamps to history messages", true));

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final List<String> history = new ArrayList<>();

    public ChatHistory() {
        super("ChatHistory", "Stores and replays recent chat messages on enable", Category.MISC);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) {
            disable();
            return;
        }

        // Print last 10 stored messages
        int start = Math.max(0, history.size() - 10);
        ChatUtil.info("[ChatHistory] Last " + Math.min(10, history.size()) + " messages:");
        for (int i = start; i < history.size(); i++) {
            ChatUtil.info(history.get(i));
        }

        disable();
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (!(event.getPacket() instanceof GameMessageS2CPacket packet)) return;

        String content = packet.content().getString();
        if (content == null || content.isEmpty()) return;

        String entry;
        if (timestamps.isEnabled()) {
            entry = "[" + LocalTime.now().format(TIME_FMT) + "] " + content;
        } else {
            entry = content;
        }

        history.add(entry);
        // Trim to maxLines
        while (history.size() > maxLines.get()) {
            history.remove(0);
        }
    }
}
