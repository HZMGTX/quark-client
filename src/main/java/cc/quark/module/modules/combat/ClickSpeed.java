package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventAttack;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;

import java.util.ArrayDeque;
import java.util.Deque;

public class ClickSpeed extends Module {

    private final IntSetting minCPS = register(new IntSetting(
            "Min CPS", "Minimum allowed clicks per second", 4, 4, 12));

    private final IntSetting maxCPS = register(new IntSetting(
            "Max CPS", "Maximum allowed clicks per second (excess attacks cancelled)", 12, 8, 20));

    // Timestamps of attack events in the last second
    private final Deque<Long> clickTimes = new ArrayDeque<>();
    private int currentCPS = 0;

    public ClickSpeed() {
        super("ClickSpeed", "Enforces a target CPS range by cancelling excess attack events", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        clickTimes.clear();
        currentCPS = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        long now = System.currentTimeMillis();
        // Remove entries older than 1 second
        while (!clickTimes.isEmpty() && now - clickTimes.peekFirst() > 1000L) {
            clickTimes.pollFirst();
        }
        currentCPS = clickTimes.size();
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        long now = System.currentTimeMillis();
        // Prune stale entries
        while (!clickTimes.isEmpty() && now - clickTimes.peekFirst() > 1000L) {
            clickTimes.pollFirst();
        }

        int cps = clickTimes.size();
        int max = maxCPS.get();
        if (max < minCPS.get()) max = minCPS.get();

        if (cps >= max) {
            event.cancel();
            return;
        }

        clickTimes.addLast(now);
        currentCPS = clickTimes.size();
    }

    @Override
    public String getSuffix() {
        return currentCPS + " CPS";
    }
}
