package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.StringSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.client.gui.DrawContext;

import java.net.InetAddress;
import java.util.ArrayDeque;
import java.util.Deque;

public class PingChecker extends Module {

    private final StringSetting targetIP = register(new StringSetting(
            "TargetIP", "IP address or hostname to ping", "8.8.8.8"));

    private final TimerUtil timer = new TimerUtil();
    private final Deque<Long> history = new ArrayDeque<>();
    private static final int MAX_HISTORY = 20;
    private long lastPing = -1;

    public PingChecker() {
        super("PingChecker", "Pings a configurable IP repeatedly and shows latency graph", Category.MISC);
    }

    @Override
    public void onEnable() {
        history.clear();
        lastPing = -1;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (!timer.hasReached(2000)) return;
        timer.reset();

        String ip = targetIP.get();
        new Thread(() -> {
            try {
                long start = System.currentTimeMillis();
                InetAddress addr = InetAddress.getByName(ip);
                boolean reachable = addr.isReachable(1500);
                long ping = reachable ? System.currentTimeMillis() - start : -1L;
                mc.execute(() -> {
                    lastPing = ping;
                    if (history.size() >= MAX_HISTORY) history.pollFirst();
                    history.addLast(ping < 0 ? 9999L : ping);
                });
            } catch (Exception ignored) {
                mc.execute(() -> lastPing = -1);
            }
        }, "PingChecker-Thread").start();
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        DrawContext ctx = event.getDrawContext();
        String label = lastPing < 0 ? "§cTimeout" : "§aPing: §f" + lastPing + "ms §7(" + targetIP.get() + ")";
        ctx.drawTextWithShadow(mc.textRenderer, label, 4, 4, 0xFFFFFFFF);

        if (history.size() > 1) {
            Long[] arr = history.toArray(new Long[0]);
            long max = 1;
            for (long v : arr) if (v < 9999 && v > max) max = v;
            int gx = 4, gy = 16, gh = 30;
            for (int i = 1; i < arr.length; i++) {
                int h1 = arr[i - 1] >= 9999 ? gh : (int) (arr[i - 1] * gh / max);
                int h2 = arr[i]     >= 9999 ? gh : (int) (arr[i]     * gh / max);
                int x1 = gx + (i - 1) * 4;
                int x2 = gx + i * 4;
                ctx.drawHorizontalLine(x1, x2, gy + gh - h1, 0xFF00FF88);
            }
        }
    }

    @Override
    public String getSuffix() {
        return lastPing < 0 ? "Timeout" : lastPing + "ms";
    }
}
