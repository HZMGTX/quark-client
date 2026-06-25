package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.ChatUtil;

import java.util.ArrayDeque;
import java.util.Deque;

public class AntiAutoClicker2 extends Module {

    private final DoubleSetting threshold = register(new DoubleSetting(
            "Threshold", "Maximum CPS before flagging as auto-clicker", 16.0, 5.0, 30.0));

    /** Stores timestamps (ms) of recorded attack packets this session. */
    private final Deque<Long> clickTimestamps = new ArrayDeque<>();
    private int flaggedTicks = 0;

    public AntiAutoClicker2() {
        super("AntiAutoClicker2", "Detects and counters auto-clickers", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        clickTimestamps.clear();
        flaggedTicks = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        long now = System.currentTimeMillis();
        // Remove entries older than 1 second
        clickTimestamps.removeIf(t -> now - t > 1000L);

        // Simulate sampling from attack cooldown changes as a proxy for CPS detection
        // In a real implementation, this would hook into incoming combat packets
        double currentCPS = clickTimestamps.size();

        if (currentCPS >= threshold.get()) {
            flaggedTicks++;
            if (flaggedTicks == 20) {
                ChatUtil.addMessage("[AntiAutoClicker2] Possible auto-clicker detected!");
            }
            // Counter-measure: briefly nullify our own sprint to disrupt the timing
            if (flaggedTicks % 10 == 0 && mc.player.isSprinting()) {
                mc.player.setSprinting(false);
            }
        } else {
            if (flaggedTicks > 0) flaggedTicks = Math.max(0, flaggedTicks - 1);
        }
    }

    /** Called externally (e.g., from a mixin) to record a click event. */
    public void recordClick() {
        clickTimestamps.add(System.currentTimeMillis());
    }

    public double getCurrentCPS() {
        long now = System.currentTimeMillis();
        clickTimestamps.removeIf(t -> now - t > 1000L);
        return clickTimestamps.size();
    }

    @Override
    public String getSuffix() {
        return String.format("%.0f CPS", getCurrentCPS());
    }
}
