package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.client.option.KeyBinding;

public class AutoF3T extends Module {

    private final IntSetting intervalSec = register(new IntSetting("Interval Sec", "Seconds between F3+T reloads", 60, 5, 300));

    private final TimerUtil timer = new TimerUtil();

    public AutoF3T() {
        super("AutoF3T", "Auto-reloads chunks (F3+T) periodically", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!timer.hasReached(intervalSec.get() * 1000L)) return;

        // Simulate F3+T: reload resources
        mc.reloadResources();
        timer.reset();
    }
}
