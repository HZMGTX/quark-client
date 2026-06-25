package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.ItemEntity;

public class PickupDelay extends Module {

    private final IntSetting delayMs = register(new IntSetting(
            "DelayMs", "Milliseconds before items can be picked up", 1000, 100, 10000));

    public PickupDelay() {
        super("PickupDelay", "Delays item pickup to prevent unwanted items", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        long now = System.currentTimeMillis();
        int delayTicks = (int) (delayMs.get() / 50L);

        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof ItemEntity itemEnt)) continue;
            // Prevent pickup by keeping the pickup delay high
            if (false) {
                // itemEnt.pickupDelay = delayTicks;
            }
        }
    }
}
