package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventAttack;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;

/**
 * AntiAutoClicker - detects suspiciously rapid attack patterns originating from
 * auto-clicker modules and cancels excess attacks to stay under a safe CPS cap.
 *
 * <p>This is intended as a self-rate-limiter so the client's own attack frequency
 * stays within a human-plausible range, helping avoid anti-cheat flagging.
 * It tracks attacks per second using a sliding window and cancels any attack
 * event that would push the CPS above the configured maximum.
 */
public class AntiAutoClicker extends Module {

    private final IntSetting maxCps = register(new IntSetting(
            "Max CPS", "Maximum allowed clicks per second before excess attacks are cancelled", 12, 1, 30));

    private final BoolSetting notifyExcess = register(new BoolSetting(
            "Notify", "Print a chat message when excess attacks are cancelled", false));

    // Rolling window: timestamps (ms) of the last N attacks
    private final long[] attackTimestamps = new long[64];
    private int writeIdx = 0;
    private int totalAttacksThisWindow = 0;

    public AntiAutoClicker() {
        super("AntiAutoClicker", "Cancels excess attacks to keep CPS within a human-plausible range",
                Category.COMBAT);
    }

    @Override
    public void onEnable() {
        java.util.Arrays.fill(attackTimestamps, 0L);
        writeIdx = 0;
        totalAttacksThisWindow = 0;
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        long now = System.currentTimeMillis();
        int currentCps = countCpsInWindow(now);

        if (currentCps >= maxCps.get()) {
            event.cancel();
            if (notifyExcess.isEnabled() && mc.player != null) {
                mc.player.sendMessage(net.minecraft.text.Text.literal(
                        "[AntiAutoClicker] Cancelled excess attack (" + currentCps + " CPS)"), false);
            }
            return;
        }

        // Record this attack
        attackTimestamps[writeIdx % attackTimestamps.length] = now;
        writeIdx++;
    }

    @EventHandler
    public void onTick(EventTick event) {
        // Periodically evict old entries to avoid stale CPS counts
        long now = System.currentTimeMillis();
        countCpsInWindow(now); // prunes implicitly via the count method
    }

    /**
     * Count how many attack timestamps fall within the last 1000 ms.
     */
    private int countCpsInWindow(long now) {
        int count = 0;
        for (long ts : attackTimestamps) {
            if (ts > 0 && now - ts <= 1000L) count++;
        }
        return count;
    }
}
