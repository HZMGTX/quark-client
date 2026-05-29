package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;

public class TimeChanger extends Module {

    private final IntSetting  time = register(new IntSetting(
            "Time", "Fixed time of day (0=midnight, 6000=noon, 12000=sunset, 18000=night)", 6000, 0, 24000));
    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "Fixed: pin to a time; Cycle: speed up time", "Fixed", "Fixed", "Cycle"));
    private final IntSetting  speed = register(new IntSetting(
            "Cycle Speed", "Ticks to advance per game tick in Cycle mode", 20, 1, 200));

    public TimeChanger() {
        super("TimeChanger", "Changes the client-side time of day", Category.RENDER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.world == null) return;
        if (mode.is("Fixed")) {
            mc.world.setTimeOfDay(time.get());
        } else {
            // Cycle mode: advance time by the configured speed
            long current = mc.world.getTimeOfDay();
            mc.world.setTimeOfDay((current + speed.get()) % 24000L);
        }
    }

    /** Exposed so other modules (e.g., sky rendering hooks) can query the target time. */
    public long getClientTime() {
        return time.get();
    }
}
