package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;

public class WaterBreath extends Module {

    private final BoolSetting infinite = register(new BoolSetting(
            "Infinite", "Keep air supply at maximum indefinitely", true));

    private final IntSetting airTicks = register(new IntSetting(
            "Air Ticks", "Minimum air ticks to maintain when not infinite", 300, 10, 300));

    public WaterBreath() {
        super("WaterBreath", "Extends underwater breathing time", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        // Only act when player is submerged (air < max)
        int maxAir = mc.player.getMaxAir();
        int currentAir = mc.player.getAir();

        if (infinite.isEnabled()) {
            // Always maintain full air supply
            mc.player.setAir(maxAir);
        } else {
            // Only refill when below threshold
            int threshold = Math.min(airTicks.get(), maxAir);
            if (currentAir < threshold) {
                mc.player.setAir(threshold);
            }
        }
    }
}
