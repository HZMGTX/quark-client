package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.event.events.EventPacketSend;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PacketAnalyzer extends Module {

    private final BoolSetting analyzeIncoming = register(new BoolSetting(
            "Analyze Incoming", "Track and flag suspicious incoming packets", true));
    private final BoolSetting analyzeOutgoing = register(new BoolSetting(
            "Analyze Outgoing", "Track and flag suspicious outgoing packets", true));
    private final IntSetting floodThreshold = register(new IntSetting(
            "Flood Threshold", "Packets per second threshold to flag as flood", 50, 10, 500));
    private final BoolSetting logToFile = register(new BoolSetting(
            "Log To File", "Write flagged patterns to a log file", true));
    private final BoolSetting logAll = register(new BoolSetting(
            "Log All", "Log every packet (verbose, for debugging)", false));

    private final Map<String, Integer> incomingCount  = new HashMap<>();
    private final Map<String, Integer> outgoingCount  = new HashMap<>();
    private long windowStart = 0;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public PacketAnalyzer() {
        super("PacketAnalyzer", "Analyzes and logs suspicious packet patterns", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        incomingCount.clear();
        outgoingCount.clear();
        windowStart = System.currentTimeMillis();
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (!analyzeIncoming.isEnabled()) return;
        String type = event.getPacket().getClass().getSimpleName();
        if (logAll.isEnabled()) log("[IN] " + type);
        track(incomingCount, type, "[IN FLOOD]");
    }

    @EventHandler
    public void onPacketSend(EventPacketSend event) {
        if (!analyzeOutgoing.isEnabled()) return;
        String type = event.getPacket().getClass().getSimpleName();
        if (logAll.isEnabled()) log("[OUT] " + type);
        track(outgoingCount, type, "[OUT FLOOD]");
    }

    private void track(Map<String, Integer> counts, String type, String tag) {
        counts.merge(type, 1, Integer::sum);

        long now = System.currentTimeMillis();
        if (now - windowStart >= 1000) {
            // Report any floods
            counts.forEach((t, c) -> {
                if (c >= floodThreshold.get()) {
                    String msg = tag + " " + t + " x" + c + "/sec";
                    ChatUtil.warn("[PacketAnalyzer] " + msg);
                    if (logToFile.isEnabled()) log(msg);
                }
            });
            counts.clear();
            windowStart = now;
        }
    }

    private void log(String line) {
        try {
            File dir = new File(MinecraftClient.getInstance().runDirectory, "quark");
            if (!dir.exists()) dir.mkdirs();
            try (PrintWriter pw = new PrintWriter(new FileWriter(new File(dir, "packetanalyzer.log"), true))) {
                pw.println("[" + DATE_FORMAT.format(new Date()) + "] " + line);
            }
        } catch (IOException ignored) {}
    }
}
